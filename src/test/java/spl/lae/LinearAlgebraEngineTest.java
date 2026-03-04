package spl.lae;

import org.junit.jupiter.api.Test;

import parser.ComputationNode;
import parser.ComputationNodeType;
import spl.lae.LinearAlgebraEngine;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class LinearAlgebraEngineTest {
    private LinearAlgebraEngine engine;

    // run before every test
    @BeforeEach
    void setUp() {
        // make engine, use 2 threads
        engine = new LinearAlgebraEngine(2);
    }

    // cleanup after test
    @AfterEach
    void tearDown() {
        // kill engine reference
        engine = null;
    }

    @Test
    void testSanityCheck() {
        // make data 2x2
        double[][] data = {{1.0, 0.0}, {0.0, 1.0}};
        
        // create leaf node, type matrix
        ComputationNode root = new ComputationNode(data); 

        // run engine
        ComputationNode result = engine.run(root);

        // check result is not null
        assertNotNull(result);
        
        // check math result same as input
        assertArrayEquals(data, result.getMatrix());
    }

    @Test
    public void testSimpleAddition() {
        // setup data 1x2
        double[][] d1 = {{1.0, 2.0}};
        double[][] d2 = {{3.0, 4.0}};
        
        // make leaf nodes
        ComputationNode n1 = new ComputationNode(d1);
        ComputationNode n2 = new ComputationNode(d2);

        // list for children
        List<ComputationNode> kids = new ArrayList<>();
        kids.add(n1);
        kids.add(n2);

        // make root add op
        ComputationNode root = new ComputationNode(ComputationNodeType.ADD, kids);

        // run engine
        ComputationNode res = engine.run(root);

        // get result matrix
        double[][] mat = res.getMatrix();

        // check math correct
        assertEquals(4.0, mat[0][0], 0.001); // 1+3=4
        assertEquals(6.0, mat[0][1], 0.001); // 2+4=6
    }

    @Test
    public void testComplexFlow() {
        // data setup
        double[][] a = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] b = {{1.0, 1.0}, {1.0, 1.0}};
        double[][] c = {{2.0, 0.0}, {0.0, 2.0}}; // identity * 2

        // build tree (a+b)*c
        ComputationNode nodeA = new ComputationNode(a);
        ComputationNode nodeB = new ComputationNode(b);
        ComputationNode nodeC = new ComputationNode(c);

        ComputationNode sumNode = new ComputationNode(ComputationNodeType.ADD, List.of(nodeA, nodeB));
        ComputationNode root = new ComputationNode(ComputationNodeType.MULTIPLY, List.of(sumNode, nodeC));

        // run engine
        LinearAlgebraEngine engine = new LinearAlgebraEngine(2);
        ComputationNode result = engine.run(root);

        // check math
        double[][] out = result.getMatrix();
        assertEquals(4.0, out[0][0], 0.001); // (1+1)*2
        assertEquals(6.0, out[0][1], 0.001); // (2+1)*2
        assertEquals(8.0, out[1][0], 0.001); // (3+1)*2
        assertEquals(10.0, out[1][1], 0.001); // (4+1)*2
    }

    @Test
    public void testMultiplyDimensionMismatch() {
        // 2x2 matrix
        double[][] m1 = {{1, 2}, {3, 4}};
        // 3x2 matrix
        double[][] m2 = {{1, 2}, {3, 4}, {5, 6}};

        ComputationNode node1 = new ComputationNode(m1);
        ComputationNode node2 = new ComputationNode(m2);

        // bad math: 2 cols vs 3 rows
        ComputationNode root = new ComputationNode("*", List.of(node1, node2));

        // verify crash on bad dims
        assertThrows(IllegalArgumentException.class, () -> {
            engine.run(root);
        });
    }
}
