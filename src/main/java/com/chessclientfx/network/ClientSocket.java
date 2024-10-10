package com.chessclientfx.network;

import java.io.*;
import java.net.Socket;

public class ClientSocket {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    public ClientSocket(String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
    }

    public void sendMessage(String message) {
        output.println(message);
    }

    public String receiveMessage() throws IOException {
        return input.readLine();
    }

    public void close() throws IOException {
        socket.close();
        input.close();
        output.close();
    }

    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }
}

