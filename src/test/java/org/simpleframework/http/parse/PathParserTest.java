package org.simpleframework.http.parse;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PathParserTest {

    private PathParser path;

    @BeforeMethod
    protected void setUp() {
        path = new PathParser();
    }

    @Test
    public void testEmpty() {
        AssertJUnit.assertEquals(null, path.getPath());
        AssertJUnit.assertEquals(null, path.getExtension());
        AssertJUnit.assertEquals(null, path.getName());
    }

    @Test
    public void testSegments() {
        path.parse("/a/b/c/d");

        final String[] list = path.getSegments();

        AssertJUnit.assertEquals("a", list[0]);
        AssertJUnit.assertEquals("b", list[1]);
        AssertJUnit.assertEquals("c", list[2]);
        AssertJUnit.assertEquals("d", list[3]);
    }

    @Test
    public void testSubPath() {
        path.parse("/0/1/2/3/4/5/6/index.html");

        testSubPath(1);
        testSubPath(2);
        testSubPath(3);
        testSubPath(4);
        testSubPath(5);
        testSubPath(6);
        testSubPath(7);

        testSubPath(0,4);
        testSubPath(1,2);
        testSubPath(2,3);
        testSubPath(3,4);
        testSubPath(1,3);
        testSubPath(1,4);
        testSubPath(1,5);

        path.parse("/a/b/c/d/e/index.html");

        testSubPath(1,2);
        testSubPath(2,3);
        testSubPath(3,1);
        testSubPath(1,3);
    }

    @Test(enabled = false)
    private void testSubPath(int from) {
        System.err.printf("[%s] %s: %s%n", path, from, path.getPath(from));
    }

    @Test(enabled = false)
    private void testSubPath(int from, int to) {
        System.err.printf("[%s] %s, %s: %s%n", path, from, to, path.getPath(from, to));
    }

    @Test
    public void testDirectory() {
        path.parse("/some/directory/path/index.html");
        AssertJUnit.assertEquals("/some/directory/path/", path.getDirectory());

        path.parse("/some/path/README");
        AssertJUnit.assertEquals("/some/path/", path.getDirectory());
    }

    @Test
    public void testNormalization() {
        path.parse("/path/./../index.html");
        AssertJUnit.assertEquals("/", path.getDirectory());

        path.parse("/path/hidden/./index.html");
        AssertJUnit.assertEquals("/path/hidden/", path.getDirectory());

        path.parse("/path/README");
        AssertJUnit.assertEquals("/path/", path.getDirectory());
    }

    @Test
    public void testString() {
        path.parse("/some/path/../path/./to//a/file.txt");
        AssertJUnit.assertEquals("/some/path/to//a/file.txt", path.toString());
    }

    @Test
    public void testAIOB(){
        path.parse("/admin/ws");
        final String result = path.getRelative("/admin/ws/");
        final String expResult = null;
        AssertJUnit.assertEquals(expResult, result);
    }
}
