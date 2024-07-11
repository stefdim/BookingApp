
import java.io.*;
import java.net.*;

import java.util.ArrayList;
import java.util.List;


public class Master {
    private static final int CLIENT_PORT = 12345; // Port to listen for clients
    private List<Worker> workers = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Usage: java Master worker_port1 worker_port2 ... worker_portN");
            System.exit(1);
        }

        Master master = new Master(args);
        Room.readFileLines("jsonPaths.txt");
        master.startServer();
       
    }

    public Master(String[] ports) throws IOException {
        for (String port : ports) {
            try {
                int workerPort = Integer.parseInt(port);
                Worker worker = new Worker("localhost", workerPort); // Assuming Workers are on localhost
                workers.add(worker);
                System.out.println("Worker added on port: " + workerPort);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + port);
            }
        }
    }

    @SuppressWarnings("resource")
	private void startServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(CLIENT_PORT);
        System.out.println("Master server started. Waiting for clients on port " + CLIENT_PORT);

        while (true) { //starting a new thread for every connection
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected: " + clientSocket.getInetAddress());
            Thread clientThread = new Thread(new ClientHandler(clientSocket, workers));
            clientThread.start();
        }
    }
}

//helper class, handling communication
class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, List<Worker> workers) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from client: " + inputLine);
                String response = Worker.processRequest(inputLine, in, out);// from here 'talking' is happening.
                out.println(response); 
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}