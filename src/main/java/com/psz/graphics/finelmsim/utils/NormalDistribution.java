package com.psz.graphics.finelmsim.utils;

public class NormalDistribution {
    
    private static final double TWO_PI = 2 * Math.PI;

    // Box–Muller transform
    // https://en.wikipedia.org/wiki/Box%E2%80%93Muller_transform
    public static DoublePair getValueRandomPair(){
        return getValueRandomPair(0.0, 1.0);
    }


    public static DoublePair getValueRandomPair(double mean, double sigma){
        double phi = TWO_PI * Math.random();
        double R = sigma * Math.sqrt(-2*Math.log(Math.random()));
        double x = mean + R * Math.sin(phi);
        double y = mean + R * Math.cos(phi);
        return new DoublePair(x, y);
    }

    public static DoublePair getValueRandomPair(double meanX, double meanY, double sigma){
        double phi = TWO_PI * Math.random();
        double R = sigma * Math.sqrt(-2*Math.log(Math.random()));
        double x = meanX + R * Math.sin(phi);
        double y = meanY + R * Math.cos(phi);
        return new DoublePair(x, y);
    }

    public static IntPair getIntValueRandomPair(int mean, int sigma){
        DoublePair doublePair = getValueRandomPair(mean, sigma);

        return new IntPair((int)Math.floor(doublePair.z0()), (int)Math.floor(doublePair.z1()));
    }

    public static final record DoublePair(double z0, double z1){
    }

    public static final record IntPair(int z0, int z1){
    }
}
