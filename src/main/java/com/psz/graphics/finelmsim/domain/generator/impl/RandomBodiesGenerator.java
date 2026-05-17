package com.psz.graphics.finelmsim.domain.generator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import com.psz.graphics.finelmsim.domain.element.MassiveBody;
import com.psz.graphics.finelmsim.domain.element.Position;
import com.psz.graphics.finelmsim.domain.generator.MassBodyGenerator;

@Component
public class RandomBodiesGenerator implements MassBodyGenerator {
    @Override
    public String getName() {
        return "Random Bodies Generator";
    }

    @Override
    public String getDescription() {
        return "Generates a random distribution of massive bodies within the simulation grid.";
    }

    @Override
    public List<MassiveBody> createBodies(double gridSize, double density, int maxInitialMass) {
        return createRandomBodies(gridSize, density, maxInitialMass);
    }

    private List<MassiveBody> createRandomBodies(double gridSize, double density, int maxInitialMass){
        List<MassiveBody> bodies = new ArrayList<>();

        int numElements = (int)Math.floor(gridSize * gridSize * density);
        for( int i = 0 ; i < numElements ; i++){
            bodies.add(MassiveBody.stationary(
                    new Position(ThreadLocalRandom.current().nextDouble(0, gridSize), 
                                 ThreadLocalRandom.current().nextDouble(0, gridSize)), 
                                 ThreadLocalRandom.current().nextInt(1, maxInitialMass)));
        }
        return bodies;
    }    
}
