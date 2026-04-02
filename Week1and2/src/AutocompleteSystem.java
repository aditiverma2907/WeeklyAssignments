import java.util.*;

public class AutocompleteSystem {

    // Trie Node
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        Map<String, Integer> frequencyMap = new HashMap<>();
    }

    private TrieNode root;
    private Map<String, Integer> globalFrequency;

    public AutocompleteSystem() {
        root = new TrieNode();
        globalFrequency = new HashMap<>();
    }

    // Insert query into Trie
    public void insert(String query) {
        globalFrequency.put(query,
                globalFrequency.getOrDefault(query, 0) + 1);

        TrieNode node = root;

        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);

            // Store frequency at each prefix node
            node.frequencyMap.put(query, globalFrequency.get(query));
        }
    }

    // Search top 10 suggestions
    public List<String> search(String prefix) {
        TrieNode node = root;

        // Traverse Trie
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return handleTypo(prefix); // fallback
            }
            node = node.children.get(c);
        }

        // Get top 10 using min heap
        PriorityQueue<Map.Entry<String, Integer>> pq =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        for (Map.Entry<String, Integer> entry : node.frequencyMap.entrySet()) {
            pq.offer(entry);
            if (pq.size() > 10) {
                pq.poll();
            }
        }

        List<String> result = new ArrayList<>();
        while (!pq.isEmpty()) {
            result.add(pq.poll().getKey() +
                    " (" + pq.peek().getValue() + ")");
        }

        Collections.reverse(result);
        return result;
    }

    // Update frequency
    public void updateFrequency(String query) {
        insert(query);
    }

    // Basic typo handling (prefix trimming)
    private List<String> handleTypo(String prefix) {
        if (prefix.length() <= 1) return new ArrayList<>();

        return search(prefix.substring(0, prefix.length() - 1));
    }

    // MAIN METHOD (Demo)
    public static void main(String[] args) {

        AutocompleteSystem system = new AutocompleteSystem();

        // Insert queries
        system.insert("java tutorial");
        system.insert("javascript");
        system.insert("java download");
        system.insert("java tutorial");
        system.insert("java 21 features");

        // Search
        System.out.println("Suggestions for 'jav':");
        List<String> results = system.search("jav");

        for (int i = 0; i < results.size(); i++) {
            System.out.println((i + 1) + ". " + results.get(i));
        }

        // Update frequency
        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");

        System.out.println("\nAfter updating frequency:");
        System.out.println(system.search("jav"));
    }
}
