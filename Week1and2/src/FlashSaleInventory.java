import java.util.concurrent.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FlashSaleInventory {

    // productId -> stock count
    private ConcurrentHashMap<String, AtomicInteger> stockMap;

    // productId -> waiting list (FIFO)
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Integer>> waitingListMap;

    public FlashSaleInventory() {
        stockMap = new ConcurrentHashMap<>();
        waitingListMap = new ConcurrentHashMap<>();
    }

    // Initialize product stock
    public void addProduct(String productId, int stock) {
        stockMap.put(productId, new AtomicInteger(stock));
        waitingListMap.put(productId, new ConcurrentLinkedQueue<>());
    }

    // Check stock (O(1))
    public String checkStock(String productId) {
        AtomicInteger stock = stockMap.get(productId);
        if (stock == null) return "Product not found";

        return stock.get() + " units available";
    }

    // Purchase item (thread-safe)
    public String purchaseItem(String productId, int userId) {
        AtomicInteger stock = stockMap.get(productId);

        if (stock == null) return "Product not found";

        while (true) {
            int currentStock = stock.get();

            // If stock available → try to decrement atomically
            if (currentStock > 0) {
                if (stock.compareAndSet(currentStock, currentStock - 1)) {
                    return "Success, " + (currentStock - 1) + " units remaining";
                }
            } else {
                // Add to waiting list
                ConcurrentLinkedQueue<Integer> queue = waitingListMap.get(productId);
                queue.add(userId);
                int position = queue.size();
                return "Added to waiting list, position #" + position;
            }
        }
    }

    // Get waiting list for debugging
    public List<Integer> getWaitingList(String productId) {
        return new ArrayList<>(waitingListMap.get(productId));
    }

    // MAIN METHOD (Simulation)
    public static void main(String[] args) throws InterruptedException {
        FlashSaleInventory manager = new FlashSaleInventory();

        String product = "IPHONE15_256GB";
        manager.addProduct(product, 100);

        System.out.println(manager.checkStock(product));

        // Simulate 200 concurrent users
        ExecutorService executor = Executors.newFixedThreadPool(50);

        for (int i = 1; i <= 200; i++) {
            int userId = i;

            executor.submit(() -> {
                String result = manager.purchaseItem(product, userId);
                System.out.println("User " + userId + ": " + result);
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("Final Stock: " + manager.checkStock(product));
        System.out.println("Waiting List Size: " + manager.getWaitingList(product).size());
    }
}
