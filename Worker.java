import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Worker {

	private String host;
	private int port;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Worker(String host, int port) throws IOException {
		this.host = host;
		this.port = port;

	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Please provide a port number for the worker to listen on.\n Usage: java Worker portNumber");
			System.exit(1);
		}

		int port = Integer.parseInt(args[0]);
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Worker listening on port " + port);
			while (true) {
				try (Socket clientSocket = serverSocket.accept();
						BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
					String request;
					while ((request = in.readLine()) != null) {
						String response = processRequest(request, in, out);
						out.println(response);
					}
				} catch (IOException e) {
					System.out.println("Exception in worker on port " + port);
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.out.println("Could not listen on port " + port);
			e.printStackTrace();
		}
	}

	public synchronized static String processRequest(String requestType, BufferedReader in, PrintWriter out)
			throws IOException {
		
		String response = "Unknown request.";
		switch (requestType) {
		case "2":
			out.println("Send json path...");
			String jsonPath = in.readLine();
			response = Room.addRoom(jsonPath);
			break;
		case "1":
		case "A":
			response = formatRoomList(new ArrayList<>(Room.availableRooms.values())); 
			break;
		case "3":
			response = formatRoomList(new ArrayList<>(Room.bookedRooms.values())); 
			break;
		case "4": // Handle booked rooms for a date range
			out.println("Send search date period (dd/MM/yyyy-dd/MM/yyyy):");
			String dateRange = in.readLine();
			response = handleBookedRooms(dateRange);
			break;
		case "D": // Handle available rooms based on criteria
			out.println("Send attribute criteria (key:value pairs, comma-separated):");
			String criteriaStr = in.readLine();
			response = handleAvailableRooms(criteriaStr);
			break;
		case "B":
			out.println("Send room name...");
			String roomName = in.readLine();
			response = Room.bookRoom(roomName);
			break;
		case "C":
			out.println("Send room name...");
			String name = in.readLine();
			out.println("Send number of stars...");
			int stars = Integer.parseInt(in.readLine());
			double rating = Room.ratingRoom(name, stars, Room.allRooms);
			response = String.valueOf(rating);
			break;
		}
		return response;
	}

	//formating room list(same as sendDataToMaster)
	private static String formatRoomList(List<Room> rooms) {
		StringBuilder builder = new StringBuilder();
		for (Room room : rooms) {
			builder.append(room.toString()).append("\n");
		}
		builder.append("END"); // Append 'END' to signal the end of the data
		return builder.toString();
	}

	private static String handleBookedRooms(String dateRange) {
		String[] dates = dateRange.split("-");
		if (dates.length == 2) {
			String startDate = dates[0].trim();
			String endDate = dates[1].trim();
			List<Room> filteredRooms = MapReduce.mapFilterByDate(startDate, endDate, //map phase          
					new ArrayList<>(Room.bookedRooms.values()));													
			List<String> reducedRooms = MapReduce.reduceCountRoomsByArea(filteredRooms); //reduce phase     
			return sendReducedStringsToMaster(reducedRooms);
		} else {
			return "Invalid date format.";
		}
	}

	private static String handleAvailableRooms(String criteriaStr) {
		Map<String, String> searchCriteria = parseSearchCriteria(criteriaStr);
		List<Room> availableRooms = MapReduce.mapFilterByAttributes(searchCriteria,
				new ArrayList<>(Room.availableRooms.values()));
		return sendDataToMaster(availableRooms);
	}

	private static String sendReducedStringsToMaster(List<String> data) {
		StringBuilder builder = new StringBuilder();
		for (String item : data) {
			builder.append(item).append("\n");
		}
		builder.append("END");
		return builder.toString();
	}

	//same as formatToList
	private static String sendDataToMaster(List<Room> rooms) {
		StringBuilder builder = new StringBuilder();
		for (Room room : rooms) {
			builder.append(room.toString()).append("\n");
		}
		builder.append("END");
		return builder.toString();
	}

	//method for criteria format
	private static Map<String, String> parseSearchCriteria(String criteriaStr) {
		Map<String, String> criteriaMap = new HashMap<>();
		String[] pairs = criteriaStr.split(",");
		for (String pair : pairs) {
			String[] keyValue = pair.split(":");
			if (keyValue.length == 2) {
				criteriaMap.put(keyValue[0].trim(), keyValue[1].trim());
			}
		}
		return criteriaMap;
	}

}