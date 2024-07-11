import java.io.*;
import java.net.*;

public class Client {
	private static final String SERVER_ADDRESS = "localhost";
	private static final int SERVER_PORT = 12345;

	public static void main(String[] args) {
		try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

			System.out.println("Connected to server.");

			while (true) {
				System.out.println("\nChoose an option:");
				System.out.println("1. Display Available Rooms");
				System.out.println("B. Book");
				System.out.println("C. Rating");
				System.out.println("D. Search");
				System.out.println("E. Exit");
				System.out.print("Option:>");

				String response;
				String userInput = stdIn.readLine(); // Read user input like scanner System.in
				out.println(userInput); // Send user input to server

				if ("E".equals(userInput)) {
					System.out.println("Exiting...");
					break;
				}
				// Handle user input
				switch (userInput) {

				case "B":
					System.out.println("Server response: " + in.readLine()); // Print server response
					System.out.print("Give room name: ");
					String roomName = stdIn.readLine();
					out.println(roomName);
					response = in.readLine(); // read server's response
					System.out.println(response);
					break;
				case "1":
					// Print available rooms received from the server
					System.out.println("Available Rooms:");
					String roomInfo;
					while (!(roomInfo = in.readLine()).equals("END")) {
						System.out.println(roomInfo);
					}

					break;
				case "C":
					System.out.println("Server response: " + in.readLine()); // Print server response
					System.out.print("Give room name: ");
					String name = stdIn.readLine();
					out.println(name);
					response = in.readLine();
					System.out.println("Server response: " + response);
					System.out.print("Give stars: ");
					String stars = stdIn.readLine();
					out.println(stars);
					response = in.readLine();
					System.out.println(response);
					break;
				case "D":

					System.out.println(response = in.readLine());
					String criteria = stdIn.readLine();
					out.println(criteria); // Send search criteria to server

					System.out.println("Search Results:");
					while (true) {
						String result = in.readLine();
						if ("END".equals(result)) {
							break; // End of results
						}
						System.out.println(result); // Display each result from the reduction
					}
					break;
				default:

					System.out.println("Wrong input. Check options and spelling..!!");
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
