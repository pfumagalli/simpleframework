package org.simpleframework.http.parse;

import org.simpleframework.http.Query;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AddressParserTest {

    private AddressParser link;

    @BeforeMethod
    protected void setUp() {
        link = new AddressParser();
    }

    @Test
    public void testEmptyPath() {
        AssertJUnit.assertEquals("/", link.getPath().toString());
    }

    @Test
    public void testEmptyQuery() {
        final Query query = link.getQuery();
        AssertJUnit.assertEquals(0, query.size());
    }

    @Test
    public void testPath() {
        link.parse("/this/./is//some/relative/./hidden/../URI.txt");
        AssertJUnit.assertEquals("/this/is//some/relative/URI.txt", link.getPath().toString());

        link.parse("/this//is/a/simple/path.html?query");
        AssertJUnit.assertEquals("/this//is/a/simple/path.html", link.getPath().toString());
    }

    @Test
    public void testQuery() {
        link.parse("/?name=value&attribute=string");

        final Query query = link.getQuery();

        AssertJUnit.assertEquals(2, query.size());
        AssertJUnit.assertEquals("value", query.get("name"));
        AssertJUnit.assertTrue(query.containsKey("attribute"));

        query.clear();
        query.put("name", "change");

        AssertJUnit.assertEquals("change", query.get("name"));
    }

    @Test
    public void testPathParameters() {
        link.parse("/index.html;jsessionid=1234567890?jsessionid=query");
        AssertJUnit.assertEquals("1234567890", link.getParameters().get("jsessionid"));

        link.parse("/path/index.jsp");
        link.getParameters().put("jsessionid", "value");

        AssertJUnit.assertEquals("/path/index.jsp;jsessionid=value", link.toString());

        link.parse("/path");
        link.getParameters().put("a", "1");
        link.getParameters().put("b", "2");
        link.getParameters().put("c", "3");

        link.parse(link.toString());

        AssertJUnit.assertEquals("1", link.getParameters().get("a"));
        AssertJUnit.assertEquals("2", link.getParameters().get("b"));
        AssertJUnit.assertEquals("3", link.getParameters().get("c"));


    }

    @Test
    public void testAbsolute() {
        link.parse("http://domain:9090/index.html?query=value");
        AssertJUnit.assertEquals("domain", link.getDomain());

        link.setDomain("some.domain");
        AssertJUnit.assertEquals("some.domain", link.getDomain());
        AssertJUnit.assertEquals("http://some.domain:9090/index.html?query=value", link.toString());
        AssertJUnit.assertEquals(9090, link.getPort());

        link.parse("domain.com:80/index.html?a=b&c=d");
        AssertJUnit.assertEquals("domain.com", link.getDomain());
        AssertJUnit.assertEquals(80, link.getPort());

        link.parse("https://secure.com/index.html");
        AssertJUnit.assertEquals("https", link.getScheme());
        AssertJUnit.assertEquals("secure.com", link.getDomain());

        link.setDomain("www.google.com:45");
        AssertJUnit.assertEquals("www.google.com", link.getDomain());
        AssertJUnit.assertEquals("https://www.google.com:45/index.html", link.toString());
        AssertJUnit.assertEquals(45, link.getPort());
    }
}
