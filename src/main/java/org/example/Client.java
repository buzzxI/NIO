package org.example;

import java.io.IOException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        ClientChannelOperation client = new ClientChannelOperation(8888);
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String in = scanner.nextLine();
            try {
                client.write(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
