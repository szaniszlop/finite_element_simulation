package com.psz.graphics.finelmsim.domain.simulator;

import java.util.List;

import com.psz.graphics.finelmsim.domain.element.MassiveBody;
import com.psz.graphics.finelmsim.domain.element.Position;
import com.psz.graphics.finelmsim.domain.tree.MassBodyTree;
import com.psz.graphics.finelmsim.domain.tree.TreeNode;
import com.psz.graphics.finelmsim.domain.tree.impl.MassBodyTreeImpl;
import com.psz.graphics.finelmsim.utils.MethodTimer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GravitySimulator2DCalculation {

    private final int gridSize;
    private MassBodyTree tree;
    private MethodTimer<List<TreeNode>> outOfGridNodesTimer;

    public GravitySimulator2DCalculation(List<MassiveBody> bodies, int gridSize){
        this.gridSize = gridSize;
        rebuildTree(bodies);
         outOfGridNodesTimer = new MethodTimer<>();
    }

    public List<MassiveBody> step(final double deltaT, final double theta){

        MethodTimer.timeMethodExecution("calculateMassDistribution",  MethodTimer.TimeUnit.mili,
            () -> tree.calculateMassDistribution());


        /* 1.  Calculate the new position for each node */    
        log.debug("Step1 ");
        MethodTimer.timeMethodExecution("recalculateBodyPositions",  MethodTimer.TimeUnit.mili,
            () -> this.recalculateBodyPositions(deltaT, theta));

        /* 1.5 retrieve the new list of bodies for display */
        log.debug("Step1.5 ");
        List<MassiveBody> bodies = tree.getNodes()
            .stream()    
            .parallel()
            .filter( e -> e.isLeaf() && e.content().isPresent())
            .map( e -> e.content().get()).toList();

        /* 2. Identify nodes where the mass is outside of the boundary of the node - return these as list and remove them from the tree */
        log.debug("Step2 ");
        List<TreeNode> outOfGridNodes = outOfGridNodesTimer.timeMethodExecution("getOutOfGridNodes",  MethodTimer.TimeUnit.mili,
                () -> this.getOutOfGridNodes());
        log.debug("Nodes to replace: {}",  outOfGridNodes.size());

        if(outOfGridNodes.size() < (bodies.size() * 0.4)){
            /* Number of out of grid bodies not too high - optimize rearrange only out of grid ones */
            log.info("Number of bodies to reposition {}", outOfGridNodes.size());
            MethodTimer.timeMethodExecution("repositionOutOfGridBodies",  MethodTimer.TimeUnit.mili,
                () -> this.repositionOutOfGridBodies(outOfGridNodes)); 

        } else {
            log.info("Too many nodes to replace, skipping tree update rebuilding tree from scratch");
            MethodTimer.timeMethodExecution("rebuildTree",  MethodTimer.TimeUnit.mili,
                () -> this.rebuildTree(bodies));
        } 
        return bodies;

    }

    private void recalculateBodyPositions(final double deltaT, final double theta){
        tree.getNodes()
            .stream()
            .parallel()
            .filter( e -> e.isLeaf() && e.content().isPresent())
            .forEach( e -> applyForce(e, tree, deltaT, theta));
    }

    private List<TreeNode> getOutOfGridNodes(){
        return tree.getNodes()
            .stream()
            .parallel()
            .filter( e -> e.isLeaf() && e.content().isPresent())
            .filter( e -> positionOutOfGrid(e))
            .map( e -> e.clone()).toList();
    }

    private void repositionOutOfGridBodies(final List<TreeNode> outOfGridNodes){
        /* 3 Remove out of grid nodes from the tree */    
        log.debug("Step3 ");
        MethodTimer.timeMethodExecution("repositionOutOfGridBodies.clear",  MethodTimer.TimeUnit.mili,
                () -> outOfGridNodes
                        .stream()
                        //.parallel()
                        .forEach( e -> tree.clearNode(e.childIndex())));

        /* 4. insert the out of boundary bodies back into the tree */
        log.debug("Step4 ");        
        MethodTimer.timeMethodExecution("repositionOutOfGridBodies.addBody",  MethodTimer.TimeUnit.mili,
                () -> outOfGridNodes
                        .stream()
                        //.parallel()
                        .map( e -> e.content().get())
                        .forEach( e -> tree.addBody(e)));       

    }

    private void rebuildTree(final List<MassiveBody> bodies){
            this.tree = new MassBodyTreeImpl(gridSize, bodies.size());
            bodies
                .stream()
                .parallel()
                .forEach(e -> tree.addBody(e));
    }

    private boolean positionOutOfGrid(TreeNode node){
        Position center = node.center();
        double size = node.size();
        MassiveBody body = node.content().get();

        return body.position().x() < center.x() - size / 2 || body.position().x() > center.x() + size / 2 ||
            body.position().y() < center.y() - size / 2 || body.position().y() > center.y() + size / 2;
    }

    private void applyForce(TreeNode node, MassBodyTree tree, double deltaT, double theta){
        MassiveBody body = node.content().get();
        List<MassiveBody> attractors = tree.getAttractors(body.position(), theta);

        double a_x = body.acceleration().x();
        double a_y = body.acceleration().y();

        for(MassiveBody attractor : attractors){
            double distancesquared = body.position().distanceFromSquared(attractor.position());
            double scaleFactor = attractor.mass() / body.mass() / distancesquared;
            a_x = a_x + (attractor.position().x() - body.position().x() ) * scaleFactor;
            a_y = a_y + (attractor.position().y() - body.position().y() ) * scaleFactor;
        }

        Position newVelocity = new Position(
                        body.velocity().x() + body.acceleration().x() * deltaT, 
                        body.velocity().y() + body.acceleration().y() * deltaT);
        Position newPosition = new Position(
                        body.position().x() + newVelocity.x() * deltaT,
                        body.position().y() + newVelocity.y() * deltaT);

        Position newAcceleration = new Position( a_x, a_y );
        double accelerationMagnitude = newAcceleration.distanceFromOrigin();
        double dumpenning = 1 / ( 1 + (accelerationMagnitude / 100));
        Position newDumbenedAcceleration = new Position(newAcceleration.x() * dumpenning, newAcceleration.y() * dumpenning);
        
        node.setContent(new MassiveBody(newPosition, body.mass(), newVelocity, newDumbenedAcceleration));

    }
}
