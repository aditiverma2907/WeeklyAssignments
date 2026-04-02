import java.util.*;

class Asset {
    String name;
    double returnRate;

    Asset(String name, double rate) {
        this.name = name;
        this.returnRate = rate;
    }

    public String toString() {
        return name + ":" + returnRate;
    }
}

public class Problem4 {

    public static void main(String[] args) {
        List<Asset> list = new ArrayList<>();
        list.add(new Asset("AAPL", 12));
        list.add(new Asset("TSLA", 8));
        list.add(new Asset("GOOG", 15));

        // Merge (using built-in stable sort)
        list.sort(Comparator.comparingDouble(a -> a.returnRate));
        System.out.println("Merge Sort: " + list);

        // Quick (desc)
        list.sort((a, b) -> Double.compare(b.returnRate, a.returnRate));
        System.out.println("Quick Sort Desc: " + list);
    }
}