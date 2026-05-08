package com.psz.graphics.finelmsim.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class MethodTimer< T > {

    public static enum TimeUnit{
        mili(1000000),
        micro(1000),
        nano(1);

        private final long nanoMultiplier;
        
        TimeUnit(long nanoMultiplier){
            this.nanoMultiplier = nanoMultiplier;
        }

    }

    public T timeMethodExecution(String methodName, Supplier<T> method){
        return timeMethodExecution(methodName, TimeUnit.nano, method);
    }

    public static void timeMethodExecution(String methodName, Executer method){
        timeMethodExecution(methodName, TimeUnit.nano, method);

    }

    public static void timeMethodExecution(String methodName, TimeUnit timeUnit, Executer method){
        long start = System.nanoTime();
        method.execute();
        long end = System.nanoTime();
        log.info("{} executed in {} {}-seconds ", methodName, (end - start) / timeUnit.nanoMultiplier, timeUnit.name());
    }    

    public T timeMethodExecution(String methodName, TimeUnit timeUnit, Supplier<T> method){
        long start = System.nanoTime();
        T result = method.get();
        long end = System.nanoTime();
        log.info("{} executed in {} {}-seconds ", methodName, (end - start) / timeUnit.nanoMultiplier, timeUnit.name());
        return result;
    }       
}


