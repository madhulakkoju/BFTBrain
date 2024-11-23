package com.gbft.framework.core.architecture;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class TarjanAlgorithmTest {

    @Test
    public void testNoSCCs() {
        // Create vertices
        List<Vertex> graph = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            graph.add(new Vertex());
        }

        // Add edges (DAG)
        graph.get(0).outEdges.add(1);
        graph.get(1).outEdges.add(2);
        graph.get(2).outEdges.add(3);
        graph.get(3).outEdges.add(4);

        // Initialize TarjanAlgorithm
        TarjanAlgorithm tarjan = new TarjanAlgorithm();
        List<Integer> component = new ArrayList<>();
        for (int i = 0; i < graph.size(); i++) {
            tarjan.execute(i, graph, component);

            // Each node should be its own SCC since there are no cycles
            assertEquals(1, component.size(), "Each node should be its own SCC.");
            assertEquals(i, component.get(0), "SCC should contain only node " + i + ".");
            component.clear();
        }
    }


    @Test
    public void testSingleNodeSCCs() {
        // Create vertices
        List<Vertex> graph = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            graph.add(new Vertex());
        }

        // No edges added

        // Initialize TarjanAlgorithm
        TarjanAlgorithm tarjan = new TarjanAlgorithm();
        List<Integer> component = new ArrayList<>();
        for (int i = 0; i < graph.size(); i++) {
            tarjan.execute(i, graph, component);

            // Each node is an SCC of itself
            assertEquals(1, component.size(), "Each node should be its own SCC.");
            assertEquals(i, component.get(0), "SCC should contain only node " + i + ".");
            component.clear();
        }
    }


    @Test
    public void testOneBigSCC() {
        // Create vertices
        List<Vertex> graph = new ArrayList<>();
        int numNodes = 5;
        for (int i = 0; i < numNodes; i++) {
            graph.add(new Vertex());
        }

        // Add edges to form a cycle covering all nodes
        for (int i = 0; i < numNodes; i++) {
            graph.get(i).outEdges.add((i + 1) % numNodes); // Edge from i to (i+1)%numNodes
        }

        // Initialize TarjanAlgorithm
        TarjanAlgorithm tarjan = new TarjanAlgorithm();
        List<Integer> component = new ArrayList<>();
        tarjan.execute(0, graph, component);

        // All nodes should be in one SCC
        assertEquals(numNodes, component.size(), "All nodes should be in one SCC.");
        for (int i = 0; i < numNodes; i++) {
            assertTrue(component.contains(i), "SCC should contain node " + i + ".");
        }
    }


    @Test
    public void testNestedSCCs() {
        // Create vertices
        List<Vertex> graph = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            graph.add(new Vertex());
        }

        // Add edges
        // SCC1: 0↔1↔2↔0
        graph.get(0).outEdges.add(1);
        graph.get(1).outEdges.add(0);
        graph.get(1).outEdges.add(2);
        graph.get(2).outEdges.add(1);
        graph.get(2).outEdges.add(0);

        // SCC2: 3↔4↔5↔3
        graph.get(3).outEdges.add(4);
        graph.get(4).outEdges.add(3);
        graph.get(4).outEdges.add(5);
        graph.get(5).outEdges.add(4);
        graph.get(5).outEdges.add(3);

        // Edge from SCC1 to SCC2
        graph.get(2).outEdges.add(3);

        // Initialize TarjanAlgorithm
        TarjanAlgorithm tarjan = new TarjanAlgorithm();
        List<Integer> component = new ArrayList<>();

        // SCC1
        tarjan.execute(0, graph, component);
        assertEquals(3, component.size(), "SCC1 should contain 3 nodes.");
        for (int i = 0; i <= 2; i++) {
            assertTrue(component.contains(i), "SCC1 should contain node " + i + ".");
        }
        component.clear();

        // SCC2
        tarjan.execute(3, graph, component);
        assertEquals(3, component.size(), "SCC2 should contain 3 nodes.");
        for (int i = 3; i <= 5; i++) {
            assertTrue(component.contains(i), "SCC2 should contain node " + i + ".");
        }
    }

    @Test
    public void testDisconnectedGraph() {
        // Create vertices
        List<Vertex> graph = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            graph.add(new Vertex());
        }

        // Component 1: Nodes 0-1-2 (DAG)
        graph.get(0).outEdges.add(1);
        graph.get(1).outEdges.add(2);

        // Component 2: Nodes 3↔4↔5↔3 (SCC)
        graph.get(3).outEdges.add(4);
        graph.get(4).outEdges.add(5);
        graph.get(5).outEdges.add(3);

        // Component 3: Node 6 (isolated)
        // No edges

        // Initialize TarjanAlgorithm
        TarjanAlgorithm tarjan = new TarjanAlgorithm();
        List<Integer> component = new ArrayList<>();

        // Component 1: Each node should be its own SCC
        for (int i = 0; i <= 2; i++) {
            tarjan.execute(i, graph, component);
            assertEquals(1, component.size(), "Node " + i + " should be its own SCC.");
            assertEquals(i, component.get(0), "SCC should contain node " + i + ".");
            component.clear();
        }

        // Component 2: SCC containing nodes 3, 4, 5
        tarjan.execute(3, graph, component);
        assertEquals(3, component.size(), "SCC should contain nodes 3, 4, 5.");
        for (int i = 3; i <= 5; i++) {
            assertTrue(component.contains(i), "SCC should contain node " + i + ".");
        }
        component.clear();

        // Component 3: Node 6
        tarjan.execute(6, graph, component);
        assertEquals(1, component.size(), "Node 6 should be its own SCC.");
        assertEquals(6, component.get(0), "SCC should contain node 6.");
    }


    @Test
    public void testSelfLoops() {
        // Create vertices
        List<Vertex> graph = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            graph.add(new Vertex());
        }

        // Add self-loops
        graph.get(1).outEdges.add(1); // Node 1 has a self-loop
        graph.get(3).outEdges.add(3); // Node 3 has a self-loop

        // Other edges
        graph.get(0).outEdges.add(1);
        graph.get(1).outEdges.add(2);
        graph.get(2).outEdges.add(3);

        // Initialize TarjanAlgorithm
        TarjanAlgorithm tarjan = new TarjanAlgorithm();
        List<Integer> component = new ArrayList<>();

        // Node 0
        tarjan.execute(0, graph, component);
        assertEquals(1, component.size(), "Node 0 should be its own SCC.");
        assertEquals(0, component.get(0), "SCC should contain node 0.");
        component.clear();

        // Node 1 (self-loop)
        tarjan.execute(1, graph, component);
        assertEquals(1, component.size(), "Node 1 should be its own SCC (self-loop).");
        assertEquals(1, component.get(0), "SCC should contain node 1.");
        component.clear();

        // Node 2
        tarjan.execute(2, graph, component);
        assertEquals(1, component.size(), "Node 2 should be its own SCC.");
        assertEquals(2, component.get(0), "SCC should contain node 2.");
        component.clear();

        // Node 3 (self-loop)
        tarjan.execute(3, graph, component);
        assertEquals(1, component.size(), "Node 3 should be its own SCC (self-loop).");
        assertEquals(3, component.get(0), "SCC should contain node 3.");
    }


    @Test
    public void testComplexGraph() {
        // Create vertices
        List<Vertex> graph = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            graph.add(new Vertex());
        }

        // Add edges to form SCCs and connections
        // SCC1: 0↔1↔2↔0
        graph.get(0).outEdges.add(1);
        graph.get(1).outEdges.add(2);
        graph.get(2).outEdges.add(0);

        // SCC2: 3↔4↔5↔3
        graph.get(3).outEdges.add(4);
        graph.get(4).outEdges.add(5);
        graph.get(5).outEdges.add(3);

        // Connections between SCCs
        graph.get(2).outEdges.add(3); // From SCC1 to SCC2
        graph.get(5).outEdges.add(6); // From SCC2 to node 6

        // Node 6: No outgoing edges

        // Initialize TarjanAlgorithm
        TarjanAlgorithm tarjan = new TarjanAlgorithm();
        List<Integer> component = new ArrayList<>();

        // SCC1
        tarjan.execute(0, graph, component);
        assertEquals(3, component.size(), "SCC1 should contain nodes 0, 1, 2.");
        for (int i = 0; i <= 2; i++) {
            assertTrue(component.contains(i), "SCC1 should contain node " + i + ".");
        }
        component.clear();

        // SCC2
        tarjan.execute(3, graph, component);
        assertEquals(3, component.size(), "SCC2 should contain nodes 3, 4, 5.");
        for (int i = 3; i <= 5; i++) {
            assertTrue(component.contains(i), "SCC2 should contain node " + i + ".");
        }
        component.clear();

        // Node 6
        tarjan.execute(6, graph, component);
        assertEquals(1, component.size(), "Node 6 should be its own SCC.");
        assertEquals(6, component.get(0), "SCC should contain node 6.");
    }

    @Test
    public void testSingleNodeWithSelfLoop() {
        // Create vertex
        List<Vertex> graph = new ArrayList<>();
        graph.add(new Vertex());

        // Add self-loop
        graph.get(0).outEdges.add(0);

        // Initialize TarjanAlgorithm
        TarjanAlgorithm tarjan = new TarjanAlgorithm();
        List<Integer> component = new ArrayList<>();

        tarjan.execute(0, graph, component);
        assertEquals(1, component.size(), "Node 0 should be its own SCC.");
        assertEquals(0, component.get(0), "SCC should contain node 0.");
    }


}
