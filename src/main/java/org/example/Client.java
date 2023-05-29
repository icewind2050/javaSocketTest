package org.example;

import javax.naming.NamingEnumeration;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Scanner;

import static java.lang.Thread.sleep;

public class Client {
    private Socket socket;
    private Scanner scanner;

    public Client() {

    }

    public static void main(String[] args) {
        var A = new Client();
        A.start("127.0.0.1",5848);

    }
    private byte[] Get(Socket socket) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        byte[] bytes = bufferedReader.readLine().getBytes();

        return bytes;
    }
    public void start(String ip,int port){
        try {
            Scanner scanner = new Scanner(System.in);
            socket=new Socket();
            socket.setReuseAddress(true);
            socket.connect(new InetSocketAddress(ip,port));
            var MB = Message.Quest.newBuilder();
            MB.setControl(Message.Control.INIT);

            System.out.print("your name:");
            MB.setName(scanner.nextLine());

            MB.build().writeTo(socket.getOutputStream());
            var A = Message.Quest.parseFrom(Get(socket));
            System.out.println("this is all of Users online");
            for(int i=0;i<A.getAllNameCount();i++){
                System.out.println("index "+i+"   Name: "+A.getAllName(i));
            }
            System.out.println("select your choice");
            Scanner sc = new Scanner(System.in);
            int target = sc.nextInt();

            MB.setControl(Message.Control.BEGIN);
            MB.setName(A.getAllName(target));
            MB.build().writeTo(socket.getOutputStream());

            var tar = Message.Quest.parseFrom(Get(socket));
            String targetIp =tar.getState().getIp();
            int targetPort = Integer.parseInt(tar.getState().getPort());


            new Thread(()->{
                Socket newSocket = new Socket();
                try {
                    newSocket.setReuseAddress(true);
                    newSocket.bind(new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(),socket.getLocalPort()));
                    for(int i=0;i<100;i++){
                        newSocket.connect(new InetSocketAddress(targetIp,targetPort));
                    }
                    BufferedReader b = new BufferedReader(
                            new InputStreamReader(newSocket.getInputStream()));
                    PrintWriter p = new PrintWriter(newSocket.getOutputStream());

                    while (true) {

                        p.write("hello " + System.currentTimeMillis() + "\n");
                        p.flush();

                        String message = b.readLine();

                        System.out.println(message);

                        p.write(message + "\n");
                        p.flush();

                        if("exit".equals(message)) {
                            break;
                        }


                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
            ).start();


        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
