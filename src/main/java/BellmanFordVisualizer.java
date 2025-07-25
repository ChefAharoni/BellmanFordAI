import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import java.util.*;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;

/**
 * JavaFX Pane for visualizing the graph and Bellman-Ford algorithm.
 * Handles drawing, animation, and user interaction.
 */
public class BellmanFordVisualizer extends Pane {
    private Graph graph;
    private Map<Integer, Circle> vertexNodes = new HashMap<>();
    private Map<Graph.Edge, Line> edgeLines = new HashMap<>();
    private Map<Integer, Text> distanceLabels = new HashMap<>();
    private int selectedVertex = -1;
    private List<BellmanFordAlgorithm.Step> steps = Collections.emptyList();
    private int currentStep = 0;
    private PauseTransition playTimer;
    private double[] lastDistances;
    private int sourceVertex = 0;
    private Integer tempEdgeFrom = null; // For edge creation
    private Map<Integer, Double[]> vertexPositions = new HashMap<>(); // Store positions for each vertex

    public BellmanFordVisualizer(Graph graph) {
        this.graph = graph;
        setPrefSize(800, 600);
        drawGraph();
        setupMouseHandlers();
    }

    /**
     * Sets up mouse event handlers for user interaction.
     */
    private void setupMouseHandlers() {
        setOnMouseClicked(this::handleMouseClick);
    }

