
package com.bradmcevoy.web;

import com.bradmcevoy.web.component.AbstractInput;
import com.bradmcevoy.web.component.Text;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.batik.transcoder.TranscoderException;

import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.jdom.Element;

public class SvgImageFile extends ImageFile {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SvgImageFile.class);
    
    private static final long serialVersionUID = 1L;
        
    public SvgImageFile(Folder parentFolder, String newName) {
        this("image/jpeg", parentFolder, newName);
    }
    
    public SvgImageFile(String contentType, Folder parentFolder, String newName) {
        super(contentType, parentFolder, newName);
        Text content = new Text(this, "content");
        content.setRows(20);
        content.setCols(80);
        this.componentMap.add(content);
    }

    @Override
    protected BaseResource newInstance(Folder parent, String newName) {
        return new SvgImageFile(parent, newName);
    }

    
    @Override
    public void loadFromXml(Element el) {
        super.loadFromXml(el);
    }

    AbstractInput<String> getContent() {
        return (AbstractInput<String>) this.componentMap.get("content");
    }
    
    @Override
    public void save() {
        super.save();
        generateImage();
    }

    private void generateImage() {
        try {            
            String s = getContent().getValue();
            if( s != null && s.length() > 0 ) {
                JPEGTranscoder t = new JPEGTranscoder();

                t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(.8));
                ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes());
                TranscoderInput input = new TranscoderInput(in);

                ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                TranscoderOutput output = new TranscoderOutput(ostream);

                t.transcode(input, output);

                ostream.flush();
                ostream.close();

                in = new ByteArrayInputStream(ostream.toByteArray());
                this.setContent(in);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (TranscoderException ex) {
            throw new RuntimeException(ex);
        } 
    }    
}
