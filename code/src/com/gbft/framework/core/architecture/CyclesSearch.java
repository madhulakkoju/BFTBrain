package com.gbft.framework.core.architecture;
import java.util.*;

public class CyclesSearch {
    public List<List<Integer>> cycles = new ArrayList<>();
    private Deque<Integer> stack;
    private boolean[] blocked;
    private List<Set<Integer>> blockedMap;
    private int currentVertex;
    private List<Vertex> subgraph;

    public boolean getElementaryCycles(List<Vertex> graph) {
        subgraph = graph;
        TarjanAlgorithm tarjan = new TarjanAlgorithm();

        for (int s = 0; s < subgraph.size(); s++) {
            // Build subgraph vertices (vertices with index >= s)
            Set<Integer> subgraphVertices = new HashSet<>();
            for (int j = s; j < subgraph.size(); j++) {
                subgraphVertices.add(j);
            }

            // Initialize subgraphEdges for all vertices
            for (Vertex v : subgraph) {
                v.subgraphEdges.clear();
            }

            // Build subgraphEdges for vertices in subgraphVertices
            for (int vIndex : subgraphVertices) {
                Vertex v = subgraph.get(vIndex);
                for (int w : v.outEdges) {
                    if (subgraphVertices.contains(w)) {
                        v.subgraphEdges.add(w);
                    }
                }
            }

            // Find SCCs containing the current vertex s
            List<Integer> componentIndexes = new ArrayList<>();
            tarjan.execute(s, subgraph, componentIndexes);

            if (componentIndexes.isEmpty()) {
                continue; // No SCC containing s
            }

            // Rebuild subgraphEdges to include only edges within the SCC
            Set<Integer> componentSet = new HashSet<>(componentIndexes);
            for (int idx : componentIndexes) {
                Vertex v = subgraph.get(idx);
                v.subgraphEdges.clear();
                for (int w : subgraph.get(idx).outEdges) {
                    if (componentSet.contains(w)) {
                        v.subgraphEdges.add(w);
                    }
                }
            }

            currentVertex = s;
            blocked = new boolean[subgraph.size()];
            blockedMap = new ArrayList<>();
            for (int k = 0; k < subgraph.size(); k++) {
                blockedMap.add(new HashSet<>());
            }
            stack = new ArrayDeque<>();
            circuit(currentVertex);
        }
        // Assuming no timeout mechanism is needed
        return true;
    }


    private boolean circuit(int vertex) {
        boolean found = false;
        stack.push(vertex);
        blocked[vertex] = true;
        for (int w : subgraph.get(vertex).subgraphEdges) {
            if (w == currentVertex) {
                // Collect the cycle
                List<Integer> cycle = new ArrayList<>(stack);
                // Reverse the cycle to maintain the correct order
                Collections.reverse(cycle);
                cycles.add(cycle);
                found = true;
            } else if (!blocked[w]) {
                if (circuit(w)) {
                    found = true;
                }
            }
        }
        if (found) {
            unblock(vertex);
        } else {
            for (int w : subgraph.get(vertex).subgraphEdges) {
                if (!blockedMap.get(w).contains(vertex)) {
                    blockedMap.get(w).add(vertex);
                }
            }
        }
        stack.pop();
        return found;
    }


    private void unblock(int u) {
        blocked[u] = false;
        for (Iterator<Integer> it = blockedMap.get(u).iterator(); it.hasNext(); ) {
            int w = it.next();
            it.remove();
            if (blocked[w]) {
                unblock(w);
            }
        }
    }
}
