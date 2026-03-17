import java.util.*;

public class TransactionAnalyzer {

    // Transaction class
    static class Transaction {
        int id;
        int amount;
        String merchant;
        String account;
        long time; // store as epoch millis

        Transaction(int id, int amount, String merchant, String account, long time) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.account = account;
            this.time = time;
        }
    }

    // ------------------- 1. CLASSIC TWO-SUM -------------------
    public static List<int[]> findTwoSum(List<Transaction> txns, int target) {
        Map<Integer, Transaction> map = new HashMap<>();
        List<int[]> result = new ArrayList<>();

        for (Transaction t : txns) {
            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                result.add(new int[]{map.get(complement).id, t.id});
            }
            map.put(t.amount, t);
        }
        return result;
    }

    // ------------------- 2. TWO-SUM WITH TIME WINDOW -------------------
    public static List<int[]> findTwoSumWithTime(List<Transaction> txns, int target, long windowMillis) {
        List<int[]> result = new ArrayList<>();
        Map<Integer, List<Transaction>> map = new HashMap<>();

        for (Transaction t : txns) {
            int complement = target - t.amount;

            if (map.containsKey(complement)) {
                for (Transaction prev : map.get(complement)) {
                    if (Math.abs(t.time - prev.time) <= windowMillis) {
                        result.add(new int[]{prev.id, t.id});
                    }
                }
            }

            map.putIfAbsent(t.amount, new ArrayList<>());
            map.get(t.amount).add(t);
        }
        return result;
    }

    // ------------------- 3. K-SUM -------------------
    public static List<List<Integer>> findKSum(List<Transaction> txns, int k, int target) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(txns, k, target, 0, new ArrayList<>(), result);
        return result;
    }

    private static void backtrack(List<Transaction> txns, int k, int target, int start,
                                  List<Integer> current, List<List<Integer>> result) {

        if (k == 0 && target == 0) {
            result.add(new ArrayList<>(current));
            return;
        }

        if (k < 0 || target < 0) return;

        for (int i = start; i < txns.size(); i++) {
            Transaction t = txns.get(i);

            current.add(t.id);
            backtrack(txns, k - 1, target - t.amount, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    // ------------------- 4. DUPLICATE DETECTION -------------------
    public static List<String> detectDuplicates(List<Transaction> txns) {
        Map<String, Set<String>> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : txns) {
            String key = t.amount + "_" + t.merchant;

            map.putIfAbsent(key, new HashSet<>());
            map.get(key).add(t.account);
        }

        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.add("Duplicate: " + entry.getKey() +
                        " Accounts: " + entry.getValue());
            }
        }
        return result;
    }

    // ------------------- MAIN METHOD -------------------
    public static void main(String[] args) {

        List<Transaction> txns = new ArrayList<>();

        txns.add(new Transaction(1, 500, "StoreA", "acc1", System.currentTimeMillis()));
        txns.add(new Transaction(2, 300, "StoreB", "acc2", System.currentTimeMillis()));
        txns.add(new Transaction(3, 200, "StoreC", "acc3", System.currentTimeMillis()));
        txns.add(new Transaction(4, 500, "StoreA", "acc4", System.currentTimeMillis()));

        // 1. Two Sum
        System.out.println("Two Sum:");
        for (int[] pair : findTwoSum(txns, 500)) {
            System.out.println(pair[0] + ", " + pair[1]);
        }

        // 2. Two Sum with Time Window (1 hour)
        System.out.println("\nTwo Sum with Time Window:");
        long oneHour = 60 * 60 * 1000;
        for (int[] pair : findTwoSumWithTime(txns, 500, oneHour)) {
            System.out.println(pair[0] + ", " + pair[1]);
        }

        // 3. K-Sum
        System.out.println("\nK-Sum:");
        List<List<Integer>> ksum = findKSum(txns, 3, 1000);
        for (List<Integer> list : ksum) {
            System.out.println(list);
        }

        // 4. Duplicate Detection
        System.out.println("\nDuplicates:");
        for (String s : detectDuplicates(txns)) {
            System.out.println(s);
        }
    }
}