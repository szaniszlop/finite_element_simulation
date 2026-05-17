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

    private static final double REBUILD_TO_REPOSITION_THRESHOLD = 0.1;
    private final int gridSize;
    private MassBodyTree tree;
    private MethodTimer<List<TreeNode>> outOfGridNodesTimer;

    public GravitySimulator2DCalculation(List<MassiveBody> bodies, int gridSize){
        this.gridSize = gridSize;
        rebuildTree(bodies);
        outOfGridNodesTimer = new MethodTimer<>();
    }

    public List<MassiveBody> step(final double deltaT, final double theta){

        /* 0.  Calculate mass distribution of the tree */    
        calculateMassDistribution();

        /* 1.  Calculate the new position for each node */    
        recalculateBodyPositions(deltaT, theta);

        /* 2 retrieve the new list of bodies for display */
        List<MassiveBody> bodies = getBodiesForDisplay();

        /* 3. Identify nodes where the mass is outside of the boundary of the node - return these as list and remove them from the tree */
        List<TreeNode> outOfGridNodes = identifyOutOfGridBodies(bodies);

        /* 4. Reposition bodies of rebuild tree if repositioning not effective */
        if(outOfGridNodes.size() < (bodies.size() * REBUILD_TO_REPOSITION_THRESHOLD)){
            try{
                repositionOutOfGridBodies(outOfGridNodes); 
            } catch (ArrayIndexOutOfBoundsException e) {
                rebuildTreeFromScratch(bodies);
            }
        } else {
            rebuildTreeFromScratch(bodies);
        } 
        return bodies;

    }

    private void rebuildTreeFromScratch(List<MassiveBody> bodies) {
        log.info("Too many nodes to replace, skipping tree update rebuilding tree from scratch");
        MethodTimer.timeMethodExecution("rebuildTree",  MethodTimer.TimeUnit.mili,
            () -> this.rebuildTree(bodies));
    }

    private void repositionOutOfGridBodies(List<TreeNode> outOfGridNodes) {
        /* Number of out of grid bodies not too high - optimize rearrange only out of grid ones */
        MethodTimer.timeMethodExecution("repositionOutOfGridBodies",  MethodTimer.TimeUnit.mili,
            () -> this._repositionOutOfGridBodies(outOfGridNodes));
    }

    private List<TreeNode> identifyOutOfGridBodies(List<MassiveBody> bodies) {
        log.debug("Step3 ");
        List<TreeNode> outOfGridNodes = outOfGridNodesTimer.timeMethodExecution("getOutOfGridNodes",  MethodTimer.TimeUnit.mili,
                () -> this.getOutOfGridNodes());
        log.info("Nodes overall: {}, Nodes to replace: {}",  bodies.size(), outOfGridNodes.size());
        return outOfGridNodes;
    }

    private List<MassiveBody> getBodiesForDisplay() {
        log.debug("Step2 ");
        List<MassiveBody> bodies = this.tree.getNodes()
            .stream()    
            .parallel()
            .filter( e -> e.isLeaf() && e.content().isPresent())
            .map( e -> e.content().get()).toList();
        return bodies;
    }

    private void recalculateBodyPositions(final double deltaT, final double theta) {
        log.debug("Step1 ");
        MethodTimer.timeMethodExecution("recalculateBodyPositions",  MethodTimer.TimeUnit.mili,
            () -> this._recalculateBodyPositions(deltaT, theta));
    }

    private void calculateMassDistribution() {
        MethodTimer.timeMethodExecution("calculateMassDistribution",  MethodTimer.TimeUnit.mili,
            () -> tree.calculateMassDistribution());
    }

    private void _recalculateBodyPositions(final double deltaT, final double theta){
        this.tree.getNodes()
            .stream()
            .parallel()
            .filter( e -> e.isLeaf() && e.content().isPresent())
            .forEach( e -> applyForce(e, deltaT, theta));
    }

    private List<TreeNode> getOutOfGridNodes(){
        return this.tree.getNodes()
            .stream()
            .parallel()
            .filter( e -> e.isLeaf() && e.content().isPresent())
            .filter( e -> e.bodyOutOfGrid(e.content().get()))
            .map( e -> e.clone()).toList();
    }

    private void _repositionOutOfGridBodies(final List<TreeNode> outOfGridNodes){
        /* 3 Remove out of grid nodes from the tree */    
        log.debug("Step3 ");
        MethodTimer.timeMethodExecution("repositionOutOfGridBodies.clear",  MethodTimer.TimeUnit.mili,
                () -> outOfGridNodes
                        .stream()
                        .parallel()
                        .forEach( e -> tree.clearNode(e.myIndex())));

        /* 4. insert the out of boundary bodies back into the tree */
        log.debug("Step4 ");        
        MethodTimer.timeMethodExecution("repositionOutOfGridBodies.addBody",  MethodTimer.TimeUnit.mili,
                () -> outOfGridNodes
                        .stream()
                        .parallel()
                        .map( e -> e.content().get())
                        .forEach( e -> this.tree.addBody(e)));      

    }

    private void rebuildTree(final List<MassiveBody> bodies){
        log.debug("Rebuild Tree called");
            this.tree = new MassBodyTreeImpl(gridSize, bodies.size());
            bodies
                .stream()
                // .parallel()
                .forEach(e -> this.tree.addBody(e));
    }

    private void applyForce(TreeNode node, double deltaT, double theta){
        MassiveBody body = node.content().get();
        List<MassiveBody> attractors = this.tree.getAttractors(body.getPosition(), theta);

        double a_x = body.getAcceleration().getX();
        double a_y = body.getAcceleration().getY();

        for(MassiveBody attractor : attractors){
            double distancesquared = body.getPosition().distanceFromSquared(attractor.getPosition());
            double scaleFactor = attractor.getMass() / body.getMass() / distancesquared;
            a_x = a_x + (attractor.getPosition().getX() - body.getPosition().getX() ) * scaleFactor;
            a_y = a_y + (attractor.getPosition().getY() - body.getPosition().getY() ) * scaleFactor;
        }

        Position newAcceleration = new Position( a_x, a_y );
        double accelerationMagnitude = newAcceleration.distanceFromOrigin();
        double dumpenning = 1 / ( 1 + (accelerationMagnitude / 100));
        body.getAcceleration().setCoordinates(newAcceleration.getX() * dumpenning, newAcceleration.getY() * dumpenning);

        body.getVelocity().setCoordinates(
            body.getVelocity().getX() + body.getAcceleration().getX() * deltaT,
            body.getVelocity().getY() + body.getAcceleration().getY() * deltaT);
 
        body.getPosition().setCoordinates(
            body.getPosition().getX() + body.getVelocity().getX() * deltaT,
            body.getPosition().getY() + body.getVelocity().getY() * deltaT);
        
    }
}
