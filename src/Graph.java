import java.util.*;

/**
 * Represents a directed, weighted graph for the Bellman-Ford algorithm.
 * Supports adding/removing vertices and edges, and adjusting edge weights.
 */
public class Graph {
    /** Represents an edge in the graph. */
    public static class Edge {
        public int from, to;
        public double weight;

        public Edge(int from, int to, double weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    // Map vertex id to list of outgoing edges
    private final Map<Integer, List<Edge>> adjList = new HashMap<>();
    // Set of all vertices
    private final Set<Integer> vertices = new HashSet<>();

    /** Adds a vertex to the graph. */
    public void addVertex(int v) {
        vertices.add(v);
        adjList.putIfAbsent(v, new ArrayList<>());
    }

    /** Removes a vertex and all its edges. */
    public void removeVertex(int v) {
        vertices.remove(v);
        adjList.remove(v);
        for (List<Edge> edges : adjList.values()) {
            edges.removeIf(e -> e.to == v);
        }
    }

    /** Adds or updates an edge. */
    public void addEdge(int from, int to, double weight) {
        addVertex(from);
        addVertex(to);
        List<Edge> edges = adjList.get(from);
        for (Edge e : edges) {
            if (e.to == to) {
                e.weight = weight;
                return;
            }
        }
        edges.add(new Edge(from, to, weight));
    }

    /** Removes an edge. */
    public void removeEdge(int from, int to) {
        List<Edge> edges = adjList.get(from);
        if (edges != null) {
            edges.removeIf(e -> e.to == to);
        }
    }

    /** Returns all vertices. */
    public Set<Integer> getVertices() {
        return Collections.unmodifiableSet(vertices);
    }

    /** Returns all edges. */
    public List<Edge> getEdges() {
        List<Edge> all = new ArrayList<>();
        for (List<Edge> edges : adjList.values()) {
            all.addAll(edges);
        }
        return all;
    }

    /** Returns outgoing edges from a vertex. */
    public List<Edge> getOutgoingEdges(int v) {
        return adjList.getOrDefault(v, Collections.emptyList());
    }
}