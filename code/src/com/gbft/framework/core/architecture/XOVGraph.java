package com.gbft.framework.core.architecture;

import com.gbft.framework.data.OperationSet;
import com.gbft.framework.data.RequestData;
import com.gbft.framework.data.RequestDataList;

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
                    conflictGraph.get(j).outEdges.add(i); // Edge from writer (txJ) to reader (txI)
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

                // According to the rules, ignore Write-After-Read (WAR) and Read-Read (RAR) conflicts
            }
        }
        return conflictGraph;
    }

    // New method to select transactions to abort in a cycle
    private void resolveCycle(List<Integer> cycle, List<RequestData> S, Set<Integer> abortedTransactions) {
        // Analyze the cycle to decide which transaction(s) to abort
        // For simplicity, abort transactions with the highest index (assuming they are newer)
        int transactionToAbort = Collections.max(cycle);
        if (!abortedTransactions.contains(transactionToAbort)) {
            RequestData tx = S.get(transactionToAbort);
            RequestData abortedTx = tx.toBuilder().setIsTnxValid(false).build(); // Mark as aborted
            S.set(transactionToAbort, abortedTx);
            abortedTransactions.add(transactionToAbort);
        }
    }


    public RequestDataList processXOVReordering(List<RequestData> transactions, boolean earlyAbort, boolean reorder) {
        // Initialize
        List<RequestData> S = new ArrayList<>(transactions);

        // Build conflict graph over S
        List<Vertex> conflictGraph = buildConflictGraphXOV(S);

        // Find elementary cycles in the conflict graph
        CyclesSearch cyclesSearch = new CyclesSearch();
        cyclesSearch.getElementaryCycles(conflictGraph);
        System.out.println("Detected Cycles: " + cyclesSearch.cycles);

        // Aborted transactions due to cycles
        Set<Integer> abortedTransactions = new HashSet<>();
        // Process each cycle individually
        for (List<Integer> cycle : cyclesSearch.cycles) {
            resolveCycle(cycle, S, abortedTransactions);
        }

        // Remove aborted transactions from conflict graph
        for (Vertex v : conflictGraph) {
            v.outEdges.removeAll(abortedTransactions);
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

        System.out.println("Execution Order:");
        for (RequestData tx : blockTransactions) {
            System.out.println("Transaction " + tx.getRequestNum() + " Aborted: " + !tx.getIsTnxValid());
        }

        // Return the reordered transactions
        RequestDataList result = RequestDataList.newBuilder()
                .addAllReqDataList(blockTransactions)
                .build();

        return result;
    }
}
