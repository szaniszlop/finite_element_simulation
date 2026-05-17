package com.psz.graphics.finelmsim.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class MethodTimer< T > {

    private static final java.util.Map<String, MethodTimingStatistics> methodStatistics = new java.util.concurrent.ConcurrentHashMap<>();

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
        log.debug("{} executed in {} {}-seconds ", methodName, (end - start) / timeUnit.nanoMultiplier, timeUnit.name());
        methodStatistics.computeIfAbsent(methodName, k -> new MethodTimingStatistics(timeUnit)).addExecutionTime(end - start);
    }    

    public T timeMethodExecution(String methodName, TimeUnit timeUnit, Supplier<T> method){
        long start = System.nanoTime();
        T result = method.get();
        long end = System.nanoTime();
        log.debug("{} executed in {} {}-seconds ", methodName, (end - start) / timeUnit.nanoMultiplier, timeUnit.name());
        methodStatistics.computeIfAbsent(methodName, k -> new MethodTimingStatistics(timeUnit)).addExecutionTime(end - start);
        return result;
    }   
    
    public static void logStatistics(){
        methodStatistics.keySet().forEach(methodName -> {
            MethodTimingStatistics stats = methodStatistics.get(methodName);
            log.info("Method: {}, Average Execution Time: {} {} over {} executions", methodName, stats.getAverageTime() / stats.timeUnit.nanoMultiplier, stats.timeUnit.name(), stats.executionCount);
        });
    }

    private static class MethodTimingStatistics{
        private long totalTime;
        private int executionCount;
        private TimeUnit timeUnit;

        public MethodTimingStatistics(TimeUnit timeUnit){
            this.totalTime = 0;
            this.executionCount = 0;
            this.timeUnit = timeUnit;
        }

        public void addExecutionTime(long time){
            this.totalTime += time;
            this.executionCount++;
        }

        public long getAverageTime(){
            return executionCount == 0 ? 0 : totalTime / executionCount;
        }
    }   
}


