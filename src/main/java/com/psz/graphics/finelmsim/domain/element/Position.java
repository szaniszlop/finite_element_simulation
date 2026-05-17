package com.psz.graphics.finelmsim.domain.element;

import lombok.Data;

@Data
public class Position {
    private double x;
    private double y;

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Position clone(){
        return new Position(x, y);
    }
    
    public static final double MIN_DISTANCE = 0.01;

    public double distanceFromSquared(Position other){
        double deltaX = x - other.x;
        double deltaY = y - other.y;
        return Math.max(MIN_DISTANCE, deltaX * deltaX + deltaY * deltaY);
    }

    public double distanceFromOrigin(){
        return Math.sqrt(x * x + y * y); 
    }   
    
    public void setCoordinates(double x, double y){
        this.x = x;
        this.y = y;
    }
}
