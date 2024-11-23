package com.gbft.framework.core.architecture;

import com.gbft.framework.data.OperationSet;
import com.gbft.framework.data.RequestData;
import com.gbft.framework.data.RequestDataList;

import java.util.List;

import java.util.*;

public class XOVGraph {

    public List<Vertex> buildConflictGraphXOV(List<RequestData> transactions) {
        int n = transactions.size();
        List<Vertex> conflictGraph = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            conflictGraph.add(new Vertex());
        }

        // Build the conflict graph
        for (int i = 0; i < n; i++) {
            RequestData txI = transactions.get(i);
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                RequestData txJ = transactions.get(j);

                // Check for Read-After-Write (RAW) conflicts
                // If txI reads a key that txJ writes, add an edge from txJ to txI (txJ must come before txI)
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
                    conflictGraph.get(j).outEdges.add(i);
                }

                // Check for Write-Write (WAW) conflicts
                // If txI and txJ both write to the same key, add bidirectional edges
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
                    conflictGraph.get(i).outEdges.add(j);
                    conflictGraph.get(j).outEdges.add(i);
                }

                // According to the rules, ignore Read-Write (WAR) and Read-Read (RAR) conflicts
            }
        }
        return conflictGraph;
    }



    public RequestDataList processXOVReordering(List<RequestData> transactions, boolean earlyAbort, boolean reorder) {
        // Initialize
        List<RequestData> S = new ArrayList<>(transactions);

        // Build conflict graph over S
        List<Vertex> conflictGraph = buildConflictGraphXOV(S);

        // Find elementary cycles in the conflict graph
        CyclesSearch cyclesSearch = new CyclesSearch();
        cyclesSearch.getElementaryCycles(conflictGraph);

        // Aborting transactions involved in cycles
        Set<Integer> abortedTransactions = new HashSet<>();
        if (!cyclesSearch.cycles.isEmpty()) {
            // Collect all transactions involved in cycles
            Set<Integer> transactionsInCycles = new HashSet<>();
            for (List<Integer> cycle : cyclesSearch.cycles) {
                transactionsInCycles.addAll(cycle);
            }
            // Abort all transactions involved in cycles
            for (Integer idx : transactionsInCycles) {
                RequestData tx = S.get(idx);
                RequestData abortedTx = tx.toBuilder().setIsTnxValid(false).build(); // Mark as aborted
                S.set(idx, abortedTx);
                abortedTransactions.add(idx);
            }
            // Remove aborted transactions from conflict graph
            for (Vertex v : conflictGraph) {
                v.outEdges.removeAll(abortedTransactions);
            }
        }

        // Proceed to perform topological sort for reordering
        List<RequestData> blockTransactions = new ArrayList<>();
        if (reorder) {
            // Perform topological sort on the conflict graph
            int[] inDegree = new int[conflictGraph.size()];
            for (int u = 0; u < conflictGraph.size(); u++) {
                if (abortedTransactions.contains(u)) continue; // Skip aborted transactions
                for (int v : conflictGraph.get(u).outEdges) {
                    if (!abortedTransactions.contains(v)) {
                        inDegree[v]++;
                    }
                }
            }

            Queue<Integer> queue = new LinkedList<>();
            for (int u = 0; u < inDegree.length; u++) {
                if (inDegree[u] == 0 && !abortedTransactions.contains(u)) {
                    queue.offer(u);
                }
            }

            while (!queue.isEmpty()) {
                int u = queue.poll();
                blockTransactions.add(S.get(u));
                for (int v : conflictGraph.get(u).outEdges) {
                    if (abortedTransactions.contains(v)) continue; // Skip edges to aborted transactions
                    inDegree[v]--;
                    if (inDegree[v] == 0 && !abortedTransactions.contains(v)) {
                        queue.offer(v);
                    }
                }
            }

            // Check if all non-aborted transactions are processed
            if (blockTransactions.size() != S.size() - abortedTransactions.size()) {
                System.err.println("Cycle detected in topological sort.");
                // Abort remaining transactions involved in cycles
                for (int i = 0; i < S.size(); i++) {
                    if (!abortedTransactions.contains(i) && !blockTransactions.contains(S.get(i))) {
                        RequestData tx = S.get(i);
                        RequestData abortedTx = tx.toBuilder().setIsTnxValid(false).build();
                        S.set(i, abortedTx);
                        abortedTransactions.add(i);
                    }
                }
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
// Handle early aborts
        if (earlyAbort) {
            Set<Integer> abortedKeys = new HashSet<>();
            Set<Integer> newlyAbortedTransactions = new HashSet<>(abortedTransactions);

            // Keep applying early aborts until no new transactions are aborted
            boolean newAborts;
            do {
                newAborts = false;

                // Collect keys written by newly aborted transactions
                for (Integer idx : newlyAbortedTransactions) {
                    RequestData abortedTx = S.get(idx);
                    for (OperationSet writeOp : abortedTx.getWriteSetList()) {
                        abortedKeys.add(writeOp.getRecord());
                    }
                }
                newlyAbortedTransactions.clear();

                List<RequestData> finalBlock = new ArrayList<>();
                for (int i = 0; i < blockTransactions.size(); i++) {
                    RequestData tx = blockTransactions.get(i);
                    if (!tx.getIsTnxValid()) {
                        // Already aborted
                        finalBlock.add(tx);
                        continue;
                    }
                    boolean shouldAbort = false;
                    for (OperationSet readOp : tx.getReadSetList()) {
                        if (abortedKeys.contains(readOp.getRecord())) {
                            shouldAbort = true;
                            break;
                        }
                    }
                    if (shouldAbort) {
                        RequestData abortedTx = tx.toBuilder().setIsTnxValid(false).build(); // Mark as aborted
                        finalBlock.add(abortedTx);
                        newlyAbortedTransactions.add(i);
                        newAborts = true;
                    } else {
                        finalBlock.add(tx);
                    }
                }
                blockTransactions = finalBlock;
            } while (newAborts);
        }


        // Return the reordered transactions
        RequestDataList result = RequestDataList.newBuilder()
                .addAllReqDataList(blockTransactions)
                .build();

        return result;
    }


}
