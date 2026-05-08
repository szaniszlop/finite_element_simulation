package com.psz.graphics.finelmsim.utils;

import org.junit.jupiter.api.Test;

import com.psz.graphics.finelmsim.domain.element.Position;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccelerationCalculatorTest {

    @Test
    public void angleCalculatorTest(){
        Position position = new Position(-2, -2);
        double distance = position.distanceFromOrigin();
        double velocityMagnitude = distance;                
        double angle = Math.acos((position.x() ) / distance);
        if(position.y() < 0){
            angle = angle * -1;
        }
        angle = angle + Math.PI / 2;
        Position initialVelocity = new Position(velocityMagnitude * Math.cos(angle) , 
                                                velocityMagnitude * Math.sin(angle) );
        Position initialacceleration = new Position(velocityMagnitude  * Math.cos(angle + Math.PI / 2) , 
                                                velocityMagnitude  * Math.sin(angle + Math.PI / 2) );         
                                                
        log.info("initial velocity: {}", initialVelocity);
        log.info("initial acceleration: {}", initialacceleration);                                          
    }
}
