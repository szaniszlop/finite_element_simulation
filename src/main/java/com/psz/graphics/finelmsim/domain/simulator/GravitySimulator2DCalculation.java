package com.psz.graphics.finelmsim.domain.simulator;

import java.util.List;

import com.psz.graphics.finelmsim.domain.element.MassiveBody;
import com.psz.graphics.finelmsim.domain.element.Position;
import com.psz.graphics.finelmsim.domain.tree.MassBodyTree;
import com.psz.graphics.finelmsim.domain.tree.impl.MassBodyTreeImpl;
import com.psz.graphics.finelmsim.utils.MethodTimer;


public class GravitySimulator2DCalculation {
    public static List<MassiveBody> step(List<MassiveBody> bodies, final double deltaT, final double theta, int gridSize){
        final MassBodyTree tree = new MassBodyTreeImpl(gridSize, bodies.size());

        MethodTimer.timeMethodExecution("Tree construction", MethodTimer.TimeUnit.micro, 
            () -> bodies.stream().parallel().forEach(e -> tree.addBody(e)));

        MethodTimer.timeMethodExecution("Mass distribution calculation",  MethodTimer.TimeUnit.micro,
            () -> tree.calculateMassDistribution());
        return tree.getNodes()
            .stream()
            .parallel()
            .filter( e -> e.isLeaf() && e.content().isPresent())
            .map( e -> e.content().get())
            .map( e -> applyForce(e, tree, deltaT, theta)).toList();

    }

    private static MassiveBody applyForce(MassiveBody body, MassBodyTree tree, double deltaT, double theta){
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

        return new MassiveBody(newPosition, body.mass(), newVelocity, newDumbenedAcceleration);
         
    }
}
