package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server implements Runnable {
    private final String IP;
    private final Integer port;
    private static ServerSocket serverSocket;
    public static HashMap<String,String> user = new HashMap<>();
    public Server(String IP, int port) throws IOException {
        this.IP = IP;
        this.port = port;
        serverSocket = new ServerSocket(port);
    }

    public Server(String IP, Integer port) {
        this.IP = IP;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Server Online");
        while (true){
            try {
                SocketThread socketThread = new SocketThread(serverSocket.accept());
                socketThread.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            serverSocket = new ServerSocket(5848);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Server Online");
        while (true){
            try {
                SocketThread socketThread = new SocketThread(serverSocket.accept());
                socketThread.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
