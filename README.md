# TCPChat
<<<<<<< HEAD
=======

A real-time multi-client chat application built in Java using TCP sockets. The project consists of two Maven applications: a server and a client, both with JavaFX graphical interfaces.

---

## Project Structure

```
TCPChat/
├── tcp-server/        # Server application (NIO-based)
└── tcp-client-new/    # Client application (JavaFX GUI)
```

---

## Features

### Server
- Accepts multiple simultaneous client connections using Java NIO (non-blocking I/O)
- Broadcasts messages from any client to all connected clients
- Supports read-only clients (clients who join without a username)
- Responds to the `allUsers` command with a list of active users
- JavaFX dashboard showing connected users and a live message log
- Configurable port via `server.properties`

### Client
- JavaFX login screen to enter a username and connect to the server
- Live chat window with a scrollable message history
- Send messages by clicking the Send button or pressing Enter
- Read-only mode: joining without a username disables the send input
- Online/Offline status indicator
- Graceful disconnect via typing `bye` or `end`

---

## How to Run

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- JavaFX SDK (if not bundled)

### 1. Run the Server

```bash
cd tcp-server
mvn javafx:run
```

Or with a custom port:
```bash
mvn javafx:run -Djavafx.args="6000"
```

The server starts on port `5000` by default (configured in `src/main/resources/server.properties`).

### 2. Run the Client

```bash
cd tcp-client-new
mvn javafx:run
```

- Enter a username and click **Connect** to join as an active user
- Leave the username blank and click **Connect** to join in **read-only** mode

Multiple client instances can be launched simultaneously to simulate a multi-user chat.

---

## Chat Commands

| Command    | Description                          |
|------------|--------------------------------------|
| `allUsers` | Lists all currently connected users  |
| `bye`      | Disconnects gracefully from server   |
| `end`      | Same as `bye`                        |

---

## Architecture Overview

The project follows an **MVC pattern** on the client side and an **event-driven listener pattern** on both sides to keep network logic decoupled from the UI.

| Component | Role |
|---|---|
| `TCPServer` | Core NIO server; manages all client connections via a Selector loop |
| `TCPServer.Client` | Inner class tracking each connected client's state |
| `ServerController` | JavaFX controller displaying logs and connected users |
| `ServerEventListener` | Interface connecting server events to the UI |
| `TCPClient` | JavaFX entry point for the client application |
| `LoginController` | Handles username input and initiates connection |
| `ClientModel` | Manages the socket, sending, and receiving on a background thread |
| `ChatController` | JavaFX controller rendering the chat UI |
| `MessageListener` | Interface connecting incoming messages to the UI |

---

## Technologies Used

- **Java 17**
- **Java NIO** (ServerSocketChannel, Selector) — server-side non-blocking I/O
- **Java Sockets** (Socket, BufferedReader, PrintWriter) — client-side blocking I/O
- **JavaFX** — graphical user interface for both server and client
- **FXML** — declarative UI layout
- **Maven** — build and dependency management

---

## Configuration

**Server port** — edit `tcp-server/src/main/resources/server.properties`:
```properties
server.port=5000
```

**Client default connection** — pass IP and port as program arguments:
```bash
mvn javafx:run -Djavafx.args="192.168.1.10 5000"
```
>>>>>>> a3cfaeb44669b5e3352f4a5443f0fa00634bede5
