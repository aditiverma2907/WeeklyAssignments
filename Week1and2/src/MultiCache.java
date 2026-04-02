import java.util.*;

public class MultiCache {

    // ----------- LRU CACHE IMPLEMENTATION -----------
    static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private int capacity;

        public LRUCache(int capacity) {
            super(capacity, 0.75f, true); // access-order
            this.capacity = capacity;
        }

        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    // ----------- VIDEO DATA -----------
    static class Video {
        String id;
        String content;

        Video(String id, String content) {
            this.id = id;
            this.content = content;
        }
    }

    // ----------- CACHE LEVELS -----------
    private LRUCache<String, Video> L1; // memory
    private LRUCache<String, Video> L2; // SSD
    private Map<String, Video> L3;      // database

    // Access count for promotion
    private Map<String, Integer> accessCount;

    // Stats
    private int l1Hits = 0, l2Hits = 0, l3Hits = 0;
    private int l1Miss = 0, l2Miss = 0;

    private final int PROMOTION_THRESHOLD = 2;

    public MultiCache() {
        L1 = new LRUCache<>(10000);
        L2 = new LRUCache<>(100000);
        L3 = new HashMap<>();
        accessCount = new HashMap<>();

        // preload database
        for (int i = 1; i <= 200000; i++) {
            String id = "video_" + i;
            L3.put(id, new Video(id, "Content of " + id));
        }
    }

    // ----------- GET VIDEO -----------
    public String getVideo(String videoId) {
        long start = System.currentTimeMillis();

        // L1 check
        if (L1.containsKey(videoId)) {
            l1Hits++;
            return "L1 HIT → " + videoId + " (0.5ms)";
        } else {
            l1Miss++;
        }

        // L2 check
        if (L2.containsKey(videoId)) {
            l2Hits++;
            Video v = L2.get(videoId);

            // promote to L1
            promoteToL1(videoId, v);

            return "L1 MISS → L2 HIT → Promoted (" + videoId + ")";
        } else {
            l2Miss++;
        }

        // L3 (DB)
        if (L3.containsKey(videoId)) {
            l3Hits++;

            Video v = L3.get(videoId);

            // add to L2 first
            L2.put(videoId, v);
            accessCount.put(videoId, 1);

            return "L1 MISS → L2 MISS → L3 HIT (" + videoId + ")";
        }

        return "Video not found";
    }

    // ----------- PROMOTION LOGIC -----------
    private void promoteToL1(String videoId, Video v) {
        int count = accessCount.getOrDefault(videoId, 0) + 1;
        accessCount.put(videoId, count);

        if (count >= PROMOTION_THRESHOLD) {
            L1.put(videoId, v);
        }
    }

    // ----------- INVALIDATE CACHE -----------
    public void invalidate(String videoId) {
        L1.remove(videoId);
        L2.remove(videoId);
        L3.remove(videoId);
        accessCount.remove(videoId);
    }

    // ----------- STATISTICS -----------
    public void getStatistics() {
        int total = l1Hits + l2Hits + l3Hits;

        System.out.println("\n===== CACHE STATS =====");

        System.out.println("L1 Hit Rate: " +
                (l1Hits * 100.0 / total) + "%");

        System.out.println("L2 Hit Rate: " +
                (l2Hits * 100.0 / total) + "%");

        System.out.println("L3 Hit Rate: " +
                (l3Hits * 100.0 / total) + "%");

        System.out.println("Overall Hit Rate: " +
                ((l1Hits + l2Hits) * 100.0 / total) + "%");
    }

    // ----------- MAIN METHOD (DEMO) -----------
    public static void main(String[] args) {

        MultiCache cache = new MultiCache();

        // First access → L3
        System.out.println(cache.getVideo("video_123"));

        // Second → L2
        System.out.println(cache.getVideo("video_123"));

        // Third → L1 (after promotion)
        System.out.println(cache.getVideo("video_123"));

        // Another video
        System.out.println(cache.getVideo("video_999"));

        // Stats
        cache.getStatistics();
    }
}