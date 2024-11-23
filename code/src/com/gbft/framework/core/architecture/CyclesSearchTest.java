package com.gbft.framework.core.architecture;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class CyclesSearchTest {

    // Include this method in your test class
    private boolean containsCycle(List<List<Integer>> cycles, List<Integer> expectedCycle) {
        for (List<Integer> cycle : cycles) {
            if (areCyclicPermutations(cycle, expectedCycle)) {
                return true;
            }
        }
        return false;
    }

    private boolean areCyclicPermutations(List<Integer> cycle1, List<Integer> cycle2) {
        if (cycle1.size() != cycle2.size()) {
            return false;
        }
        int size = cycle1.size();
        for (int shift = 0; shift < size; shift++) {
            boolean match = true;
            for (int i = 0; i < size; i++) {
                if (!cycle1.get((i + shift) % size).equals(cycle2.get(i))) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return true;
            }
        }
        return false;
    }



    @Test
    public void testNoCycles() {
        // Create vertices
        List<Vertex> graph = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            graph.add(new Vertex());
        }

        // Add edges (Directed Acyclic Graph)
        graph.get(0).outEdges.add(1);
        graph.get(1).outEdges.add(2);
        graph.get(2).outEdges.add(3);
        graph.get(3).outEdges.add(4);

        // Initialize CyclesSearch
        CyclesSearch cyclesSearch = new CyclesSearch();
        boolean result = cyclesSearch.getElementaryCycles(graph);

        // Assert that no cycles are found
        assertTrue(result, "Cycle detection should complete successfully.");
        assertTrue(cyclesSearch.cycles.isEmpty(), "No cycles should be detected.");
    }

    @Test
    public void testSingleCycle() {
        // Create vertices
        List<Vertex> graph = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            graph.add(new Vertex());
        }

        // Add edges to form a cycle: 0→1→2→0
        graph.get(0).outEdges.add(1);
        graph.get(1).outEdges.add(2);
        graph.get(2).outEdges.add(0);

        // Initialize CyclesSearch
        CyclesSearch cyclesSearch = new CyclesSearch();
        boolean result = cyclesSearch.getElementaryCycles(graph);

        // Assert that cycles are found
        assertTrue(result, "Cycle detection should complete successfully.");
        assertEquals(1, cyclesSearch.cycles.size(), "One cycle should be detected.");

        // Check the detected cycle
        List<Integer> expectedCycle = Arrays.asList(0, 1, 2);
        assertTrue(containsCycle(cyclesSearch.cycles, expectedCycle), "The cycle [0, 1, 2] should be detected.");
    }




    @Test
    public void testMultipleCycles() {
        // Create vertices
        List<Vertex> graph = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            graph.add(new Vertex());
        }

        // Add edges to form two cycles
        // Cycle 1: 0→1→2→0
        graph.get(0).outEdges.add(1);
        graph.get(1).outEdges.add(2);
        graph.get(2).outEdges.add(0);

        // Cycle 2: 3→4→5→3
        graph.get(3).outEdges.add(4);
        graph.get(4).outEdges.add(5);
        graph.get(5).outEdges.add(3);

        // Initialize CyclesSearch
        CyclesSearch cyclesSearch = new CyclesSearch();
        boolean result = cyclesSearch.getElementaryCycles(graph);

        // Assert that cycles are found
        assertTrue(result, "Cycle detection should complete successfully.");
        assertEquals(2, cyclesSearch.cycles.size(), "Two cycles should be detected.");

        // Check the detected cycles
        List<List<Integer>> expectedCycles = new ArrayList<>();
        expectedCycles.add(Arrays.asList(0, 1, 2));
        expectedCycles.add(Arrays.asList(3, 4, 5));

        for (List<Integer> expectedCycle : expectedCycles) {
            assertTrue(cyclesSearch.cycles.contains(expectedCycle), "Cycle " + expectedCycle + " should be detected.");
        }
    }

    @Test
    public void testNestedCycles() {
        // Create vertices
        List<Vertex> graph = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            graph.add(new Vertex());
        }

        // Add edges to form nested cycles
        // Cycle 1: 0→1→2→0
        graph.get(0).outEdges.add(1);
        graph.get(1).outEdges.add(2);
        graph.get(2).outEdges.add(0);

        // Cycle 2: 2→3→4→2
        graph.get(2).outEdges.add(3);
        graph.get(3).outEdges.add(4);
        graph.get(4).outEdges.add(2);

        // Initialize CyclesSearch
        CyclesSearch cyclesSearch = new CyclesSearch();
        boolean result = cyclesSearch.getElementaryCycles(graph);

        // Assert that cycles are found
        assertTrue(result, "Cycle detection should complete successfully.");
        assertEquals(3, cyclesSearch.cycles.size(), "Three cycles should be detected.");

        // Check the detected cycles
        List<List<Integer>> expectedCycles = new ArrayList<>();
        expectedCycles.add(Arrays.asList(0, 1, 2));
        expectedCycles.add(Arrays.asList(2, 3, 4));
        expectedCycles.add(Arrays.asList(0, 1, 2, 3, 4));

        for (List<Integer> expectedCycle : expectedCycles) {
            assertTrue(cyclesSearch.cycles.contains(expectedCycle), "Cycle " + expectedCycle + " should be detected.");
        }
    }

    @Test
    public void testComplexCycles() {
        // Create vertices
        List<Vertex> graph = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            graph.add(new Vertex());
        }

        // Add edges to form complex cycles
        // Cycle 1: 0→1→2→0
        graph.get(0).outEdges.add(1);
        graph.get(1).outEdges.add(2);
        graph.get(2).outEdges.add(0);

        // Cycle 2: 2→3→4→2
        graph.get(2).outEdges.add(3);
        graph.get(3).outEdges.add(4);
        graph.get(4).outEdges.add(2);

        // Overlapping node
        graph.get(4).outEdges.add(5);
        graph.get(5).outEdges.add(0); // Connecting back to node 0

        // Initialize CyclesSearch
        CyclesSearch cyclesSearch = new CyclesSearch();
        boolean result = cyclesSearch.getElementaryCycles(graph);

        // Assert that cycles are found
        assertTrue(result, "Cycle detection should complete successfully.");
        assertEquals(5, cyclesSearch.cycles.size(), "Five cycles should be detected.");

        // Check the detected cycles
        List<List<Integer>> expectedCycles = new ArrayList<>();
        expectedCycles.add(Arrays.asList(0, 1, 2));
        expectedCycles.add(Arrays.asList(2, 3, 4));
        expectedCycles.add(Arrays.asList(0, 1, 2, 3, 4, 5));
        expectedCycles.add(Arrays.asList(2, 3, 4, 5, 0));
        expectedCycles.add(Arrays.asList(0, 1, 2, 3, 4));

        for (List<Integer> expectedCycle : expectedCycles) {
            assertTrue(containsCycle(cyclesSearch.cycles, expectedCycle), "Cycle " + expectedCycle + " should be detected.");
        }

    }

    @Test
    public void testLargeGraphNoCycles() {
        // Create vertices
        int numVertices = 1000;
        List<Vertex> graph = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) {
            graph.add(new Vertex());
        }

        // Add edges to form a chain (DAG)
        for (int i = 0; i < numVertices - 1; i++) {
            graph.get(i).outEdges.add(i + 1);
        }

        // Initialize CyclesSearch
        CyclesSearch cyclesSearch = new CyclesSearch();
        boolean result = cyclesSearch.getElementaryCycles(graph);

        // Assert that no cycles are found
        assertTrue(result, "Cycle detection should complete successfully.");
        assertTrue(cyclesSearch.cycles.isEmpty(), "No cycles should be detected in a large acyclic graph.");
    }

    @Test
    public void testDisconnectedGraph() {
        // Create vertices
        List<Vertex> graph = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            graph.add(new Vertex());
        }

        // Component 1: No cycles (0→1→2)
        graph.get(0).outEdges.add(1);
        graph.get(1).outEdges.add(2);

        // Component 2: Cycle (3→4→5→3)
        graph.get(3).outEdges.add(4);
        graph.get(4).outEdges.add(5);
        graph.get(5).outEdges.add(3);

        // Component 3: Self-loop at node 6
        graph.get(6).outEdges.add(6);

        // Initialize CyclesSearch
        CyclesSearch cyclesSearch = new CyclesSearch();
        boolean result = cyclesSearch.getElementaryCycles(graph);

        // Assert that cycles are found in components with cycles
        assertTrue(result, "Cycle detection should complete successfully.");
        assertEquals(2, cyclesSearch.cycles.size(), "Two cycles should be detected.");

        // Check the detected cycles
        List<List<Integer>> expectedCycles = new ArrayList<>();
        expectedCycles.add(Arrays.asList(3, 4, 5));
        expectedCycles.add(Arrays.asList(6));

        for (List<Integer> expectedCycle : expectedCycles) {
            assertTrue(cyclesSearch.cycles.contains(expectedCycle), "Cycle " + expectedCycle + " should be detected.");
        }
    }


}
