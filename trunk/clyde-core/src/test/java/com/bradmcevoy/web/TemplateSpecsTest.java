package com.bradmcevoy.web;

import com.bradmcevoy.web.TemplateSpecs.AllowTemplateSpec;
import com.bradmcevoy.web.TemplateSpecs.TemplateSpec;
import com.bradmcevoy.web.security.PermissionRecipient.Role;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author brad
 */
public class TemplateSpecsTest {

    @Test
    public void testFindApplicable() {
    }

    @Test
    public void testParse() {
        TemplateSpecs specs = TemplateSpecs.parse( "+normal -invoice" );
        assertNotNull( specs );
        assertEquals( 2, specs.size() );
        TemplateSpecs.AllowTemplateSpec allow = (AllowTemplateSpec) specs.get( 0 );
        assertNull( allow.createNewRole );
        assertNull( allow.editRole );
        assertEquals( "normal", allow.pattern );
        assertTrue( specs.get( 1 ) instanceof TemplateSpecs.DisallowTemplateSpec );
    }

    @Test
    public void testParseSpec() {
        TemplateSpecs.AllowTemplateSpec spec = (AllowTemplateSpec) TemplateSpecs.parseSpec( "+normal" );
        assertEquals( "normal", spec.pattern);

    }

    @Test
    public void testParseSpec_WithRoles() {
        String anon = Role.ANONYMOUS.name();
        String author = Role.AUTHOR.name();
        TemplateSpecs.AllowTemplateSpec spec = (AllowTemplateSpec) TemplateSpecs.parseSpec( "+normal(" + anon + "," + author + ")" );
        assertEquals( "normal", spec.pattern);
        assertEquals( Role.ANONYMOUS, spec.createNewRole);
        assertEquals( Role.AUTHOR, spec.editRole);

    }


    @Test
    public void testAdd() {
    }

    @Test
    public void testFindAllowed_Folder() {
    }

    @Test
    public void testFindAllowedDirect() {
    }

    @Test
    public void testFindAllowed_List() {
    }

    @Test
    public void testIsAllowed() {
    }

    @Test
    public void testFormat() {
        TemplateSpecs specs = new TemplateSpecs();
        specs.add( new AllowTemplateSpec( "normal", null, null));
        specs.add( new TemplateSpecs.DisallowTemplateSpec( "*"));
        assertEquals( "+normal -* ", specs.format());
    }

    @Test
    public void testFormat_WithRoles() {
        TemplateSpecs specs = new TemplateSpecs();
        specs.add( new AllowTemplateSpec( "normal", Role.ANONYMOUS, Role.AUTHOR));
        specs.add( new TemplateSpecs.DisallowTemplateSpec( "*"));
        assertEquals( "+normal(ANONYMOUS,AUTHOR) -* ", specs.format());
    }

}