    /**
     * Handles mouse clicks for adding/removing vertices/edges and setting source.
     */
    private void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        // Check if click is on a vertex
        Integer clickedVertex = getVertexAt(x, y);
        if (event.getButton() == MouseButton.PRIMARY) {
            if (clickedVertex == null) {
                // Add new vertex
                int newId = getNextVertexId();
                graph.addVertex(newId);
                // Store position where user clicked, or center if not available
                vertexPositions.put(newId, new Double[] { x, y });
                drawGraph();
            } else {
                // Start or finish edge creation
                if (tempEdgeFrom == null) {
                    tempEdgeFrom = clickedVertex;
                } else if (!tempEdgeFrom.equals(clickedVertex)) {
                    // Prompt for weight
                    TextInputDialog dialog = new TextInputDialog("1");
                    dialog.setTitle("Edge Weight");
                    dialog.setHeaderText("Enter weight for edge " + tempEdgeFrom + " → " + clickedVertex);
                    dialog.setContentText("Weight:");
                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent()) {
                        try {
                            double weight = Double.parseDouble(result.get());
                            graph.addEdge(tempEdgeFrom, clickedVertex, weight);
                        } catch (NumberFormatException e) {
                            showError("Invalid weight.");
                        }
                    }
                    tempEdgeFrom = null;
                    drawGraph();
                }
            }
        } else if (event.getButton() == MouseButton.SECONDARY) {
            if (clickedVertex != null) {
                // Show context menu: remove vertex, set as source
                ChoiceDialog<String> dialog = new ChoiceDialog<>("Remove Vertex",
                        Arrays.asList("Remove Vertex", "Set as Source"));
                dialog.setTitle("Vertex Options");
                dialog.setHeaderText("Options for vertex " + clickedVertex);
                dialog.setContentText("Choose:");
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    if (result.get().equals("Remove Vertex")) {
                        graph.removeVertex(clickedVertex);
                        vertexPositions.remove(clickedVertex);
                        drawGraph();
                    } else if (result.get().equals("Set as Source")) {
                        sourceVertex = clickedVertex;
                        drawGraph();
                    }
                }
            } else {
                // Remove edge if right-clicked near edge
                Graph.Edge edge = getEdgeAt(x, y);
                if (edge != null) {
                    graph.removeEdge(edge.from, edge.to);
                    drawGraph();
                }
            }
        }
    }

    /**
     * Returns the vertex at the given coordinates, or null if none.
     */
    private Integer getVertexAt(double x, double y) {
        for (Map.Entry<Integer, Circle> entry : vertexNodes.entrySet()) {
            Circle c = entry.getValue();
            if (c.contains(x, y))
                return entry.getKey();
        }
        return null;
    }

    /**
     * Returns the edge at the given coordinates, or null if none.
     */
    private Graph.Edge getEdgeAt(double x, double y) {
        for (Map.Entry<Graph.Edge, Line> entry : edgeLines.entrySet()) {
            Line l = entry.getValue();
            double dist = ptLineDist(l.getStartX(), l.getStartY(), l.getEndX(), l.getEndY(), x, y);
            if (dist < 10)
                return entry.getKey();
        }
        return null;
    }

    /**
     * Helper: distance from point to line segment.
     */
    private double ptLineDist(double x1, double y1, double x2, double y2, double px, double py) {
        double dx = x2 - x1, dy = y2 - y1;
        double len2 = dx * dx + dy * dy;
        if (len2 == 0)
            return Math.hypot(px - x1, py - y1);
        double t = ((px - x1) * dx + (py - y1) * dy) / len2;
        t = Math.max(0, Math.min(1, t));
        double projX = x1 + t * dx, projY = y1 + t * dy;
        return Math.hypot(px - projX, py - projY);
    }

    /**
     * Returns the next available vertex id (smallest non-negative integer not in
     * use).
     */
    private int getNextVertexId() {
        Set<Integer> used = graph.getVertices();
        int id = 0;
        while (used.contains(id))
            id++;
        return id;
    }

    /**
     * Shows an error dialog.
     */
    private void showError(String msg) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * Returns the current source vertex.
     */
    public int getSourceVertex() {
        return sourceVertex;
    }

    /**
     * Sets the source vertex.
     */
    public void setSourceVertex(int v) {
        this.sourceVertex = v;
        drawGraph();
    }

    /**
     * Loads steps for visualization and resets state.
     */
    public void loadSteps(List<BellmanFordAlgorithm.Step> steps) {
        this.steps = steps;
        this.currentStep = 0;
        if (!steps.isEmpty()) {
            lastDistances = Arrays.copyOf(steps.get(0).distanceSnapshot, steps.get(0).distanceSnapshot.length);
        }
        drawGraph();
        updateDistances(lastDistances);
    }

    /**
     * Moves to the next step and animates it.
     */
    public void nextStep() {
        if (steps == null || currentStep >= steps.size())
            return;
        BellmanFordAlgorithm.Step step = steps.get(currentStep);
        animateStep(step);
        currentStep++;
    }

    /**
     * Moves to the previous step and animates it.
     */
    public void prevStep() {
        if (steps == null || currentStep <= 1)
            return;
        currentStep -= 2;
        nextStep();
    }

    /**
     * Plays the animation automatically.
     */
    public void play() {
        if (playTimer != null)
            playTimer.stop();
        playTimer = new PauseTransition(Duration.seconds(1));
        playTimer.setOnFinished(e -> {
            if (currentStep < steps.size()) {
                nextStep();
                play();
            }
        });
        playTimer.play();
    }

    /**
     * Pauses the animation.
     */
    public void pause() {
        if (playTimer != null)
            playTimer.stop();
    }

    /**
     * Draws a legend explaining the colors and controls.
     */
    private void drawLegend() {
        VBox legend = new VBox(5);
        legend.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 8; -fx-border-color: #bbb; -fx-border-width: 1;");
        legend.setLayoutX(10);
        legend.setLayoutY(10);
        legend.getChildren().addAll(
                new Text("Legend:"),
                new Text("• Gold node: Source vertex"),
                new Text("• Orange edge: Relaxed (distance updated)"),
                new Text("• Red edge: Not relaxed (no update)"),
                new Text("• Green label: Current distance"),
                new Text("• Left-click empty: Add vertex"),
                new Text("• Left-click vertex: Start/end edge creation"),
                new Text("• Right-click vertex: Remove/Set as source"),
                new Text("• Right-click edge: Remove edge"));
        getChildren().add(legend);
    }

    /** Draws the current state of the graph. */
    public void drawGraph() {
        getChildren().clear();
        vertexNodes.clear();
        edgeLines.clear();
        distanceLabels.clear();
        List<Integer> vertices = new ArrayList<>(graph.getVertices());
        int n = vertices.size();
        double centerX = 400, centerY = 300, radius = 200;
        // Assign default positions for any missing
        for (int i = 0; i < n; i++) {
            int v = vertices.get(i);
            if (!vertexPositions.containsKey(v)) {
                double angle = 2 * Math.PI * i / n;
                double x = centerX + radius * Math.cos(angle);
                double y = centerY + radius * Math.sin(angle);
                vertexPositions.put(v, new Double[] { x, y });
            }
        }
        // Draw edges
        for (Graph.Edge e : graph.getEdges()) {
            Double[] fromPos = vertexPositions.get(e.from);
            Double[] toPos = vertexPositions.get(e.to);
            if (fromPos != null && toPos != null) {
                Line line = new Line(fromPos[0], fromPos[1], toPos[0], toPos[1]);
                line.setStroke(Color.GRAY);
                getChildren().add(line);
                edgeLines.put(e, line);
                double midX = (fromPos[0] + toPos[0]) / 2;
                double midY = (fromPos[1] + toPos[1]) / 2;
                Text weightText = new Text(midX, midY, String.format("%.1f", e.weight));
                weightText.setFill(Color.DARKBLUE);
                getChildren().add(weightText);
            }
        }
        // Draw vertices
        for (Map.Entry<Integer, Double[]> entry : vertexPositions.entrySet()) {
            int v = entry.getKey();
            Double[] pos = entry.getValue();
            Circle circle = new Circle(pos[0], pos[1], 25, v == sourceVertex ? Color.GOLD : Color.LIGHTBLUE);
            circle.setStroke(v == sourceVertex ? Color.DARKGOLDENROD : Color.DARKBLUE);
            circle.setStrokeWidth(v == sourceVertex ? 4 : 2);
            // Drag-and-drop handlers (robust: only update node and edges during drag)
            circle.setOnMousePressed(e -> {
                circle.setUserData(
                        new double[] { e.getSceneX() - circle.getCenterX(), e.getSceneY() - circle.getCenterY() });
                e.consume();
            });
            circle.setOnMouseDragged(e -> {
                Object userData = circle.getUserData();
                if (userData instanceof double[]) {
                    double[] offset = (double[]) userData;
                    double newX = e.getSceneX() - offset[0];
                    double newY = e.getSceneY() - offset[1];
                    newX = Math.max(25, Math.min(getWidth() - 25, newX));
                    newY = Math.max(25, Math.min(getHeight() - 25, newY));
                    circle.setCenterX(newX);
                    circle.setCenterY(newY);
                    vertexPositions.put(v, new Double[] { newX, newY });
                    // Update connected edges and labels only
                    updateConnectedEdgesAndLabels(v, newX, newY);
                }
                e.consume();
            });
            circle.setOnMouseReleased(e -> {
                Double[] newPos = new Double[] { circle.getCenterX(), circle.getCenterY() };
                vertexPositions.put(v, newPos);
                drawGraph(); // Redraw everything after drag is finished
                e.consume();
            });
            getChildren().add(circle);
            vertexNodes.put(v, circle);
            Text label = new Text(pos[0] - 5, pos[1] + 5, String.valueOf(v));
            label.setFill(Color.BLACK);
            getChildren().add(label);
        }
        updateDistances(lastDistances);
        drawLegend();
    }

    /**
     * Animates a single step of the Bellman-Ford algorithm.
     * Highlights the edge and updates distance labels.
     */
    public void animateStep(BellmanFordAlgorithm.Step step) {
        drawGraph();
        // Highlight the edge being relaxed
        Line line = edgeLines.get(step.edge);
        if (line != null) {
            line.setStroke(step.relaxed ? Color.ORANGE : Color.RED);
            line.setStrokeWidth(4);
        }
        // Update distances
        updateDistances(step.distanceSnapshot);
        lastDistances = Arrays.copyOf(step.distanceSnapshot, step.distanceSnapshot.length);
    }

    /**
     * Updates the distance labels for each vertex.
     */
    public void updateDistances(double[] distances) {
        if (distances == null)
            return;
        for (Map.Entry<Integer, Circle> entry : vertexNodes.entrySet()) {
            int v = entry.getKey();
            Circle circle = entry.getValue();
            double d = v < distances.length ? distances[v] : Double.POSITIVE_INFINITY;
            String label = (d == Double.POSITIVE_INFINITY) ? "∞" : String.format("%.1f", d);
            Text distLabel = new Text(circle.getCenterX() - 15, circle.getCenterY() - 30, label);
            distLabel.setFill(Color.FORESTGREEN);
            getChildren().add(distLabel);
            distanceLabels.put(v, distLabel);
        }
    }

    // TODO: Add methods for user interaction (add/remove vertex/edge, set source,
    // etc.)

    // Add this helper method to update only the edges and labels connected to a
    // node during drag
    private void updateConnectedEdgesAndLabels(int vertex, double newX, double newY) {
        // Update outgoing edges
        for (Graph.Edge e : graph.getEdges()) {
            if (e.from == vertex || e.to == vertex) {
                Line line = edgeLines.get(e);
                if (line != null) {
                    Double[] fromPos = vertexPositions.get(e.from);
                    Double[] toPos = vertexPositions.get(e.to);
                    if (fromPos != null && toPos != null) {
                        line.setStartX(fromPos[0]);
                        line.setStartY(fromPos[1]);
                        line.setEndX(toPos[0]);
                        line.setEndY(toPos[1]);
                    }
                }
            }
        }
        // Update label position
        Text label = distanceLabels.get(vertex);
        if (label != null) {
            label.setX(newX - 5);
            label.setY(newY + 5);
        }
    }
}