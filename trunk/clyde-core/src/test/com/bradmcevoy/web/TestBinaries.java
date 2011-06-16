package com.bradmcevoy.web;

import com.bradmcevoy.AbstractTest;
import com.bradmcevoy.TestUtils;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.context.Context;
import com.bradmcevoy.context.Executable;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.vfs.DataNode;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsSession;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class TestBinaries extends AbstractTest{
    public TestBinaries() {
        super("testtemplating");
    }
    
    public void test() {
        TestUtils.runTest( new Executable() {
            @Override
            public Object execute(Context context) {
                doTest1(context);
                return null;
        } });
    }
    
    
    private void doTest1(final Context context) {
        try {
            VfsSession vfs = context.get(VfsSession.class);
            NameNode nn = vfs.find(Path.path("test.ettrema.com/test.bradmcevoy.com/templates/test.jpg"));
            if( nn != null ) nn.delete();
            
            nn = vfs.find(Path.path("test.ettrema.com/test.bradmcevoy.com/templates"));
            assertNotNull(nn);
            DataNode dn = nn.getData();
            assertNotNull(dn);
            Folder folder = (Folder)dn;
            BinaryFile bf = new BinaryFile("image/jpeg",folder,"test.jpg");
            bf.save();
            InputStream in = this.getClass().getResourceAsStream("daisies.jpg");
            bf.setContent(in);
            int size = (int)bf.getContentLength().longValue();
            assertEquals(22358,size );
            assertEquals( 22358,(long)bf.getContentLength() );
            
            in = bf.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamUtils.readTo(in,out);
            byte[] arr = out.toByteArray();
            assertEquals( bf.getContentLength(), (Long)(long)arr.length );
            System.out.println("read: " + arr.length);
            
            vfs.commit();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
