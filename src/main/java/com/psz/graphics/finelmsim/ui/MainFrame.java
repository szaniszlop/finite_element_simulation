package com.psz.graphics.finelmsim.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.GridLayout;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.time.Instant;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JPanel;

import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.psz.graphics.finelmsim.domain.element.MassiveBody;
import com.psz.graphics.finelmsim.domain.element.Position;
import com.psz.graphics.finelmsim.domain.simulator.GravitySimulator;
import com.psz.graphics.finelmsim.domain.tree.MassBodyTree;
import com.psz.graphics.finelmsim.domain.tree.impl.MassBodyTreeImpl;
import com.psz.graphics.finelmsim.utils.MethodTimer;
import com.psz.graphics.finelmsim.utils.NormalDistribution;
import com.psz.graphics.finelmsim.utils.MethodTimer.TimeUnit;
import com.psz.graphics.finelmsim.utils.NormalDistribution.DoublePair;
import com.psz.graphics.finelmsim.utils.NormalDistribution.IntPair;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MainFrame extends JFrame implements ApplicationRunner, ApplicationContextAware{

    private ApplicationContext applicationContext;
    private final JTextField tetxtField;
    private MassTreeCanvas masTreeCanvas;
    private List<MassiveBody> bodies;
    private GravitySimulator gravitySimulator;

    private double deltaT = 0.001;
    private double simulationTheta = 2.5;

	public MainFrame() {
        this.tetxtField = titleText();
        initUI();
    }

    private void initUI() {

        Map<String, JComponent> components = new HashMap<String, JComponent>();

        double gridSize = 1024.0;
        masTreeCanvas = new MassTreeCanvas(2048, 1250, gridSize);
        this.gravitySimulator = new GravitySimulator(masTreeCanvas, deltaT, simulationTheta,  (int)gridSize);

        components.put(BorderLayout.NORTH, buttonsPanel());
        components.put(BorderLayout.CENTER, masTreeCanvas);
        components.put(BorderLayout.SOUTH, quitButton());

        createLayout(components);

        setTitle("Finite Elements Simulation");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                
            }
        });

        MethodTimer<List<MassiveBody>> createBodiesTimer = new MethodTimer<>();
        this.bodies = createBodiesTimer.timeMethodExecution("createTwoSwarmsNormalRandomBodies", TimeUnit.mili, 
            () -> createTwoSwarmsNormalRandomBodies((int)Math.round(gridSize), 0.05, 100));
        log.info("Number of Bodies created {}", bodies.size());            

        MethodTimer<MassBodyTree> treeTimer = new MethodTimer<>();
        MassBodyTree tree = treeTimer.timeMethodExecution("createTree", TimeUnit.mili, 
            () -> createTree(gridSize, this.bodies));

        MethodTimer.timeMethodExecution("calculateMassDistribution", TimeUnit.mili, 
            () -> tree.calculateMassDistribution());

        masTreeCanvas.setTree(tree);
    }

    private void createLayout(Map<String, JComponent> components) {

        var pane = getContentPane();
        var bl = new BorderLayout();
        pane.setLayout(bl);
        for (Entry<String, JComponent> componentEntry : components.entrySet()){
            pane.add(componentEntry.getValue(), componentEntry.getKey());
        }
    }    

    private JTextField titleText(){
        return new JTextField("Spring Boot can be used with Swing apps");
    }

    private JComponent buttonsPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2,2));
        panel.add(toggleGridButton());
        panel.add(toggleMassesButton());
        panel.add(showRandomBodyButon());
        panel.add(startStopSimulationButton());
        return panel;

    }

    private JComponent quitButton(){
        var quitButton = new JButton("Quit");

        quitButton.addActionListener((ActionEvent event) -> {
            System.exit(0);
        });
        return quitButton;
    }

    private JComponent toggleGridButton(){
        var button = new JButton("Toggle Grid");

        button.addActionListener((ActionEvent event) -> {
            masTreeCanvas.toggleShowGrid();
        });
        return button;
    }

    private JComponent toggleMassesButton(){
        var button = new JButton("Toggle Masses");

        button.addActionListener((ActionEvent event) -> {
            masTreeCanvas.toggleShowMasses();
        });
        return button;
    }

    private JComponent startStopSimulationButton(){
        var button = new JButton("Start Simulation");

        button.addActionListener((ActionEvent event) -> {
            if(gravitySimulator.isRunning()){
                gravitySimulator.stopSimulation();
                button.setText("start Simulation");
            } else {
                masTreeCanvas.setBodyToShow(Optional.empty());
                gravitySimulator.startSimulation(bodies);
                button.setText("Stop Simulation");
            }
        });
        return button;
    }

    private JComponent showRandomBodyButon(){
        var button = new JButton("Show Random Body");

        button.addActionListener((ActionEvent event) -> {
            masTreeCanvas.setBodyToShow(Optional.of(bodies.get(ThreadLocalRandom.current().nextInt(0, bodies.size()))));
            masTreeCanvas.setShowTree(false);
            masTreeCanvas.setShowBody(true);
            masTreeCanvas.setTheta(simulationTheta);
            masTreeCanvas.showContent();
        });
        return button;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        EventQueue.invokeLater(() -> {
            var ex = applicationContext.getBean(MainFrame.class);
            ex.pack();
            ex.setVisible(true);
        });	
    }

    private List<MassiveBody> createRandomBodies(int gridSize, double density, int maxInitialMass){
        List<MassiveBody> bodies = new ArrayList<>();

        int numElements = (int)Math.floor(gridSize * gridSize * density);
        for( int i = 0 ; i < numElements ; i++){
            bodies.add(MassiveBody.stationary(
                    new Position(ThreadLocalRandom.current().nextInt(0, gridSize), 
                                 ThreadLocalRandom.current().nextInt(0, gridSize)), 
                                 ThreadLocalRandom.current().nextInt(1, maxInitialMass)));
        }
        return bodies;
    }


    private List<MassiveBody> createNormalRandomBodies(double gridSize, double density, int maxInitialMass){
        List<MassiveBody> bodies = new ArrayList<>();
        double mean = gridSize / 2;
        double stdev = Math.max(1, gridSize / 10);
        int numElements = (int)Math.floor(gridSize * gridSize * density);
        for( int i = 0 ; i < numElements ; i++){
            DoublePair pair = NormalDistribution.getValueRandomPair(mean, stdev);
            if(pair.z0() >= 0 && pair.z0() < gridSize && pair.z1() >= 0 && pair.z1() < gridSize){
                bodies.add(MassiveBody.stationary(
                    new Position(pair.z0(), pair.z1()), ThreadLocalRandom.current().nextInt(1, maxInitialMass)));
            }
            
        }
        return bodies;
    }

    private List<MassiveBody> createTwoSwarmsNormalRandomBodies(double gridSize, double density, int maxInitialMass){
        List<MassiveBody> bodies = new ArrayList<>();
        double meanY = gridSize / 2;
        double meanXLeft = gridSize / 2 - gridSize / 6;
        double meanXRight = gridSize / 2 + gridSize / 6;
        double stdev = Math.max(1, gridSize / 10);
        int numElements = (int)Math.floor(gridSize * gridSize * density);
        for( int i = 0 ; i < numElements / 2 ; i++){
            DoublePair pair = NormalDistribution.getValueRandomPair(meanXLeft, meanY, stdev);
            if(isInsideGrid(pair, gridSize)){
                bodies.add(MassiveBody.stationary(
                    new Position(pair.z0(), pair.z1()), ThreadLocalRandom.current().nextInt(1, maxInitialMass)));
            }
            pair = NormalDistribution.getValueRandomPair(meanXRight, meanY, stdev);
            if(isInsideGrid(pair, gridSize)){
                bodies.add(MassiveBody.stationary(
                    new Position(pair.z0(), pair.z1()), ThreadLocalRandom.current().nextInt(1, maxInitialMass)));
            }
        }
        bodies.add(MassiveBody.stationary(new Position(meanY, meanY), maxInitialMass * 1000));
        return bodies;
    }

    private boolean isInsideGrid(DoublePair pair, double gridSize){
        return pair.z0() >= 0 && pair.z0() < gridSize && pair.z1() >= 0 && pair.z1() < gridSize;
    }

    private List<MassiveBody> createNormalRandomBodiesWithInitialVelocity(double gridSize, double density, int maxInitialMass){
        List<MassiveBody> bodies = new ArrayList<>();
        double mean = gridSize / 2;
        double stdev = Math.max(1, gridSize / 8);
        int numElements = (int)Math.floor(gridSize * gridSize * density);
        Position origin = new Position(gridSize / 2, gridSize / 2);
        for( int i = 0 ; i < numElements ; i++){
            DoublePair pair = NormalDistribution.getValueRandomPair(mean, stdev);
            if(pair.z0() >= 0 && pair.z0() < gridSize && pair.z1() >= 0 && pair.z1() < gridSize){
                Position position = new Position(pair.z0(), pair.z1());
                double distance = Math.sqrt(position.distanceFromSquared(origin));
                // velocity vector is perpendicular to point vector and inversly proportional to its length
                // double velocityMagnitude = gridSize / 10 / (distance * distance) / deltaT;
                // double initialVelocityDumpening = (2 * distance /  (1 + distance * distance ));
                double initialVelocityDumpening = 0.05;
                double distanceLimit = 3.0 * stdev ;
                double velocityMagnitude = distance > distanceLimit ? 1 / deltaT * initialVelocityDumpening : 1 / deltaT * Math.sqrt(distance) / distanceLimit ;                
                double angle = Math.acos((position.x() - gridSize / 2) / distance);
                if(position.y() > gridSize / 2){
                    angle = angle * -1;
                }
                angle = angle + Math.PI / 2;
                Position initialVelocity = new Position(velocityMagnitude * Math.cos(angle), 
                                                        -1 * velocityMagnitude * Math.sin(angle) );
                Position initialacceleration = new Position(velocityMagnitude  * Math.cos(angle + Math.PI / 2) , 
                                                        -1 * velocityMagnitude  * Math.sin(angle + Math.PI / 2) );                                                        
                bodies.add(new MassiveBody(
                    position, 
                    ThreadLocalRandom.current().nextInt(1, maxInitialMass),
                    initialVelocity,
                    initialacceleration
                    ));
            }
            
        }
        /*
        bodies.add(new MassiveBody(new Position(gridSize / 2 + 20, gridSize / 2), maxInitialMass * numElements, 
            new Position(0, -10), 
            new Position(-10, 0)));
        bodies.add(new MassiveBody(new Position(gridSize / 2 - 20, gridSize / 2), maxInitialMass * numElements, 
            new Position(0, 10), 
            new Position(10, 0)));
         */    
        // bodies.add(MassiveBody.stationary(new Position(gridSize / 2, gridSize / 2), maxInitialMass * numElements));        
        return bodies;
    }

    private MassBodyTree createTree(double gridSize, List<MassiveBody> bodies){
        MassBodyTreeImpl tree = new MassBodyTreeImpl(gridSize, bodies.size());
        bodies.stream().forEach( e -> tree.addBody(e));
        return tree;
    }

}
