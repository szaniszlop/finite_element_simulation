package com.psz.graphics.finelmsim.domain.generator;

import java.util.List;

import com.psz.graphics.finelmsim.domain.element.MassiveBody;

public interface MassBodyGenerator {
    String getName();
    String getDescription();
    List<MassiveBody> createBodies(double gridSize, double density, int maxInitialMass);
}
