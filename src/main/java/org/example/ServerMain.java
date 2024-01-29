package org.example;


public class ServerMain {
    public static void main(String[] args) {
        ServerChannelOperation server = new ServerChannelOperation(8888);
        server.listen();
    }
}
