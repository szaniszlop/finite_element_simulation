package com.psz.graphics.finelmsim.ui;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

import com.psz.graphics.finelmsim.domain.element.MassiveBody;
import com.psz.graphics.finelmsim.domain.element.Position;
import com.psz.graphics.finelmsim.domain.tree.MassBodyTree;
import com.psz.graphics.finelmsim.domain.tree.TreeNode;
import com.psz.graphics.finelmsim.utils.MethodTimer;
import com.psz.graphics.finelmsim.utils.MethodTimer.TimeUnit;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MassTreeCanvas extends BaseCanvas {

    private final double gridSize;
    private final double xstep, ystep;
    private final int xStepInt, yStepInt;

    @Setter
    private MassBodyTree tree;
    @Setter
    private Optional<MassiveBody> bodyToShow;
    @Setter
    private List<MassiveBody> bodiesToShow;

    @Getter
    @Setter
    private boolean showGrid, showTree, showMasses, showBody;

    @Setter
    private double theta = 0.05;

    @Setter
    private int fps;

    public MassTreeCanvas(int width, int height, double gridSize) {
        super(width, height);
        this.gridSize = gridSize;
        this.xstep = width / gridSize;
        this.ystep = height / gridSize;
        this.xStepInt = (int)Math.max(1, Math.round(xstep));
        this.yStepInt = (int)Math.max(1, Math.round(ystep));
        this.showGrid = false;
        this.showTree = true;
        this.showMasses = true;
        this.showBody = true;
        this.bodyToShow = Optional.empty();
        this.bodiesToShow = new ArrayList<>();
    }

    public void setTree(MassBodyTree tree){
        this.tree = tree;
        this.revalidate();
        this.repaint();
    }

    public void toggleShowGrid(){
        this.showGrid = !this.showGrid;
        this.revalidate();
        this.repaint();
    }

    public void toggleShowMasses(){
        this.showMasses = !this.showMasses;
        this.revalidate();
        this.repaint();
    }

    public void showContent(){
        this.revalidate();
        this.repaint();
    }

    @Override
    protected void paint(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        if(this.showGrid){
            paintGrid(g);  
        }
        if(this.showTree){
            MethodTimer.timeMethodExecution("paintTree", TimeUnit.mili, () -> paintTree(g));
        } 
        if( this.showBody){
            if(bodyToShow.isPresent()){
                showSingleBody(g, bodyToShow.get());
            } else {
                showBodies(g, bodiesToShow);
            }
            
        }
        
    }

    private void paintTree(Graphics2D g){
        if(tree != null){
            tree.getNodes().stream().forEach( n -> paintNode(n, g));
        }
    }

    private void paintNode(TreeNode node, Graphics2D g){
        if(this.showGrid){
            g.setColor(Color.WHITE);
            Stroke dotted = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
            0, new float[]{3}, 0);
            g.setStroke(dotted);
            if(node.size() == 1){
                int x = (int)Math.round(node.center().x() * xstep + 2);
                int y = (int)Math.round(node.center().y() * ystep + 2);
                if(x >= 0 && x < width && y >= 0 && y < height){
                    g.drawRect(x, y,  xStepInt - 4, yStepInt - 4);
                }
                
            } else {
                int x = (int)Math.round((node.center().x() - node.size() / 2) * xstep + 2);
                int y = (int)Math.round((node.center().y() - node.size() / 2) * ystep + 2);
                int nodeSize = (int) Math.max(1, Math.round(node.size()));
                if(x >= 0 && x < width && y >= 0 && y < height){
                    g.drawRect(x, y,  nodeSize * xStepInt - 4, nodeSize * yStepInt - 4);
                }                
            }      
        }
        if(this.showMasses && node.centerMass().isPresent()){
            drawMass(g, node.centerMass().get());
        }
        if(node.isLeaf() && node.content().isPresent()){
            drawBody(g, node.content().get());
        }        
    }
    
    private void drawMass(Graphics2D g, MassiveBody body){
        g.setColor(Color.BLUE);
        Position p = body.position();
        int size = Math.max(xStepInt / 4, 2) * (int)Math.round(Math.sqrt(body.mass()));
        int x = (int) Math.round(p.x() * xstep - size / 2);
        int y = (int) Math.round(p.y() * ystep - size / 2);
        g.drawOval(x, y, size, size);
    }

    private void drawBody(Graphics2D g, MassiveBody body){

        g.setColor(new Color(255, Math.max(0, 255 - body.mass()), Math.max(0, 255 - body.mass())));
        Position p = body.position();
        int x = (int) Math.round(p.x() * xstep );
        int y = (int) Math.round(p.y() * ystep);
        g.drawLine(x, y, x, y);
    }   
    
    private void paintGrid(Graphics2D g){
        g.setColor(Color.DARK_GRAY);
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
        0, new float[]{9}, 0);
        g.setStroke(dashed);
        for(int i = 1; i < width / xStepInt ; i++){
            g.drawLine(i * xStepInt, 0, i * xStepInt, height);
        }
        for(int i = 1; i < height / yStepInt ; i++){
            g.drawLine(0, i * yStepInt, width, i * yStepInt);
        }          
    }

    private void showSingleBody(Graphics2D g, MassiveBody body){
        List<MassiveBody> attractors = tree.getAttractors(body.position(), this.theta);
        attractors.stream().forEach( e -> drawMass(g, e));
        log.info("Number of attractors for body {} is {}, theta: {}", body, attractors.size(), theta);
        drawBody(g, body);
    }

    private void showBodies(Graphics2D g, List<MassiveBody> bodiesToShow){
        bodiesToShow.stream().forEach( e -> drawBody(g, e));
        drawFps(g);
        drawNumberBodies(g);
    }

    private void drawFps(Graphics2D g){
        String fpsText = "FPS: %d".formatted(fps);
        drawText(g, Color.RED, fpsText, 20, -3);

    }

    private void drawNumberBodies(Graphics2D g){
        String fpsText = "#bodies: %d".formatted(bodiesToShow.size());
        drawText(g, Color.RED, fpsText, 20, -2);
    }

    private void drawText(Graphics2D g, Color color, String text, int column, int line){
        Rectangle bounds = getStringBounds(g, text, 0f, 0f);
        g.setColor(color);
        g.drawString(text, column, height + (bounds.height + 3) * line);
    }

    private Rectangle getStringBounds(Graphics2D g2, String str,
                                      float x, float y)
    {
        FontRenderContext frc = g2.getFontRenderContext();
        GlyphVector gv = g2.getFont().createGlyphVector(frc, str);
        return gv.getPixelBounds(null, x, y);
    }    
}
