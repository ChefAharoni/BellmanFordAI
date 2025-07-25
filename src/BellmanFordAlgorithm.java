import java.util.*;

/**
 * Implements the Bellman-Ford shortest path algorithm with step-by-step support
 * for visualization.
 */
public class BellmanFordAlgorithm {
    private final Graph graph;
    private int source;
    private double[] distance;
    private int[] predecessor;
    private List<Step> steps; // For visualization

    /**
     * Represents a single step in the algorithm for visualization.
     */
    public static class Step {
        public final int iteration;
        public final Graph.Edge edge;
        public final double[] distanceSnapshot;
        public final int[] predecessorSnapshot;
        public final boolean relaxed;

        public Step(int iteration, Graph.Edge edge, double[] distance, int[] predecessor, boolean relaxed) {
            this.iteration = iteration;
            this.edge = edge;
            this.distanceSnapshot = Arrays.copyOf(distance, distance.length);
            this.predecessorSnapshot = Arrays.copyOf(predecessor, predecessor.length);
            this.relaxed = relaxed;
        }
    }

    /**
     * Initializes the algorithm with a graph and source vertex.
     */
    public BellmanFordAlgorithm(Graph graph, int source) {
        this.graph = graph;
        this.source = source;
        int n = graph.getVertices().stream().max(Integer::compareTo).orElse(0) + 1;
        distance = new double[n];
        predecessor = new int[n];
        steps = new ArrayList<>();
    }

    /**
     * Runs the Bellman-Ford algorithm, recording each step for visualization.
     * 
     * @return true if no negative-weight cycles, false otherwise
     */
    public boolean run() {
        int n = graph.getVertices().stream().max(Integer::compareTo).orElse(0) + 1;
        Arrays.fill(distance, Double.POSITIVE_INFINITY);
        Arrays.fill(predecessor, -1);
        distance[source] = 0;
        steps.clear();

        List<Graph.Edge> edges = graph.getEdges();
        for (int i = 1; i < n; i++) {
            for (Graph.Edge e : edges) {
                boolean relaxed = false;
                if (distance[e.from] + e.weight < distance[e.to]) {
                    distance[e.to] = distance[e.from] + e.weight;
                    predecessor[e.to] = e.from;
                    relaxed = true;
                }
                steps.add(new Step(i, e, distance, predecessor, relaxed));
            }
        }
        // Check for negative-weight cycles
        for (Graph.Edge e : edges) {
            if (distance[e.from] + e.weight < distance[e.to]) {
                return false; // Negative cycle detected
            }
        }
        return true;
    }

    /** Returns the list of steps for visualization. */
    public List<Step> getSteps() {
        return steps;
    }

    /** Returns the shortest distances from the source. */
    public double[] getDistances() {
        return Arrays.copyOf(distance, distance.length);
    }

    /** Returns the predecessor array for path reconstruction. */
    public int[] getPredecessors() {
        return Arrays.copyOf(predecessor, predecessor.length);
    }

    /** Sets the source vertex and resets the algorithm. */
    public void setSource(int source) {
        this.source = source;
    }
}