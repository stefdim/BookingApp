import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapReduce {

    // Map method for filtering rooms based on attributes
    public static List<Room> mapFilterByAttributes(Map<String, String> criteria, List<Room> rooms) {
        List<Room> filteredRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (matchesAttributes(room, criteria)) {
                filteredRooms.add(room);
            }
        }
        return filteredRooms;
    }

    // Map method for filtering rooms based on date availability
    public static List<Room> mapFilterByDate(String startDate, String endDate, List<Room> rooms) {
        List<Room> filteredRooms = new ArrayList<>();
        for (Room room : rooms) {
            if (room.isAvailable(startDate, endDate)) {
                filteredRooms.add(room);
            }
        }
        return filteredRooms;
    }

    // Reduce method for counting the number of rooms in each area
    public static List<String> reduceCountRoomsByArea(List<Room> rooms) {
        Map<String, Integer> roomCountByArea = new HashMap<>();

        for (Room room : rooms) {
            String area = room.getArea();
            roomCountByArea.put(area, roomCountByArea.getOrDefault(area, 0) + 1);
        }

        List<String> output = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : roomCountByArea.entrySet()) {
            output.add(entry.getKey() + ": " + entry.getValue());
        }

        return output;
    }
    private static boolean matchesAttributes(Room room, Map<String, String> criteria) {
        boolean matchFound = false;  // Track if any attribute matches

        for (Map.Entry<String, String> entry : criteria.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String value = entry.getValue();

            switch (key) {
                case "roomname":
                    matchFound = room.getRoomName().equalsIgnoreCase(value);
                    break;
                case "area":
                    matchFound = room.getArea().equalsIgnoreCase(value);
                    break;
                case "stars":
                    matchFound = Double.compare(room.getStars(), Double.parseDouble(value)) == 0;
                    break;
                case "noofpersons":
                    matchFound = room.getNoOfPersons() == Integer.parseInt(value);
                    break;
                case "noofreviews":
                    matchFound = room.getNoOfReviews() == Integer.parseInt(value);
                    break;
            }

            // If any attribute matches, return true immediately
            if (matchFound) return true;
        }

        // Return false if no matches found
        return false;
    }
}