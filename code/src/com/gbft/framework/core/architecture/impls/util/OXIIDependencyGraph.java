package com.gbft.framework.core.architecture.impls.util;

import com.gbft.framework.core.Entity;
import com.gbft.framework.data.RequestData;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class OXIIDependencyGraph {
    private Map<RequestData, List<RequestData>> graph = new HashMap<>();
    private Map<RequestData, Integer> inDegree = new HashMap<>();

    private Entity entity;

    public boolean success;

    public List<RequestData> block;

    public OXIIDependencyGraph(Entity entity, List<RequestData> block) {

        try {
            this.entity = entity;

            for(RequestData request : block) {
                addRequest(request);
            }

            this.constructGraph(block);
            this.executeTransactionsInParallel();
            this.block = block;
            this.success = true;
        } catch (Exception e) {
            this.success = false;
        }

    }

    public void addRequest(RequestData request) {
        if (!graph.containsKey(request)) {
            graph.put(request, new ArrayList<>());
            inDegree.put(request, 0);
        }
    }

    public void addDependency(RequestData from, RequestData to) {
        graph.get(from).add(to);
        inDegree.put(to, inDegree.get(to) + 1);
    }

    public void constructGraph(List<RequestData> requests) {
        for (int i = 0; i < requests.size(); i++) {
            for (int j = i + 1; j < requests.size(); j++) {
                RequestData t1 = requests.get(i);
                RequestData t2 = requests.get(j);
                if (conflicts(t1, t2)) {
                    addDependency(t1, t2);
                }
            }
        }
    }

    private boolean conflicts(RequestData t1, RequestData t2) {
        return t1.getSender() == t2.getSender() || t1.getReceiver() == t2.getReceiver() ||
                t1.getSender() == t2.getReceiver() || t1.getReceiver() == t2.getSender();
    }

    public void executeTransactionsInParallel() {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Map<RequestData, Future<?>> futures = new HashMap<>();

        // Topological sort to find the order of execution
        Queue<RequestData> queue = new LinkedList<>();
        for (Map.Entry<RequestData, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        while (!queue.isEmpty()) {
            RequestData current = queue.poll();
            futures.put(current, executor.submit(() -> executeTransaction(current)));

            for (RequestData neighbor : graph.get(current)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void executeTransaction(RequestData request) {
        // Simulate transaction execution
        this.entity.getDataset().execute(request);
    }

}
