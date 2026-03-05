package com.chatapp.server.listener;

public interface ServerEventListener {
    void onClientConnected(String username);
    void onClientDisconnected(String username);
    void onLogMessage(String message);
}
