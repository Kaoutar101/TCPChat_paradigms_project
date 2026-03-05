package com.chat.client.model;

import com.chat.client.listener.MessageListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientModel {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private boolean readOnly;
    private MessageListener listener;

    public ClientModel(String ip, int port, String username, MessageListener listener) {
        this.username = username;
        this.readOnly = (username == null || username.isBlank());
        this.listener = listener;
    }

    public void connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
            );
            out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

            // send username immediately (empty string if read-only)
            out.println(username == null ? "" : username);

        } catch (Exception e) {
            listener.onDisconnected();
        }
    }

    public void startListening() {
        Thread thread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    listener.onMessageReceived(line);
                }
            } catch (Exception e) {
                // connection dropped
            }
            listener.onDisconnected();
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void sendMessage(String text) {
        if (readOnly) return;
        if (out != null) {
            out.println(text);
        }
        if (text.equalsIgnoreCase("end") || text.equalsIgnoreCase("bye")) {
            disconnect();
        }
    }

    public void disconnect() {
        try {
            if (out != null) out.println("bye");
            if (socket != null) socket.close();
        } catch (Exception ignored) {}
        listener.onDisconnected();
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public String getUsername() {
        return username;
    }
}