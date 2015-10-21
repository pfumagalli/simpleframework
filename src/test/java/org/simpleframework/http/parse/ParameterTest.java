package org.simpleframework.http.parse;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ParameterTest {

    private QueryParser data;

    @BeforeMethod
    protected void setUp() {
        data = new QueryParser();
    }

    @Test
    public void testEmptyPath() {
        AssertJUnit.assertEquals(0, data.size());
    }

    @Test
    public void testValue() {
        data.parse("a=");

        AssertJUnit.assertEquals(1, data.size());
        AssertJUnit.assertEquals("", data.get("a"));

        data.parse("a=&b=c");

        AssertJUnit.assertEquals(2, data.size());
        AssertJUnit.assertEquals("", data.get("a"));
        AssertJUnit.assertEquals("c", data.get("b"));

        data.parse("a=b&c=d&e=f&");

        AssertJUnit.assertEquals(3, data.size());
        AssertJUnit.assertEquals("b", data.get("a"));
        AssertJUnit.assertEquals("d", data.get("c"));
        AssertJUnit.assertEquals("f", data.get("e"));

        data.clear();
        data.put("a", "A");
        data.put("c", "C");
        data.put("x", "y");

        AssertJUnit.assertEquals(3, data.size());
        AssertJUnit.assertEquals("A", data.get("a"));
        AssertJUnit.assertEquals("C", data.get("c"));
        AssertJUnit.assertEquals("y", data.get("x"));
    }

    @Test
    public void testValueList() {
        data.parse("a=1&a=2&a=3");

        AssertJUnit.assertEquals(data.size(), 1);
        AssertJUnit.assertEquals(data.getAll("a").size(), 3);
        AssertJUnit.assertEquals(data.getAll("a").get(0), "1");
        AssertJUnit.assertEquals(data.getAll("a").get(1), "2");
        AssertJUnit.assertEquals(data.getAll("a").get(2), "3");

        data.parse("a=b&c=d&c=d&a=1");

        AssertJUnit.assertEquals(data.size(), 2);
        AssertJUnit.assertEquals(data.getAll("a").size(), 2);
        AssertJUnit.assertEquals(data.getAll("a").get(0), "b");
        AssertJUnit.assertEquals(data.getAll("a").get(1), "1");
        AssertJUnit.assertEquals(data.getAll("c").size(), 2);
        AssertJUnit.assertEquals(data.getAll("c").get(0), "d");
        AssertJUnit.assertEquals(data.getAll("c").get(1), "d");

    }
}
