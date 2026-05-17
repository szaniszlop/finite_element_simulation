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
public class TwoSwarmsOfNormalDistributedRandomBodies implements MassBodyGenerator {
    @Override
    public String getName() {
        return "Two Swarms of Normal Distributed Random Bodies Generator";
    }

    @Override
    public String getDescription() {
        return "Generates two swarms of massive bodies within the simulation grid using a normal distribution.";
    }

    @Override
    public List<MassiveBody> createBodies(double gridSize, double density, int maxInitialMass) {
        return createTwoSwarmsNormalRandomBodies(gridSize, density, maxInitialMass);
    }

        private List<MassiveBody> createTwoSwarmsNormalRandomBodies(double gridSize, double density, int maxInitialMass){
        List<MassiveBody> bodies = new ArrayList<>();
        double meanY = gridSize / 2;
        double meanXLeft = gridSize / 2 - gridSize / 5;
        double meanXRight = gridSize / 2 + gridSize / 5;
        double stdev = Math.max(1, gridSize / 15);
        int numElements = (int)Math.floor(gridSize * gridSize * density);
        for( int i = 0 ; i < numElements / 2 ; i++){
            DoublePair pair = NormalDistribution.getValueRandomPair(meanXLeft, meanY, stdev);
            if(isInsideGrid(pair, gridSize)){
                bodies.add(MassiveBody.stationary(
                    new Position(pair.z0(), pair.z1()), ThreadLocalRandom.current().nextInt(1, maxInitialMass)));
            }
            pair = NormalDistribution.getValueRandomPair(meanXRight, meanY, stdev);
            if(isInsideGrid(pair, gridSize)){
                bodies.add(MassiveBody.stationary(
                    new Position(pair.z0(), pair.z1()), ThreadLocalRandom.current().nextInt(1, maxInitialMass)));
            }
        }
        bodies.add(MassiveBody.stationary(new Position(meanY, meanY), maxInitialMass * 1000));
        return bodies;
    }

    private boolean isInsideGrid(DoublePair pair, double gridSize){
        return pair.z0() >= 0 && pair.z0() < gridSize && pair.z1() >= 0 && pair.z1() < gridSize;
    }    

}
