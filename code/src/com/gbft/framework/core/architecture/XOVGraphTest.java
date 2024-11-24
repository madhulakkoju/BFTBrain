package com.gbft.framework.core.architecture;

import com.gbft.framework.data.OperationSet;
import com.gbft.framework.data.RequestData;
import com.gbft.framework.data.RequestDataList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collectors;

public class XOVGraphTest {

    @Test
    public void testReorderingDueToWriteReadConflict() {
        // Create transactions
        RequestData t1 = RequestData.newBuilder()
                .setRequestNum(1)
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build())
                .setIsTnxValid(true) // Ensure transaction is valid initially
                .build();

        RequestData t2 = RequestData.newBuilder()
                .setRequestNum(2)
                .addReadSet(OperationSet.newBuilder().setRecord(1).build())
                .setIsTnxValid(true) // Ensure transaction is valid initially
                .build();

        List<RequestData> transactions = Arrays.asList(t1, t2);

        // Process transactions with early abort and reordering
        XOVGraph xovGraph = new XOVGraph();
        RequestDataList result = xovGraph.processXOVReordering(transactions, false, true);

        // Verify that both transactions are not aborted and are correctly ordered
        List<RequestData> reorderedTransactions = result.getReqDataListList();
        assertEquals(2, reorderedTransactions.size(), "There should be 2 transactions.");

        // Verify the execution order: T2 should come before T1
        assertEquals(2, reorderedTransactions.get(0).getRequestNum(), "T2 should be first.");
        assertEquals(1, reorderedTransactions.get(1).getRequestNum(), "T1 should be second.");

