package com.psz.graphics.finelmsim.domain.tree.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.psz.graphics.finelmsim.domain.element.MassiveBody;
import com.psz.graphics.finelmsim.domain.element.Position;
import com.psz.graphics.finelmsim.domain.tree.MassBodyTree;
import com.psz.graphics.finelmsim.domain.tree.TreeNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MassBodyTreeImpl implements MassBodyTree {

    private final List<TreeNode> content;
    private final AtomicInteger contentLastIndex;

    public MassBodyTreeImpl(double size, int numElements){
        // logb(n) = loge(n) / loge(b)
        // Approximate number of nodes will be 4 * numElements + 4 * (log4(numElements) - 1) + numElements / 2 as safety margin
        int estimatedArraySize = (int)Math.round(
            size * size + 4 * (Math.log(size * size) / Math.log(4) - 1) 
            + numElements );
        log.debug("Creating tree with capacity {} for number of elements {}", estimatedArraySize, numElements);
        content = Arrays.asList(new TreeNode[estimatedArraySize]);
        contentLastIndex = new AtomicInteger(0);
        int rootIndex = contentLastIndex.getAndIncrement();
        content.set(0, new TreeNode(true, rootIndex, rootIndex, new Position(size / 2, size / 2), size,  Optional.empty(), Optional.empty()));
    }

    @Override
    public void addBody(MassiveBody body) {
        addBody(body, 0);
    }

    @Override
    public List<TreeNode> getNodes() {
        log.debug("Nodes in tree: {}", content.size());
        return Collections.unmodifiableList(
                content.stream()
                .filter( e -> e != null)
                .toList());
    }
    
    @Override
    public void calculateMassDistribution(){
        for(int i = content.size() - 1 ; i>= 0 ; i--){
            TreeNode currentNode = content.get(i);
            if(currentNode != null){
                currentNode.setCenterMass(nodeCenterMass(currentNode));        
            }
        }
    }

    @Override
    public List<MassiveBody> getAttractors(Position position, double theta) {
        // For a given position find the relevant mass centers
        // If node is a leaf or the node size divided by distance from position is less then theta
        // return the node center mass
        // otherwise return the center masses of the child nodes
        List<MassiveBody> result = new ArrayList<>();
        collectAtractorsFromNode(position, theta, content.get(0), result);
        return result;
    }

    @Override
    public void clearNode(int nodeIndex) {
        if(nodeIndex < content.size()){
            TreeNode currentNode = content.get(nodeIndex);
            if (currentNode.childIndex() == nodeIndex & currentNode.isLeaf()) {
                currentNode.clearContent();
            }
        }
    }

    @Override
    public void replaceNode(TreeNode oldNode, TreeNode newNode) {
        if(oldNode.childIndex() < content.size()){
            TreeNode currentNode = content.get(oldNode.childIndex());
            if (currentNode.childIndex() == newNode.childIndex() & currentNode.isLeaf() & newNode.isLeaf()) {
                content.set(oldNode.childIndex(), newNode);
            }
        }
    }

    private void collectAtractorsFromNode(Position position, double theta, TreeNode node, List<MassiveBody> result){
        
        if(node.isLeaf() || isBarnesHutCondition(position, node, theta)){
            result.add(node.centerMass().get());  
        } else {
            collectAtractorsFromNode(position, theta, 
                content.get(node.childIndex() + 0), result);
            collectAtractorsFromNode(position, theta, 
                content.get(node.childIndex() + 1), result );
            collectAtractorsFromNode(position, theta, 
                content.get(node.childIndex() + 2), result);
            collectAtractorsFromNode(position, theta, 
                content.get(node.childIndex() + 3), result);                                                            
        }
  
    }

    private boolean isBarnesHutCondition(Position position, TreeNode node, double theta){
        // size squared / distance squared < theta
        return (node.sizeSquared()) / (position.distanceFromSquared(node.center())) < theta ;
    }

    private MassiveBody nodeCenterMass(TreeNode currentNode){
        MassiveBody emptyMass = MassiveBody.stationary(currentNode.center(), 0);

        if( currentNode.isLeaf() ){
            return currentNode.content().or(() -> Optional.of(emptyMass)).get();                                    
        } 
        // calculate parent node center mass based on children center mass

        MassiveBody m0 = content.get(currentNode.childIndex())
            .centerMass().orElse(emptyMass);
        MassiveBody m1 = content.get(currentNode.childIndex() + 1)
            .centerMass().orElse(emptyMass);
        MassiveBody m2 = content.get(currentNode.childIndex() + 2)
            .centerMass().orElse(emptyMass);
        MassiveBody m3 = content.get(currentNode.childIndex() + 3)
            .centerMass().orElse(emptyMass);     
        int finalMass = m0.mass() + m1.mass() + m2.mass() + m3.mass();  
        MassiveBody nodeCenterMass = null;
        if(finalMass > 0){
            nodeCenterMass = MassiveBody.stationary( new Position(
            (m0.mass() * m0.position().x() 
            + m1.mass() * m1.position().x() 
            + m2.mass() * m2.position().x()
            + m3.mass() * m3.position().x()) / finalMass,
            (m0.mass() * m0.position().y() 
            + m1.mass() * m1.position().y() 
            + m2.mass() * m2.position().y()
            + m3.mass() * m3.position().y()) / finalMass), 
            finalMass); 
        } else {
            nodeCenterMass = emptyMass;
        }

        return nodeCenterMass;           
    }

    private void addBody(MassiveBody body, int index){
        TreeNode node = content.get(index);
        // log.debug("Add Body {} to note {} on index {}", body, node, index);
        if(node.isLeaf()){
            boolean finished = false;
            synchronized(node){
                if(content.get(index) == node){  // am I still a leaf or someone changed me in the emantime
                    if(!node.content().isPresent()){
                        setBodyToNode(body, node, index);
                    } else {
                        if(node.size() <= Position.MIN_DISTANCE || 
                                node.content().get().position()
                                .distanceFromSquared(body.position()) <= Position.MIN_DISTANCE ){
                            combineBodies(body, node, index); 
                        } else {
                            splitNode(body, node, index);
                        }
                    }
                    finished = true;
                }
            }
            if(!finished){
                // node changed state before we could lock it
                // go to parent and try again
                addBody(body, node.parentIndex());
            }
        } else {
            // identify the child node to add the new mass into
            //   | 01 | 00 |
            //   | 11 | 10 |
            int childOffset = 0;
            if(node.center().x() - body.position().x() > 0){
                childOffset++;
            }
            if(node.center().y() - body.position().y() <= 0){
                childOffset++;
                childOffset++;
            }
            addBody(body, node.childIndex() + childOffset);
        }
    }

    private void setBodyToNode(MassiveBody body, TreeNode node, int index){
        // log.debug("Setting body {} to note {} on index {}", body, node, index);
        TreeNode newNode = new TreeNode(true, node.parentIndex(), node.childIndex(), node.center(), node.size(),  Optional.of(body), node.centerMass());
        content.set(index, newNode);
        // log.debug("New node created {}", newNode);
    }

    private void combineBodies(MassiveBody body, TreeNode node, int index){
        // log.debug("Adding mass {} to node {} on index {}", body.mass(), node, index);
        TreeNode newNode = new TreeNode(true, node.parentIndex(), node.childIndex(), 
            body.position(), node.size(),  
            Optional.of(node.content().get().merge(body)), 
            node.centerMass());
        content.set(index, newNode);
        // log.debug("New node created {}", newNode);
    }    

    private void splitNode(MassiveBody body, TreeNode node, int index){
        int childPosition = contentLastIndex.getAndAdd(4);
        // create new root node pointing to its children at the end of the array
        TreeNode newRoot = new TreeNode(false, node.parentIndex(), 
                childPosition, node.center(), node.size(),  
                Optional.empty(), node.centerMass());
        // Create child nodes by splitting the original node in half horizontaly and verticaly
        double childSize = node.size() / 2;
        double centerOffset = childSize / 2;
        TreeNode t0 = new TreeNode(true, index, childPosition + 0, 
            new Position(node.center().x() + centerOffset, node.center().y() - centerOffset), 
            childSize,  Optional.empty(), Optional.empty());
        TreeNode t1 = new TreeNode(true, index, childPosition + 1, 
            new Position(node.center().x() - centerOffset, node.center().y() - centerOffset), 
            childSize,  Optional.empty(), Optional.empty());
        TreeNode t2 = new TreeNode(true, index, childPosition + 2, 
            new Position(node.center().x() + centerOffset, node.center().y() + centerOffset), 
            childSize,  Optional.empty(), Optional.empty());
        TreeNode t3 = new TreeNode(true, index, childPosition + 3, 
            new Position(node.center().x() - centerOffset, node.center().y() + centerOffset), 
            childSize,  Optional.empty(), Optional.empty());
        // add the new child nodes at the positions denoted by childPosition
        content.set(childPosition + 0, t0);
        content.set(childPosition + 1, t1);
        content.set(childPosition + 2, t2);
        content.set(childPosition + 3, t3);
        // and replace the old node with the new root node
        content.set(index, newRoot);
        // add the old node content into the new root node
        addBody(node.content().get(), index);
        // add the new mass into the new root node
        addBody(body, index);
    }


}
