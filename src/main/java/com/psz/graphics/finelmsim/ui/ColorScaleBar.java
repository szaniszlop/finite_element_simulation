package com.psz.graphics.finelmsim.ui;

import java.awt.Color;
import java.awt.Graphics2D;

public class ColorScaleBar {
    public static enum Orientation{ VERTICAL, HORIZONTAL, NONE} ;
    public static void paint(Graphics2D graphics, Color[] colors, Orientation orientation, int imageWidth, int imageHeight){
        int scaleWidth = 0, scaleHeight = 0, margin = 0, xStart = 0, yStart = 0, colorStep = 0, actualScaleHeight = 0, 
            actualScaleLength = 0, deltaX = 0, deltaY = 0, segmentWidth = 0, segmentHeight = 0;
        

        switch(orientation){
            case VERTICAL:
                scaleWidth = Math.min(Math.max(imageWidth / 20, 10), 30);
                margin = scaleWidth / 2;
                scaleHeight = imageHeight - scaleWidth;
                // Upper Left corner
                xStart = margin;
                yStart = margin;
                colorStep = Math.max( scaleHeight / colors.length, 1);
                actualScaleLength = scaleWidth;
                actualScaleHeight = colorStep * colors.length;
                deltaX = 0;
                deltaY = colorStep;
                segmentWidth = scaleWidth;
                segmentHeight = colorStep;
                break;
            case HORIZONTAL:
                scaleHeight = Math.min(Math.max(imageHeight / 20, 10), 30);
                margin = scaleHeight / 2;
                scaleWidth = imageWidth - margin;
                // Upper Left corner
                xStart = margin;
                yStart = margin;     
                colorStep = Math.max( scaleWidth / colors.length, 1);
                actualScaleLength =  colorStep * colors.length;
                actualScaleHeight = scaleHeight;
                deltaX = colorStep;
                deltaY = 0;
                segmentWidth = colorStep;
                segmentHeight = scaleHeight;     
                break;     
            default:
                return;          
        }
        graphics.setPaintMode();
        graphics.setColor(Color.WHITE);
        graphics.drawRect(xStart - 1, yStart - 1, actualScaleLength + 1, actualScaleHeight + 1);
        for( int colorIndex = 0 ; colorIndex < colors.length ; colorIndex ++){
            graphics.setColor(colors[colorIndex]);
            graphics.fillRect(xStart, yStart, segmentWidth, segmentHeight);
            graphics.setColor(Color.RED);
            if(orientation == Orientation.VERTICAL){
                graphics.drawString(Integer.toString(colorIndex), xStart + deltaX/2 + margin/2, yStart + deltaY/2 + margin/2);
            } else {
                graphics.drawString(Integer.toString(colorIndex), xStart + deltaX/2 - margin/2, yStart + segmentHeight - margin/2);
            }
            
            xStart = xStart + deltaX;
            yStart = yStart + deltaY;
        }


    }
}
