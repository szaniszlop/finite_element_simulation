package com.psz.graphics.finelmsim.domain.generator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import com.psz.graphics.finelmsim.domain.element.MassiveBody;
import com.psz.graphics.finelmsim.domain.element.Position;
import com.psz.graphics.finelmsim.domain.generator.MassBodyGenerator;
import com.psz.graphics.finelmsim.utils.NormalDistribution;
import com.psz.graphics.finelmsim.utils.NormalDistribution.DoublePair;

@Component
public class NormalDistributionRandomBodiesWithInitialVelocityGenerator implements MassBodyGenerator {

    private final double deltaT;

    public NormalDistributionRandomBodiesWithInitialVelocityGenerator() {
        this.deltaT = 0.1; // default value
    }

    public NormalDistributionRandomBodiesWithInitialVelocityGenerator(double deltaT) {
        this.deltaT = deltaT;
    }

    @Override
    public String getName() {
        return "Normal Distribution Random Bodies with Initial Velocity Generator";
    }

    @Override
    public String getDescription() {
        return "Generates a random distribution of massive bodies within the simulation grid using a normal distribution, with random initial velocities.";
    }

    @Override
    public List<MassiveBody> createBodies(double gridSize, double density, int maxInitialMass) {
        return createNormalRandomBodiesWithInitialVelocity(gridSize, density, maxInitialMass);

    }

    private List<MassiveBody> createNormalRandomBodiesWithInitialVelocity(double gridSize, double density, int maxInitialMass){
        List<MassiveBody> bodies = new ArrayList<>();
        double mean = gridSize / 2;
        double stdev = Math.max(1, gridSize / 8);
        int numElements = (int)Math.floor(gridSize * gridSize * density);
        Position origin = new Position(gridSize / 2, gridSize / 2);
        for( int i = 0 ; i < numElements ; i++){
            DoublePair pair = NormalDistribution.getValueRandomPair(mean, stdev);
            if(pair.z0() >= 0 && pair.z0() < gridSize && pair.z1() >= 0 && pair.z1() < gridSize){
                Position position = new Position(pair.z0(), pair.z1());
                double distance = Math.sqrt(position.distanceFromSquared(origin));
                // velocity vector is perpendicular to point vector and inversly proportional to its length
                // double velocityMagnitude = gridSize / 10 / (distance * distance) / deltaT;
                // double initialVelocityDumpening = (2 * distance /  (1 + distance * distance ));
                double initialVelocityDumpening = 0.05;
                double distanceLimit = 3.0 * stdev ;
                double velocityMagnitude = distance > distanceLimit ? 1 / deltaT * initialVelocityDumpening : 1 / deltaT * Math.sqrt(distance) / distanceLimit ;                
                double angle = Math.acos((position.getX() - gridSize / 2) / distance);
                if(position.getY() > gridSize / 2){
                    angle = angle * -1;
                }
                angle = angle + Math.PI / 2;
                Position initialVelocity = new Position(velocityMagnitude * Math.cos(angle), 
                                                        -1 * velocityMagnitude * Math.sin(angle) );
                Position initialacceleration = new Position(velocityMagnitude  * Math.cos(angle + Math.PI / 2) , 
                                                        -1 * velocityMagnitude  * Math.sin(angle + Math.PI / 2) );                                                        
                bodies.add(new MassiveBody(
                    position, 
                    ThreadLocalRandom.current().nextInt(1, maxInitialMass),
                    initialVelocity,
                    initialacceleration
                    ));
            }
            
        }
        
        bodies.add(new MassiveBody(new Position(gridSize / 2 + 20, gridSize / 2), maxInitialMass * numElements, 
            new Position(0, -10), 
            new Position(-10, 0)));
        bodies.add(new MassiveBody(new Position(gridSize / 2 - 20, gridSize / 2), maxInitialMass * numElements, 
            new Position(0, 10), 
            new Position(10, 0)));
          
        bodies.add(MassiveBody.stationary(new Position(gridSize / 2, gridSize / 2), maxInitialMass * numElements));        
        return bodies;
    }    
}