        // Verify that no transactions are aborted
        for (RequestData tx : reorderedTransactions) {
            assertTrue(tx.getIsTnxValid(), "No transactions should be aborted.");
        }
    }


    @Test
    public void testSimpleReorderingDueToWriteReadConflict() {
        // Create transactions
        RequestData t1 = RequestData.newBuilder()
                .setRequestNum(1)
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .build();

        RequestData t2 = RequestData.newBuilder()
                .setRequestNum(2)
                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A
                .build();

        List<RequestData> transactions = Arrays.asList(t1, t2);

        // Process transactions with early abort and reordering
        XOVGraph xovGraph = new XOVGraph();
        RequestDataList result = xovGraph.processXOVReordering(transactions, true, true);

        // Verify the order: T1 should come before T2
        List<RequestData> reorderedTransactions = result.getReqDataListList();
        assertEquals(2, reorderedTransactions.size(), "There should be 2 transactions.");
        assertEquals(2, reorderedTransactions.get(0).getRequestNum(), "T1 should be second.");
        assertEquals(1, reorderedTransactions.get(1).getRequestNum(), "T2 should be first.");
        assertFalse(reorderedTransactions.get(0).getIsTnxValid(), "T1 should not be aborted.");
        assertFalse(reorderedTransactions.get(1).getIsTnxValid(), "T2 should not be aborted.");
    }


    @Test
    public void testAbortingDueToWriteWriteConflict() {
        // Create transactions
        RequestData t1 = RequestData.newBuilder()
                .setRequestNum(1)
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .build();

        RequestData t2 = RequestData.newBuilder()
                .setRequestNum(2)
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .build();

        List<RequestData> transactions = Arrays.asList(t1, t2);

        // Process transactions with early abort and reordering
        XOVGraph xovGraph = new XOVGraph();
        RequestDataList result = xovGraph.processXOVReordering(transactions, true, true);

        // Verify that two transactions are aborted
        List<RequestData> reorderedTransactions = result.getReqDataListList();
        assertEquals(2, reorderedTransactions.size(), "There should be 2 transactions.");

        long abortedCount = reorderedTransactions.stream()
                .filter(tx -> !tx.getIsTnxValid())
                .count();
        assertEquals(2, abortedCount, "Both transactions should be aborted.");
    }
    @Test
    public void testEarlyAbortDueToReadingFromAbortedTransaction() {
        // Create transactions
        RequestData t1 = RequestData.newBuilder()
                .setRequestNum(1)
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .build();

        RequestData t2 = RequestData.newBuilder()
                .setRequestNum(2)
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .build();

        RequestData t3 = RequestData.newBuilder()
                .setRequestNum(3)
                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A
                .build();

        List<RequestData> transactions = Arrays.asList(t1, t2, t3);

        // Process transactions with early abort and reordering
        XOVGraph xovGraph = new XOVGraph();
        RequestDataList result = xovGraph.processXOVReordering(transactions, true, true);

        // Verify that three transactions are aborted
        List<RequestData> reorderedTransactions = result.getReqDataListList();
        assertEquals(3, reorderedTransactions.size(), "There should be 3 transactions.");

        long abortedCount = reorderedTransactions.stream()
                .filter(tx -> !tx.getIsTnxValid())
                .count();
        assertEquals(3, abortedCount, "All three transactions should be aborted.");

        // Verify that T3 is aborted due to early abort
        RequestData t3Result = reorderedTransactions.stream()
                .filter(tx -> tx.getRequestNum() == 3)
                .findFirst()
                .orElse(null);
        assertNotNull(t3Result, "T3 should be in the result.");
        assertTrue(!t3Result.getIsTnxValid(), "T3 should be aborted due to early abort.");
    }

    @Test
    public void testTransactionsWithNoConflicts() {
        // Create transactions
        RequestData t1 = RequestData.newBuilder()
                .setRequestNum(1)
                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A
                .build();

        RequestData t2 = RequestData.newBuilder()
                .setRequestNum(2)
                .addWriteSet(OperationSet.newBuilder().setRecord(2).build()) // Writes B
                .build();

        RequestData t3 = RequestData.newBuilder()
                .setRequestNum(3)
                .addReadSet(OperationSet.newBuilder().setRecord(3).build()) // Reads C
                .build();

        List<RequestData> transactions = Arrays.asList(t1, t2, t3);

        // Process transactions with early abort and reordering
        XOVGraph xovGraph = new XOVGraph();
        RequestDataList result = xovGraph.processXOVReordering(transactions, true, true);

        // Verify that transactions remain in original order and are not aborted
        List<RequestData> reorderedTransactions = result.getReqDataListList();
        assertEquals(3, reorderedTransactions.size(), "There should be 3 transactions.");
        assertEquals(1, reorderedTransactions.get(0).getRequestNum(), "T1 should be first.");
        assertEquals(2, reorderedTransactions.get(1).getRequestNum(), "T2 should be second.");
        assertEquals(3, reorderedTransactions.get(2).getRequestNum(), "T3 should be third.");
        for (RequestData tx : reorderedTransactions) {
            assertFalse(tx.getIsTnxValid(), "No transactions should be aborted.");
        }
    }

    @Test
    public void testTransactionsWithSelfLoops() {
        // Create transactions
        RequestData t1 = RequestData.newBuilder()
                .setRequestNum(1)
                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .setIsTnxValid(true) // Explicitly set as valid
                .build();

        RequestData t2 = RequestData.newBuilder()
                .setRequestNum(2)
                .addReadSet(OperationSet.newBuilder().setRecord(2).build()) // Reads B
                .addWriteSet(OperationSet.newBuilder().setRecord(2).build()) // Writes B
                .setIsTnxValid(true) // Explicitly set as valid
                .build();

        RequestData t3 = RequestData.newBuilder()
                .setRequestNum(3)
                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A
                .addWriteSet(OperationSet.newBuilder().setRecord(2).build()) // Writes B
                .setIsTnxValid(true) // Explicitly set as valid
                .build();

        List<RequestData> transactions = Arrays.asList(t1, t2, t3);

        // Process transactions with early abort and reordering
        XOVGraph xovGraph = new XOVGraph();
        RequestDataList result = xovGraph.processXOVReordering(transactions, true, true);

        // Verify that two transactions are aborted
        List<RequestData> reorderedTransactions = result.getReqDataListList();
        assertEquals(3, reorderedTransactions.size(), "There should be 3 transactions.");

        long abortedCount = reorderedTransactions.stream()
                .filter(tx -> !tx.getIsTnxValid())
                .count();
        assertEquals(2, abortedCount, "Two transactions should be aborted.");

        // Verify that T2 and T3 are aborted due to the cycle
        RequestData t2Result = reorderedTransactions.stream()
                .filter(tx -> tx.getRequestNum() == 2)
                .findFirst()
                .orElse(null);
        assertNotNull(t2Result, "T2 should be in the result.");
        assertFalse(t2Result.getIsTnxValid(), "T2 should be aborted due to the cycle.");

        RequestData t3Result = reorderedTransactions.stream()
                .filter(tx -> tx.getRequestNum() == 3)
                .findFirst()
                .orElse(null);
        assertNotNull(t3Result, "T3 should be in the result.");
        assertFalse(t3Result.getIsTnxValid(), "T3 should be aborted due to the cycle.");

        // Verify the execution order of remaining transaction
        List<RequestData> executedTransactions = reorderedTransactions.stream()
                .filter(RequestData::getIsTnxValid)
                .collect(Collectors.toList());

        assertEquals(1, executedTransactions.size(), "One transaction should be executed.");
        assertEquals(1, executedTransactions.get(0).getRequestNum(), "T1 should be executed.");
    }



    @Test
    public void testTransactionsWithEmptyReadAndWriteSets() {
        // Create transactions
        RequestData t1 = RequestData.newBuilder()
                .setRequestNum(1)
                .build(); // No reads or writes

        RequestData t2 = RequestData.newBuilder()
                .setRequestNum(2)
                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A
                .build();

        RequestData t3 = RequestData.newBuilder()
                .setRequestNum(3)
                .addWriteSet(OperationSet.newBuilder().setRecord(2).build()) // Writes B
                .build();

        List<RequestData> transactions = Arrays.asList(t1, t2, t3);

        // Process transactions with early abort and reordering
        XOVGraph xovGraph = new XOVGraph();
        RequestDataList result = xovGraph.processXOVReordering(transactions, true, true);

        // Verify that all transactions are executed in order
        List<RequestData> reorderedTransactions = result.getReqDataListList();
        assertEquals(3, reorderedTransactions.size(), "There should be 3 transactions.");
        assertEquals(1, reorderedTransactions.get(0).getRequestNum(), "T1 should be first.");
        assertEquals(2, reorderedTransactions.get(1).getRequestNum(), "T2 should be second.");
        assertEquals(3, reorderedTransactions.get(2).getRequestNum(), "T3 should be third.");
        for (RequestData tx : reorderedTransactions) {
            assertFalse(tx.getIsTnxValid(), "No transactions should be aborted.");
        }
    }

    @Test
    public void testAllTransactionsConflictingOnSingleKey() {
        // Create transactions
        RequestData t1 = RequestData.newBuilder()
                .setRequestNum(1)
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .build();

        RequestData t2 = RequestData.newBuilder()
                .setRequestNum(2)
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .build();

        RequestData t3 = RequestData.newBuilder()
                .setRequestNum(3)
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .build();

        RequestData t4 = RequestData.newBuilder()
                .setRequestNum(4)
                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A
                .build();

        List<RequestData> transactions = Arrays.asList(t1, t2, t3, t4);

        // Process transactions with early abort and reordering
        XOVGraph xovGraph = new XOVGraph();
        RequestDataList result = xovGraph.processXOVReordering(transactions, true, true);

        // Verify that all transactions are aborted
        List<RequestData> reorderedTransactions = result.getReqDataListList();
        assertEquals(4, reorderedTransactions.size(), "There should be 4 transactions.");

        long abortedCount = reorderedTransactions.stream()
                .filter(tx -> !tx.getIsTnxValid())
                .count();
        assertEquals(4, abortedCount, "All transactions should be aborted.");
    }

    @Test
    public void testTransactionsWithMultipleAccessesToSameKey() {
        // Create transactions
        RequestData t1 = RequestData.newBuilder()
                .setRequestNum(1)
                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A again
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A again
                .build();

        RequestData t2 = RequestData.newBuilder()
                .setRequestNum(2)
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .build();

        List<RequestData> transactions = Arrays.asList(t1, t2);

        // Process transactions with early abort and reordering
        XOVGraph xovGraph = new XOVGraph();
        RequestDataList result = xovGraph.processXOVReordering(transactions, true, true);

        // Verify that both transactions are aborted
        List<RequestData> reorderedTransactions = result.getReqDataListList();
        assertEquals(2, reorderedTransactions.size(), "There should be 2 transactions.");

        long abortedCount = reorderedTransactions.stream()
                .filter(tx -> !tx.getIsTnxValid())
                .count();
        assertEquals(2, abortedCount, "Both transactions should be aborted.");

        // Verify that no transactions are executed
        List<RequestData> executedTransactions = reorderedTransactions.stream()
                .filter(tx -> tx.getIsTnxValid())
                .collect(Collectors.toList());
        assertEquals(0, executedTransactions.size(), "No transactions should be executed.");
    }

    @Test
    public void testTransactionsWithReadReadConflictsOnly() {
        // Create transactions
        RequestData t1 = RequestData.newBuilder()
                .setRequestNum(1)
                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A
                .build();

        RequestData t2 = RequestData.newBuilder()
                .setRequestNum(2)
                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A
                .build();

        List<RequestData> transactions = Arrays.asList(t1, t2);

        // Process transactions with early abort and reordering
        XOVGraph xovGraph = new XOVGraph();
        RequestDataList result = xovGraph.processXOVReordering(transactions, true, true);

        // Verify that transactions remain in original order and are not aborted
        List<RequestData> reorderedTransactions = result.getReqDataListList();
        assertEquals(2, reorderedTransactions.size(), "There should be 2 transactions.");
        assertEquals(1, reorderedTransactions.get(0).getRequestNum(), "T1 should be first.");
        assertEquals(2, reorderedTransactions.get(1).getRequestNum(), "T2 should be second.");
        for (RequestData tx : reorderedTransactions) {
            assertFalse(tx.getIsTnxValid(), "No transactions should be aborted.");
        }
    }

    @Test
    public void testTransactionsWithReadAfterWriteConflicts() {
        // Create transactions
        RequestData t1 = RequestData.newBuilder()
                .setRequestNum(1)
                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A
                .build();

        RequestData t2 = RequestData.newBuilder()
                .setRequestNum(2)
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .build();

        List<RequestData> transactions = Arrays.asList(t1, t2);

        // Process transactions with early abort and reordering
        XOVGraph xovGraph = new XOVGraph();
        RequestDataList result = xovGraph.processXOVReordering(transactions, true, true);

        // Verify that transactions remain in original order and are not aborted
        List<RequestData> reorderedTransactions = result.getReqDataListList();
        assertEquals(2, reorderedTransactions.size(), "There should be 2 transactions.");
        assertEquals(2, reorderedTransactions.get(1).getRequestNum(), "T1 should be first.");
        assertEquals(1, reorderedTransactions.get(0).getRequestNum(), "T2 should be second.");
        for (RequestData tx : reorderedTransactions) {
            assertFalse(tx.getIsTnxValid(), "No transactions should be aborted.");
        }
    }

    @Test
    public void testMultipleCyclesWithSharedTransactions() {
        // Create transactions
        RequestData t1 = RequestData.newBuilder()
                .setRequestNum(1)
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .build();

        RequestData t2 = RequestData.newBuilder()
                .setRequestNum(2)
                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A
                .addWriteSet(OperationSet.newBuilder().setRecord(2).build()) // Writes B
                .build();

        RequestData t3 = RequestData.newBuilder()
                .setRequestNum(3)
                .addReadSet(OperationSet.newBuilder().setRecord(2).build()) // Reads B
                .addWriteSet(OperationSet.newBuilder().setRecord(3).build()) // Writes C
                .build();

        RequestData t4 = RequestData.newBuilder()
                .setRequestNum(4)
                .addReadSet(OperationSet.newBuilder().setRecord(3).build()) // Reads C
                .addWriteSet(OperationSet.newBuilder().setRecord(4).build()) // Writes D
                .build();

        RequestData t5 = RequestData.newBuilder()
                .setRequestNum(5)
                .addReadSet(OperationSet.newBuilder().setRecord(4).build()) // Reads D
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .build();

        List<RequestData> transactions = Arrays.asList(t1, t2, t3, t4, t5);

        // Process transactions with early abort and reordering
        XOVGraph xovGraph = new XOVGraph();
        RequestDataList result = xovGraph.processXOVReordering(transactions, true, true);

        // Verify that all transactions are aborted
        List<RequestData> reorderedTransactions = result.getReqDataListList();
        assertEquals(5, reorderedTransactions.size(), "There should be 5 transactions.");

        long abortedCount = reorderedTransactions.stream()
                .filter(tx -> !tx.getIsTnxValid())
                .count();
        assertEquals(5, abortedCount, "All transactions should be aborted due to cycles.");

        // Ensure that there are no executed transactions
        List<RequestData> executedTransactions = reorderedTransactions.stream()
                .filter(tx -> tx.getIsTnxValid())
                .collect(Collectors.toList());
        assertEquals(0, executedTransactions.size(), "No transactions should be executed.");
    }

    @Test
    public void testDisconnectedCyclesInConflictGraph() {
        // Create transactions for Cycle 1
        RequestData t1 = RequestData.newBuilder()
                .setRequestNum(1)
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .build();

        RequestData t2 = RequestData.newBuilder()
                .setRequestNum(2)
                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A
                .addWriteSet(OperationSet.newBuilder().setRecord(2).build()) // Writes B
                .build();

        RequestData t3 = RequestData.newBuilder()
                .setRequestNum(3)
                .addReadSet(OperationSet.newBuilder().setRecord(2).build()) // Reads B
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .build();

        // Create transactions for Cycle 2
        RequestData t4 = RequestData.newBuilder()
                .setRequestNum(4)
                .addWriteSet(OperationSet.newBuilder().setRecord(3).build()) // Writes C
                .build();

        RequestData t5 = RequestData.newBuilder()
                .setRequestNum(5)
                .addReadSet(OperationSet.newBuilder().setRecord(3).build()) // Reads C
                .addWriteSet(OperationSet.newBuilder().setRecord(4).build()) // Writes D
                .build();

        RequestData t6 = RequestData.newBuilder()
                .setRequestNum(6)
                .addReadSet(OperationSet.newBuilder().setRecord(4).build()) // Reads D
                .addWriteSet(OperationSet.newBuilder().setRecord(3).build()) // Writes C
                .build();

        List<RequestData> transactions = Arrays.asList(t1, t2, t3, t4, t5, t6);

        // Process transactions with early abort and reordering
        XOVGraph xovGraph = new XOVGraph();
        RequestDataList result = xovGraph.processXOVReordering(transactions, true, true);

        // Verify that all transactions are aborted
        List<RequestData> reorderedTransactions = result.getReqDataListList();
        assertEquals(6, reorderedTransactions.size(), "There should be 6 transactions.");

        long abortedCount = reorderedTransactions.stream()
                .filter(tx -> !tx.getIsTnxValid())
                .count();
        assertEquals(6, abortedCount, "All transactions should be aborted due to cycles.");

        // Ensure that there are no executed transactions
        List<RequestData> executedTransactions = reorderedTransactions.stream()
                .filter(tx -> tx.getIsTnxValid())
                .collect(Collectors.toList());
        assertEquals(0, executedTransactions.size(), "No transactions should be executed.");
    }

    @Test
    public void testTransactionsWithConflictingReadWritePatterns() {
        // Create transactions
        RequestData t1 = RequestData.newBuilder()
                .setRequestNum(1)
                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A
                .addWriteSet(OperationSet.newBuilder().setRecord(2).build()) // Writes B
                .build();

        RequestData t2 = RequestData.newBuilder()
                .setRequestNum(2)
                .addReadSet(OperationSet.newBuilder().setRecord(2).build()) // Reads B
                .addWriteSet(OperationSet.newBuilder().setRecord(3).build()) // Writes C
                .build();

        RequestData t3 = RequestData.newBuilder()
                .setRequestNum(3)
                .addReadSet(OperationSet.newBuilder().setRecord(3).build()) // Reads C
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .build();

        List<RequestData> transactions = Arrays.asList(t1, t2, t3);

        // Process transactions with early abort and reordering
        XOVGraph xovGraph = new XOVGraph();
        RequestDataList result = xovGraph.processXOVReordering(transactions, false, true);

        // Verify that all transactions are aborted
        List<RequestData> reorderedTransactions = result.getReqDataListList();
        assertEquals(3, reorderedTransactions.size(), "There should be 3 transactions.");

        long abortedCount = reorderedTransactions.stream()
                .filter(tx -> !tx.getIsTnxValid())
                .count();
        assertEquals(3, abortedCount, "All transactions should be aborted due to cycle.");
    }
