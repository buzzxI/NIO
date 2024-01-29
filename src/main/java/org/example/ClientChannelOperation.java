package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class ClientChannelOperation {
    private Selector selector;
    private SocketChannel client;
    public ClientChannelOperation(int port) {
        try {
            this.client = SocketChannel.open();
            this.client.configureBlocking(false);
            this.selector = Selector.open();
            this.client.register(selector, SelectionKey.OP_CONNECT);
            this.client.connect(new InetSocketAddress("127.0.0.1", port));
            this.waitForConnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(String msg) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
        client.write(buffer);
    }

    private void waitForConnect() throws IOException {
        selector.select();
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = keys.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            if (key.isValid() && client.finishConnect()) {
                client.register(selector, SelectionKey.OP_READ);
                Thread readThread = new Thread(this::read);
                readThread.start();
            }
            iterator.remove();
        }
    }

    private void read() {
        try {
            while (true) {
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isValid() && key.isReadable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(8);
                        int len = client.read(buffer);
                        if (len > 0) {
                            System.out.print("from " + client.getRemoteAddress() + ":");
                            while (len > 0) {
                                byte[] bytes = new byte[buffer.remaining()];
                                buffer.flip();
                                buffer.get(bytes, 0, len);
                                String msg = new String(bytes, 0, len, StandardCharsets.UTF_8);
                                System.out.print(msg);
                                buffer.clear();
                                len = client.read(buffer);
                            }
                            System.out.println();
                        }
                    }
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
