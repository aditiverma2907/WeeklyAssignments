import java.util.*;

public class ParkingLot {

    // Spot status enum
    enum Status {
        EMPTY, OCCUPIED, DELETED
    }

    // Parking Slot
    static class Slot {
        String licensePlate;
        long entryTime;
        Status status;

        Slot() {
            this.status = Status.EMPTY;
        }
    }

    private Slot[] table;
    private int capacity;
    private int occupiedSpots = 0;

    // Stats
    private int totalProbes = 0;
    private int totalParkOperations = 0;
    private Map<Integer, Integer> hourlyUsage = new HashMap<>();

    public ParkingLot(int size) {
        this.capacity = size;
        table = new Slot[size];
        for (int i = 0; i < size; i++) {
            table[i] = new Slot();
        }
    }

    // Hash function
    private int hash(String license) {
        return Math.abs(license.hashCode()) % capacity;
    }

    // Park vehicle
    public String parkVehicle(String license) {
        int index = hash(license);
        int probes = 0;

        for (int i = 0; i < capacity; i++) {
            int current = (index + i) % capacity;

            if (table[current].status == Status.EMPTY ||
                    table[current].status == Status.DELETED) {

                table[current].licensePlate = license;
                table[current].entryTime = System.currentTimeMillis();
                table[current].status = Status.OCCUPIED;

                occupiedSpots++;
                totalProbes += probes;
                totalParkOperations++;

                // Track peak hour
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                hourlyUsage.put(hour, hourlyUsage.getOrDefault(hour, 0) + 1);

                return "Assigned spot #" + current + " (" + probes + " probes)";
            }
            probes++;
        }

        return "Parking Full!";
    }

    // Exit vehicle
    public String exitVehicle(String license) {
        int index = hash(license);

        for (int i = 0; i < capacity; i++) {
            int current = (index + i) % capacity;

            if (table[current].status == Status.EMPTY) {
                return "Vehicle not found";
            }

            if (table[current].status == Status.OCCUPIED &&
                    license.equals(table[current].licensePlate)) {

                long exitTime = System.currentTimeMillis();
                long durationMs = exitTime - table[current].entryTime;

                double hours = durationMs / (1000.0 * 60 * 60);
                double fee = hours * 5; // $5 per hour

                table[current].status = Status.DELETED;
                occupiedSpots--;

                return "Spot #" + current + " freed, Duration: "
                        + String.format("%.2f", hours) + "h, Fee: $"
                        + String.format("%.2f", fee);
            }
        }

        return "Vehicle not found";
    }

    // Find nearest available spot (from entrance = index 0)
    public int findNearestAvailable() {
        for (int i = 0; i < capacity; i++) {
            if (table[i].status == Status.EMPTY) {
                return i;
            }
        }
        return -1;
    }

    // Get statistics
    public String getStatistics() {
        double occupancy = (occupiedSpots * 100.0) / capacity;
        double avgProbes = totalParkOperations == 0 ? 0 :
                (double) totalProbes / totalParkOperations;

        // Find peak hour
        int peakHour = -1, max = 0;
        for (Map.Entry<Integer, Integer> entry : hourlyUsage.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                peakHour = entry.getKey();
            }
        }

        return "Occupancy: " + String.format("%.2f", occupancy) + "%, "
                + "Avg Probes: " + String.format("%.2f", avgProbes) + ", "
                + "Peak Hour: " + peakHour + ":00 - " + (peakHour + 1) + ":00";
    }

    // MAIN METHOD (Demo)
    public static void main(String[] args) throws InterruptedException {

        ParkingLot lot = new ParkingLot(10);

        System.out.println(lot.parkVehicle("ABC-1234"));
        System.out.println(lot.parkVehicle("ABC-1235"));
        System.out.println(lot.parkVehicle("XYZ-9999"));

        Thread.sleep(2000); // simulate time

        System.out.println(lot.exitVehicle("ABC-1234"));

        System.out.println("Nearest Available Spot: " + lot.findNearestAvailable());

        System.out.println(lot.getStatistics());
    }
}