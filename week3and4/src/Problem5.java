import java.util.*;

public class Problem5 {

    static int linearSearch(String[] arr, String target) {
        for (int i = 0; i < arr.length; i++)
            if (arr[i].equals(target)) return i;
        return -1;
    }

    static int binarySearch(String[] arr, String target) {
        int l = 0, r = arr.length - 1;

        while (l <= r) {
            int m = (l + r) / 2;

            if (arr[m].equals(target)) return m;
            else if (arr[m].compareTo(target) < 0) l = m + 1;
            else r = m - 1;
        }
        return -1;
    }

    public static void main(String[] args) {
        String[] arr = {"accA", "accB", "accB", "accC"};

        System.out.println("Linear: " + linearSearch(arr, "accB"));
        System.out.println("Binary: " + binarySearch(arr, "accB"));
    }
}
