package com.ettrema.web.stats;

import java.io.IOException;
import java.io.OutputStream;

public class CountingOutputStream extends OutputStream {

    final OutputStream wrapped;
    int count;

    public CountingOutputStream(OutputStream wrapped) {
        super();
        this.wrapped = wrapped;
    }

    public int getCount() {
        return count;
    }
    
    
    @Override
    public void write(int b) throws IOException {
        wrapped.write(b);
        count++;
    }

    @Override
    public void write(byte[] b) throws IOException {
        wrapped.write(b);
        count += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            wrapped.write(b, off, len);
        } catch (OutOfMemoryError e) {
            throw new RuntimeException("out of memory writing bytes: array: " + b.length + " offset: " + off + " length: " + len, e);
        }
        count += len;
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        wrapped.flush();
    }

    @Override
    public void close() throws IOException {
        super.close();
        wrapped.close();
    }
}
