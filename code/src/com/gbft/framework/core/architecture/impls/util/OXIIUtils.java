import java.util.*;
import java.util.concurrent.*;


public class OXIIUtils {
    private Map<RequestData, List<RequestData>> graph ;
    private Map<RequestData, Integer> inDegree;
    private Dataset dataset;

    private HashMap<Long, Integer> repliesMap;
private long seqNum;
    public OXIIUtils(long seqNum, Dataset dataset, HashMap<Long, Integer> replies){
        this.graph = new HashMap<>();
        this.inDegree = new HashMap<>();
        this.dataset = dataset;
        this.repliesMap = replies;

        this.seqNum = seqNum;

    }


    public void addRequest(RequestData request) {
        if (!graph.containsKey(request)) {
            graph.put(request, new ArrayList<>());
            inDegree.put(request, 0);
        }
    }

    // Method to add a dependency between two transactions
    public void addDependency(RequestData from, RequestData to) {
        graph.get(from).add(to);
        inDegree.put(to, inDegree.get(to) + 1);
    }

    // Method to construct a dependency graph based on transaction order and conflicts
    public void constructGraph(List<RequestData> requests) {

        for(RequestData r : requests){
            this.addRequest(r);
        }

        for (int i = 0; i < requests.size(); i++) {
            for (int j = i + 1; j < requests.size(); j++) {
                RequestData t1 = requests.get(i);
                RequestData t2 = requests.get(j);
                if (conflicts(t1, t2)) {
                    addDependency(t1, t2);  // Add edge t1 -> t2 if they conflict and t1 is ordered before t2
                }
            }
        }
    }


    private boolean conflicts(RequestData t1, RequestData t2) {
        return t1.getSender() == t2.getSender() || t1.getReceiver() == t2.getReceiver() ||
                t1.getSender() == t2.getReceiver() || t1.getReceiver() == t2.getSender();
    }


    public void executeTransactionsInParallel() {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() );
        Map<RequestData, Future<?>> futures = new HashMap<>();

        // Find the transactions that have no dependencies (inDegree == 0)
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

        // Await the completion of all tasks
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Simulated transaction execution
    private void executeTransaction(RequestData request) {

        repliesMap.put(request.getRequestNum(), dataset.execute(request));

    }


}
