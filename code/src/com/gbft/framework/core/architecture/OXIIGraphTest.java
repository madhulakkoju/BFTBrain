package com.gbft.framework.core.architecture;

import com.gbft.framework.data.Operation;
import com.gbft.framework.data.OperationSet;
import com.gbft.framework.data.RequestData;

import com.gbft.framework.data.RequestDataList;
import org.junit.jupiter.api.Test; // For the @Test annotation

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class OXIIGraphTest {

    // Test Case 1: No Conflicts
    @Test
    public void testNoConflicts() {
        List<RequestData> transactions = new ArrayList<>();

        // Transaction 0 reads key 1
        RequestData transaction0 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder()
                        .setRecord(1)
                        .setOp(Operation.READ_ONLY)
                        .setValue(0)
                        .build())
                .build();

        // Transaction 1 writes to key 2
        RequestData transaction1 = RequestData.newBuilder()
                .addWriteSet(OperationSet.newBuilder()
                        .setRecord(2)
                        .setOp(Operation.ADD)
                        .setValue(10)
                        .build())
                .build();

        // Transaction 2 reads key 3
        RequestData transaction2 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder()
                        .setRecord(3)
                        .setOp(Operation.READ_ONLY)
                        .setValue(0)
                        .build())
                .build();

        transactions.add(transaction0);
        transactions.add(transaction1);
        transactions.add(transaction2);

        // Build the conflict graph
        OXIIGraph graphBuilder = new OXIIGraph();
        List<Vertex> conflictGraph = graphBuilder.buildConflictGraphOXII(transactions);

        // Verify that there are no edges
        for (int i = 0; i < conflictGraph.size(); i++) {
            Vertex vertex = conflictGraph.get(i);
            assertTrue(vertex.inEdges.isEmpty(), "Vertex " + i + " should have no in-edges");

            assertTrue(vertex.outEdges.isEmpty(), "Vertex " + i + " should have no out-edges");
        }
    }

    // Test Case 2: Read-After-Write Conflict
    @Test
    public void testReadAfterWriteConflict() {
        List<RequestData> transactions = new ArrayList<>();

        // Transaction 0 reads key 1
        RequestData transaction0 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder()
                        .setRecord(1)
                        .setOp(Operation.READ_ONLY)
                        .setValue(0)
                        .build())
                .build();

        // Transaction 1 writes to key 1
        RequestData transaction1 = RequestData.newBuilder()
                .addWriteSet(OperationSet.newBuilder()
                        .setRecord(1)
                        .setOp(Operation.ADD)
                        .setValue(10)
                        .build())
                .build();

        transactions.add(transaction0);
        transactions.add(transaction1);

        // Build the conflict graph
        OXIIGraph graphBuilder = new OXIIGraph();
        List<Vertex> conflictGraph = graphBuilder.buildConflictGraphOXII(transactions);

        // Expected: Edge from Transaction 0 to Transaction 1
        Vertex vertex0 = conflictGraph.get(0);
        Vertex vertex1 = conflictGraph.get(1);

        assertTrue(vertex0.outEdges.contains(1), "Vertex 0 should have an out-edge to Vertex 1");
        assertTrue(vertex1.inEdges.contains(0), "Vertex 1 should have an in-edge from Vertex 0");

        // Verify no other edges exist
        assertEquals(1, vertex0.outEdges.size(), "Vertex 0 should have exactly one out-edge");
        assertEquals(1, vertex1.inEdges.size(), "Vertex 1 should have exactly one in-edge");
        assertTrue(vertex0.inEdges.isEmpty(), "Vertex 0 should have no in-edges");
        assertTrue(vertex1.outEdges.isEmpty(), "Vertex 1 should have no out-edges");
    }

    // Test Case 3: Write-After-Read Conflict (Not Detected)
    @Test
    public void testWriteAfterReadConflict() {
        List<RequestData> transactions = new ArrayList<>();

        // Transaction 0 writes to key 1
        RequestData transaction0 = RequestData.newBuilder()
                .addWriteSet(OperationSet.newBuilder()
                        .setRecord(1)
                        .setOp(Operation.ADD)
                        .setValue(10)
                        .build())
                .build();

        // Transaction 1 read key 1
        RequestData transaction1 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder()
                        .setRecord(1)
                        .setOp(Operation.READ_ONLY)
                        .setValue(0)
                        .build())
                .build();

        transactions.add(transaction0);
        transactions.add(transaction1);

        // Build the conflict graph
        OXIIGraph graphBuilder = new OXIIGraph();
        List<Vertex> conflictGraph = graphBuilder.buildConflictGraphOXII(transactions);

        Vertex vertex0 = conflictGraph.get(0);
        Vertex vertex1 = conflictGraph.get(1);

        // No conflicts should be detected
