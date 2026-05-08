package com.psz.graphics.finelmsim.domain.tree;

import java.util.Optional;

import com.psz.graphics.finelmsim.domain.element.MassiveBody;
import com.psz.graphics.finelmsim.domain.element.Position;

public record TreeNode(
    boolean isLeaf, 
    int parentIndex, 
    int childIndex, 
    Position center, 
    double size, 
    Optional<MassiveBody> content, 
    Optional<MassiveBody> centerMass) {    
    
    public static TreeNode cloneWithMass(TreeNode node, Optional<MassiveBody> centerMass){
        return new TreeNode(node.isLeaf(), node.parentIndex(),
        node.childIndex(), node.center(), node.size(),
        node.content(), centerMass);
    }
}
