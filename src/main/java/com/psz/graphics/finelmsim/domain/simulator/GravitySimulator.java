package com.psz.graphics.finelmsim.domain.simulator;

import java.util.List;

import com.psz.graphics.finelmsim.domain.element.MassiveBody;
import com.psz.graphics.finelmsim.ui.MassTreeCanvas;
import com.psz.graphics.finelmsim.utils.MethodTimer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GravitySimulator implements Runnable{

    private final int MAX_FPS = 120;
    private final double NANOS_IN_SECOND = 1000000000.0;
    private final double NANOS_IN_MILI = 1000000.0;

    private final MassTreeCanvas canvas;

    private final double deltaT, theta;

    private int gridSize;

    private boolean running;

    private Thread runtimeThread;

    private List<MassiveBody> bodies;

    public GravitySimulator(MassTreeCanvas canvas, double deltaT, double theta, int gridSize){
        this.canvas = canvas;
        this.deltaT = deltaT;
         this.theta = theta;
         this.gridSize = gridSize;
        this.running = false;
        this.runtimeThread = new Thread(this);
        canvas.setTheta(theta);
    }

    public void startSimulation(List<MassiveBody> bodies){
        if(isRunning()){
            stopSimulation();
        }
        canvas.setShowTree(false);
        canvas.setShowBody(true);
        canvas.setShowMasses(false);             
        this.runtimeThread = new Thread(this);
        this.bodies = bodies;
        this.running = true;
        runtimeThread.start();
    }

    public void stopSimulation(){
        this.running = false;
    }

    public boolean isRunning(){
        return this.running;
    }

    @Override
    public void run() {
        log.info("Simulation run started");
        try{
            while(running && bodies != null){
                long frameStartTime = System.nanoTime();
                MethodTimer.timeMethodExecution("UpdateBodies", MethodTimer.TimeUnit.micro, 
                    () -> updateBodies()); 
                MethodTimer.timeMethodExecution("DisplayBodies", MethodTimer.TimeUnit.micro, 
                    () -> displayBodies());     
                long frameStopTime = System.nanoTime();
                long frameDuration = frameStopTime - frameStartTime;
                int sleepTime = (int)Math.max(0, (NANOS_IN_SECOND / MAX_FPS - frameDuration) / NANOS_IN_MILI);
                Thread.sleep(sleepTime);
                int fps = (int)Math.floor(NANOS_IN_SECOND / (System.nanoTime() - frameStartTime)  );   
                canvas.setFps(fps);                         
            }
        } catch(InterruptedException e){}
        log.info("Simulation run finished");
    }

    private void updateBodies(){
        this.bodies = GravitySimulator2DCalculation.step(bodies, deltaT, theta, gridSize);
    }

    private void displayBodies(){
        canvas.setBodiesToShow(bodies);
        canvas.showContent();
    }


    
}
