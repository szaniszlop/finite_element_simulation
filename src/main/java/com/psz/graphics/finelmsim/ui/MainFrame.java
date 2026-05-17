package com.psz.graphics.finelmsim.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.psz.graphics.finelmsim.domain.element.MassiveBody;
import com.psz.graphics.finelmsim.domain.generator.MassBodyGenerator;
import com.psz.graphics.finelmsim.domain.simulator.GravitySimulator;
import com.psz.graphics.finelmsim.domain.tree.MassBodyTree;
import com.psz.graphics.finelmsim.domain.tree.impl.MassBodyTreeImpl;
import com.psz.graphics.finelmsim.utils.MethodTimer;
import com.psz.graphics.finelmsim.utils.MethodTimer.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MainFrame extends JFrame implements ApplicationRunner, ApplicationContextAware{

    private static final double DEFAULT_GRID_SIZE = 1024.0;

    private static final int DEFAULT_WEIGHT = 100;

    private static final double DEFAULT_DENSITY = 0.05;

    private static final double DEFAULT_SIMULATION_THETA = 10.0;

    private static final double DEFAULT_DELTA_T = 0.005;

    private ApplicationContext applicationContext;
    private MassTreeCanvas masTreeCanvas;
    private List<MassiveBody> bodies;
    private GravitySimulator gravitySimulator;
    private final Map<String, MassBodyGenerator> generators;
    private JButton startStopSimulationButton;

    private double deltaT = DEFAULT_DELTA_T;
    private double simulationTheta = DEFAULT_SIMULATION_THETA;
    private double density = DEFAULT_DENSITY;
    private int initialWeight = DEFAULT_WEIGHT;

    private final double gridSize;

	public MainFrame() {
        generators = new HashMap<>();
        this.gridSize = DEFAULT_GRID_SIZE;
        this.masTreeCanvas = new MassTreeCanvas((int)gridSize * 2, (int)gridSize, gridSize);
        this.gravitySimulator = new GravitySimulator(masTreeCanvas, deltaT, simulationTheta,  (int)gridSize);     
        this.bodies = new ArrayList<>();   
    }

    private void setupGenerators(){
        applicationContext.getBeansOfType(MassBodyGenerator.class).values().stream().forEach( generator -> {
            generators.put(generator.getName(), generator);
            log.info("Registered Mass Body Generator: {} - {}", generator.getName(), generator.getDescription());
        });
    }

    private void initUI() {

        Map<String, JComponent> components = new HashMap<String, JComponent>();

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

    }

    private void createLayout(Map<String, JComponent> components) {

        var pane = getContentPane();
        var bl = new BorderLayout();
        pane.setLayout(bl);
        for (Entry<String, JComponent> componentEntry : components.entrySet()){
            pane.add(componentEntry.getValue(), componentEntry.getKey());
        }
    }    

    private JComponent buttonsPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3,2));
        panel.add(toggleGridButton());
        panel.add(toggleMassesButton());
        panel.add(showRandomBodyButon());
        panel.add(startStopSimulationButton());
        panel.add(massBodyGeneratorSelector());
        return panel;

    }

    private java.awt.Component massBodyGeneratorSelector() {
        var generatorSelector = new JComboBox<>();

        generators.entrySet().stream().forEach( entry -> generatorSelector.addItem(entry.getKey()));
        generatorSelector.addActionListener((ActionEvent event) -> {
            String selectedGeneratorName = (String) generatorSelector.getSelectedItem();
            generatorSelectedEventHandler(selectedGeneratorName);
        });
        return generatorSelector;
    }

    private void generatorSelectedEventHandler(String selectedGeneratorName){
        MassBodyGenerator selectedGenerator = generators.get(selectedGeneratorName);
        if(selectedGenerator != null){
            stopSimulationAction();
            MethodTimer<List<MassiveBody>> createBodiesTimer = new MethodTimer<>();
            this.bodies = createBodiesTimer.timeMethodExecution(selectedGeneratorName, TimeUnit.mili, 
                () -> selectedGenerator.createBodies(this.gridSize, this.density, this.initialWeight));
            log.info("Number of Bodies created {}", bodies.size());            
            MassBodyTree tree = createTree(this.gridSize, this.bodies);
            tree.calculateMassDistribution();
            masTreeCanvas.setTree(tree);
            masTreeCanvas.setBodiesToShow(bodies);
            masTreeCanvas.showContent();
        }
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
                stopSimulationAction();
            } else {
                startSimulationAction();
            }
        });
        this.startStopSimulationButton = button;
        return button;
    }

    private void stopSimulationAction(){
        gravitySimulator.stopSimulation();
        this.bodies = gravitySimulator.getBodies();
        startStopSimulationButton.setText("Start Simulation");
    }

    private void startSimulationAction() {
        masTreeCanvas.setBodyToShow(Optional.empty());
        startStopSimulationButton.setText("Stop Simulation");
        gravitySimulator.startSimulation(bodies);
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
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        EventQueue.invokeLater(() -> {
            var ex = applicationContext.getBean(MainFrame.class);
            ex.setupGenerators();
            ex.initUI();
            ex.pack();
            ex.setVisible(true);
        });	
    }





    private MassBodyTree createTree(double gridSize, List<MassiveBody> bodies){
        MassBodyTreeImpl tree = new MassBodyTreeImpl(gridSize, bodies.size());
        bodies.stream().forEach( e -> tree.addBody(e));
        if(!tree.verifyTree()){
            log.error("Tree verification failed after creating tree");
            throw new RuntimeException("Tree verification failed after creating tree");
        }
        return tree;
    }

}