//    @Test
//    public void testChainOfTransactionsWithMixedConflicts() {
//        // Create transactions
//        RequestData t1 = RequestData.newBuilder()
//                .setRequestNum(1)
//                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
//                .setIsTnxValid(true) // Mark as valid initially
//                .build();
//
//        RequestData t2 = RequestData.newBuilder()
//                .setRequestNum(2)
//                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A
//                .addWriteSet(OperationSet.newBuilder().setRecord(2).build()) // Writes B
//                .setIsTnxValid(true)
//                .build();
//
//        RequestData t3 = RequestData.newBuilder()
//                .setRequestNum(3)
//                .addReadSet(OperationSet.newBuilder().setRecord(2).build()) // Reads B
//                .addWriteSet(OperationSet.newBuilder().setRecord(3).build()) // Writes C
//                .setIsTnxValid(true)
//                .build();
//
//        RequestData t4 = RequestData.newBuilder()
//                .setRequestNum(4)
//                .addReadSet(OperationSet.newBuilder().setRecord(3).build()) // Reads C
//                .addWriteSet(OperationSet.newBuilder().setRecord(4).build()) // Writes D
//                .setIsTnxValid(true)
//                .build();
//
//        RequestData t5 = RequestData.newBuilder()
//                .setRequestNum(5)
//                .addReadSet(OperationSet.newBuilder().setRecord(4).build()) // Reads D
//                .addWriteSet(OperationSet.newBuilder().setRecord(5).build()) // Writes E
//                .setIsTnxValid(true)
//                .build();
//
//        RequestData t6 = RequestData.newBuilder()
//                .setRequestNum(6)
//                .addReadSet(OperationSet.newBuilder().setRecord(5).build()) // Reads E
//                .setIsTnxValid(true)
//                .build();
//
//        List<RequestData> transactions = Arrays.asList(t1, t2, t3, t4, t5, t6);
//
//        // Process transactions with early abort and reordering
//        XOVGraph xovGraph = new XOVGraph();
//        RequestDataList result = xovGraph.processXOVReordering(transactions, true, true);
//
//        // Verify that all transactions are executed in correct order
//        List<RequestData> reorderedTransactions = result.getReqDataListList();
//        assertEquals(6, reorderedTransactions.size(), "There should be 6 transactions.");
//
//        // Expected execution order: T1 → T2 → T3 → T4 → T5 → T6
//        int[] expectedOrder = {1, 2, 3, 4, 5, 6};
//        for (int i = 0; i < reorderedTransactions.size(); i++) {
//            RequestData tx = reorderedTransactions.get(i);
//            assertEquals(i + 1, tx.getRequestNum(), "Transaction order should follow dependencies.");
//            assertTrue(tx.getIsTnxValid(), "No transactions should be aborted.");
//        }
//    }
    @Test
    public void testEarlyAbortWithRAWConflict() {
        // Create transactions
        RequestData t1 = RequestData.newBuilder()
                .setRequestNum(1)
                .setIsTnxValid(true)
                .addWriteSet(OperationSet.newBuilder().setRecord(1).build()) // Writes A
                .build();

        RequestData t2 = RequestData.newBuilder()
                .setRequestNum(2)
                .setIsTnxValid(true)
                .addReadSet(OperationSet.newBuilder().setRecord(1).build()) // Reads A
                .build();

        List<RequestData> transactions = Arrays.asList(t1, t2);

        // Process transactions with early abort and reordering
        XOVGraph xovGraph = new XOVGraph();
        RequestDataList result = xovGraph.processXOVReordering(transactions, true, false);

        // Verify that T2 is aborted due to RAW conflict
        List<RequestData> reorderedTransactions = result.getReqDataListList();
        assertEquals(2, reorderedTransactions.size(), "There should be 2 transactions.");

        RequestData t1Result = reorderedTransactions.get(0);
        RequestData t2Result = reorderedTransactions.get(1);

        assertEquals(1, t1Result.getRequestNum(), "T1 should be executed.");
        assertEquals(2, t2Result.getRequestNum(), "T2 should be aborted.");

        assertTrue(t1Result.getIsTnxValid(), "T1 should not be aborted.");
        assertFalse(t2Result.getIsTnxValid(), "T2 should be aborted due to RAW conflict.");
    }

