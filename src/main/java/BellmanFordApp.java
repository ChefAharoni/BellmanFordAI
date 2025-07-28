// BellmanFordApp.java
// Main entry point for the Bellman-Ford visualization application.
// Uses JavaFX for GUI. This class launches the application window.

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.Random;
import java.util.ArrayList;

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
        Button randomizeBtn = new Button("Randomize Weights");
        Button clearBtn = new Button("Clear");
        HBox controls = new HBox(10, runBtn, prevBtn, nextBtn, playBtn, pauseBtn, randomizeBtn, clearBtn);
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
            int source = visualizer.getSourceVertex();
            BellmanFordAlgorithm bfa = new BellmanFordAlgorithm(graph, source);
            bfa.run();
            visualizer.showFinalDistances(bfa.getDistances());
        });
        nextBtn.setOnAction(e -> visualizer.nextStep());
        prevBtn.setOnAction(e -> visualizer.prevStep());
        playBtn.setOnAction(e -> visualizer.play());
        pauseBtn.setOnAction(e -> visualizer.pause());
        randomizeBtn.setOnAction(e -> {
            Random rand = new Random();
            var vertices = new ArrayList<>(graph.getVertices());
            for (int i = 0; i < vertices.size(); i++) {
                for (int j = i + 1; j < vertices.size(); j++) {
                    int from = vertices.get(i);
                    int to = vertices.get(j);
                    double weight = -10 + rand.nextDouble() * 20; // random weight between -10 and 10
                    weight = Math.round(weight * 10.0) / 10.0; // round to 1 decimal
                    graph.addEdge(from, to, weight);
                    graph.removeEdge(to, from); // ensure only one direction
                }
            }
            visualizer.drawGraph();
        });
        clearBtn.setOnAction(e -> {
            // Remove all nodes and edges
            for (int v : new ArrayList<>(graph.getVertices())) {
                graph.removeVertex(v);
            }
            visualizer.vertexPositions.clear();
            visualizer.drawGraph();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}