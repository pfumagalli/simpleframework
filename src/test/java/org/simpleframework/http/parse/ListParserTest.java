package org.simpleframework.http.parse;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ListParserTest {

    private ValueParser list;

    @BeforeMethod
    protected void setUp() {
        list = new ValueParser();
    }

    @Test
    public void testEmpty() {
        AssertJUnit.assertEquals(0, list.list().size());
    }

    @Test
    public void testQvalue() {
        list.parse("ISO-8859-1,utf-8;q=0.7,*;q=0.7");
        AssertJUnit.assertEquals(list.list().get(0), "ISO-8859-1");
        AssertJUnit.assertEquals(list.list().get(1), "utf-8");
        AssertJUnit.assertEquals(list.list().get(2), "*");
    }

    @Test
    public void testPlain() {
        list.parse("en-gb");
        AssertJUnit.assertEquals("en-gb", list.list().get(0));

        list.parse("en");
        AssertJUnit.assertEquals("en", list.list().get(0));
    }

    @Test
    public void testList() {
        list.parse("en-gb, en-us");
        AssertJUnit.assertEquals(2, list.list().size());
        AssertJUnit.assertEquals("en-gb", list.list().get(0));
        AssertJUnit.assertEquals("en-us", list.list().get(1));
    }

    @Test
    public void testOrder() {
        list.parse("en-gb, en-us");
        AssertJUnit.assertEquals(2, list.list().size());
        AssertJUnit.assertEquals("en-gb", list.list().get(0));
        AssertJUnit.assertEquals("en-us", list.list().get(1));

        list.parse("da, en-gb;q=0.8, en;q=0.7");
        AssertJUnit.assertEquals("da", list.list().get(0));
        AssertJUnit.assertEquals("en-gb", list.list().get(1));
        AssertJUnit.assertEquals("en", list.list().get(2));

        list.parse("fr;q=0.1, en-us;q=0.4, en-gb; q=0.8, en;q=0.7");
        AssertJUnit.assertEquals("en-gb", list.list().get(0));
        AssertJUnit.assertEquals("en", list.list().get(1));
        AssertJUnit.assertEquals("en-us", list.list().get(2));
        AssertJUnit.assertEquals("fr", list.list().get(3));

        list.parse("en;q=0.2, en-us;q=1.0, en-gb");
        AssertJUnit.assertEquals("en-gb", list.list().get(0));
        AssertJUnit.assertEquals("en-us", list.list().get(1));
        AssertJUnit.assertEquals("en", list.list().get(2));
    }

    @Test
    public void testRange() {
        list.parse("image/gif, image/jpeg, text/html");
        AssertJUnit.assertEquals(3, list.list().size());
        AssertJUnit.assertEquals("image/gif", list.list().get(0));
        AssertJUnit.assertEquals("text/html", list.list().get(2));

        list.parse("image/gif;q=1.0, image/jpeg;q=0.8, image/png;  q=1.0,*;q=0.1");
        AssertJUnit.assertEquals("image/gif", list.list().get(0));
        AssertJUnit.assertEquals("image/png", list.list().get(1));
        AssertJUnit.assertEquals("image/jpeg", list.list().get(2));

        list.parse("gzip;q=1.0, identity; q=0.5, *;q=0");
        AssertJUnit.assertEquals("gzip", list.list().get(0));
        AssertJUnit.assertEquals("identity", list.list().get(1));
    }

    @Test
    public void testFlexibility() {
        list.parse("last; quantity=1;q=0.001, first; text=\"a, b, c, d\";q=0.4");
        AssertJUnit.assertEquals(2, list.list().size());
        AssertJUnit.assertEquals("first; text=\"a, b, c, d\"", list.list().get(0));
        AssertJUnit.assertEquals("last; quantity=1", list.list().get(1));

        list.parse("image/gif, , image/jpeg, image/png;q=0.8, *");
        AssertJUnit.assertEquals(4, list.list().size());
        AssertJUnit.assertEquals("image/gif", list.list().get(0));
        AssertJUnit.assertEquals("image/jpeg", list.list().get(1));
        AssertJUnit.assertEquals("*", list.list().get(2));
        AssertJUnit.assertEquals("image/png", list.list().get(3));

        list.parse("first=\"\\\"a, b, c, d\\\", a, b, c, d\", third=\"a\";q=0.9,,second=2");
        AssertJUnit.assertEquals(3, list.list().size());
        AssertJUnit.assertEquals("first=\"\\\"a, b, c, d\\\", a, b, c, d\"", list.list().get(0));
        AssertJUnit.assertEquals("second=2", list.list().get(1));
        AssertJUnit.assertEquals("third=\"a\"", list.list().get(2));
    }
}
