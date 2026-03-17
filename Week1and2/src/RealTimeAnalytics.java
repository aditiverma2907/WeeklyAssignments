import java.util.*;
import java.util.concurrent.*;

public class RealTimeAnalytics {

    // pageUrl -> total visits
    private ConcurrentHashMap<String, Integer> pageViews;

    // pageUrl -> unique users
    private ConcurrentHashMap<String, Set<String>> uniqueVisitors;

    // source -> count
    private ConcurrentHashMap<String, Integer> sourceCount;

    public RealTimeAnalytics() {
        pageViews = new ConcurrentHashMap<>();
        uniqueVisitors = new ConcurrentHashMap<>();
        sourceCount = new ConcurrentHashMap<>();

        startDashboardUpdater();
    }

    // Event class
    static class Event {
        String url;
        String userId;
        String source;

        Event(String url, String userId, String source) {
            this.url = url;
            this.userId = userId;
            this.source = source;
        }
    }

    // Process incoming event (O(1))
    public void processEvent(Event event) {

        // Update page views
        pageViews.merge(event.url, 1, Integer::sum);

        // Update unique visitors
        uniqueVisitors.putIfAbsent(event.url, ConcurrentHashMap.newKeySet());
        uniqueVisitors.get(event.url).add(event.userId);

        // Update source count
        sourceCount.merge(event.source, 1, Integer::sum);
    }

    // Get top 10 pages
    private List<Map.Entry<String, Integer>> getTopPages() {
        PriorityQueue<Map.Entry<String, Integer>> minHeap =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > 10) {
                minHeap.poll();
            }
        }

        List<Map.Entry<String, Integer>> result = new ArrayList<>(minHeap);
        result.sort((a, b) -> b.getValue() - a.getValue()); // descending
        return result;
    }

    // Display dashboard
    public void getDashboard() {
        System.out.println("\n===== REAL-TIME DASHBOARD =====");

        List<Map.Entry<String, Integer>> topPages = getTopPages();

        int rank = 1;
        for (Map.Entry<String, Integer> entry : topPages) {
            String url = entry.getKey();
            int views = entry.getValue();
            int unique = uniqueVisitors.get(url).size();

            System.out.println(rank + ". " + url +
                    " - " + views + " views (" + unique + " unique)");
            rank++;
        }

        System.out.println("\nTraffic Sources:");
        for (Map.Entry<String, Integer> entry : sourceCount.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    // Auto update dashboard every 5 seconds
    private void startDashboardUpdater() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            getDashboard();
        }, 5, 5, TimeUnit.SECONDS);
    }

    // MAIN METHOD (Simulation)
    public static void main(String[] args) throws InterruptedException {

        RealTimeAnalytics analytics = new RealTimeAnalytics();

        String[] urls = {
                "/article/breaking-news",
                "/sports/championship",
                "/tech/ai",
                "/health/tips"
        };

        String[] sources = {"google", "facebook", "direct", "twitter"};

        Random rand = new Random();

        // Simulate 1000 events
        for (int i = 0; i < 1000; i++) {
            String url = urls[rand.nextInt(urls.length)];
            String user = "user_" + rand.nextInt(500);
            String source = sources[rand.nextInt(sources.length)];

            analytics.processEvent(new Event(url, user, source));

            Thread.sleep(5); // simulate stream
        }

        // Keep program running to see dashboard updates
        Thread.sleep(20000);
    }
}
