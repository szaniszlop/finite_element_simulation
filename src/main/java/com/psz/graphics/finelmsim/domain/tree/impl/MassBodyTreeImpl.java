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

    private final List<TreeNode> treeContent;
    private final AtomicInteger contentLastIndex;

    public MassBodyTreeImpl(double size, int numElements){
        // logb(n) = loge(n) / loge(b)
        // Approximate number of nodes will be 4 * numElements + 4 * (log4(numElements) - 1) + numElements / 2 as safety margin
        int estimatedArraySize = (int)Math.round(
            size * size + 4 * (Math.log(size * size) / Math.log(4) - 1) 
            + numElements );
        log.debug("Creating tree with capacity {} for number of elements {}", estimatedArraySize, numElements);
        treeContent = Arrays.asList(new TreeNode[estimatedArraySize]);
        contentLastIndex = new AtomicInteger(0);
        int rootIndex = contentLastIndex.getAndIncrement();
        treeContent.set(0, new TreeNode(true, rootIndex, rootIndex, 0, new Position(size / 2, size / 2), size,  Optional.empty(), Optional.empty()));
    }

    @Override
    public void addBody(MassiveBody body) {
        log.debug("Adding body {} to tree", body);
        addBody(body, treeContent.get(0));
    }

    @Override
    public List<TreeNode> getNodes() {
        log.debug("Nodes in tree: {}", treeContent.size());
        return Collections.unmodifiableList(
                treeContent.stream()
                .filter( e -> e != null)
                .toList());
    }
    
    @Override
    public void calculateMassDistribution(){
        for(int i = treeContent.size() - 1 ; i>= 0 ; i--){
            TreeNode currentNode = treeContent.get(i);
            if(currentNode != null){
                currentNode.setCenterMass(nodeCenterMass(currentNode));        
            }
        }
        log.info("Final mass calculated for tree {}", treeContent.get(0).centerMass().get().getMass());
    }

    @Override
    public List<MassiveBody> getAttractors(Position position, double theta) {
        // For a given position find the relevant mass centers
        // If node is a leaf or the node size divided by distance from position is less then theta
        // return the node center mass
        // otherwise return the center masses of the child nodes
        List<MassiveBody> result = new ArrayList<>();
        collectAtractorsFromNode(position, theta, treeContent.get(0), result);
        return result;
    }

    @Override
    public void clearNode(int nodeIndex) {
        if(nodeIndex < treeContent.size()){
            TreeNode currentNode = treeContent.get(nodeIndex);
            if (currentNode.myIndex() == nodeIndex && currentNode.isLeaf()) {
                currentNode.clearContent();
            }
        }
    }

    @Override
    public void replaceNode(TreeNode oldNode, TreeNode newNode) {
        if(oldNode.childIndex() < treeContent.size()){
            TreeNode currentNode = treeContent.get(oldNode.childIndex());
            if (currentNode.childIndex() == newNode.childIndex() & currentNode.isLeaf() & newNode.isLeaf()) {
                treeContent.set(oldNode.childIndex(), newNode);
            }
        }
    }

    @Override
    public boolean verifyTree() {
        log.info("Checking tree");
        boolean result = verifyNode(treeContent.get(0));
        log.info("Tree verification result: {}", result);
        return result; 
    }

     private void collectAtractorsFromNode(Position position, double theta, TreeNode node, List<MassiveBody> result){
        
        if(node.isLeaf() || isBarnesHutCondition(position, node, theta)){
            result.add(node.centerMass().get());  
        } else {
            collectAtractorsFromNode(position, theta, 
                treeContent.get(node.childIndex() + 0), result);
            collectAtractorsFromNode(position, theta, 
                treeContent.get(node.childIndex() + 1), result );
            collectAtractorsFromNode(position, theta, 
                treeContent.get(node.childIndex() + 2), result);
            collectAtractorsFromNode(position, theta, 
                treeContent.get(node.childIndex() + 3), result);                                                            
        }
  
    }

    private boolean isBarnesHutCondition(Position position, TreeNode node, double theta){
        // size squared / distance squared < theta
        return (node.sizeSquared()) / (position.distanceFromSquared(node.center())) < theta ;
    }

    private MassiveBody nodeCenterMass(TreeNode currentNode){
        MassiveBody emptyMass = MassiveBody.stationary(currentNode.center().clone(), 0);

        if( currentNode.isLeaf() ){
            return currentNode.content().or(() -> Optional.of(emptyMass)).get();                                    
        } 
        // calculate parent node center mass based on children center mass

        MassiveBody m0 = treeContent.get(currentNode.childIndex())
            .centerMass().orElse(emptyMass);
        MassiveBody m1 = treeContent.get(currentNode.childIndex() + 1)
            .centerMass().orElse(emptyMass);
        MassiveBody m2 = treeContent.get(currentNode.childIndex() + 2)
            .centerMass().orElse(emptyMass);
        MassiveBody m3 = treeContent.get(currentNode.childIndex() + 3)
            .centerMass().orElse(emptyMass);     
        int finalMass = m0.getMass() + m1.getMass() + m2.getMass() + m3.getMass();  

        MassiveBody nodeCenterMass = emptyMass;
        if(finalMass > 0){
            nodeCenterMass.getPosition().setCoordinates(
                (m0.getMass() * m0.getPosition().getX() 
                + m1.getMass() * m1.getPosition().getX() 
                + m2.getMass() * m2.getPosition().getX()
                + m3.getMass() * m3.getPosition().getX()) / finalMass,
                (m0.getMass() * m0.getPosition().getY() 
                + m1.getMass() * m1.getPosition().getY() 
                + m2.getMass() * m2.getPosition().getY()
                + m3.getMass() * m3.getPosition().getY()) / finalMass
            );
            nodeCenterMass.setMass(finalMass);
        } 
        return nodeCenterMass;           
    }

    private void addBody(MassiveBody body, TreeNode node){
        log.debug("Add Body {} to note {} on index {}", body, node);

        if(node.isLeaf()){
            boolean finished = false;   
            TreeNode myNode = treeContent.get(node.myIndex());                         
            synchronized(myNode){              
                if(treeContent.get(node.myIndex()) == myNode &&
                    treeContent.get(node.myIndex()).isLeaf() ){  // am I still a leaf or someone changed me in the emantime
                    if(!node.content().isPresent()){
                        setBodyToNode(body, node);
                    } else {
                        if(node.size() <= Position.MIN_DISTANCE || 
                                node.content().get().getPosition()
                                .distanceFromSquared(body.getPosition()) <= Position.MIN_DISTANCE ){
                            mergeBodies(body, node); 
                        } else {
                            splitNode(body, node);
                        }
                    }
                    finished = true;
                }
            }
            if(!finished){
                // node changed state before we could lock it
                // go to parent and try again
                log.debug("Node changed state before we could lock it, trying to add to parent node on index {}", treeContent.get(node.parentIndex()));
                addBody(body, treeContent.get(myNode.parentIndex()));
            }
        } else {
      
            // identify the child node to add the new mass into
            //   | 01 | 00 |
            //   | 11 | 10 |
            int childOffset = 0;            
            TreeNode myNode = treeContent.get(node.myIndex());
            int childIndex = myNode.childIndex();
            Position nodeCenter = myNode.center();

            if(nodeCenter.getX() - body.getPosition().getX() > 0.0){
                childOffset++;
            }
            if(nodeCenter.getY() - body.getPosition().getY() < 0.0){
                childOffset++;
                childOffset++;
            }
            TreeNode childNode = treeContent.get(childIndex + childOffset);
/*             
            if(childNode.bodyOutOfGrid(body)){
                log.info("Selected Node {} boundary {}, Body position {}, child offset {}, node size {}, my index {}, my child index {}, child parent index {}, child index{}", 
                childOffset, node.printBoundary(), body.getPosition(), childOffset, node.size(), node.myIndex(), node.childIndex(), childNode.parentIndex(), childNode.myIndex());
                
                TreeNode n = treeContent.get(childIndex + 0);
                log.info("Node 0 coordinates box X {} - {}, Box Y {} - {}, parent {}, self {}, size {}, node child index {}",   
                    n.center().getX() - n.size() / 2, n.center().getX() + n.size() / 2, 
                    n.center().getY() - n.size() / 2, n.center().getY() + n.size() / 2,
                    n.parentIndex(), n.myIndex(), n.size(), n.childIndex());      
                n = treeContent.get(childIndex + 1);
                log.info("Node 1 coordinates box X {} - {}, Box Y {} - {}, parent {}, self {}, size {}, node child index {}",   
                    n.center().getX() - n.size() / 2, n.center().getX() + n.size() / 2, 
                    n.center().getY() - n.size() / 2, n.center().getY() + n.size() / 2,
                    n.parentIndex(), n.myIndex(), n.size(), n.childIndex());   
                n = treeContent.get(childIndex + 2);
                log.info("Node 2 coordinates box X {} - {}, Box Y {} - {}, parent {}, self {}, size {}, node child index {}",   
                    n.center().getX() - n.size() / 2, n.center().getX() + n.size() / 2, 
                    n.center().getY() - n.size() / 2, n.center().getY() + n.size() / 2,
                    n.parentIndex(), n.myIndex(), n.size(), n.childIndex());   
                n = treeContent.get(childIndex + 3);
                log.info("Node 3 coordinates box X {} - {}, Box Y {} - {}, parent {}, self {}, size {}, node child index {}",   
                    n.center().getX() - n.size() / 2, n.center().getX() + n.size() / 2, 
                    n.center().getY() - n.size() / 2, n.center().getY() + n.size() / 2,
                    n.parentIndex(), n.myIndex(), n.size(), n.childIndex());                                                                     
            } 
    */
            addBody(body, childNode);
        }
    }

    private void setBodyToNode(MassiveBody body, TreeNode node){
        log.debug("Setting body {} to node {} on index {}", body, node, node.myIndex());
        node.setContent(body);
    }

    private void mergeBodies(MassiveBody body, TreeNode node){
        // log.debug("Adding mass {} to node {} on index {}", body.getMass(), node, index);
        node.content().get().mergeWith(body);
        log.debug("New node mass {}", node.content().get().getMass());
    }    

    private void splitNode(MassiveBody body, TreeNode node){
        int childPosition = contentLastIndex.getAndAdd(4);
        int index = node.myIndex();

        // Create child nodes by splitting the original node in half horizontaly and verticaly
        double childSize = node.size() / 2.0;
        double centerOffset = childSize / 2.0;
        TreeNode t0 = new TreeNode(true, index, childPosition + 0, childPosition + 0, 
            new Position(node.center().getX() + centerOffset, node.center().getY() - centerOffset), 
            childSize,  Optional.empty(), Optional.empty());
        TreeNode t1 = new TreeNode(true, index, childPosition + 1, childPosition + 1,
            new Position(node.center().getX() - centerOffset, node.center().getY() - centerOffset), 
            childSize,  Optional.empty(), Optional.empty());
        TreeNode t2 = new TreeNode(true, index, childPosition + 2, childPosition + 2,
            new Position(node.center().getX() + centerOffset, node.center().getY() + centerOffset), 
            childSize,  Optional.empty(), Optional.empty());
        TreeNode t3 = new TreeNode(true, index, childPosition + 3, childPosition + 3,
            new Position(node.center().getX() - centerOffset, node.center().getY() + centerOffset), 
            childSize,  Optional.empty(), Optional.empty());
        // add the new child nodes at the positions denoted by childPosition
        treeContent.set(childPosition + 0, t0);
        treeContent.set(childPosition + 1, t1);
        treeContent.set(childPosition + 2, t2);
        treeContent.set(childPosition + 3, t3);

        // and replace the old node with the new root node
        MassiveBody oldContent = node.content().get();
        node.setChildIndex(childPosition);
        node.clearContent();  
        node.setIsLeaf(false);

        // add the old node content into the new root node
        log.debug("** Adding old body {} to new root node {} on index {}", oldContent, node, index);
        addBody(oldContent, node);

        // add the new mass into the new root node
        log.debug("** Adding new body {} to new root node {} on index {}", body, node, index);
        addBody(body, node);

       
    }

   private boolean verifyNode(TreeNode node){
        boolean result = true;
        if(node.isLeaf()){
            return true;
        } else {
            double minX, maxX, minY, maxY;
                minX = node.center().getX() - node.size() / 2;
                maxX = node.center().getX() + node.size() / 2;
                minY = node.center().getY() - node.size() / 2;
                maxY = node.center().getY() + node.size() / 2;
                TreeNode n0 = treeContent.get(node.childIndex() + 0);
                TreeNode n1 = treeContent.get(node.childIndex() + 1);    
                TreeNode n2 = treeContent.get(node.childIndex() + 2);    
                TreeNode n3 = treeContent.get(node.childIndex() + 3);    
                if(n0.size() != node.size() / 2 || n1.size() != node.size() / 2 || n2.size() != node.size() / 2 || n3.size() != node.size() / 2){
                    log.error("Size incorrect, parent node {}, parent size {}, child 0 size {}, child 1 size {}, child 2 size {}, child 3 size {}", 
                    node.myIndex(), node.size(), n0.size(), n1.size(), n2.size(), n3.size());
                    result = false;
                }

                if(n0.center().getX() + n0.size() / 2 < maxX || n0.center().getY() - n0.size() / 2 > minY){
                    log.error("Child node {} boundary {}, is out of bounds for parent node {} boundary {}, size {}", 
                        n0, n0.printBoundary(), node, node.printBoundary(), node.size());
                    result = false; 
                    }
                if(n1.center().getX() - n1.size() / 2 > minX || n1.center().getY() - n1.size() / 2 > minY){
                    log.error("Child node {} boundary {}, is out of bounds for parent node {} boundary {}, size {}", 
                        n1, n1.printBoundary(), node, node.printBoundary(), node.size());
                    result = false;
                    }
                if(n2.center().getX() + n2.size() / 2 < maxX || n2.center().getY() + n2.size() / 2 < maxY){
                    log.error("Child node {} boundary {}, is out of bounds for parent node {} boundary {}, size {}", 
                        n2, n2.printBoundary(), node, node.printBoundary(), node.size());
                    result = false;
                    }                
                if(n3.center().getX() - n3.size() / 2 > minX || n3.center().getY() + n3.size() / 2 < maxY){
                    log.error("Child node {} boundary {}, is out of bounds for parent node {} boundary {}, size {}", 
                        n3, n3.printBoundary(), node, node.printBoundary(), node.size());
                    result = false;
                    }   
                for(int i = 0; i < 4; i++){
                    result = result && verifyNode(treeContent.get(node.childIndex() + i));
                }                 
        }
        return result;
    }

}
