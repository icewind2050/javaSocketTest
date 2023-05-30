package org.example;

import com.google.protobuf.MessageOrBuilder;

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
    public Client(String ip,int port) throws IOException {
        socket=new Socket();
        socket.setReuseAddress(true);
        socket.connect(new InetSocketAddress(ip,port));
        scanner = new Scanner(System.in);
    }

    public static void main(String[] args) throws IOException {

        var A = new Client("127.0.0.1",5848);
        A.start();

    }
    private byte[] Get(Socket socket) throws IOException, InterruptedException {
        sleep(100);
        int len =0;
        byte[] bytes = new byte[1024];
        while((len = socket.getInputStream().read(bytes)) == -1){
            break;
        }
        byte[] parse = new byte[len];
        for(int i=0;i<len;i++){
            parse[i]=bytes[i];
        }
        return parse;
    }
    private void printName(Message.Quest A){
        if(A.getControl().equals(Message.Control.READY)){
            for(int i=0;i<A.getAllNameCount();i++){
                System.out.println("index "+i+"   Name: "+A.getAllName(i));
            }
        }
    }
    private void INIT() throws IOException {
        var MB = Message.Quest.newBuilder();
        MB.setControl(Message.Control.INIT);
        System.out.print("your name:");
        MB.setName(scanner.nextLine());
        MB.build().writeTo(socket.getOutputStream());
    }
    private Message.Quest ParseMessage() throws IOException, InterruptedException {
        var A = Message.Quest.parseFrom(Get(socket));
        return A;
    }
    private int selectName(Message.Quest A){
        System.out.println("this is all of Users online");
        printName(A);
        System.out.println("select your choice");
        Scanner sc = new Scanner(System.in);
        int target = sc.nextInt();
        return target;
    }
    private Message.Quest quest() throws IOException, InterruptedException {
        var MB = Message.Quest.newBuilder();
        MB = MB.setControl(Message.Control.READY);
        MB.build().writeTo(socket.getOutputStream());
        var B = ParseMessage();
        return B;
    }
    private Message.Quest Begin(Message.Quest A) throws IOException, InterruptedException {
        var MB = Message.Quest.newBuilder();
        MB.setControl(Message.Control.BEGIN);
        MB.setName(A.getAllName(selectName(A)));
        MB.build().writeTo(socket.getOutputStream());
        var B = ParseMessage();
        return B;
    }
    public void start(){
        try {
            INIT();
            var A =  ParseMessage();
            selectName(A);
            var B = quest();

            var tar = Begin(B);

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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
