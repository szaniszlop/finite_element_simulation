package com.psz.graphics.finelmsim.domain.element;

import lombok.Data;

@Data
public class MassiveBody {
    private final Position position;
    private int mass;
    private final Position velocity;
    private final Position acceleration;

    public MassiveBody(Position position, int mass, Position velocity, Position acceleration) {
        this.position = position;
        this.mass = mass;
        this.velocity = velocity;
        this.acceleration = acceleration;
    }

    public static MassiveBody stationary(Position position, int mass){
        return new MassiveBody(position, mass, new Position(0.0, 0.0), new Position(0.0, 0.0));
    }

    public MassiveBody merge(MassiveBody other){
        int newMass = this.mass + other.mass;
        Position newVelocity = new Position(
            (this.velocity.getX() * this.mass + other.velocity.getX() * other.mass) / newMass,
            (this.velocity.getY() * this.mass + other.velocity.getY() * other.mass) / newMass);
        Position newAcceleration = new Position(
            (this.acceleration.getX() * this.mass + other.acceleration.getX() * other.mass) / newMass,
            (this.acceleration.getY() * this.mass + other.acceleration.getY() * other.mass ) / newMass
        );    

        return new MassiveBody(position, newMass, newVelocity, newAcceleration);
    }

    public MassiveBody mergeWith(MassiveBody other){
        int newMass = this.mass + other.mass;
        this.getVelocity().setCoordinates(
            (this.getVelocity().getX() * this.getMass() + other.getVelocity().getX() * other.getMass()) / newMass,
            (this.getVelocity().getY() * this.getMass() + other.getVelocity().getY() * other.getMass()) / newMass);
        this.getAcceleration().setCoordinates(
            (this.getAcceleration().getX() * this.getMass() + other.getAcceleration().getX() * other.getMass()) / newMass,
            (this.getAcceleration().getY() * this.getMass() + other.getAcceleration().getY() * other.getMass() ) / newMass
        );  
        this.setMass(newMass);  

        return this;
    }    
}
