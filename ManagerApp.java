import java.io.*;
import java.net.*;

public class ManagerApp {
    private static final String SERVER_ADDRESS = "localhost"; //server's ip
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
                System.out.println("2. Add Room");
                System.out.println("3. Display Booked Rooms");
                System.out.println("4. Search by Date");    // 15/06/2024-20/06/2024 for searching by date.
                System.out.println("5. Exit");
                System.out.print("Enter option: ");  

                String userInput = stdIn.readLine(); // Read user input
                out.println(userInput); // Send user input to server

                if ("5".equals(userInput)) {
                    System.out.println("Exiting...");
                    break;
                }
                // Handle user input
                switch(userInput) {
               
                    case "2":
                        System.out.println("Server response: " + in.readLine()); // Print server response
                        System.out.print("Give json file path: ");
                        String path = stdIn.readLine(); // Read file path from user
                        out.println(path); // Send file path to server
                        String response = in.readLine();
                        System.out.println(response);
                        break;
                    case "1":
                        // Print available rooms received from the server
                        System.out.println("Available Rooms:");
                        String roomInfo;
                        while (!(roomInfo = in.readLine()).equals("END")) {
                            System.out.println(roomInfo);
                        }
                        // Return to options menu
                        break;
                    case "3":
                       System.out.println("Booked rooms:");
                       String bookedRoomInfo;
                       while(!(bookedRoomInfo = in.readLine()).equals("END")) {
                    	   System.out.println(bookedRoomInfo);
                       }
                        break;
                    case "4":
                        System.out.println("Server response: " + in.readLine() + "\ne.g 15/06/2024-20/06/2024");  // Expecting a prompt from the server for details
                       
                        String criteria = stdIn.readLine();
                        out.println(criteria); // Sending search criteria to server

                        System.out.println("Search Results:");
                        while (true) {
                            String result = in.readLine();
                            if ("END".equals(result)) {
                                break;  // Check for end of result transmission
                            }
                            System.out.println(result);  // Display each result line from the server
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