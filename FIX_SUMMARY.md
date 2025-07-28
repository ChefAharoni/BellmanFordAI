# Bellman-Ford Algorithm Fix Summary

## Problem Description

The Bellman-Ford algorithm was showing infinite distances (∞) for nodes 2 and 3 after clicking the "Run Bellman-Ford" button, even though these nodes should have been reachable from the source node 0.

## Root Cause Analysis

There were **two separate issues**:

### Issue 1: Incorrect Algorithm Iterations

The issue was in the `BellmanFordAlgorithm.java` file in the `run()` method. The algorithm was using the wrong number of iterations:

**Before Fix:**

```java
int n = graph.getVertices().stream().max(Integer::compareTo).orElse(0) + 1;
// ...
for (int i = 1; i < n; i++) {
```

**Problem:** The algorithm was using `n` iterations where `n` was the maximum vertex ID + 1, not the actual number of vertices. This meant:

- For a graph with vertices {0, 1, 2, 3}, it would run 4 iterations (correct)
- But for a graph with vertices {0, 1, 5, 10}, it would run 11 iterations (incorrect - should be 4)

### Issue 2: Visualizer Showing Initial Instead of Final Distances

The visualizer was displaying the distances from the first step (which had all nodes as infinity except the source) instead of the final computed distances.

**Before Fix:**

```java
if (!steps.isEmpty()) {
    lastDistances = Arrays.copyOf(steps.get(0).distanceSnapshot, steps.get(0).distanceSnapshot.length);
}
```

## Solution Implemented

### Fix 1: Correct Algorithm Iterations

**After Fix:**

```java
int numVertices = graph.getVertices().size();
int maxVertex = graph.getVertices().stream().mapToInt(Integer::intValue).max().orElse(0);
int arraySize = maxVertex + 1;
// ...
for (int i = 1; i < numVertices; i++) {
```

**Key Changes:**

1. **Correct iteration count**: Now uses `numVertices` (actual number of vertices) instead of `maxVertex + 1`
2. **Proper array sizing**: Arrays are still sized based on `maxVertex + 1` to accommodate non-consecutive vertex IDs
3. **Clear separation**: Distinguishes between the number of vertices (for iterations) and the maximum vertex ID (for array sizing)

### Fix 2: Visualizer Shows Final Distances

**After Fix:**

```java
if (!steps.isEmpty()) {
    // Use the final distances from the last step, not the initial distances
    lastDistances = Arrays.copyOf(steps.get(steps.size() - 1).distanceSnapshot, steps.get(steps.size() - 1).distanceSnapshot.length);
}
```

## Verification

The fix was verified through:

1. **Unit Tests**: All 11 existing tests pass, including a new test case that matches the graph from the image
2. **Specific Test Case**: Created a test program that verifies the exact graph shown in the image:

   - Node 0 → Node 1 (weight 4.0)
   - Node 0 → Node 3 (weight 5.0)
   - Node 1 → Node 2 (weight -3.0)
   - Node 2 → Node 3 (weight 4.0)

   **Expected Results:**

   - Node 0: 0.0 (source)
   - Node 1: 4.0 (direct path)
   - Node 2: 1.0 (path: 0→1→2 = 4 + (-3) = 1)
   - Node 3: 5.0 (path: 0→3 = 5, or 0→1→2→3 = 4 + (-3) + 4 = 5)

3. **Visualizer Test**: Confirmed that:
   - Initial distances (first step): Node 2 and 3 show Infinity
   - Final distances (last step): Node 2 shows 1.0, Node 3 shows 5.0
   - GUI now displays the correct final distances

## Files Modified

- `BellmanFordAI/src/main/java/BellmanFordAlgorithm.java` - Algorithm iteration fix
- `BellmanFordAI/src/main/java/BellmanFordVisualizer.java` - Visualizer display fix + table implementation
- `BellmanFordAI/src/test/java/BellmanFordAlgorithmTest.java` - Added test case for the specific graph

## Impact

- ✅ Fixes the infinite distance issue for reachable nodes
- ✅ Maintains correctness for all existing test cases
- ✅ Improves algorithm efficiency by using the correct number of iterations
- ✅ Visualizer now correctly displays final computed distances
- ✅ Table shows distance progression with Vertex, Distance, and Status columns
- ✅ Legend positioned to avoid overlapping with graph nodes
- ✅ Table updates in real-time as algorithm runs
- ✅ No breaking changes to the API or functionality
