package com.gbft.framework.core.architecture;

import com.gbft.framework.data.OperationSet;
import com.gbft.framework.data.RequestData;
import com.gbft.framework.data.RequestDataList;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.*;

public class XOVGraph {

    /**
     * Builds a conflict graph based on RAW and WAW conflicts using JGraphT.
     *
     * @param transactions List of transactions
     * @param reorder      Whether to reorder based on conflicts
     * @return A directed graph representing conflicts
     */
    public Graph<Integer, DefaultEdge> buildConflictGraphXOV(List<RequestData> transactions, boolean reorder) {
        // Create a directed graph with Integer vertices and DefaultEdge edges
        Graph<Integer, DefaultEdge> conflictGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

        // Add all transaction indices as vertices
        for (int i = 0; i < transactions.size(); i++) {
            conflictGraph.addVertex(i);
        }

        // Build the conflict graph
        for (int i = 0; i < transactions.size(); i++) {
            RequestData txI = transactions.get(i);
            for (int j = 0; j < transactions.size(); j++) {
                if (i == j) continue;
                RequestData txJ = transactions.get(j);

                // Check for Read-After-Write (RAW) conflicts
                boolean rawConflict = false;
                for (OperationSet readI : txI.getReadSetList()) {
                    for (OperationSet writeJ : txJ.getWriteSetList()) {
                        if (readI.getRecord() == writeJ.getRecord()) {
                            rawConflict = true;
                            break;
                        }
                    }
                    if (rawConflict) break;
                }
                if (rawConflict) {
                    if (reorder) {
                        conflictGraph.addEdge(i, j); // Edge from txJ to txI
                    }
                }

                // Check for Write-Write (WAW) conflicts
                boolean wawConflict = false;
                for (OperationSet writeI : txI.getWriteSetList()) {
                    for (OperationSet writeJ : txJ.getWriteSetList()) {
                        if (writeI.getRecord() == writeJ.getRecord()) {
                            wawConflict = true;
                            break;
                        }
                    }
                    if (wawConflict) break;
                }
                if (wawConflict) {
                    conflictGraph.addEdge(i, j);
                    conflictGraph.addEdge(j, i);
                }
                // Ignore Read-Write (WAR) and Read-Read (RAR) conflicts
            }
        }
        return conflictGraph;
    }

