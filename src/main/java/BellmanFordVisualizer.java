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
import javafx.scene.layout.HBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.layout.AnchorPane;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.Priority;

/**
 * JavaFX Pane for visualizing the graph and Bellman-Ford algorithm.
 * Handles drawing, animation, and user interaction.
 */
public class BellmanFordVisualizer extends StackPane {

    /**
     * Data class for table rows showing distance information.
     */
    public static class DistanceRow {
        private final int vertex;
        private final String distance;
        private final String status;

        public DistanceRow(int vertex, String distance, String status) {
            this.vertex = vertex;
            this.distance = distance;
            this.status = status;
        }

        public int getVertex() {
            return vertex;
        }

        public String getDistance() {
            return distance;
        }

        public String getStatus() {
            return status;
        }
    }

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
    public Map<Integer, Double[]> vertexPositions = new HashMap<>(); // Store positions for each vertex
    private boolean isDragging = false;

    // Table components for distance tracking
    private TableView<DistanceRow> distanceTable;
    private ObservableList<DistanceRow> tableData = FXCollections.observableArrayList();

    private AnchorPane graphPane; // For graph drawing only
    private VBox overlayBox; // For table and legend
    private static final double LEFT_MARGIN = 300; // px reserved for sidebar (table + legend)

    public BellmanFordVisualizer(Graph graph) {
        this.graph = graph;
        setPrefSize(800, 600);
        graphPane = new AnchorPane();
        graphPane.setPrefSize(800, 600);
        overlayBox = new VBox();
        overlayBox.setPrefWidth(LEFT_MARGIN);
        overlayBox.setMinWidth(LEFT_MARGIN);
        overlayBox.setMaxWidth(LEFT_MARGIN);
        overlayBox.setStyle("-fx-background-color: #ffffff;");
        overlayBox.setPickOnBounds(true); // consume clicks in sidebar
        overlayBox.setMouseTransparent(false);
        setupDistanceTable();
        setupLegend();
        getChildren().addAll(graphPane, overlayBox);
        StackPane.setAlignment(overlayBox, Pos.TOP_LEFT);

        // Initialize with initial distances
        initializeDistances();
        drawGraph();
        setupMouseHandlers();
    }

    /**
     * Initialize distances with all infinity except source
     */
    private void initializeDistances() {
        int maxVertex = graph.getVertices().stream().mapToInt(Integer::intValue).max().orElse(0);
        lastDistances = new double[maxVertex + 1];
        Arrays.fill(lastDistances, Double.POSITIVE_INFINITY);
        lastDistances[sourceVertex] = 0;
        updateDistanceTable(lastDistances);
    }