// Transaction 0 should have an out-edge to Transaction 1
        assertTrue(vertex0.outEdges.contains(1), "Vertex 0 should have an out-edge to Vertex 1");
        assertEquals(1, vertex0.outEdges.size(), "Vertex 0 should have exactly one out-edge");
        assertTrue(vertex0.inEdges.isEmpty(), "Vertex 0 should have no in-edges");

// Transaction 1 should have an in-edge from Transaction 0
        assertTrue(vertex1.inEdges.contains(0), "Vertex 1 should have an in-edge from Vertex 0");
        assertEquals(1, vertex1.inEdges.size(), "Vertex 1 should have exactly one in-edge");
        assertTrue(vertex1.outEdges.isEmpty(), "Vertex 1 should have no out-edges");
    }

    // Test Case 4: Write-After-Write Conflict (Not Detected)
    @Test
    public void testWriteAfterWriteConflict() {
        List<RequestData> transactions = new ArrayList<>();

        // Transaction 0 writes to key 1
        RequestData transaction0 = RequestData.newBuilder()
                .addWriteSet(OperationSet.newBuilder()
                        .setRecord(1)
                        .setOp(Operation.ADD)
                        .setValue(10)
                        .build())
                .build();

        // Transaction 1 writes to key 1
        RequestData transaction1 = RequestData.newBuilder()
                .addWriteSet(OperationSet.newBuilder()
                        .setRecord(1)
                        .setOp(Operation.SUB)
                        .setValue(5)
                        .build())
                .build();

        transactions.add(transaction0);
        transactions.add(transaction1);

        // Build the conflict graph
        OXIIGraph graphBuilder = new OXIIGraph();
        List<Vertex> conflictGraph = graphBuilder.buildConflictGraphOXII(transactions);

        Vertex vertex0 = conflictGraph.get(0);
        Vertex vertex1 = conflictGraph.get(1);

        // No conflicts should be detected
// Expect an edge from Transaction 0 to Transaction 1
        assertTrue(vertex0.outEdges.contains(1), "Vertex 0 should have an out-edge to Vertex 1");
        assertTrue(vertex1.inEdges.contains(0), "Vertex 1 should have an in-edge from Vertex 0");
        assertEquals(1, vertex0.outEdges.size(), "Vertex 0 should have exactly one out-edge");
        assertEquals(1, vertex1.inEdges.size(), "Vertex 1 should have exactly one in-edge");

// Verify that there are no other edges
        assertTrue(vertex0.inEdges.isEmpty(), "Vertex 0 should have no in-edges");
        assertTrue(vertex1.outEdges.isEmpty(), "Vertex 1 should have no out-edges");
    }

    // Test Case 5: Multiple Conflicts
    @Test
    public void testMultipleConflicts() {
        List<RequestData> transactions = new ArrayList<>();

        // Transaction 0 reads key 1 and writes to key 2
        RequestData transaction0 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder()
                        .setRecord(1)
                        .setOp(Operation.READ_ONLY)
                        .setValue(0)
                        .build())
                .addWriteSet(OperationSet.newBuilder()
                        .setRecord(2)
                        .setOp(Operation.ADD)
                        .setValue(10)
                        .build())
                .build();

        // Transaction 1 writes to key 1
        RequestData transaction1 = RequestData.newBuilder()
                .addWriteSet(OperationSet.newBuilder()
                        .setRecord(1)
                        .setOp(Operation.SUB)
                        .setValue(5)
                        .build())
                .build();

        // Transaction 2 reads key 2
        RequestData transaction2 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder()
                        .setRecord(2)
                        .setOp(Operation.READ_ONLY)
                        .setValue(0)
                        .build())
                .build();

        // Transaction 3 writes to key 3
        RequestData transaction3 = RequestData.newBuilder()
                .addWriteSet(OperationSet.newBuilder()
                        .setRecord(3)
                        .setOp(Operation.INC)
                        .setValue(1)
                        .build())
                .build();

        // Transaction 4 reads key 3
        RequestData transaction4 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder()
                        .setRecord(3)
                        .setOp(Operation.READ_ONLY)
                        .setValue(0)
                        .build())
                .build();

        transactions.add(transaction0);
        transactions.add(transaction1);
        transactions.add(transaction2);
        transactions.add(transaction3);
        transactions.add(transaction4);

        // Build the conflict graph
        OXIIGraph graphBuilder = new OXIIGraph();
        List<Vertex> conflictGraph = graphBuilder.buildConflictGraphOXII(transactions);

        // Verify conflicts
        // Transaction 0 should have out-edges to Transactions 1 and 2
        Vertex vertex0 = conflictGraph.getFirst();
        assertTrue(vertex0.outEdges.contains(1), "Vertex 0 should have out-edge to Vertex 1");
        assertTrue(vertex0.outEdges.contains(2), "Vertex 0 should have out-edge to Vertex 2");
        assertEquals(2, vertex0.outEdges.size(), "Vertex 0 should have 2 out-edges");

        // Transaction 1 should have in-edge from Transaction 0
        Vertex vertex1 = conflictGraph.get(1);
        assertTrue(vertex1.inEdges.contains(0), "Vertex 1 should have in-edge from Vertex 0");
        assertEquals( 1, vertex1.inEdges.size(),"Vertex 1 should have 1 in-edge");

        // Transaction 2 should have in-edge from Transaction 0
        Vertex vertex2 = conflictGraph.get(2);
        assertTrue(vertex2.inEdges.contains(0), "Vertex 2 should have in-edge from Vertex 0");
        assertEquals( 1, vertex2.inEdges.size(),"Vertex 2 should have 1 in-edge");

        // Transaction 3 should have out-edge to Transaction 4
        Vertex vertex3 = conflictGraph.get(3);
        assertTrue(vertex3.outEdges.contains(4), "Vertex 3 should have out-edge to Vertex 4");
        assertEquals(1, vertex3.outEdges.size(), "Vertex 3 should have 1 out-edge");

        // Transaction 4 should have in-edge from Transaction 3
        Vertex vertex4 = conflictGraph.get(4);
        assertTrue(vertex4.inEdges.contains(3), "Vertex 4 should have in-edge from Vertex 3");
        assertEquals(1, vertex4.inEdges.size(),"Vertex 4 should have 1 in-edge");
    }


    // TESTS FOR OXIIReordering

    @Test
    public void testProcessOXIIReordering() {
        List<RequestData> transactions = new ArrayList<>();

        // Transaction 0: No dependencies
        RequestData transaction0 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder().setRecord(1).setOp(Operation.READ_ONLY).build())
                .build();

        // Transaction 1: Depends on Transaction 0
        RequestData transaction1 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder().setRecord(2).setOp(Operation.READ_ONLY).build())
                .addWriteSet(OperationSet.newBuilder().setRecord(1).setOp(Operation.ADD).build())
                .build();

        // Transaction 2: Depends on Transactions 0 and 1
        RequestData transaction2 = RequestData.newBuilder()
                .addWriteSet(OperationSet.newBuilder().setRecord(1).setOp(Operation.SUB).build())
                .build();

        // Transaction 3: Depends on Transaction 1
        RequestData transaction3 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder().setRecord(3).setOp(Operation.READ_ONLY).build())
                .addWriteSet(OperationSet.newBuilder().setRecord(2).setOp(Operation.ADD).build())
                .build();

        transactions.add(transaction0);
        transactions.add(transaction1);
        transactions.add(transaction2);
        transactions.add(transaction3);

        // Process OXII Reordering
        OXIIGraph graphBuilder = new OXIIGraph();
        List<RequestDataList> iterativeList = graphBuilder.processOXIIReordering(transactions);

        // Verify the result
        assertEquals(3, iterativeList.size(), "There should be 3 levels of transactions");

        // Level 0 should contain Transaction 0
        assertEquals(1, iterativeList.get(0).getReqDataListCount(), "Level 0 should have 1 transaction");
        assertEquals(transaction0, iterativeList.get(0).getReqDataList(0), "Level 0 should contain Transaction 0");

        // Level 1 should contain Transaction 1
        assertEquals(1, iterativeList.get(1).getReqDataListCount(), "Level 1 should have 1 transaction");
        assertEquals(transaction1, iterativeList.get(1).getReqDataList(0), "Level 1 should contain Transaction 1");

        // Level 2 should contain Transactions 2 and 3
        assertEquals(2, iterativeList.get(2).getReqDataListCount(), "Level 2 should have 2 transactions");
        List<RequestData> level2Transactions = iterativeList.get(2).getReqDataListList();
        assertTrue(level2Transactions.contains(transaction2), "Level 2 should contain Transaction 2");
        assertTrue(level2Transactions.contains(transaction3), "Level 2 should contain Transaction 3");
    }

    @Test
    public void testNoDependencies() {
        List<RequestData> transactions = new ArrayList<>();

        // Transaction 0
        RequestData transaction0 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder().setRecord(1).setOp(Operation.READ_ONLY).build())
                .addWriteSet(OperationSet.newBuilder().setRecord(2).setOp(Operation.ADD).build())
                .build();

        // Transaction 1
        RequestData transaction1 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder().setRecord(3).setOp(Operation.READ_ONLY).build())
                .addWriteSet(OperationSet.newBuilder().setRecord(4).setOp(Operation.SUB).build())
                .build();

        // Transaction 2
        RequestData transaction2 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder().setRecord(5).setOp(Operation.READ_ONLY).build())
                .addWriteSet(OperationSet.newBuilder().setRecord(6).setOp(Operation.INC).build())
                .build();

        transactions.add(transaction0);
        transactions.add(transaction1);
        transactions.add(transaction2);

        // Process OXII Reordering
        OXIIGraph graphBuilder = new OXIIGraph();
        List<RequestDataList> iterativeList = graphBuilder.processOXIIReordering(transactions);

        // Verify the result
        assertEquals(1, iterativeList.size(), "There should be 1 level of transactions");

        // Level 0 should contain all transactions
        assertEquals(3, iterativeList.get(0).getReqDataListCount(), "Level 0 should have 3 transactions");
        List<RequestData> level0Transactions = iterativeList.get(0).getReqDataListList();
        assertTrue(level0Transactions.contains(transaction0), "Level 0 should contain Transaction 0");
        assertTrue(level0Transactions.contains(transaction1), "Level 0 should contain Transaction 1");
        assertTrue(level0Transactions.contains(transaction2), "Level 0 should contain Transaction 2");
    }

    @Test
    public void testComplexDependencies() {
        List<RequestData> transactions = new ArrayList<>();

        // Transaction 0
        RequestData transaction0 = RequestData.newBuilder()
                .addWriteSet(OperationSet.newBuilder().setRecord(1).setOp(Operation.ADD).build())
                .build();

        // Transaction 1
        RequestData transaction1 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder().setRecord(1).setOp(Operation.READ_ONLY).build())
                .addWriteSet(OperationSet.newBuilder().setRecord(2).setOp(Operation.SUB).build())
                .build();

        // Transaction 2
        RequestData transaction2 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder().setRecord(1).setOp(Operation.READ_ONLY).build())
                .addWriteSet(OperationSet.newBuilder().setRecord(3).setOp(Operation.INC).build())
                .build();

        // Transaction 3
        RequestData transaction3 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder().setRecord(2).setOp(Operation.READ_ONLY).build())
                .addReadSet(OperationSet.newBuilder().setRecord(3).setOp(Operation.READ_ONLY).build())
                .addWriteSet(OperationSet.newBuilder().setRecord(4).setOp(Operation.DEC).build())
                .build();

        // Transaction 4
        RequestData transaction4 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder().setRecord(4).setOp(Operation.READ_ONLY).build())
                .build();

        transactions.add(transaction0);
        transactions.add(transaction1);
        transactions.add(transaction2);
        transactions.add(transaction3);
        transactions.add(transaction4);

        // Process OXII Reordering
        OXIIGraph graphBuilder = new OXIIGraph();
        List<RequestDataList> iterativeList = graphBuilder.processOXIIReordering(transactions);

        // Verify the result
        assertEquals(4, iterativeList.size(), "There should be 4 levels of transactions");

        // Level 0: Transaction 0
        assertEquals(1, iterativeList.get(0).getReqDataListCount(), "Level 0 should have 1 transaction");
        assertEquals(transaction0, iterativeList.get(0).getReqDataList(0), "Level 0 should contain Transaction 0");

        // Level 1: Transactions 1 and 2
        assertEquals(2, iterativeList.get(1).getReqDataListCount(), "Level 1 should have 2 transactions");
        List<RequestData> level1Transactions = iterativeList.get(1).getReqDataListList();
        assertTrue(level1Transactions.contains(transaction1), "Level 1 should contain Transaction 1");
        assertTrue(level1Transactions.contains(transaction2), "Level 1 should contain Transaction 2");

        // Level 2: Transaction 3
        assertEquals(1, iterativeList.get(2).getReqDataListCount(), "Level 2 should have 1 transaction");
        assertEquals(transaction3, iterativeList.get(2).getReqDataList(0), "Level 2 should contain Transaction 3");

        // Level 3: Transaction 4
        assertEquals(1, iterativeList.get(3).getReqDataListCount(), "Level 3 should have 1 transaction");
        assertEquals(transaction4, iterativeList.get(3).getReqDataList(0), "Level 3 should contain Transaction 4");
    }

    @Test
    public void testMultipleDependencies() {
        List<RequestData> transactions = new ArrayList<>();

        // Transaction 0
        RequestData transaction0 = RequestData.newBuilder()
                .addWriteSet(OperationSet.newBuilder().setRecord(1).setOp(Operation.ADD).build())
                .build();

        // Transaction 1
        RequestData transaction1 = RequestData.newBuilder()
                .addWriteSet(OperationSet.newBuilder().setRecord(2).setOp(Operation.SUB).build())
                .build();

        // Transaction 2
        RequestData transaction2 = RequestData.newBuilder()
                .addReadSet(OperationSet.newBuilder().setRecord(1).setOp(Operation.READ_ONLY).build())
                .addReadSet(OperationSet.newBuilder().setRecord(2).setOp(Operation.READ_ONLY).build())
                .addWriteSet(OperationSet.newBuilder().setRecord(3).setOp(Operation.INC).build())
                .build();

        transactions.add(transaction0);
        transactions.add(transaction1);
        transactions.add(transaction2);

        // Process OXII Reordering
        OXIIGraph graphBuilder = new OXIIGraph();
        List<RequestDataList> iterativeList = graphBuilder.processOXIIReordering(transactions);

        // Verify the result
        assertEquals(2, iterativeList.size(), "There should be 2 levels of transactions");

        // Level 0: Transactions 0 and 1
        assertEquals(2, iterativeList.get(0).getReqDataListCount(), "Level 0 should have 2 transactions");
        List<RequestData> level0Transactions = iterativeList.get(0).getReqDataListList();
        assertTrue(level0Transactions.contains(transaction0), "Level 0 should contain Transaction 0");
        assertTrue(level0Transactions.contains(transaction1), "Level 0 should contain Transaction 1");

        // Level 1: Transaction 2
        assertEquals(1, iterativeList.get(1).getReqDataListCount(), "Level 1 should have 1 transaction");
        assertEquals(transaction2, iterativeList.get(1).getReqDataList(0), "Level 1 should contain Transaction 2");
    }

    @Test
    public void testLargeNumberOfTransactions() {
        List<RequestData> transactions = new ArrayList<>();
        int numTransactions = 20;

        // Generate transactions
        for (int i = 0; i < numTransactions; i++) {
            RequestData.Builder builder = RequestData.newBuilder();

            // Every even transaction writes to key i, reads key i-1
            if (i % 2 == 0) {
                if (i > 0) {
                    builder.addReadSet(OperationSet.newBuilder().setRecord(i - 1).setOp(Operation.READ_ONLY).build());
                }
                builder.addWriteSet(OperationSet.newBuilder().setRecord(i).setOp(Operation.ADD).build());
            } else {
                // Every odd transaction reads key i, writes to key i+1
                builder.addReadSet(OperationSet.newBuilder().setRecord(i).setOp(Operation.READ_ONLY).build());
                builder.addWriteSet(OperationSet.newBuilder().setRecord(i + 1).setOp(Operation.SUB).build());
            }

            transactions.add(builder.build());
        }

        // Process OXII Reordering
        OXIIGraph graphBuilder = new OXIIGraph();
        List<RequestDataList> iterativeList = graphBuilder.processOXIIReordering(transactions);

        // Verify that the method completes without errors
        assertNotNull(iterativeList, "Iterative list should not be null");

        // Since dependencies are complex, we won't check exact levels
        // Instead, we'll ensure all transactions are scheduled
        int totalTransactions = iterativeList.stream()
                .mapToInt(RequestDataList::getReqDataListCount)
                .sum();

        assertEquals(numTransactions, totalTransactions, "All transactions should be scheduled");
    }



}
