import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {
	private String roomName;
	private int noOfPersons;
	private String area;
	private double stars;
	private int noOfReviews;
	private String roomImage;
	private String availableDates;

	static Map<String, Room> allRooms = new HashMap<>();
	static Map<String, Room> availableRooms = new HashMap<>();
	static Map<String, Room> bookedRooms = new HashMap<>();
	static List<String> lines = new ArrayList<>();

	// Constructor
	public Room(String roomName, int noOfPersons, String area, int stars, int noOfReviews, String roomImage,
			String availableDates) {
		this.roomName = roomName;
		this.noOfPersons = noOfPersons;
		this.area = area;
		this.stars = stars;
		this.noOfReviews = noOfReviews;
		this.roomImage = roomImage;
		this.availableDates = availableDates;
	}

	//Helping method for availableRooms initialization 
	public static void readFileLines(String filePath) throws IOException {

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}
		initialRoomInsertion();

	}

	//same as above,we could implement the code in readFileLines
	public static void initialRoomInsertion() {
		for (int i = 0; i < lines.size(); i++) {
			String path = lines.get(i);
			addRoom(path);
		}
	}

//Method to check if a room is available within the requested date range.
	public boolean isAvailable(String startDate, String endDate) {
		// Assume dates are in the format "dd/MM/yyyy" and ranges are
		// "startDate-endDate"
		String[] availableRange = this.availableDates.split("-");
		// Here we directly compare date strings; this assumes exact matches and does
		// not account for overlaps
		// You would typically parse the strings into Date objects and check if the
		// ranges overlap
		return availableRange[0].trim().equals(startDate.trim()) && availableRange[1].trim().equals(endDate.trim());
	}

	public static double ratingRoom(String roomName, int newRating, Map<String, Room> rooms) {
		if (newRating < 1 || newRating > 5) {
			throw new IllegalArgumentException("Rating must be between 1 and 5 stars.");
		}

		Room room = rooms.get(roomName);
		if (room == null) {
			throw new IllegalArgumentException("Room not found: " + roomName);
		}

		room.stars = (room.stars * room.noOfReviews + newRating) / (double) (room.noOfReviews + 1);
		room.noOfReviews++;
		return room.stars;
	}

	public static String bookRoom(String roomName) {
		if (availableRooms.containsKey(roomName)) {
			Room roomToBook = availableRooms.remove(roomName); // Remove from availableRooms
			bookedRooms.put(roomName, roomToBook); // Add to bookedRooms
			return "Room '" + roomName + "' booked successfully!";
		} else {
			return "Room '" + roomName + "' not found in available rooms.";
		}
	}

	// inserts room from json file
	public static String addRoom(String jsonFilePath) {
		JSONParser parser = new JSONParser();

		try (FileReader reader = new FileReader(jsonFilePath)) {
			Object obj = parser.parse(reader);
			JSONObject jsonObject = (JSONObject) obj;

			String roomName = (String) jsonObject.get("roomName");
			long noOfPersons = (long) jsonObject.get("noOfPersons");
			String area = (String) jsonObject.get("area");
			long stars = (long) jsonObject.get("stars");
			long noOfReviews = (long) jsonObject.get("noOfReviews");
			String roomImage = (String) jsonObject.get("roomImage");
			String availableDates = (String) jsonObject.get("availableDates");

			Room room = new Room(roomName, (int) noOfPersons, area, (int) stars, (int) noOfReviews, roomImage,
					availableDates);
			availableRooms.putIfAbsent(roomName, room);
			allRooms.putIfAbsent(roomName, room);
			return "Room added successfully!"; // Print the message here
			// Room added successfully
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		return "Failed to add the room."; // Room addition failed
	}

//getters,setters

	public String getAvailableDates() {
		return availableDates;
	}

	public void setAvailableDates(String availableDates) {
		this.availableDates = availableDates;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public int getNoOfPersons() {
		return noOfPersons;
	}

	public void setNoOfPersons(int noOfPersons) {
		this.noOfPersons = noOfPersons;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public double getStars() {
		return stars;
	}

	public void setStars(int stars) {
		this.stars = stars;
	}

	public int getNoOfReviews() {
		return noOfReviews;
	}

	public void setNoOfReviews(int noOfReviews) {
		this.noOfReviews = noOfReviews;
	}

	public String getRoomImage() {
		return roomImage;
	}

	public void setRoomImage(String roomImage) {
		this.roomImage = roomImage;
	}

	@Override
	public String toString() {
		return "Room [roomName=" + roomName + ", noOfPersons=" + noOfPersons + ", area=" + area + ", stars=" + stars
				+ ", noOfReviews=" + noOfReviews + ", roomImage=" + roomImage + ", availableDates=" + availableDates
				+ "]";
	}

}