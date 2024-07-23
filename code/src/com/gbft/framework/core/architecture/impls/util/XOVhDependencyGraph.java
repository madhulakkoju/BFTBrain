package com.gbft.framework.core.architecture.impls.util;

import com.gbft.framework.data.RequestData;

import java.util.*;

public class XOVhDependencyGraph {

    private Map<RequestData, List<RequestData>> graph = new HashMap<>();
    private Set<RequestData> droppedRequests = new HashSet<>();
    public boolean success = true;

    public void addRequest(RequestData request) {
        if (!graph.containsKey(request)) {
            graph.put(request, new ArrayList<>());
        }
    }

    public void addDependency(RequestData from, RequestData to) {
        graph.get(from).add(to);
    }

    public void constructGraph(List<RequestData> requests) {
        Map<Integer, RequestData> senderMap = new HashMap<>();
        Map<Integer, RequestData> receiverMap = new HashMap<>();

        for (RequestData r : requests) {
            senderMap.put(r.getSender(), r);
            receiverMap.put(r.getReceiver(), r);
        }

        for (RequestData r : requests) {
            if (senderMap.containsKey(r.getReceiver())) {
                addDependency(r, senderMap.get(r.getReceiver()));
                if (detectCycle(r)) {
                    dropRequest(r);
                }
            }
            if (receiverMap.containsKey(r.getSender())) {
                addDependency(receiverMap.get(r.getSender()), r);
                if (detectCycle(receiverMap.get(r.getSender()))) {
                    dropRequest(receiverMap.get(r.getSender()));
                }
            }
        }
    }

    private void dropRequest(RequestData request) {
        graph.remove(request);
        droppedRequests.add(request);
        for (List<RequestData> dependencies : graph.values()) {
            dependencies.remove(request);
        }
    }

    private boolean detectCycle(RequestData request) {
        Set<RequestData> visited = new HashSet<>();
        Stack<RequestData> stack = new Stack<>();
        return detectCycleHelper(request, visited, stack);
    }

    private boolean detectCycleHelper(RequestData current, Set<RequestData> visited, Stack<RequestData> stack) {
        if (stack.contains(current)) {
            return true;
        }
        if (visited.contains(current)) {
            return false;
        }
        visited.add(current);
        stack.push(current);
        for (RequestData neighbor : graph.get(current)) {
            if (detectCycleHelper(neighbor, visited, stack)) {
                return true;
            }
        }
        stack.pop();
        return false;
    }

    public List<RequestData> getOrderedRequests() {
        List<RequestData> orderedRequests = new ArrayList<>();
        Set<RequestData> visited = new HashSet<>();

        for (RequestData request : graph.keySet()) {
            if (!visited.contains(request)) {
                topologicalSort(request, visited, orderedRequests);
            }
        }

        Collections.reverse(orderedRequests);
        return orderedRequests;
    }

    private void topologicalSort(RequestData request, Set<RequestData> visited, List<RequestData> orderedRequests) {
        visited.add(request);
        for (RequestData neighbor : graph.get(request)) {
            if (!visited.contains(neighbor)) {
                topologicalSort(neighbor, visited, orderedRequests);
            }
        }
        orderedRequests.add(request);
    }

    public XOVhDependencyGraph(List<RequestData> block) {

        try {

            for (RequestData request : block) {
                addRequest(request);
            }
            constructGraph(block);
        }
        catch (Exception e) {
            success = false;
        }

    }

}
