package org.example;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.util.List;

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

    private byte[] Get(Socket socket) throws IOException {
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
    private boolean dealMessage(Message.Quest A) throws IOException {

                if(A == null){
                    return true;
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
                    return true;
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
                    return true;
                } else if(A.getControl().equals(Message.Control.INIT)){
                    Server.user.put(A.getName(),socket.getLocalAddress().getHostAddress()+":"+socket.getPort());
                    var Sent = Message.Quest.newBuilder();
                    Sent.addAllAllName(Server.user.keySet());
                    Sent.setControl(Message.Control.READY);
                    Sent.setName("Server");
                    Sent.build().writeTo(socket.getOutputStream());
                    return true;
                } else if (A.getControl().equals(Message.Control.CLOSE)) {
                    return false;
                }
        return false;
    }
    public void run(){
        var ServerAddress = socket.getLocalAddress();
        System.out.println("User IP:"+ServerAddress.getHostAddress()+" port: "+socket.getPort());
        while (true){
            try {
                Message.Quest A = Message.Quest.parseFrom(Get(socket));
                if(!dealMessage(A)){
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
