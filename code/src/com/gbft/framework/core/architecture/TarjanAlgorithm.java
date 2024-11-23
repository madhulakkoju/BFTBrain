package com.gbft.framework.core.architecture;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TarjanAlgorithm {
    private int index;
    private int requiredVertex;
    private Deque<Integer> stack;
    private List<List<Integer>> components;

    public void execute(int vertex, List<Vertex> graph, List<Integer> out) {

        if (graph.isEmpty()) {
            // If the graph is empty, there are no SCCs to find
            return;
        }

        stack = new ArrayDeque<>();
        components = new ArrayList<>();
        index = 0;
        for (Vertex v : graph) {
            v.index = -1;
            v.lowlink = -1;
        }
        requiredVertex = vertex;
        strongConnect(vertex, graph);
        if (!components.isEmpty()) {
            out.addAll(components.get(0));
        }
    }

    private void strongConnect(int vertex, List<Vertex> graph) {
        if (graph.get(vertex).index != -1) {
            return;
        }
        graph.get(vertex).index = index;
        graph.get(vertex).lowlink = index;
        index++;
        stack.push(vertex);
        for (int vertexNext : graph.get(vertex).subgraphEdges) {
            if (graph.get(vertexNext).index == -1) {
                strongConnect(vertexNext, graph);
                graph.get(vertex).lowlink = Math.min(graph.get(vertex).lowlink, graph.get(vertexNext).lowlink);
            } else if (stack.contains(vertexNext)) {
                graph.get(vertex).lowlink = Math.min(graph.get(vertex).lowlink, graph.get(vertexNext).index);
            }
        }
        if (graph.get(vertex).lowlink == graph.get(vertex).index) {
            List<Integer> component = new ArrayList<>();
            int otherVertex;
            do {
                otherVertex = stack.pop();
                component.add(otherVertex);
            } while (otherVertex != vertex);
            if (component.contains(requiredVertex)) {
                components.add(component);
            }
        }
    }
}
