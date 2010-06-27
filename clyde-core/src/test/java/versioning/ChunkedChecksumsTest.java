package versioning;

import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.Checksum;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class ChunkedChecksumsTest extends TestCase {
    
    public ChunkedChecksumsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCalcChunks() throws ReadingException, WritingException {
        String s = "abcdefghijklnnopabcdefghijklnnopabcdefghijklnnopabcdefghijklnnopabcdefghijklnnopabcdefghijklnnopabcdefghijklnnopabcdefghijklnnop";
        System.out.println("s: " + s.length() + " --> " + s.length()/5);
        ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes());
        ChunkedChecksums inChunks = new ChunkedChecksums(in, 5);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtils.readTo(inChunks, out);
        List<Checksum> crcs = inChunks.getCheckSums();
        System.out.println("crcs: " + crcs.size());
        for( Checksum cs : crcs) {
            System.out.println("crc: " + cs.getValue());
        }
    }

}