    /**
     * Processes transaction reordering based on conflict graph and cycle detection.
     *
     * @param transactions List of transactions
     * @param earlyAbort   Whether to perform early aborts
     * @param reorder      Whether to reorder transactions
     * @return A list of reordered transactions with abort flags
     */
    public RequestDataList processXOVReordering(List<RequestData> transactions, boolean earlyAbort, boolean reorder) {
        // Initialize all transactions as valid
        List<RequestData> S = new ArrayList<>();
        for (int i = 0; i < transactions.size(); i++) {
            RequestData tx = transactions.get(i).toBuilder().setIsTnxValid(true).build();
            S.add(tx);
        }

        // Build conflict graph using JGraphT
        Graph<Integer, DefaultEdge> conflictGraph = buildConflictGraphXOV(S, reorder);

        // Detect cycles using CycleDetector
        CycleDetector<Integer, DefaultEdge> cycleDetector = new CycleDetector<>(conflictGraph);
        boolean hasCycles = cycleDetector.detectCycles();

        Set<Integer> abortedTransactions = new HashSet<>();
        if (hasCycles) {
            // Identify transactions involved in cycles
            Set<Integer> transactionsInCycles = cycleDetector.findCycles();
            abortedTransactions.addAll(transactionsInCycles);

            // Abort all transactions involved in cycles
            for (Integer idx : abortedTransactions) {
                RequestData tx = S.get(idx);
                RequestData abortedTx = tx.toBuilder().setIsTnxValid(false).build(); // Mark as aborted
                S.set(idx, abortedTx);
            }
        }

        // Proceed with topological sort or other processing as before
        List<RequestData> blockTransactions = new ArrayList<>();
        if (reorder) {
            // Remove aborted transactions from the graph
            Graph<Integer, DefaultEdge> activeGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
            for (int i = 0; i < S.size(); i++) {
                if (!abortedTransactions.contains(i)) {
                    activeGraph.addVertex(i);
                }
            }
            for (DefaultEdge edge : conflictGraph.edgeSet()) {
                Integer source = conflictGraph.getEdgeSource(edge);
                Integer target = conflictGraph.getEdgeTarget(edge);
                if (!abortedTransactions.contains(source) && !abortedTransactions.contains(target)) {
                    activeGraph.addEdge(source, target);
                }
            }

            // Perform topological sort
            List<Integer> sortedVertices;
            try {
                sortedVertices = topologicalSort(activeGraph);
            } catch (IllegalArgumentException e) {
                // Graph has cycles even after aborting conflicting transactions
                System.err.println("Cycle detected in active transactions after aborting. Skipping topological sort.");
                sortedVertices = new ArrayList<>();
            }

            // Add transactions in topological order
            for (Integer idx : sortedVertices) {
                blockTransactions.add(S.get(idx));
            }
        } else {
            // No reordering, proceed in original order
            for (int i = 0; i < S.size(); i++) {
                if (!abortedTransactions.contains(i)) {
                    blockTransactions.add(S.get(i));
                }
            }
        }

        // Add aborted transactions at the end
        for (Integer idx : abortedTransactions) {
            blockTransactions.add(S.get(idx));
        }

        // Handle early aborts
        if (earlyAbort) {
            boolean newAborts;
            do {
                newAborts = false;
                // Map from keys to the indices of transactions that write them
                Map<Integer, Set<Integer>> keyToWritingTxs = new HashMap<>();
                for (int i = 0; i < blockTransactions.size(); i++) {
                    RequestData tx = blockTransactions.get(i);
                    if (!tx.getIsTnxValid()) continue; // Skip aborted transactions
                    for (OperationSet writeOp : tx.getWriteSetList()) {
                        keyToWritingTxs.computeIfAbsent(writeOp.getRecord(), k -> new HashSet<>()).add(i);
                    }
                }

                // Abort transactions that read from keys written by other transactions
                for (int i = 0; i < blockTransactions.size(); i++) {
                    RequestData tx = blockTransactions.get(i);
                    if (!tx.getIsTnxValid()) continue; // Skip aborted transactions

                    boolean shouldAbort = false;
                    for (OperationSet readOp : tx.getReadSetList()) {
                        Set<Integer> writers = keyToWritingTxs.get(readOp.getRecord());
                        if (writers != null) {
                            for (Integer writerIdx : writers) {
                                if (writerIdx != i) {
                                    shouldAbort = true;
                                    break;
                                }
                            }
                        }
                        if (shouldAbort) break;
                    }
                    if (shouldAbort) {
                        RequestData abortedTx = tx.toBuilder().setIsTnxValid(false).build(); // Mark as aborted
                        blockTransactions.set(i, abortedTx);
                        newAborts = true;
                    }
                }
            } while (newAborts);
        }

        // Return the reordered transactions
        RequestDataList result = RequestDataList.newBuilder()
                .addAllReqDataList(blockTransactions)
                .build();

        return result;
    }

    /**
     * Performs a topological sort on the given graph.
     *
     * @param graph The graph to sort
     * @return A list of vertices in topologically sorted order
     */
    private List<Integer> topologicalSort(Graph<Integer, DefaultEdge> graph) {
        List<Integer> sorted = new ArrayList<>();
        TopologicalOrderIterator<Integer, DefaultEdge> iterator = new TopologicalOrderIterator<>(graph);
        while (iterator.hasNext()) {
            sorted.add(iterator.next());
        }
        return sorted;
    }

    /**
     * Prints the conflict graph edges.
     *
     * @param graph The conflict graph to print
     */
    private void printConflictGraph(Graph<Integer, DefaultEdge> graph) {
        System.out.println("Conflict Graph:");
        for (DefaultEdge edge : graph.edgeSet()) {
            System.out.println(graph.getEdgeSource(edge) + " -> " + graph.getEdgeTarget(edge));
        }
    }
}