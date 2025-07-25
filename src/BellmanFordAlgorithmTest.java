import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

/**
 * Comprehensive test suite for Bellman-Ford algorithm.
 * Covers edge cases and ensures production-level robustness.
 */
public class BellmanFordAlgorithmTest {
    /** Helper to create a graph from edge list. */
    private Graph makeGraph(int[][] edges) {
        Graph g = new Graph();
        for (int[] e : edges) {
            g.addEdge(e[0], e[1], e[2]);
        }
        return g;
    }

    @Test
    public void testSingleNode() {
        Graph g = new Graph();
        g.addVertex(0);
        BellmanFordAlgorithm bfa = new BellmanFordAlgorithm(g, 0);
        assertTrue(bfa.run());
        assertEquals(0.0, bfa.getDistances()[0], 1e-9);
    }

    @Test
    public void testDisconnectedGraph() {
        Graph g = makeGraph(new int[][] { { 0, 1, 5 } });
        g.addVertex(2); // Disconnected
        BellmanFordAlgorithm bfa = new BellmanFordAlgorithm(g, 0);
        assertTrue(bfa.run());
        double[] d = bfa.getDistances();
        assertEquals(0.0, d[0], 1e-9);
        assertEquals(5.0, d[1], 1e-9);
        assertEquals(Double.POSITIVE_INFINITY, d[2], 1e-9);
    }

    @Test
    public void testNegativeEdgeNoCycle() {
        Graph g = makeGraph(new int[][] { { 0, 1, 4 }, { 0, 2, 5 }, { 1, 2, -2 } });
        BellmanFordAlgorithm bfa = new BellmanFordAlgorithm(g, 0);
        assertTrue(bfa.run());
        double[] d = bfa.getDistances();
        assertEquals(0.0, d[0], 1e-9);
        assertEquals(4.0, d[1], 1e-9);
        assertEquals(2.0, d[2], 1e-9);
    }

    @Test
    public void testNegativeCycle() {
        Graph g = makeGraph(new int[][] { { 0, 1, 1 }, { 1, 2, -1 }, { 2, 0, -1 } });
        BellmanFordAlgorithm bfa = new BellmanFordAlgorithm(g, 0);
        assertFalse(bfa.run());
    }

    @Test
    public void testZeroWeightEdges() {
        Graph g = makeGraph(new int[][] { { 0, 1, 0 }, { 1, 2, 0 }, { 2, 3, 0 } });
        BellmanFordAlgorithm bfa = new BellmanFordAlgorithm(g, 0);
        assertTrue(bfa.run());
        double[] d = bfa.getDistances();
        assertEquals(0.0, d[0], 1e-9);
        assertEquals(0.0, d[1], 1e-9);
        assertEquals(0.0, d[2], 1e-9);
        assertEquals(0.0, d[3], 1e-9);
    }

    @Test
    public void testSelfLoop() {
        Graph g = makeGraph(new int[][] { { 0, 0, 2 }, { 0, 1, 3 } });
        BellmanFordAlgorithm bfa = new BellmanFordAlgorithm(g, 0);
        assertTrue(bfa.run());
        double[] d = bfa.getDistances();
        assertEquals(0.0, d[0], 1e-9);
        assertEquals(3.0, d[1], 1e-9);
    }

    @Test
    public void testMultipleShortestPaths() {
        Graph g = makeGraph(new int[][] { { 0, 1, 1 }, { 0, 2, 1 }, { 1, 3, 1 }, { 2, 3, 1 } });
        BellmanFordAlgorithm bfa = new BellmanFordAlgorithm(g, 0);
        assertTrue(bfa.run());
        double[] d = bfa.getDistances();
        assertEquals(0.0, d[0], 1e-9);
        assertEquals(1.0, d[1], 1e-9);
        assertEquals(1.0, d[2], 1e-9);
        assertEquals(2.0, d[3], 1e-9);
    }

    @Test
    public void testLargeGraph() {
        int n = 1000;
        Graph g = new Graph();
        for (int i = 0; i < n - 1; i++) {
            g.addEdge(i, i + 1, 1);
        }
        BellmanFordAlgorithm bfa = new BellmanFordAlgorithm(g, 0);
        assertTrue(bfa.run());
        double[] d = bfa.getDistances();
        for (int i = 0; i < n; i++) {
            assertEquals((double) i, d[i], 1e-9);
        }
    }

    @Test
    public void testUnreachableNodes() {
        Graph g = makeGraph(new int[][] { { 0, 1, 2 }, { 1, 2, 2 } });
        g.addVertex(3); // unreachable
        BellmanFordAlgorithm bfa = new BellmanFordAlgorithm(g, 0);
        assertTrue(bfa.run());
        double[] d = bfa.getDistances();
        assertEquals(Double.POSITIVE_INFINITY, d[3], 1e-9);
    }

    @Test
    public void testNegativeEdgeFromSource() {
        Graph g = makeGraph(new int[][] { { 0, 1, -5 }, { 1, 2, 2 } });
        BellmanFordAlgorithm bfa = new BellmanFordAlgorithm(g, 0);
        assertTrue(bfa.run());
        double[] d = bfa.getDistances();
        assertEquals(0.0, d[0], 1e-9);
        assertEquals(-5.0, d[1], 1e-9);
        assertEquals(-3.0, d[2], 1e-9);
    }
}