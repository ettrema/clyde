package com.bradmcevoy.media;

import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.utils.FileUtils;
import com.bradmcevoy.web.BaseResource;
import com.bradmcevoy.web.CommonTemplated;
import com.bradmcevoy.web.Folder;
import com.bradmcevoy.web.ImageFile;
import com.ettrema.context.Context;
import com.ettrema.grid.Processable;
import com.ettrema.vfs.DataNode;
import com.ettrema.vfs.NameNode;
import com.ettrema.vfs.OutputStreamWriter;
import com.ettrema.vfs.VfsSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.UUID;
//import org.xhtmlrenderer.simple.Graphics2DRenderer;
//import org.xhtmlrenderer.util.DownscaleQuality;
//import org.xhtmlrenderer.util.ImageUtil;
//import org.xhtmlrenderer.util.ScalingOptions;

public class PageImageRenderer implements Processable, Serializable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PageImageRenderer.class);
    private static final long serialVersionUID = 1L;
    final String pageName;
    final UUID pageNameNodeId;

    public PageImageRenderer(UUID pageNameNodeId, String pageName) {
        this.pageName = pageName;
        this.pageNameNodeId = pageNameNodeId;
    }

    public void doProcess(Context context) {
        log.debug("doing preview generation for: " + this.pageName);
        try {
            generate(context);
        } catch (IOException ex) {
            log.error("Exception generating preview", ex);
        }
    }

    void generate(Context context) throws IOException {
        VfsSession vfs = context.get(VfsSession.class);
        NameNode pageNameNode = vfs.get(pageNameNodeId);
        DataNode dn = pageNameNode.getData();
        if (dn == null) {
            log.warn("Could not find target: " + pageNameNodeId);
            return;
        }
        CommonTemplated targetPage;
        if (dn instanceof CommonTemplated) {
            targetPage = (CommonTemplated) dn;
        } else {
            log.warn("Target page is not of type CommonTemplated. Is a: " + dn.getClass().getName());
            return;
        }
        generate(targetPage);
        vfs.commit();
        log.debug("done and committed");
    }

    
    static void generate(CommonTemplated targetPage) throws IOException {
        Folder templatesFolder = targetPage.getParentFolder();
        ByteArrayOutputStream out = new ByteArrayOutputStream(5000);
        try {
            targetPage.sendContent( out, null, null, null );
        } catch(BadRequestException e) {
            throw new RuntimeException( e );
        } catch( NotAuthorizedException e ) {
            throw new RuntimeException( e );
        }
        final String html = out.toString();

        String previewName = targetPage.getName() + ".preview.jpg";
        BaseResource r = templatesFolder.childRes(previewName);
        if (r != null) {
            r.delete();
        }
        ImageFile preview = new ImageFile("image/jpeg", templatesFolder, previewName);
        preview.save();

        String s = targetPage.getHref();
        if( "true".equals(System.getProperty("simulate.server") ) ) {
            s = s.replace("www.", "test.");
            log.warn("Changing url to: " + s);
        }
        final String url = s;
        InputStream in = null;
        try {
            preview.useOutputStream(new OutputStreamWriter<Long>() {

                @Override
                public Long writeTo(final OutputStream out) {
                    return generate( out, url);
                }
            });
        } finally {
            FileUtils.close(in);
        }
        log.debug("..generated: " + preview.getHref());
    }

    static Long generate(OutputStream out, String url) {
        log.debug("generate: url: " + url);
        return null;
//        try {
//
//            Graphics2DRenderer renderer = new Graphics2DRenderer();
//            renderer.setDocument(url);
//            BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
//            Graphics2D imageGraphics = (Graphics2D) image.getGraphics();
//            imageGraphics.setColor(Color.white);
//            imageGraphics.fillRect(0, 0, 800, 600);
//            renderer.layout(imageGraphics, new Dimension(800, 600));
//            renderer.render(imageGraphics);
//
//            ScalingOptions scalingOptions = new ScalingOptions(
//                    250, 250,
//                    BufferedImage.TYPE_INT_ARGB,
//                    DownscaleQuality.LOW_QUALITY,
//                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
//
//            BufferedImage scaled = ImageUtil.getScaledInstance(scalingOptions, image);
//
//            CountingOutputStream countingOut = new CountingOutputStream(out);
//            ImageIO.write(scaled, "jpg", countingOut);
//            countingOut.flush();
//            return (long) countingOut.getCount();
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
////        } catch(SAXParseException ex) {
////            throw new RuntimeException("Exception parsing html. line: " + ex.getLineNumber() + "  col: " + ex.getColumnNumber(),ex);
////        } catch (SAXException ex) {
////            throw new RuntimeException(ex);
//        }
    }

    public void pleaseImplementSerializable() {
    }
} 
