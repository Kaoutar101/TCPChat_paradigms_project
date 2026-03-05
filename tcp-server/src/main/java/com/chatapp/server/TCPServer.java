package com.chatapp.server;

import com.chatapp.server.listener.ServerEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TCPServer {
    private static final int DEFAULT_PORT = 5000;
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Map<SocketChannel, Client> clients = new HashMap<>();
    private int nextId = 1;
    private ServerEventListener listener;

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : loadPortFromConfig();
        new TCPServer().run(port);
    }

    public TCPServer() {}

    public void setListener(ServerEventListener listener) {
        this.listener = listener;
    }

    private void fireLog(String message) {
        System.out.println(message);
        if (listener != null) listener.onLogMessage(message);
    }

    private void fireConnected(String username) {
        if (listener != null) listener.onClientConnected(username);
    }

    private void fireDisconnected(String username) {
        if (listener != null) listener.onClientDisconnected(username);
    }

    public static int loadPortFromConfig() {
        Properties props = new Properties();
        try (InputStream is = TCPServer.class.getClassLoader()
                .getResourceAsStream("server.properties")) {
            if (is != null) {
                props.load(is);
                String v = props.getProperty("server.port");
                if (v != null && !v.isBlank()) return Integer.parseInt(v.trim());
            }
        } catch (Exception ignored) {}
        return DEFAULT_PORT;
    }

    public void run(int port) throws Exception {
        Selector selector = Selector.open();
        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(new InetSocketAddress(port));
        server.register(selector, SelectionKey.OP_ACCEPT);
        fireLog("Server started on port " + port);
        fireLog("Waiting for clients...");
        while (true) {
            selector.select();
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                if (!key.isValid()) continue;
                if (key.isAcceptable()) handleAccept(server, selector);
                else if (key.isReadable()) handleRead(key);
            }
        }
    }

    private void handleAccept(ServerSocketChannel server, Selector selector) throws IOException {
        SocketChannel channel = server.accept();
        if (channel == null) return;
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        Client c = new Client(nextId++);
        clients.put(channel, c);
        fireLog("New connection established.");
        writeLine(channel, "Connection established.");
        writeLine(channel, "Please send your username (if any).");
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        Client client = clients.get(channel);
        if (client == null) { closeChannel(channel); return; }
        ByteBuffer buf = ByteBuffer.allocate(1024);
        int read;
        try { read = channel.read(buf); }
        catch (IOException e) { disconnect(channel, client, true); return; }
        if (read == -1) { disconnect(channel, client, true); return; }
        buf.flip();
        String s = UTF8.decode(buf).toString();
        client.in.append(s);
        int idx;
        while ((idx = indexOfNewline(client.in)) >= 0) {
            String line = client.in.substring(0, idx);
            client.in.delete(0, idx + 1);
            line = trimNewline(line);
            processLine(channel, client, line);
        }
    }

    private void processLine(SocketChannel channel, Client client, String line) throws IOException {
        if (!client.active) {
            String u = line.trim();
            if (u.isEmpty()) {
                client.readOnly = true;
                client.active = true;
                String name = "client" + client.id;
                client.username = name;
                writeLine(channel, "Welcome " + name + " (read-only).");
                fireLog("Welcome " + name + " (read-only).");
                fireConnected(name + " (read-only)");
            } else {
                client.username = u;
                client.active = true;
                writeLine(channel, "Welcome " + client.username + ".");
                fireLog("Welcome " + client.username + ".");
                fireConnected(client.username);
            }
            return;
        }
        String cmd = line.trim();
        if (equalsAnyIgnoreCase(cmd, "end", "bye")) {
            writeLine(channel, "Goodbye from server.");
            fireLog(client.username + " has left.");
            disconnect(channel, client, false);
            return;
        }
        if (equalsAnyIgnoreCase(cmd, "allUsers")) {
            writeUsersList(channel);
            return;
        }
        if (client.readOnly) {
            writeLine(channel, "Read-only mode.");
            return;
        }
        String from = client.username != null ? client.username : ("client" + client.id);
        String msg = "Message from " + from + ": " + line + " " + LocalDateTime.now().format(TS);
        fireLog(msg);
        broadcast(msg);
    }

    private void writeUsersList(SocketChannel channel) throws IOException {
        List<String> withNames = new ArrayList<>();
        List<String> withoutNames = new ArrayList<>();
        for (Client c : clients.values()) {
            if (!c.active) continue;
            if (c.username != null && !c.readOnly) withNames.add(c.username);
            else withoutNames.add("client" + c.id);
        }
        writeLine(channel, "--- Active Users ---");
        for (int i = 0; i < withNames.size(); i++)
            writeLine(channel, (i + 1) + ": " + withNames.get(i));
        writeLine(channel, "--- Read-Only ---");
        for (int i = 0; i < withoutNames.size(); i++)
            writeLine(channel, (i + 1) + ": " + withoutNames.get(i));
    }

    private void broadcast(String msg) throws IOException {
        for (SocketChannel ch : clients.keySet())
            if (ch.isOpen()) writeLine(ch, msg);
    }

    private void disconnect(SocketChannel channel, Client client, boolean silent) throws IOException {
        String name = client.username != null ? client.username : ("client" + client.id);
        clients.remove(channel);
        closeChannel(channel);
        fireDisconnected(name);
        if (!silent) fireLog(name + " disconnected.");
    }

    private void closeChannel(SocketChannel ch) {
        try { ch.close(); } catch (Exception ignored) {}
    }

    private void writeLine(SocketChannel ch, String s) throws IOException {
        String out = s + System.lineSeparator();
        ByteBuffer b = UTF8.encode(CharBuffer.wrap(out));
        while (b.hasRemaining()) ch.write(b);
    }

    private static boolean equalsAnyIgnoreCase(String s, String... xs) {
        for (String x : xs) if (s.equalsIgnoreCase(x)) return true;
        return false;
    }

    private static int indexOfNewline(StringBuilder sb) {
        for (int i = 0; i < sb.length(); i++)
            if (sb.charAt(i) == '\n') return i;
        return -1;
    }

    private static String trimNewline(String s) {
        int end = s.length();
        while (end > 0 && (s.charAt(end-1) == '\r' || s.charAt(end-1) == '\n')) end--;
        return s.substring(0, end);
    }

    private static class Client {
        final int id;
        String username;
        boolean readOnly;
        boolean active;
        final StringBuilder in = new StringBuilder();
        Client(int id) { this.id = id; }
    }
}
