package com.ettrema.web.velocity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

public class VelocityInterpreter {

    static {        
        try {
            Velocity.setProperty("resource.loader", "file,yadboro");
            Velocity.setProperty("yadboro.resource.loader.class", YadboroVelocityResourceLoader.class.getName());
            Velocity.setProperty("input.encoding","UTF-8");
            Velocity.setProperty("output.encoding","UTF-8");
			Velocity.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
            Velocity.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String evalToString(String sTemplate,VelocityContext vc) {
        YadboroVelocityResourceLoader.setCurrentTemplate(sTemplate);
        try {
            Template template = null;

            try {
                template = Velocity.getTemplate(".");
            } catch (ResourceNotFoundException e) {
                throw new RuntimeException(e);
            } catch (ParseErrorException e) {
                throw new RuntimeException(e);
            } catch (MethodInvocationException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            StringWriter sw = new StringWriter();

            template.merge(vc, sw);
            String result = sw.toString();
            return result;
        } catch (ResourceNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (ParseErrorException ex) {
            throw new RuntimeException(ex);
        } catch (MethodInvocationException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static void evalToStream(String sTemplate,VelocityContext vc, OutputStream out ) {        
        YadboroVelocityResourceLoader.setCurrentTemplate(sTemplate);
        try {
            Template template = null;

            try {
                template = Velocity.getTemplate(".");
            } catch (ResourceNotFoundException e) {
                throw new RuntimeException(e);
            } catch (ParseErrorException e) {
                throw new RuntimeException(e);
            } catch (MethodInvocationException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            Writer w =  new OutputStreamWriter(out);
            template.merge(vc, w);
            w.flush();
            w.close();
        } catch (ResourceNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (ParseErrorException ex) {
            throw new RuntimeException(ex);
        } catch (MethodInvocationException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
    }
}
