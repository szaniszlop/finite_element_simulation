package com.psz.graphics.finelmsim.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NormalDistributionTest {
    @Test
    void testGetIntValueRandomPair() {
        int[] histogram = new int[]{0,0,0,0,0,0,0,0,0,0};
        for(int i = 0; i< 5000 ; i++){
            NormalDistribution.IntPair result = NormalDistribution.getIntValueRandomPair(5, 1);
            if(result.z0() >= 0 && result.z0() < 10){
                histogram[result.z0()]++;
            }
            if(result.z1() >= 0 && result.z1() < 10){
                histogram[result.z1()]++;
            }            
        }
        Arrays.asList(histogram).stream().forEach(e -> log.info("[{}]", e));
    }

    @Test
    void testGetValueRandomPair() {
        for(int i = 0; i< 10 ; i++){
            NormalDistribution.DoublePair result = NormalDistribution.getValueRandomPair();
            log.info("Random values: {}", result);
        }
    }

    @Test
    void testGetValueRandomPair2() {
        for(int i = 0; i< 10 ; i++){
            NormalDistribution.DoublePair result = NormalDistribution.getValueRandomPair(3.0, 2.0);
            log.info("Random values: {}", result);
        }
    }
}
