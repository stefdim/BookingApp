package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientActivity extends AppCompatActivity {

    private ListView listViewResponse;
    private ArrayAdapter<String> responseAdapter;
    private List<String> responseData = new ArrayList<>();
    private static final String SERVER_ADDRESS = "192.168.1.10"; // ή η IP του server
    private static final int SERVER_PORT = 12345;
    private static final String TAG = "ClientActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        listViewResponse = findViewById(R.id.list_view_response);
        responseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, responseData);
        listViewResponse.setAdapter(responseAdapter);

        Button buttonDisplayRooms = findViewById(R.id.button_display_rooms);
        Button buttonBookRoom = findViewById(R.id.button_book_room);
        Button buttonRateRoom = findViewById(R.id.button_rate_room);
        Button buttonSearchRoom = findViewById(R.id.button_search_room);
        Button buttonExit = findViewById(R.id.button_exit);

        buttonDisplayRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest("1");
            }
        });

        buttonBookRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog("Enter Room Name:", new InputDialogCallback() {
                    @Override
                    public void onInputReceived(String input) {
                        sendRequest("B", input);
                    }
                });
            }
        });

        buttonRateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog("Enter Room Name:", new InputDialogCallback() {
                    @Override
                    public void onInputReceived(final String roomName) {
                        showInputDialog("Enter Rating (1-5):", new InputDialogCallback() {
                            @Override
                            public void onInputReceived(String rating) {
                                sendRequest("C", roomName, rating);
                            }
                        });
                    }
                });
            }
        });

        buttonSearchRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog("Enter Search Criteria:", new InputDialogCallback() {
                    @Override
                    public void onInputReceived(String input) {
                        sendRequest("D", input);
                    }
                });
            }
        });

        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest("E");
                finish();
            }
        });
    }

    private void sendRequest(final String request, final String... additionalInputs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    Log.d(TAG, "Connected to server");

                    out.println(request);
                    Log.d(TAG, "Sent request: " + request);

                    if (additionalInputs != null) {
                        for (String input : additionalInputs) {
                            out.println(input);
                            Log.d(TAG, "Sent additional input: " + input);
                        }
                    }

                    final List<String> response = new ArrayList<>();
                    String line;
                    while ((line = in.readLine()) != null) {
                        Log.d(TAG, "Received line: " + line);
                        response.add(line);

                        if ("END".equals(line)) {
                            break;
                        }
                    }

                    Log.d(TAG, "Full response: " + response.toString());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            responseData.clear();
                            responseData.addAll(response);
                            responseAdapter.notifyDataSetChanged();

                            // Display success message if booking or rating
                            if (request.equals("B") || request.equals("C")) {
                                String successMessage = response.get(response.size() - 1);
                                showToast(successMessage);
                            }
                        }
                    });

                } catch (IOException e) {
                    Log.e(TAG, "Error in communication with server", e);
                }
            }
        }).start();
    }

    private void showToast(String message) {
        Toast.makeText(ClientActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void showInputDialog(String message, final InputDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input Required");
        builder.setMessage(message);

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String userInput = input.getText().toString();
            callback.onInputReceived(userInput);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    interface InputDialogCallback {
        void onInputReceived(String input);
    }
}
