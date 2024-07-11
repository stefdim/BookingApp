package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

public class ManagerActivity extends AppCompatActivity {

    private ListView listViewResponseManager;
    private ArrayAdapter<String> responseAdapterManager;
    private List<String> responseDataManager = new ArrayList<>();
    private TextView successMessage;
    private static final String SERVER_ADDRESS = "192.168.1.10"; // ή η IP του server
    private static final int SERVER_PORT = 12345;
    private static final String TAG = "ManagerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        listViewResponseManager = findViewById(R.id.list_view_response_manager);
        responseAdapterManager = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, responseDataManager);
        listViewResponseManager.setAdapter(responseAdapterManager);

        successMessage = findViewById(R.id.success_message);

        Button buttonDisplayRoomsManager = findViewById(R.id.button_display_rooms_manager);
        Button buttonAddRoomManager = findViewById(R.id.button_add_room_manager);
        Button buttonDisplayBookedRoomsManager = findViewById(R.id.button_display_booked_rooms_manager);
        Button buttonSearchByDateManager = findViewById(R.id.button_search_by_date_manager);
        Button buttonExitManager = findViewById(R.id.button_exit_manager);

        buttonDisplayRoomsManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest("1");
            }
        });

        buttonAddRoomManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog("Enter JSON File Path:", new InputDialogCallback() {
                    @Override
                    public void onInputReceived(String input) {
                        sendRequest("2", input);
                    }
                });
            }
        });

        buttonDisplayBookedRoomsManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest("3");
            }
        });

        buttonSearchByDateManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog("Enter Date Range (dd/MM/yyyy-dd/MM/yyyy):", new InputDialogCallback() {
                    @Override
                    public void onInputReceived(String input) {
                        sendRequest("4", input);
                    }
                });
            }
        });

        buttonExitManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest("5");
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
                            responseDataManager.clear();
                            responseDataManager.addAll(response);
                            responseAdapterManager.notifyDataSetChanged();

                            // Display message
                            for (String message : response) {
                                if (message.equals("Room added successfully!") || message.equals("Failed to add the room.")) {
                                    successMessage.setText(message);
                                    successMessage.setVisibility(View.VISIBLE);
                                } else {
                                    successMessage.setVisibility(View.GONE);
                                }
                            }
                        }
                    });

                } catch (IOException e) {
                    Log.e(TAG, "Error in communication with server", e);
                }
            }
        }).start();
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
