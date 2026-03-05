package com.chat.client.listener;

public interface MessageListener {
    void onMessageReceived(String message);
    void onDisconnected();
}
