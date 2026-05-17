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
public class NormalDistributionRandomBodiesGenerator implements MassBodyGenerator {
    @Override
    public String getName() {
        return "Normal Distribution Random Bodies Generator";
    }

    @Override
    public String getDescription() {
        return "Generates a random distribution of massive bodies within the simulation grid using a normal distribution.";
    }

    @Override
    public List<MassiveBody> createBodies(double gridSize, double density, int maxInitialMass) {
        return createNormalRandomBodies(gridSize, density, maxInitialMass);
    }

    private List<MassiveBody> createNormalRandomBodies(double gridSize, double density, int maxInitialMass){
        List<MassiveBody> bodies = new ArrayList<>();
        double mean = gridSize / 2;
        double stdev = Math.max(1, gridSize / 10);
        int numElements = (int)Math.floor(gridSize * gridSize * density);
        for( int i = 0 ; i < numElements ; i++){
            DoublePair pair = NormalDistribution.getValueRandomPair(mean, stdev);
            if(pair.z0() >= 0 && pair.z0() < gridSize && pair.z1() >= 0 && pair.z1() < gridSize){
                bodies.add(MassiveBody.stationary(
                    new Position(pair.z0(), pair.z1()), ThreadLocalRandom.current().nextInt(1, maxInitialMass)));
            }
            
        }
        return bodies;
    }

}
