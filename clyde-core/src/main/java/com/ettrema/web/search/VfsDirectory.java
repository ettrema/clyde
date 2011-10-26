package com.bradmcevoy.web.search;

import com.ettrema.context.RequestContext;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.VfsSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;

/**
 *
 */
public class VfsDirectory extends Directory {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VfsDirectory.class);
    private UUID nameNodeId;

    public VfsDirectory(UUID nameNodeId) {
        this.nameNodeId = nameNodeId;
    }

    @Override
    public String[] listAll() throws IOException {
        List<String> list = new ArrayList<String>();
        for (NameNode childNode : node().children()) {
            list.add(childNode.getName());
        }
        String[] arr = new String[list.size()];
        return list.toArray(arr);
    }

    @Override
    public boolean fileExists(String name) throws IOException {
        boolean b = node().child(name) != null;
        if( log.isDebugEnabled()){
            log.debug("fileExists: " + name + " = " + b);
        }
        return b;
    }

    @Override
    public long fileModified(String name) throws IOException {
        NameNode child = node().child(name);
        if( child != null ) {
            return child.getModifiedDate().getTime();
        } else {
            return 0;
        }
    }

    @Override
    public void touchFile(String name) throws IOException {
        node().child(name).save();
    }

    @Override
    public void deleteFile(String name) throws IOException {
        node().child(name).delete();
    }

    @Override
    public long fileLength(String name) throws IOException {
        NameNode child = node().child(name);
        DataBlock block = (DataBlock) child.getData();
        return block.length();
    }

    @Override
    public IndexOutput createOutput(String name) throws IOException {
        log.debug("openOutput: " + name);
        DataBlock block = (DataBlock) node().child(name).getData();
        return new VfsIndexOutput(block);
    }

    @Override
    public IndexInput openInput(String name) throws IOException {
        log.debug("openInput: " + name);
        NameNode n = node().child(name);
        DataBlock block;
        if (n == null) {
            throw new IOException("index doesnt exist");
//            block = new DataBlock();
//            n = node().add(name, block);
//            n.save();
        } else {
            block = (DataBlock) node().child(name).getData();
        }
        return new VfsIndexInput(block);
    }

    @Override
    public void close() throws IOException {
    }

    private VfsSession vfs() {
        return RequestContext.getCurrent().get(VfsSession.class);
    }

    private NameNode node() {
        return vfs().get(nameNodeId);
    }

    public class VfsIndexInput extends IndexInput {

        private final DataBlock dataBlock;
        private ByteArrayInputStream bin;
        private long pos;

        public VfsIndexInput(DataBlock dataBlock) {
            this.dataBlock = dataBlock;
            bin = new ByteArrayInputStream(dataBlock.getData());
        }

        @Override
        public byte readByte() throws IOException {
            pos++;
            return (byte) bin.read();
        }

        @Override
        public void readBytes(byte[] b, int offset, int len) throws IOException {
            pos += offset;
            pos += len;
            bin.read(b, len, len);
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public long getFilePointer() {
            return pos;
        }

        @Override
        public void seek(long pos) throws IOException {
            bin = new ByteArrayInputStream(dataBlock.getData());
            bin.skip(pos);
        }

        @Override
        public long length() {
            return dataBlock.length();
        }
    }

    private class RAMInputStream extends InputStream {

        private long position;
        private int buffer;
        private int bufferPos;
        private long markedPosition;

        @Override
        public synchronized void reset() throws IOException {
            position = markedPosition;
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public void mark(int readlimit) {
            this.markedPosition = position;
        }

        @Override
        public int read(byte[] dest, int destOffset, int len) throws IOException {
//            if (position == file.length) {
//                return -1;
//            }
            int remainder = 0; //(int) ((position + len > file.length) ? file.length - position : len);
            long oldPosition = position;
            while (remainder != 0) {
//                if (bufferPos == bufferSize) {
//                    bufferPos = 0;
//                    buffer++;
//                }
                int bytesToCopy = 0; //bufferSize - bufferPos;
                bytesToCopy = bytesToCopy >= remainder ? remainder : bytesToCopy;
                byte[] buf = null;//(byte[]) file.buffers.get(buffer);
                System.arraycopy(buf, bufferPos, dest, destOffset, bytesToCopy);
                destOffset += bytesToCopy;
                position += bytesToCopy;
                bufferPos += bytesToCopy;
                remainder -= bytesToCopy;
            }
            return (int) (position - oldPosition);
        }

        public int read() throws IOException {
//            if (position == file.length) {
//                return -1;
//            }
//            if (bufferPos == bufferSize) {
//                bufferPos = 0;
//                buffer++;
//            }
            byte[] buf = null; //(byte[]) file.buffers.get(buffer);
            position++;
            return buf[bufferPos++] & 0xFF;
        }
    }

    private class RAMFile {

        ArrayList buffers = new ArrayList();
        long length;
    }

    public class VfsIndexOutput extends IndexOutput {

        public static final int DEFAULT_BUFFER_SIZE = 16384;
        private final DataBlock block;
        private RAMFile file;  // TODO: remove this and use child nodes instead
        private int pointer = 0;
        private byte[] buffer;
        private long bufferStart = 0;           // position in file of buffer
        private int bufferPosition = 0;         // position in buffer
        protected int bufferSize = DEFAULT_BUFFER_SIZE;

        public VfsIndexOutput(DataBlock block) {
            this.block = block;
        }

        public void flushBuffer(byte[] src, int offset, int len) {
            byte[] buffer;
            int bufferPos = offset;
            while (bufferPos != len) {
                int bufferNumber = pointer / bufferSize;
                int bufferOffset = pointer % bufferSize;
                int bytesInBuffer = bufferSize - bufferOffset;
                int remainInSrcBuffer = len - bufferPos;
                int bytesToCopy = bytesInBuffer >= remainInSrcBuffer ? remainInSrcBuffer : bytesInBuffer;

                if (bufferNumber == file.buffers.size()) {
                    buffer = new byte[bufferSize];
                    file.buffers.add(buffer);
                } else {
                    buffer = (byte[]) file.buffers.get(bufferNumber);
                }

                System.arraycopy(src, bufferPos, buffer, bufferOffset, bytesToCopy);
                bufferPos += bytesToCopy;
                pointer += bytesToCopy;
            }

            if (pointer > file.length) {
                file.length = pointer;
            }
        }

        protected InputStream openInputStream() throws IOException {
            return new RAMInputStream();
        }

        protected void doAfterClose() throws IOException {
            file = null;
        }

        public void seek(long pos) throws IOException {
            flush();
            bufferStart = pos;
            pointer = (int) pos;
        }

        public long length() {
            return file.length;
        }

        private void flushBuffer(byte[] b, int len) throws IOException {
            flushBuffer(b, 0, len);
        }

        public void flushToIndexOutput(IndexOutput indexOutput) throws IOException {
            flushBuffer(buffer, bufferPosition);
            bufferStart += bufferPosition;
            bufferPosition = 0;
            if (file.buffers.size() == 0) {
                return;
            }
            if (file.buffers.size() == 1) {
                indexOutput.writeBytes((byte[]) file.buffers.get(0), (int) file.length);
                return;
            }
            int tempSize = file.buffers.size() - 1;
            int i;
            for (i = 0; i < tempSize; i++) {
                indexOutput.writeBytes((byte[]) file.buffers.get(i), bufferSize);
            }
            int leftOver = (int) (file.length % bufferSize);
            if (leftOver == 0) {
                indexOutput.writeBytes((byte[]) file.buffers.get(i), bufferSize);
            } else {
                indexOutput.writeBytes((byte[]) file.buffers.get(i), leftOver);
            }
        }

        /**
         * Writes a single byte.
         *
         * @see IndexInput#readByte()
         */
        public void writeByte(byte b) throws IOException {
            if (bufferPosition >= bufferSize) {
                flush();
            }
            buffer[bufferPosition++] = b;
        }

        /**
         * Writes an array of bytes.
         *
         * @param b      the bytes to write
         * @param length the number of bytes to write
         * @see IndexInput#readBytes(byte[],int,int)
         */
        public void writeBytes(byte[] b, int offset, int length) throws IOException {
            int bytesLeft = bufferSize - bufferPosition;
            // is there enough space in the buffer?
            if (bytesLeft >= length) {
                // we add the data to the end of the buffer
                System.arraycopy(b, offset, buffer, bufferPosition, length);
                bufferPosition += length;
                // if the buffer is full, flush it
                if (bufferSize - bufferPosition == 0) {
                    flush();
                }
            } else {
                // is data larger then buffer?
                if (length > bufferSize) {
                    // we flush the buffer
                    if (bufferPosition > 0) {
                        flush();
                    }
                    // and write data at once
                    flushBuffer(b, offset, length);
                    bufferStart += length;
                } else {
                    // we fill/flush the buffer (until the input is written)
                    int pos = 0; // position in the input data
                    int pieceLength;
                    while (pos < length) {
                        pieceLength = (length - pos < bytesLeft) ? length - pos : bytesLeft;
                        System.arraycopy(b, pos + offset, buffer, bufferPosition, pieceLength);
                        pos += pieceLength;
                        bufferPosition += pieceLength;
                        // if the buffer is full, flush it
                        bytesLeft = bufferSize - bufferPosition;
                        if (bytesLeft == 0) {
                            flush();
                            bytesLeft = bufferSize;
                        }
                    }
                }
            }
        }

        @Override
        public void flush() throws IOException {
            flushBuffer(buffer, bufferPosition);
            bufferStart += bufferPosition;
            bufferPosition = 0;

        }

        @Override
        public void close() throws IOException {
            flush();
        }

        @Override
        public long getFilePointer() {
            return bufferStart + bufferPosition;
        }
    }

    public static class DataBlock implements DataNode, Serializable {

        private static final long serialVersionUID = 1L;
        private UUID id;
        private transient NameNode node;
        private byte[] data;

        public void setId(UUID id) {
            this.id = id;
        }

        public UUID getId() {
            return id;
        }

        public void init(NameNode nameNode) {
            this.node = nameNode;
        }

        public void onDeleted(NameNode nameNode) {
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        private long length() {
            return data.length;
        }
    }
}
