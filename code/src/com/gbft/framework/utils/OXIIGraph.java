package com.gbft.framework.utils;

import com.gbft.framework.data.OperationSet;
import com.gbft.framework.data.RequestData;
import com.gbft.framework.data.RequestDataList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OXIIGraph{


    public List<Vertex> buildConflictGraphOXII(List<RequestData> transactions) {
        // Map from key to bitmap index
        Map<Integer, Integer> keyToBitmapIndex = new HashMap<>();
        int bitmapIdx = 0;

        // Build the key to bitmap index mapping
        for (RequestData transaction : transactions) {
            // Read keys
            for (OperationSet opSet : transaction.getReadSetList()) {
                int key = opSet.getRecord();
                if (!keyToBitmapIndex.containsKey(key)) {
                    keyToBitmapIndex.put(key, bitmapIdx++);
                }
            }
            // Write keys
            for (OperationSet opSet : transaction.getWriteSetList()) {
                int key = opSet.getRecord();
                if (!keyToBitmapIndex.containsKey(key)) {
                    keyToBitmapIndex.put(key, bitmapIdx++);
                }
            }
        }

        int numKeys = bitmapIdx; // Total number of unique keys

        // Build readBitmaps and writeBitmaps for each transaction
        List<boolean[]> readBitmaps = new ArrayList<>();
        List<boolean[]> writeBitmaps = new ArrayList<>();

        for (RequestData transaction : transactions) {
            boolean[] readBitmap = new boolean[numKeys];
            boolean[] writeBitmap = new boolean[numKeys];

            // Populate read bitmap
            for (OperationSet opSet : transaction.getReadSetList()) {
                int key = opSet.getRecord();
                int idx = keyToBitmapIndex.get(key);
                readBitmap[idx] = true;
            }

            // Populate write bitmap
            for (OperationSet opSet : transaction.getWriteSetList()) {
                int key = opSet.getRecord();
                int idx = keyToBitmapIndex.get(key);
                writeBitmap[idx] = true;
            }

            readBitmaps.add(readBitmap);
            writeBitmaps.add(writeBitmap);
        }

        // Initialize the conflict graph with vertices
        List<Vertex> conflictGraph = new ArrayList<>();
        int numTransactions = transactions.size();
        for (int i = 0; i < numTransactions; i++) {
            conflictGraph.add(new Vertex());
        }

        for (int i = 0; i < numTransactions; i++) {
            for (int j = i + 1; j < numTransactions; j++) {
                boolean conflict = false;
                for (int k = 0; k < numKeys; k++) {
                    if (
                            (readBitmaps.get(i)[k] && writeBitmaps.get(j)[k]) || // Read-after-write
                                    (writeBitmaps.get(i)[k] && readBitmaps.get(j)[k]) || // Write-after-read
                                    (writeBitmaps.get(i)[k] && writeBitmaps.get(j)[k])   // Write-after-write
                    ) {
                        conflict = true;
                        break;
                    }
                }
                if (conflict) {
                    // Add an edge from i to j
                    conflictGraph.get(i).outEdges.add(j);
                    conflictGraph.get(j).inEdges.add(i);
                }
            }
        }

        return conflictGraph;
    }

    public List<RequestDataList> processOXIIReordering(List<RequestData> transactions) {
        if (transactions.isEmpty()) {
            return List.of();
        }

        // Step 1: Build the conflict graph
        List<Vertex> conflictGraph = buildConflictGraphOXII(transactions);
        int numTransactions = transactions.size();

        // Step 2: Initialize in-degree array and processed array
        int[] inDegree = new int[numTransactions];
        boolean[] processed = new boolean[numTransactions];

        // Build in-degree array
        for (int i = 0; i < numTransactions; i++) {
            Vertex vertex = conflictGraph.get(i);
            inDegree[i] = vertex.inEdges.size();
        }

        // Step 3: Initialize list of transactions with in-degree zero
        List<Integer> zeroInDegree = new ArrayList<>();
        for (int i = 0; i < numTransactions; i++) {
            if (inDegree[i] == 0) {
                zeroInDegree.add(i);
            }
        }

        // List to store the result
        List<RequestDataList> iterativeList = new ArrayList<>();

        // Step 4: Process transactions level by level
        while (!zeroInDegree.isEmpty()) {
            List<RequestData> currentLevelTransactions = new ArrayList<>();
            List<Integer> nextZeroInDegree = new ArrayList<>();

            // For all transactions with zero in-degree, add them to the current level
            for (int idx : zeroInDegree) {
                processed[idx] = true;
                currentLevelTransactions.add(transactions.get(idx));

                // Decrease the in-degree of their neighbors
                for (int neighbor : conflictGraph.get(idx).outEdges) {
                    if (!processed[neighbor]) {
                        inDegree[neighbor]--;
                        if (inDegree[neighbor] == 0) {
                            nextZeroInDegree.add(neighbor);
                        }
                    }
                }
            }

            // Step 5: Build RequestDataList and add to iterativeList
            RequestDataList.Builder builder = RequestDataList.newBuilder();
            builder.addAllReqDataList(currentLevelTransactions);
            iterativeList.add(builder.build());

            // Move to the next level
            zeroInDegree = nextZeroInDegree;
        }

        return iterativeList;
    }
}


