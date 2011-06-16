package com.bradmcevoy.web;

import com.bradmcevoy.AbstractTest;
import com.bradmcevoy.TestUtils;
import com.bradmcevoy.common.Path;
import com.bradmcevoy.context.Context;
import com.bradmcevoy.context.Executable;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.utils.FileUtils;
import com.bradmcevoy.vfs.NameNode;
import com.bradmcevoy.vfs.VfsSession;
import com.bradmcevoy.web.component.ComponentValue;
import com.bradmcevoy.web.component.HtmlDef;
import com.bradmcevoy.web.component.HtmlInput;
import com.bradmcevoy.web.component.TextDef;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestTemplating extends AbstractTest{
    public TestTemplating() {
        super("testtemplating");
    }
    
    public void test() {
        TestUtils.runTest( new Executable() { public Object execute(Context context) {
            doTest1(context);
            return null;
        } });
        TestUtils.runTest( new Executable() { public Object execute(Context context) {
            doUpdateTest(context);
            return null;
        } });
        TestUtils.runTest( new Executable() { public Object execute(Context context) {
            doUpdateCheck(context);
            return null;
        } });        
    }
    
    public void atest2() {
        TestUtils.runTest( new Executable() { public Object execute(Context context) {
            doTest2(context);
            return null;
        } });
    }
    
    private void doTest2(final Context context) {
        VfsSession vfs = context.get(VfsSession.class);
        Path path = Path.path("/test.bradmcevoy.com/index.html");
        NameNode nn = vfs.find(path);
        assertNotNull(nn);
        Page page = (Page) nn.getData();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            page.sendContent(out, null, null, null );
        } catch(BadRequestException e) {
            e.printStackTrace();
        } catch(NotAuthorizedException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("content: " + out.toString());
    }
    
    private void doTest1(final Context context) {
        VfsSession vfs = context.get(VfsSession.class);
        NameNode rootNameNode = vfs.root();
        NameNode hostNode = rootNameNode.child("test.ettrema.com");
        if( hostNode != null ) {
            System.out.println("deleting existing host node...");
            hostNode.delete();
            System.out.println("done delete");
        } else {
            System.out.println("did not find existing host node");
        }
        System.out.println("---------------creating new root");
        
        Folder root = new RootFolder(rootNameNode);
        root.save();
        
        Organisation org = new Organisation(root,"test.ettrema.com");
        org.save();
        
        User me = new User(org.getUsers(),"brad","fred");
        me.save();                
        
        Host host = new Host(org,"test.bradmcevoy.com");
        host.save();
                
        Folder templates = (Folder) host.getTemplates();        
        assertNotNull(templates);

        User u1 = new User(host.getUsers(),"u1","pwd");
        u1.save();
        
        Template rootTemplate = new Template(templates,"rootTemplate.html");
        rootTemplate.getComponentDefs().add( new HtmlDef(rootTemplate,"head",100,15) );
        rootTemplate.getComponentDefs().add( new HtmlDef(rootTemplate,"body",100,25) );
        rootTemplate.getComponentDefs().add( new HtmlDef(rootTemplate,"root",100,25) );
//        rootTemplate.components.add( new AddParameterComponent(rootTemplate));
        rootTemplate.getComponents().add(new HtmlInput(rootTemplate,"root","<html><head>@{invoke('head',false)}</head><body>@{doBody()}</body></html>"));
        //       rootTemplate.setBody("<html><head>@{invoke('head')}</head><body>@{invoke('body')}</body></html>");
//        rootTemplate.setEditBody("<html><body>@{doEditBody()}</body></html>");
//        rootTemplate.components.add( new SaveCommand("save") );
        rootTemplate.save();
        
        Template contentTemplate = null;//rootTemplate.createTemplateFromTemplate(templates,"contentTemplate.html");
        contentTemplate.getComponentDefs().add(new TextDef(contentTemplate,"title"));
        contentTemplate.getComponentDefs().add(new HtmlDef(contentTemplate,"body",100,25)); 
        contentTemplate.getValues().get("head").setValue(readTemplate("content.header.txt"));
        contentTemplate.getValues().get("body").setValue("<div id='content'><h1>@{invoke('title',false)}</h1><a href='@{targetPage.name}.edit'>Edit</a><br/>@{invokeForEdit('title')}<br/>@{doBody()}</div>");
        contentTemplate.save();
        
        Page page = null;//contentTemplate.createPageFromTemplate(host,"index.html");
        assertNotNull( page.getComponents().get("name") );
        page.getValues().get("title").setValue("index page");
        page.getValues().get("body").setValue("first page!");
        page.save();
        
        Web web = page.getWeb();
        assertEquals(host,web);
        assertNotNull(web);
        templates = (Folder) web.child("templates");
        assertNotNull(templates);
        templates = web.getTemplates();
        assertNotNull(templates);
        
        show(page);
        System.out.println("*************************************");
        EditPage edit = new EditPage(page);
//        show(edit);
        
        Map<String,String> params = new HashMap<String,String>();
        params.put("/index.html/body","new body");
        params.put("/index.html/title","new title");
        try {
        edit.processForm(params,null);
        } catch(NotAuthorizedException e) {
            throw new RuntimeException( e );
        }
        System.out.println("----------------- showing edit page after submit ----------------------");
  //      show(edit);
    }
    
    String readTemplate(String name) {
        try {
            return FileUtils.readIn(this.getClass().getResourceAsStream(name)).toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void show(File page) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            page.sendContent(out,null,null,null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("content: " + out.toString());
    }
    
    public void doUpdateTest(final Context context) {
        VfsSession vfs = context.get(VfsSession.class);
        List<NameNode> list = vfs.find(Page.class,"index.html");
        assertNotNull(list);
        assertEquals(1,list.size());
        NameNode nn = list.get(0);
        assertNotNull(nn);
        Page page = (Page) nn.getData();
        ComponentValue cv = page.getValues().get("title");
        cv.setValue("index page 2");
        page.save();
        vfs.commit();
    }
    
    public void doUpdateCheck(final Context context) {
        VfsSession vfs = context.get(VfsSession.class);
        NameNode nn = vfs.find(Path.path("test.ettrema.com/test.bradmcevoy.com/index.html"));
        assertNotNull(nn);
        Page page = (Page) nn.getData();
        ComponentValue cv = page.getValues().get("title");
        assertEquals("index page 2",cv.getValue());
    }
}
