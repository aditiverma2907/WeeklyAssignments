import java.util.*;

public class PlagiarismDetector {

    // n-gram size
    private static final int N = 5;

    // ngram -> set of document IDs
    private Map<String, Set<String>> index;

    // document -> its ngrams
    private Map<String, List<String>> documentNgrams;

    public PlagiarismDetector() {
        index = new HashMap<>();
        documentNgrams = new HashMap<>();
    }

    // Add document to database
    public void addDocument(String docId, String content) {
        List<String> ngrams = generateNgrams(content);
        documentNgrams.put(docId, ngrams);

        for (String ngram : ngrams) {
            index.putIfAbsent(ngram, new HashSet<>());
            index.get(ngram).add(docId);
        }
    }

    // Analyze a new document
    public void analyzeDocument(String docId, String content) {
        List<String> ngrams = generateNgrams(content);
        Map<String, Integer> matchCount = new HashMap<>();

        // Count matching n-grams
        for (String ngram : ngrams) {
            if (index.containsKey(ngram)) {
                for (String existingDoc : index.get(ngram)) {
                    matchCount.put(existingDoc,
                            matchCount.getOrDefault(existingDoc, 0) + 1);
                }
            }
        }

        System.out.println("Analyzing: " + docId);
        System.out.println("Extracted " + ngrams.size() + " n-grams");

        // Calculate similarity
        for (Map.Entry<String, Integer> entry : matchCount.entrySet()) {
            String existingDoc = entry.getKey();
            int matches = entry.getValue();

            int total = documentNgrams.get(existingDoc).size();
            double similarity = (matches * 100.0) / total;

            String status = similarity > 50 ? "PLAGIARISM DETECTED"
                    : similarity > 15 ? "Suspicious"
                    : "Safe";

            System.out.println("→ Found " + matches + " matches with " + existingDoc);
            System.out.printf("→ Similarity: %.2f%% (%s)\n", similarity, status);
        }
    }

    // Generate n-grams
    private List<String> generateNgrams(String text) {
        List<String> ngrams = new ArrayList<>();

        String[] words = text.toLowerCase().split("\\s+");

        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < N; j++) {
                sb.append(words[i + j]).append(" ");
            }
            ngrams.add(sb.toString().trim());
        }

        return ngrams;
    }

    // MAIN METHOD (Demo)
    public static void main(String[] args) {

        PlagiarismDetector detector = new PlagiarismDetector();

        // Existing documents
        detector.addDocument("essay_089",
                "machine learning is a method of data analysis that automates analytical model building");

        detector.addDocument("essay_092",
                "machine learning is a method of data analysis that automates analytical model building using algorithms");

        // New document
        String newEssay = "machine learning is a method of data analysis that automates analytical model building";

        detector.analyzeDocument("essay_123", newEssay);
    }
}