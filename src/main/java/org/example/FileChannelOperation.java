package org.example;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;

public class FileChannelOperation {
    public static void read_and_write(String words) {
        Set<OpenOption> options = Set.of(StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
        try (FileChannel channel = FileChannel.open(Paths.get("./test.txt"), options)) {
            ByteBuffer buffer = ByteBuffer.allocate(1);
            int len = channel.read(buffer);
            byte[] bytes = new byte[1024];
            if (len != -1) {
                while (len != -1) {
                    buffer.flip();
                    buffer.get(bytes, 0, len);
                    String original = new String(bytes, 0, len, StandardCharsets.UTF_8);
                    System.out.print(original);
                    buffer.clear();
                    len = channel.read(buffer);
                }
            } else System.out.println("no existing works");
            bytes = words.getBytes(StandardCharsets.UTF_8);
            // switch back to write mode
            buffer.clear();
            int idx = 0;
            while (idx < bytes.length) {
                int size = buffer.remaining();
                buffer.put(bytes, idx, size);
                buffer.flip();
                int write = channel.write(buffer);
                if (write != size) System.out.println("short count");
                buffer.clear();
                idx += size;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
