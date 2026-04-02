import java.util.*;

public class Dnscache {

    // DNS Entry class
    static class DNSEntry {
        String domain;
        String ipAddress;
        long expiryTime;

        DNSEntry(String domain, String ipAddress, long ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + (ttlSeconds * 1000);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    // LRU Cache using LinkedHashMap
    private final int MAX_SIZE;
    private LinkedHashMap<String, DNSEntry> cache;

    // Stats
    private int hits = 0;
    private int misses = 0;

    public Dnscache(int capacity) {
        this.MAX_SIZE = capacity;

        cache = new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > MAX_SIZE;
            }
        };

        startCleanupThread();
    }

    // Resolve domain
    public synchronized String resolve(String domain) {
        long startTime = System.nanoTime();

        DNSEntry entry = cache.get(domain);

        if (entry != null) {
            if (!entry.isExpired()) {
                hits++;
                long time = System.nanoTime() - startTime;
                return "Cache HIT → " + entry.ipAddress + " (" + time / 1_000_000.0 + " ms)";
            } else {
                cache.remove(domain);
            }
        }

        // Cache miss → fetch from upstream
        misses++;
        String newIP = queryUpstreamDNS(domain);

        // Assume TTL = 5 seconds (demo)
        cache.put(domain, new DNSEntry(domain, newIP, 5));

        return "Cache MISS → " + newIP + " (fetched from upstream)";
    }

    // Simulated upstream DNS call
    private String queryUpstreamDNS(String domain) {
        try {
            Thread.sleep(100); // simulate 100ms delay
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Generate fake IP
        return "172." + (int)(Math.random() * 255) + "." +
                (int)(Math.random() * 255) + "." +
                (int)(Math.random() * 255);
    }

    // Background cleanup thread
    private void startCleanupThread() {
        Thread cleaner = new Thread(() -> {
            while (true) {
                synchronized (this) {
                    Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();

                    while (it.hasNext()) {
                        Map.Entry<String, DNSEntry> entry = it.next();
                        if (entry.getValue().isExpired()) {
                            it.remove();
                        }
                    }
                }

                try {
                    Thread.sleep(2000); // cleanup every 2 sec
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        cleaner.setDaemon(true);
        cleaner.start();
    }

    // Cache stats
    public String getCacheStats() {
        int total = hits + misses;
        double hitRate = total == 0 ? 0 : (hits * 100.0 / total);

        return "Hit Rate: " + String.format("%.2f", hitRate) + "% (Hits: " + hits + ", Misses: " + misses + ")";
    }

    // MAIN METHOD (Demo)
    public static void main(String[] args) throws InterruptedException {
        Dnscache dnsCache = new Dnscache(3);

        System.out.println(dnsCache.resolve("google.com"));
        System.out.println(dnsCache.resolve("google.com")); // HIT

        Thread.sleep(6000); // wait for TTL expiry

        System.out.println(dnsCache.resolve("google.com")); // EXPIRED → MISS

        // LRU test
        dnsCache.resolve("facebook.com");
        dnsCache.resolve("amazon.com");
        dnsCache.resolve("twitter.com"); // triggers eviction if needed

        System.out.println(dnsCache.getCacheStats());
    }
}
