package versioning;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 *
 */
public class ChunkedChecksums extends FilterInputStream {

    private final List<Checksum> checkSums = new ArrayList<Checksum>();
    private final int chunkSize;
    private int ctr;
    private Checksum current;

    /**
     * Creates an input stream using the specified Checksum.
     * @param in the input stream
     * @param cksum the Checksum
     */
    public ChunkedChecksums(InputStream in, int chunkSize) {
        super(in);
        this.chunkSize = chunkSize;
    }

    public List<Checksum> getCheckSums() {
        return checkSums;
    }

    

    private void update(int b) {
        if( ctr >= chunkSize ) {
            ctr = 0;
            current = null;
        }
        if(current == null ) {
            current = new CRC32();
            checkSums.add(current);
        }
        current.update(b);
        ctr++;
    }

    /**
     * Reads a byte. Will block if no input is available.
     * @return the byte read, or -1 if the end of the stream is reached.
     * @exception IOException if an I/O error has occurred
     */
    @Override
    public int read() throws IOException {
        int b = in.read();
        if (b != -1) {
            update(b);
        }
        return b;
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        len = in.read(buf, off, len);
        int b;
        if (len != -1) {
            // TODO: try to add the array whole, or split into pieces if larger then chunk size/doesnt fit
            for (int i = 0; i < len; i++) {
                b = buf[off + i];
                update(b);
            }
        }
        return len;
    }

    @Override
    public long skip(long n) throws IOException {
        throw new UnsupportedOperationException("not supported");
    }
}
