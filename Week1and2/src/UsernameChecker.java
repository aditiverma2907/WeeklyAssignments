import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UsernameChecker {

    // Stores username -> userId
    private ConcurrentHashMap<String, Integer> userMap;

    // Stores username -> attempt count
    private ConcurrentHashMap<String, Integer> attemptMap;

    public UsernameChecker() {
        userMap = new ConcurrentHashMap<>();
        attemptMap = new ConcurrentHashMap<>();
    }

    // Register a user (for testing/demo)
    public void registerUser(String username, int userId) {
        userMap.put(username, userId);
    }

    // Check availability (O(1))
    public boolean checkAvailability(String username) {
        // Track attempt frequency
        attemptMap.put(username, attemptMap.getOrDefault(username, 0) + 1);

        return !userMap.containsKey(username);
    }

    // Suggest alternatives
    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();

        // If available, return itself
        if (checkAvailability(username)) {
            suggestions.add(username);
            return suggestions;
        }

        // Strategy 1: append numbers
        for (int i = 1; i <= 5; i++) {
            String newName = username + i;
            if (!userMap.containsKey(newName)) {
                suggestions.add(newName);
            }
        }

        // Strategy 2: replace '_' with '.'
        if (username.contains("_")) {
            String modified = username.replace("_", ".");
            if (!userMap.containsKey(modified)) {
                suggestions.add(modified);
            }
        }

        // Strategy 3: random suffix
        for (int i = 0; i < 3; i++) {
            String randomName = username + (int)(Math.random() * 1000);
            if (!userMap.containsKey(randomName)) {
                suggestions.add(randomName);
            }
        }

        return suggestions;
    }

    // Get most attempted username
    public String getMostAttempted() {
        String maxUser = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : attemptMap.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxUser = entry.getKey();
            }
        }

        return maxUser + " (" + maxCount + " attempts)";
    }

    // Main method for testing
    public static void main(String[] args) {
        UsernameChecker checker = new UsernameChecker();

        // Pre-existing users
        checker.registerUser("john_doe", 1);
        checker.registerUser("admin", 2);

        // Availability checks
        System.out.println("john_doe available? " + checker.checkAvailability("john_doe"));
        System.out.println("jane_smith available? " + checker.checkAvailability("jane_smith"));

        // Suggestions
        System.out.println("Suggestions for john_doe: " + checker.suggestAlternatives("john_doe"));

        // Simulate multiple attempts
        for (int i = 0; i < 100; i++) {
            checker.checkAvailability("admin");
        }

        // Most attempted username
        System.out.println("Most attempted: " + checker.getMostAttempted());
    }
}