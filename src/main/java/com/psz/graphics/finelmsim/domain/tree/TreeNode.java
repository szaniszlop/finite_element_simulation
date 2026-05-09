package com.psz.graphics.finelmsim.domain.tree;

import java.util.Optional;

import com.psz.graphics.finelmsim.domain.element.MassiveBody;
import com.psz.graphics.finelmsim.domain.element.Position;

public class TreeNode {
    private final boolean isLeaf;
    private final int parentIndex;
    private final int childIndex;
    private final Position center;
    private final double size;
    private Optional<MassiveBody> content;
    private Optional<MassiveBody> centerMass;
    private final double sizesquared;

    public TreeNode(boolean isLeaf, int parentIndex, int childIndex, Position center, double size,
    Optional<MassiveBody> content, 
    Optional<MassiveBody> centerMass) {
        this.isLeaf = isLeaf;
        this.parentIndex = parentIndex;
        this.childIndex = childIndex;
        this.center = center;
        this.size = size;
        this.content = content;
        this.centerMass = centerMass;
        this.sizesquared = size * size;
    }    
    
    public boolean isLeaf() {
        return isLeaf;
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

    public void setCenterMass(MassiveBody centerMass){
        this.centerMass = Optional.of(centerMass);
    }

    public void setContent(MassiveBody content){
        this.content = Optional.of(content);
    }

    public void clearContent(){
        this.content = Optional.empty();
        this.centerMass = Optional.empty();
    }

    public TreeNode clone(){
        return new TreeNode(this.isLeaf, this.parentIndex, this.childIndex, this.center, this.size, this.content, this.centerMass);
    }

}
