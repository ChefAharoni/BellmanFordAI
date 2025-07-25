// BellmanFordApp.java
// Main entry point for the Bellman-Ford visualization application.
// Uses JavaFX for GUI. This class launches the application window.

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

/**
 * Main application class for Bellman-Ford visualization.
 * Launches the JavaFX GUI.
 */
public class BellmanFordApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Initialize a sample graph for demonstration
        Graph graph = new Graph();
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 5);
        graph.addEdge(1, 2, -3);
        graph.addEdge(2, 3, 4);
        graph.addEdge(3, 1, 6);
        BellmanFordVisualizer visualizer = new BellmanFordVisualizer(graph);

        // Controls for step navigation
        Button runBtn = new Button("Run Bellman-Ford");
        Button nextBtn = new Button("Next");
        Button prevBtn = new Button("Previous");
        Button playBtn = new Button("Play");
        Button pauseBtn = new Button("Pause");
        HBox controls = new HBox(10, runBtn, prevBtn, nextBtn, playBtn, pauseBtn);
        controls.setStyle("-fx-padding: 10; -fx-alignment: center;");

        BorderPane root = new BorderPane();
        root.setCenter(visualizer);
        root.setBottom(controls);
        Scene scene = new Scene(root, 800, 650);
        primaryStage.setTitle("Bellman-Ford Algorithm Visualizer");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Button actions
        runBtn.setOnAction(e -> {
            // Use the current source vertex selected by the user
            int source = visualizer.getSourceVertex();
            BellmanFordAlgorithm bfa = new BellmanFordAlgorithm(graph, source);
            bfa.run();
            visualizer.loadSteps(bfa.getSteps());
        });
        nextBtn.setOnAction(e -> visualizer.nextStep());
        prevBtn.setOnAction(e -> visualizer.prevStep());
        playBtn.setOnAction(e -> visualizer.play());
        pauseBtn.setOnAction(e -> visualizer.pause());
    }

    public static void main(String[] args) {
        launch(args);
    }
}