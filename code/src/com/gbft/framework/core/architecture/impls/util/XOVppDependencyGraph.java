package com.gbft.framework.core.architecture.impls.util;

import com.gbft.framework.data.RequestData;

import java.util.*;

public class XOVppDependencyGraph{
    // Dependency Graph : XOVpp Architecture

    private Map<RequestData, List<RequestData>> graph = new HashMap<>();

    public List<RequestData> orderedBlock = null;
    public boolean success = true;

    public XOVppDependencyGraph(List<RequestData> block) {
        try{

            for (RequestData t : block) {
                this.addTransaction(t);
            }

            this.constructGraph(block);
            this.abortCycles();

            this.orderedBlock = this.getOrderedTransactions();
        }catch(Exception e){
            success = false;
        }
        finally {
            if(!success){
                this.orderedBlock = block;
            }
        }

    }

    public List<RequestData> getOrderedBlock(){
        return this.orderedBlock;
    }

    public void addTransaction(RequestData request){
        graph.put(request, new ArrayList<>(0));
    }

    public void addDependency(RequestData request, RequestData dependency){
        graph.get(request).add(dependency);
    }

    public void constructGraph(List<RequestData> block){
        Map<Integer, RequestData> senderMap = new HashMap<>();
        Map<Integer, RequestData> receiverMap = new HashMap<>();

        for (RequestData t : block) {
            senderMap.put(t.getSender(), t);
            receiverMap.put(t.getReceiver(), t);
        }

        for (RequestData t : block) {
            if (senderMap.containsKey(t.getReceiver())) {
                addDependency(t, senderMap.get(t.getReceiver()));
            }
            if (receiverMap.containsKey(t.getSender())) {
                addDependency(receiverMap.get(t.getSender()), t);
            }
        }
    }

    public void abortCycles() {
        Set<RequestData> visited = new HashSet<>();
        Set<RequestData> inStack = new HashSet<>();
        Stack<RequestData> stack = new Stack<>();

        for (RequestData transaction : graph.keySet()) {
            if (!visited.contains(transaction)) {
                if (detectAndAbortCycle(transaction, visited, inStack, stack)) {
                    graph.remove(transaction);
                }
            }
        }
    }

    private boolean detectAndAbortCycle(RequestData transaction, Set<RequestData> visited, Set<RequestData> inStack, Stack<RequestData> stack) {
        visited.add(transaction);
        inStack.add(transaction);
        stack.push(transaction);

        for (RequestData neighbor : graph.get(transaction)) {
            if (!visited.contains(neighbor) && detectAndAbortCycle(neighbor, visited, inStack, stack)) {
                return true;
            } else if (inStack.contains(neighbor)) {
                return true;
            }
        }

        stack.pop();
        inStack.remove(transaction);
        return false;
    }

    public List<RequestData> getOrderedTransactions() {
        List<RequestData> orderedTransactions = new ArrayList<>();
        Set<RequestData> visited = new HashSet<>();

        for (RequestData transaction : graph.keySet()) {
            if (!visited.contains(transaction)) {
                topologicalSort(transaction, visited, orderedTransactions);
            }
        }

        Collections.reverse(orderedTransactions);
        return orderedTransactions;
    }

    private void topologicalSort(RequestData transaction, Set<RequestData> visited, List<RequestData> orderedTransactions) {
        visited.add(transaction);
        for (RequestData neighbor : graph.get(transaction)) {
            if (!visited.contains(neighbor)) {
                topologicalSort(neighbor, visited, orderedTransactions);
            }
        }
        orderedTransactions.add(transaction);
    }


}
