# Bellman-Ford Algorithm Visualizer

A visually appealing, interactive JavaFX application to visualize the Bellman-Ford shortest path algorithm step-by-step.

## Features

- Add/remove vertices and edges interactively
- Set source node and adjust edge weights
- Step-by-step and animated visualization of the algorithm
- Comprehensive test suite (JUnit)

## Prerequisites

- **Java 11 or newer**
- **JavaFX SDK** ([Download here](https://openjfx.io/))
- (Optional) **JUnit 4** for running tests

## Setup (macOS)

1. **Download JavaFX SDK**
   - Extract it to your project directory (e.g., `javafx-sdk-24.0.2/`)
2. **(Optional) Download JUnit 4**
   - Download `junit-4.13.2.jar` and `hamcrest-core-1.3.jar` to your project directory if you want to run tests.

## Build & Run

### 1. Compile (from the `src` directory):

```sh
javac --module-path ../javafx-sdk-24.0.2/lib --add-modules javafx.controls,javafx.fxml -d ../out *.java
```

### 2. Run (from the project root):

```sh
java --module-path javafx-sdk-24.0.2/lib --add-modules javafx.controls,javafx.fxml -cp out BellmanFordApp
```

## How to Use

- **Add Vertex:** Left-click empty space
- **Add Edge:** Left-click one vertex, then another (enter weight)
- **Remove Vertex:** Right-click vertex, choose "Remove Vertex"
- **Set Source:** Right-click vertex, choose "Set as Source"
- **Remove Edge:** Right-click near an edge
- **Run Algorithm:** Click "Run Bellman-Ford"
- **Step/Animate:** Use Next/Previous/Play/Pause buttons
- **Legend:** See top-left for color/control explanations

## Running Tests (Optional)

1. **Compile with JUnit:**

```sh
javac --module-path ../javafx-sdk-24.0.2/lib --add-modules javafx.controls,javafx.fxml -cp .:../junit-4.13.2.jar:../hamcrest-core-1.3.jar -d ../out *.java
```

2. **Run tests:**

```sh
cd ..
java -cp out:junit-4.13.2.jar:hamcrest-core-1.3.jar org.junit.runner.JUnitCore BellmanFordAlgorithmTest
```

## Troubleshooting

- **JavaFX errors:** Ensure you use the correct `--module-path` (should point to the `lib` directory inside the JavaFX SDK).
- **macOS hidden files:** `.DS_Store` and other system files are ignored by `.gitignore`.
- **UI not showing:** Make sure you are running with JavaFX and not just the standard JDK.

## License

MIT