//    t1 r(1) w(3) ---
//    t2 w(1)
//        t3 r(2)
//            t4 w(3) r(1)
@Test
public void testPraneeth() {
    // Create transactions
    RequestData t1 = RequestData.newBuilder()
            .setRequestNum(1)
            .addReadSet(OperationSet.newBuilder().setRecord(1).build())
            .addWriteSet(OperationSet.newBuilder().setRecord(3).build())
            .setIsTnxValid(true)
            .build();

    RequestData t2 = RequestData.newBuilder()
            .setRequestNum(2)
            .addReadSet(OperationSet.newBuilder().setRecord(2).build())
            .addWriteSet(OperationSet.newBuilder().setRecord(1).build())
            .setIsTnxValid(true)
            .build();

    RequestData t3 = RequestData.newBuilder()
            .setRequestNum(3)
            .addReadSet(OperationSet.newBuilder().setRecord(3).build())
            .addWriteSet(OperationSet.newBuilder().setRecord(2).build())
            .setIsTnxValid(true)
            .build();

    RequestData t4 = RequestData.newBuilder()
            .setRequestNum(4)
            .addReadSet(OperationSet.newBuilder().setRecord(1).build())
            .addWriteSet(OperationSet.newBuilder().setRecord(3).build())
            .setIsTnxValid(true)
            .build();

    List<RequestData> transactions = Arrays.asList(t1, t2, t3, t4);

    // Process transactions with early abort and reordering
    XOVGraph xovGraph = new XOVGraph();
    RequestDataList result = xovGraph.processXOVReordering(transactions, false, true);

    System.out.println(result);
}



}
