package com.psz.graphics.finelmsim.domain.element;

public record Position(double x, double y) {
    public static final double MIN_DISTANCE = 0.0001;

    public double distanceFromSquared(Position other){
        return Math.max(MIN_DISTANCE, (x - other.x) * (x - other.x) + (y - other.y) * (y - other.y)); 
    }

    public double distanceFromOrigin(){
        return Math.sqrt(x * x + y * y); 
    }    
}
