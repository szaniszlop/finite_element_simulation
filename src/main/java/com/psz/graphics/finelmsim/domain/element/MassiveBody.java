package com.psz.graphics.finelmsim.domain.element;

public record MassiveBody(Position position, int mass, Position velocity, Position acceleration) {

    public static MassiveBody stationary(Position position, int mass){
        return new MassiveBody(position, mass, new Position(0.0, 0.0), new Position(0.0, 0.0));
    }

    public MassiveBody merge(MassiveBody other){
        int newMass = mass + other.mass;
        Position newVelocity = new Position(
            (velocity.x() * mass + other.velocity().x() * other.mass) / newMass,
            (velocity.y() * mass + other.velocity().y() * other.mass) / newMass);
        Position newAcceleration = new Position(
            (acceleration.x() * mass + other.acceleration().x()) / newMass,
            (acceleration.y() * mass + other.acceleration().y()) / newMass
        );    

        return new MassiveBody(position, newMass, newVelocity, newAcceleration);
    }
}
