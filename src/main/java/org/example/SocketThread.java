package org.example;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

public class SocketThread extends Thread{
    private Socket socket;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;

    private DataInputStream dataRead;
    private DataOutputStream dataWrite;

    public SocketThread(Socket socket) throws IOException {
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.printWriter =new PrintWriter(socket.getOutputStream());
        dataRead = new DataInputStream(socket.getInputStream());
        dataWrite = new DataOutputStream(socket.getOutputStream());

    }
    public void run(){
        var ServerAddress = socket.getLocalAddress();
        System.out.println("User IP:"+ServerAddress.getHostAddress()+" port: "+socket.getPort());
        printWriter.println("successfully connect server:");
        printWriter.flush();
        while (true){
            try {
                Message.Quest A = Message.Quest.parseFrom(socket.getInputStream());
                if(A == null){
                    break;
                }
                if(A.getControl().equals(Message.Control.WAIT)){
                    var Sent = Message.Quest.newBuilder();
                    var TS = Message.Translate.newBuilder();
                    String[] add = Server.user.get(A.getName()).split(":");
                    TS.setIp(add[0]);
                    TS.setPort(add[1]);
                    Sent.setControl(Message.Control.READY);
                    Sent.setState(TS.build());
                    Sent.setName("Server");
                    Sent.build().writeTo(socket.getOutputStream());
                } else if (A.getControl().equals(Message.Control.BEGIN)) {
                    var Sent = Message.Quest.newBuilder();
                    var TS = Message.Translate.newBuilder();
                    String[] add = Server.user.get(A.getName()).split(":");
                    TS.setIp(add[0]);
                    TS.setPort(add[1]);
                    Sent.setControl(Message.Control.READY);
                    Sent.setState(TS.build());
                    Sent.setName("Server");
                    Sent.build().writeTo(socket.getOutputStream());
                } else if(A.getControl().equals(Message.Control.INIT)){
                    Server.user.put(A.getName(),ServerAddress.getHostAddress()+":"+socket.getPort());
                    var Sent = Message.Quest.newBuilder();
                    var TS = Message.Translate.newBuilder();
                    int temp =0;
                    for(var i:Server.user.keySet()){
                        Sent.setAllName(temp++,i);
                    }
                    Sent.setControl(Message.Control.READY);
                    Sent.build().writeTo(socket.getOutputStream());
                    printWriter.println("successfully received");
                    printWriter.flush();
                } else if (A.getControl().equals(Message.Control.CLOSE)) {
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            socket.close();
            printWriter.println("successfully received");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
