package com.psz.graphics.finelmsim.domain.tree;

import java.util.Optional;

import com.psz.graphics.finelmsim.domain.element.MassiveBody;
import com.psz.graphics.finelmsim.domain.element.Position;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TreeNode {
    private boolean isLeaf;
    private final int parentIndex;
    private int childIndex;
    private final int myIndex;
    private final Position center;
    private final double size;
    private Optional<MassiveBody> content;
    private Optional<MassiveBody> centerMass;
    private final double sizesquared;

    public TreeNode(boolean isLeaf, int parentIndex, int childIndex, int myIndex, Position center, double size,
    Optional<MassiveBody> content, 
    Optional<MassiveBody> centerMass) {
        this.isLeaf = isLeaf;
        this.parentIndex = parentIndex;
        this.childIndex = childIndex;
        this.myIndex = myIndex;
        this.center = center;
        this.size = size;
        this.content = content;
        this.centerMass = centerMass;
        this.sizesquared = size * size;
    }    
    
    public boolean isLeaf() {
        return isLeaf;
    }

    public int myIndex() {
        return myIndex;
    }

    public int parentIndex() {
        return parentIndex;
    }

    public int childIndex() {
        return childIndex;
    }

    public Position center() {
        return center;
    }

    public double size() {
        return size;
    }

    public Optional<MassiveBody> content() {
        return content;
    }

    public Optional<MassiveBody> centerMass() {
        return centerMass;
    }

    public double sizeSquared() {
        return sizesquared;
    }

    public void setIsLeaf(boolean isLeaf){
        this.isLeaf = isLeaf;
    }   
    
    public void setChildIndex(int childIndex){
        this.childIndex = childIndex;
    }   
    
    public void setCenterMass(MassiveBody centerMass){
        this.centerMass = Optional.of(centerMass);
    }

    public void setContent(MassiveBody content){
        this.content = Optional.of(content);
    }

    public void clearContent(){
        log.debug("Clearing content of node {} with content {}", this, this.content);
        this.content = Optional.empty();
        this.centerMass = Optional.empty();
    }

    public TreeNode clone(){
        return new TreeNode(this.isLeaf, this.parentIndex, this.childIndex, this.myIndex, this.center, this.size, this.content, this.centerMass);
    }

    public boolean bodyInGrid(MassiveBody body){
        double half_size = size / 2;
        return body.getPosition().getX() >= center.getX() - half_size - Position.MIN_DISTANCE 
        && body.getPosition().getX() < center.getX() + half_size + Position.MIN_DISTANCE
        && body.getPosition().getY() > center.getY() - half_size - Position.MIN_DISTANCE
        && body.getPosition().getY() <= center.getY() + half_size + Position.MIN_DISTANCE;
    }

    public boolean bodyOutOfGrid(MassiveBody body){
        double half_size = size / 2 + Position.MIN_DISTANCE;
        return body.getPosition().getX() < center.getX() - half_size 
        || body.getPosition().getX() > center.getX() + half_size 
        || body.getPosition().getY() < center.getY() - half_size 
        || body.getPosition().getY() > center.getY() + half_size ;
    }

    public String toString(){
        return String.format("TreeNode[isLeaf=%s, myIndex=%d, parentIndex=%d, childIndex=%d, center=(%.2f, %.2f), size=%.2f, content=%s, centerMass=%s]", 
        isLeaf, myIndex, parentIndex, childIndex, center.getX(), center.getY(), size, content.map( c -> c.toString()).orElse("empty"), centerMass.map( cm -> cm.toString()).orElse("empty"));        
    }

    public String printBoundary(){
        double half_size = size / 2;
        return String.format("Boundary: [x: %.2f - %.2f, y: %.2f - %.2f]", 
        center.getX() - half_size, center.getX() + half_size, center.getY() - half_size, center.getY() + half_size);
    }
}