    /**
     * Sets up the distance tracking table.
     */
    private void setupDistanceTable() {
        distanceTable = new TableView<>();
        distanceTable.setPrefWidth(220);
        distanceTable.setMaxWidth(220);
        distanceTable.setMinWidth(220);
        distanceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        distanceTable.setStyle("-fx-background-color: #fff; -fx-border-color: #bbb; -fx-border-width: 1;");
        // Create columns with explicit cell value factories
        TableColumn<DistanceRow, Integer> vertexCol = new TableColumn<>("Vertex");
        vertexCol.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getVertex())
                        .asObject());
        vertexCol.setPrefWidth(60);

        TableColumn<DistanceRow, String> distanceCol = new TableColumn<>("Distance");
        distanceCol.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDistance()));
        distanceCol.setPrefWidth(80);

        TableColumn<DistanceRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        statusCol.setPrefWidth(80);

        distanceTable.getColumns().setAll(vertexCol, distanceCol, statusCol);
        distanceTable.setItems(tableData);
        distanceTable.setMouseTransparent(true);
        distanceTable.setFocusTraversable(false);
        VBox.setMargin(distanceTable, new javafx.geometry.Insets(10, 0, 0, 10));
    }

    /**
     * Sets up the legend.
     */
    private void setupLegend() {
        VBox legend = new VBox(5);
        legend.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 8; -fx-border-color: #bbb; -fx-border-width: 1;");
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
        legend.setMaxWidth(260);
        legend.setMinWidth(260);

        // Add legend below the table inside sidebar
        VBox.setVgrow(distanceTable, Priority.ALWAYS);
        overlayBox.getChildren().setAll(distanceTable, legend);
        VBox.setMargin(legend, new Insets(10, 10, 10, 10));
    }

    /**
     * Sets up mouse event handlers for user interaction.
     */
    private void setupMouseHandlers() {
        graphPane.setOnMouseClicked(this::handleMouseClick);
    }

    /**
     * Handles mouse clicks for adding/removing vertices/edges and setting source.
     */
    private void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        // Ignore clicks inside sidebar
        if (x <= LEFT_MARGIN) {
            return;
        }

        Integer clickedVertex = getVertexAt(x, y);
        // Only allow node creation if not dragging
        if (event.getButton() == MouseButton.PRIMARY && !isDragging) {
            if (clickedVertex == null) {
                int newId = getNextVertexId();
                graph.addVertex(newId);
                vertexPositions.put(newId, new Double[] { x, y });
                drawGraph();
            } else {
                // Start or finish edge creation
                if (tempEdgeFrom == null) {
                    tempEdgeFrom = clickedVertex;
                } else if (!tempEdgeFrom.equals(clickedVertex)) {
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
                Graph.Edge edge = getEdgeAt(x, y);
                if (edge != null) {
                    graph.removeEdge(edge.from, edge.to);
                    drawGraph();
                }
            }
        }
        isDragging = false; // Reset after any click
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
        initializeDistances(); // Reinitialize distances with new source
        drawGraph();
    }

    /**
     * Loads steps for visualization and resets state.
     */
    public void loadSteps(List<BellmanFordAlgorithm.Step> steps) {
        this.steps = steps;
        this.currentStep = 0;
        if (!steps.isEmpty()) {
            // Initialize with initial distances (all infinity except source)
            int maxVertex = graph.getVertices().stream().mapToInt(Integer::intValue).max().orElse(0);
            lastDistances = new double[maxVertex + 1];
            Arrays.fill(lastDistances, Double.POSITIVE_INFINITY);
            lastDistances[sourceVertex] = 0;
        } else {
            // If no steps, initialize with current graph state
            initializeDistances();
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
        // This method is no longer needed as legend is persistent
    }

    /** Draws the current state of the graph. */
    public void drawGraph() {
        graphPane.getChildren().clear();
        vertexNodes.clear();
        edgeLines.clear();
        distanceLabels.clear();
        List<Integer> vertices = new ArrayList<>(graph.getVertices());
        int n = vertices.size();
        double paneWidth = getWidth() > 0 ? getWidth() : 800;
        double centerX = LEFT_MARGIN + (paneWidth - LEFT_MARGIN) / 2;
        double centerY = 300;
        double radius = 200;
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
        Set<String> drawnEdges = new HashSet<>();
        for (Graph.Edge e : graph.getEdges()) {
            // Only draw one edge per unordered pair
            int min = Math.min(e.from, e.to);
            int max = Math.max(e.from, e.to);
            String key = min + "," + max;
            if (drawnEdges.contains(key))
                continue;
            drawnEdges.add(key);
            Double[] fromPos = vertexPositions.get(e.from);
            Double[] toPos = vertexPositions.get(e.to);
            if (fromPos != null && toPos != null) {
                Line line = new Line(fromPos[0], fromPos[1], toPos[0], toPos[1]);
                line.setStroke(Color.GRAY);
                graphPane.getChildren().add(line);
                edgeLines.put(e, line);
                // Offset label perpendicular to edge
                double midX = (fromPos[0] + toPos[0]) / 2;
                double midY = (fromPos[1] + toPos[1]) / 2;
                double dx = toPos[0] - fromPos[0];
                double dy = toPos[1] - fromPos[1];
                double len = Math.hypot(dx, dy);
                double offset = 28;
                double perpX = -dy / len * (offset / 2);
                double perpY = dx / len * (offset / 2);
                Text weightText = new Text(midX + perpX, midY + perpY, String.format("%.1f", e.weight));
                weightText.setFill(Color.DARKBLUE);
                graphPane.getChildren().add(weightText);
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
                isDragging = false;
                circle.setUserData(
                        new double[] { e.getSceneX() - circle.getCenterX(), e.getSceneY() - circle.getCenterY() });
                e.consume();
            });
            circle.setOnMouseDragged(e -> {
                isDragging = true;
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
                    updateConnectedEdgesAndLabels(v, newX, newY);
                }
                e.consume();
            });
            circle.setOnMouseReleased(e -> {
                if (isDragging) {
                    Double[] newPos = new Double[] { circle.getCenterX(), circle.getCenterY() };
                    vertexPositions.put(v, newPos);
                    drawGraph();
                }
                isDragging = false;
                e.consume();
            });
            graphPane.getChildren().add(circle);
            vertexNodes.put(v, circle);
            Text label = new Text(pos[0] - 5, pos[1] + 5, String.valueOf(v));
            label.setFill(Color.BLACK);
            graphPane.getChildren().add(label);
        }
        updateDistances(lastDistances);
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
            graphPane.getChildren().add(distLabel);
            distanceLabels.put(v, distLabel);
        }

        // Update the distance table
        updateDistanceTable(distances);
    }

    /**
     * Updates the distance table with current distance values.
     */
    private void updateDistanceTable(double[] distances) {
        tableData.clear();
        List<Integer> vertices = new ArrayList<>(graph.getVertices());
        vertices.sort(Integer::compareTo);

        for (int v : vertices) {
            double d = v < distances.length ? distances[v] : Double.POSITIVE_INFINITY;
            String distanceStr = (d == Double.POSITIVE_INFINITY) ? "∞" : String.format("%.1f", d);
            String status = (v == sourceVertex) ? "Source"
                    : (d == Double.POSITIVE_INFINITY) ? "Unreachable" : "Reachable";

            tableData.add(new DistanceRow(v, distanceStr, status));
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

    /**
     * Display final distances (no animation).
     */
    public void showFinalDistances(double[] distances) {
        this.steps = Collections.emptyList();
        this.currentStep = 0;
        this.lastDistances = Arrays.copyOf(distances, distances.length);
        drawGraph();
        updateDistances(lastDistances);
    }
}