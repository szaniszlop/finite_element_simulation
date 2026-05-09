package com.psz.graphics.finelmsim.domain.tree;

import java.util.List;

import com.psz.graphics.finelmsim.domain.element.MassiveBody;
import com.psz.graphics.finelmsim.domain.element.Position;

public interface MassBodyTree {

    void clearNode(int nodeIndex);

    void replaceNode(TreeNode oldNode, TreeNode newNode);

    void addBody(MassiveBody body);
    
    void calculateMassDistribution();
    
    List<TreeNode> getNodes();

    List<MassiveBody> getAttractors(Position position, double theta);


}
