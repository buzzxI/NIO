package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class ServerChannelOperation {
    private Selector selector;
    private ServerSocketChannel server;
    public ServerChannelOperation(int port) {
        try {
            this.server = ServerSocketChannel.open();
            // non-blocking server channel
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(port));
            this.selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void listen() {
        try {
            while (true) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isValid()) {
                        if (key.isAcceptable()) {
                            SocketChannel client = server.accept();
                            client.configureBlocking(false);
                            client.register(this.selector, SelectionKey.OP_READ);
                        } else if (key.isReadable()) handle(key);
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handle(SelectionKey clientKey) {
        SocketChannel client = (SocketChannel) clientKey.channel();
        try  {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            int len = client.read(buffer);
            if (len > 0) {
                System.out.print("from " + client.getRemoteAddress() + ":");
                while (len > 0) {
                    buffer.flip();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    forward(bytes, client);
                    System.out.print(new String(bytes, StandardCharsets.UTF_8));
                    buffer.clear();
                    len = client.read(buffer);
                }
                System.out.println();
            }
        } catch (IOException e) {
            try {
                System.out.println(client.getRemoteAddress() + " off line");
                clientKey.cancel();
                client.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void forward(byte[] bytes, SocketChannel client) {
        Set<SelectionKey> keys = selector.keys();
        for (SelectionKey key : keys) {
            if (key.channel() instanceof SocketChannel socketChannel && socketChannel != client) {
                ByteBuffer tmp = ByteBuffer.wrap(bytes);
                try {
                    socketChannel.write(tmp);
                } catch (IOException e) {
                    try {
                        System.out.println(socketChannel.getRemoteAddress() + " off line");
                        key.cancel();
                        socketChannel.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }
}
