package org.simpleframework.http.parse;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ContentTypeParserTest {

    private ContentTypeParser type;

    @BeforeMethod
    protected void setUp() {
        type = new ContentTypeParser();
    }

    @Test
    public void testEmpty() {
        AssertJUnit.assertEquals(null, type.getPrimary());
        AssertJUnit.assertEquals(null, type.getSecondary());
        AssertJUnit.assertEquals(null, type.getCharset());
    }

    @Test
    public void testPlain() {
        type.parse("text/html");
        AssertJUnit.assertEquals("text", type.getPrimary());
        AssertJUnit.assertEquals("html", type.getSecondary());

        type.setSecondary("plain");
        AssertJUnit.assertEquals("text", type.getPrimary());
        AssertJUnit.assertEquals("plain", type.getSecondary());
    }

    @Test
    public void testCharset() {
        type.parse("text/html; charset=UTF-8");
        AssertJUnit.assertEquals("text", type.getPrimary());
        AssertJUnit.assertEquals("UTF-8", type.getCharset());
        AssertJUnit.assertEquals("text/html", type.getType());

        type.setCharset("ISO-8859-1");
        AssertJUnit.assertEquals("ISO-8859-1", type.getCharset());
    }

    @Test
    public void testIgnore() {
        type.parse("text/html; name=value; charset=UTF-8; property=value");
        AssertJUnit.assertEquals("UTF-8", type.getCharset());
        AssertJUnit.assertEquals("html", type.getSecondary());
    }

    @Test
    public void testFlexibility() {
        type.parse("    text/html  ;charset=   UTF-8 ;  name =     value" );
        AssertJUnit.assertEquals("text", type.getPrimary());
        AssertJUnit.assertEquals("html", type.getSecondary());
        AssertJUnit.assertEquals("text/html", type.getType());
        AssertJUnit.assertEquals("UTF-8", type.getCharset());
    }

    @Test
    public void testString() {
        type.parse("  image/gif; name=value");
        AssertJUnit.assertEquals("image/gif; name=value", type.toString());

        type.parse(" text/html; charset =ISO-8859-1");
        AssertJUnit.assertEquals("text/html; charset=ISO-8859-1", type.toString());
        AssertJUnit.assertEquals("text/html", type.getType());

        type.setSecondary("css");
        AssertJUnit.assertEquals("text", type.getPrimary());
        AssertJUnit.assertEquals("css", type.getSecondary());
        AssertJUnit.assertEquals("text/css", type.getType());
        AssertJUnit.assertEquals("text/css; charset=ISO-8859-1", type.toString());

        type.setPrimary("image");
        AssertJUnit.assertEquals("image", type.getPrimary());
        AssertJUnit.assertEquals("css", type.getSecondary());
        AssertJUnit.assertEquals("image/css", type.getType());
    }
}
