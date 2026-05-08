
package com.psz.graphics.finelmsim.domain.tree.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.psz.graphics.finelmsim.domain.element.MassiveBody;
import com.psz.graphics.finelmsim.domain.element.Position;
import com.psz.graphics.finelmsim.domain.tree.MassBodyTree;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MassBodyTreeImplTest {

    @Test
    public void simplePointsSequenceTest(){
        MassBodyTreeImpl tree = new MassBodyTreeImpl(8, 6);

        printTree(tree);
        assertEquals(1, tree.getNodes().size());

        // a(2, 2)
        tree.addBody(MassiveBody.stationary(new Position(2, 2), 1));
        printTree(tree);
        assertEquals(1, tree.getNodes().size());
  
        // b(6, 1)
        tree.addBody(MassiveBody.stationary(new Position(6, 1), 1));
        printTree(tree);
        assertEquals(5, tree.getNodes().size());
        
        // c(4, 2)
        tree.addBody(MassiveBody.stationary(new Position(4, 2), 3));
        printTree(tree);   
        assertEquals(9, tree.getNodes().size());
 
        // a'(2, 2)
        tree.addBody(MassiveBody.stationary(new Position(2, 2), 1));
        printTree(tree);  
        assertEquals(9, tree.getNodes().size());
        assertEquals(2, tree.getNodes().get(2).content().get().mass());
          
        // d(5, 3)
        tree.addBody(MassiveBody.stationary(new Position(5, 3), 5));
        printTree(tree);  
        assertEquals(13, tree.getNodes().size());
        assertEquals(3, tree.getNodes().get(10).content().get().mass());
        assertEquals(5, tree.getNodes().get(11).content().get().mass());

        // e(7, 4)
        tree.addBody(MassiveBody.stationary(new Position(7, 4), 7));
        printTree(tree);  
        assertEquals(13, tree.getNodes().size());
        assertEquals(3, tree.getNodes().get(10).content().get().mass());
        assertEquals(5, tree.getNodes().get(11).content().get().mass());    
        assertEquals(3, tree.getNodes().get(10).content().get().mass());
        assertEquals(7, tree.getNodes().get(3).content().get().mass());     
    }


    @Test
    public void distributeMassTest(){
        MassBodyTreeImpl tree = new MassBodyTreeImpl(8, 6);

        assertEquals(1, tree.getNodes().size());

        // a(2, 2)
        tree.addBody(MassiveBody.stationary(new Position(2, 2), 1));
  
        // b(6, 1)
        tree.addBody(MassiveBody.stationary(new Position(6, 1), 1));
        
        // c(4, 2)
        tree.addBody(MassiveBody.stationary(new Position(4, 2), 3));
 
        // a'(2, 2)
        tree.addBody(MassiveBody.stationary(new Position(2, 2), 1));
          
        // d(5, 3)
        tree.addBody(MassiveBody.stationary(new Position(5, 3), 5));

        // e(7, 4)
        tree.addBody(MassiveBody.stationary(new Position(7, 4), 7));
        
        tree.calculateMassDistribution();
        printTree(tree);
    }   
    
    @Test
    public void getAttractorsTest(){
        MassBodyTreeImpl tree = new MassBodyTreeImpl(8, 6);

        assertEquals(1, tree.getNodes().size());

        // a(2, 2)
        tree.addBody(MassiveBody.stationary(new Position(2, 2), 1));
  
        // b(6, 1)
        tree.addBody(MassiveBody.stationary(new Position(6, 1), 1));
        
        // c(4, 2)
        tree.addBody(MassiveBody.stationary(new Position(4, 2), 1));
 
        // a'(2, 2)
        tree.addBody(MassiveBody.stationary(new Position(2, 2), 1));
          
        // d(5, 3)
        tree.addBody(MassiveBody.stationary(new Position(5, 3), 1));

        // e(7, 4)
        tree.addBody(MassiveBody.stationary(new Position(7, 4), 1));
        
        tree.calculateMassDistribution();
        List<MassiveBody> masses = tree.getAttractors(new Position(2, 2), 10);
        masses.forEach( e -> log.info(e.toString()));
    }

    private void printTree(MassBodyTree tree){
        tree.getNodes().stream().forEach( e -> log.info( e != null ? e.toString() : "") );
    }
}