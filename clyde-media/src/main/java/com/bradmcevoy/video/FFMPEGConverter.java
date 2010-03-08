
package com.bradmcevoy.video;

import com.bradmcevoy.common.ScriptExecutor;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamToStream;
import com.bradmcevoy.io.WritingException;
import com.bradmcevoy.utils.FileUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FFMPEGConverter implements Converter{
    
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(FFMPEGConverter.class);
    
    private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));    
    
    final InputStream in;
    final File source;
    final String inputFormat;

    public FFMPEGConverter( InputStream in, String inputFormat) {
        this.in = in;
        this.inputFormat = inputFormat;
        source = createSourceFile(in, inputFormat);
    }
    
    public void close() {
        if( source.exists() ) source.delete();
    }
    
    public Long generateThumb(int height, int width, OutputStream out, String outputFormat) {
        log.debug("generateThumb");        
        File dest = getDestThumbFile(outputFormat);
        log.debug(" converting: " + source.getAbsolutePath() + "(" + source.length() + ") to: " + dest.getAbsolutePath());
        String process = "ffmpeg";
        String dimensions = width+"x" + height;
        //String[]  args = {"-i",source.getAbsolutePath(),"-s",dimensions,"-ss","s","-vframes","1","-f","mjpeg",dest.getAbsolutePath()};
        String[]  args = {"-i",source.getAbsolutePath(),"-s",dimensions,"-ss","1","-vframes","1","-f","mjpeg",dest.getAbsolutePath()};
        int successCode = 0;
        ScriptExecutor exec = new ScriptExecutor(process, args, successCode);
        exec.exec();

        if( !dest.exists() ) throw new RuntimeException("Conversion failed. Dest temp file was not created");
        if( dest.length() == 0 ) throw new RuntimeException("Conversion failed. Dest temp file has size zero.");
        
        log.debug(" ffmpeg ran ok. reading temp file back to out stream");
        FileInputStream tempIn = null;
        try {
            tempIn = new FileInputStream(dest);
            return StreamToStream.readTo(tempIn, out);
        } catch (ReadingException ex) {
            throw new RuntimeException(ex);
        } catch (WritingException ex) {
            throw new RuntimeException(ex);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } finally {
            FileUtils.close(tempIn);
            if( dest.exists() ) dest.delete();
        }        
    }

    public Long convert(OutputStream out, String outputFormat) {
        return convert(out, outputFormat, 0, 0, false);
    }
    
    public Long convert(OutputStream out, String outputFormat,int height, int width) {
        return convert(out, outputFormat, height, width, true);
    }
    
    private Long convert(OutputStream out, String outputFormat,int height, int width, boolean useDimensions) {
        log.debug("convert");        
        File dest = getDestFlvFile(outputFormat);
        log.debug(" converting: " + source.getAbsolutePath() + "(" + source.length() + ") to: " + dest.getAbsolutePath());
        String process = "ffmpeg";
        String[]  args;
        if( useDimensions ) {
            String dimensions = width+"x" + height;
            args = new String[]{"-i",source.getAbsolutePath(),"-s",dimensions,"-ar","22050",dest.getAbsolutePath()};                        
        } else {            
            args = new String[]{"-i",source.getAbsolutePath(),"-ar","22050",dest.getAbsolutePath()};
        }
        int successCode = 0;
        ScriptExecutor exec = new ScriptExecutor(process, args, successCode);
        exec.exec();

        if( !dest.exists() ) throw new RuntimeException("Conversion failed. Dest temp file was not created");
        if( dest.length() == 0 ) throw new RuntimeException("Conversion failed. Dest temp file has size zero.");
        
        log.debug(" ffmpeg ran ok. reading temp file back to out stream");
        FileInputStream tempIn = null;
        try {
            tempIn = new FileInputStream(dest);
            return StreamToStream.readTo(tempIn, out);
        } catch (ReadingException ex) {
            throw new RuntimeException(ex);
        } catch (WritingException ex) {
            throw new RuntimeException(ex);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } finally {
            FileUtils.close(tempIn);
            if( dest.exists() ) dest.delete();
        }
    }


    private File createSourceFile(InputStream in, String suffix) {
        File temp = null;
        FileOutputStream out = null;
        BufferedOutputStream out2 = null;
        try {
            temp = File.createTempFile("convert_vid_in_" + System.currentTimeMillis(), "." + suffix);
            out = new FileOutputStream(temp);
            out2 = new BufferedOutputStream(out);
            StreamToStream.readTo(in, out2);
            out.flush();
        } catch (IOException ex) {
            throw new RuntimeException("Writing to: " + temp.getAbsolutePath(),ex);
        } finally {
            FileUtils.close(out2);
            FileUtils.close(out);
        }
        return temp;
    }

    private static  File getDestFlvFile(String suffix) {
        return createTempFile("convert_vid_out_" + System.currentTimeMillis(), "." + suffix);
    }

    private static File getDestThumbFile(String suffix) {
        return createTempFile("convert_thumb_" + System.currentTimeMillis(), "." + suffix);
    }
    
    private static File createTempFile(String prefix, String suffix) {
        return new File(TEMP_DIR,prefix+suffix);
    }
}