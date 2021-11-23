package net.alterorb.patcher;

import okio.Buffer;
import okio.ForwardingSink;
import okio.Sink;

import java.io.IOException;
import java.util.zip.CRC32;

public class Crc32Sink extends ForwardingSink {

    private final CRC32 crc32 = new CRC32();
    private final byte[] buffer = new byte[4096];

    private Crc32Sink(Sink delegate) {
        super(delegate);
    }

    public static Crc32Sink of(Sink sink) {
        return new Crc32Sink(sink);
    }

    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        var peek = source.peek();
        int bytesRead;

        while ((bytesRead = peek.read(buffer)) != -1) {
            crc32.update(buffer, 0, bytesRead);
        }
        super.write(source, byteCount);
    }

    public int crc32() {
        return (int) crc32.getValue();
    }
}
