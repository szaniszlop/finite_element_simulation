package com.psz.graphics.finelmsim.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import lombok.Getter;

@Getter
public abstract class BaseCanvas extends JPanel{

    public static final int MAX_WIDTH = 1200;
    public static final int MAX_HEIGHT = 1200;

    protected final int width, height;    

    public BaseCanvas(int width, int height){
        assert width > 0;
        assert width <= MAX_WIDTH;
        assert height > 0;
        assert height <= MAX_HEIGHT;
        this.width = width;
        this.height = height;
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
    

    @Override
    public Dimension getPreferredSize(){
        return new Dimension(width, height);
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        BufferedImage imageBuffer = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED);
        paint(imageBuffer.createGraphics());

        Graphics2D g2d = (Graphics2D)g.create();
        g2d.drawImage(imageBuffer, null, 0, 0);
        g2d.dispose();
    }

    protected abstract void paint(Graphics2D g);
}
