package org.simpleframework.http.parse;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ContentDispositionParserTest {

    private ContentDispositionParser parser;

    @BeforeMethod
    public void setUp() {
        parser = new ContentDispositionParser();
    }

    @Test
    public void testDisposition() {
        parser.parse("form-data; name=\"input_check\"");

        AssertJUnit.assertFalse(parser.isFile());
        AssertJUnit.assertEquals(parser.getName(), "input_check");

        parser.parse("form-data; name=\"input_password\"");

        AssertJUnit.assertFalse(parser.isFile());
        AssertJUnit.assertEquals(parser.getName(), "input_password");

        parser.parse("form-data; name=\"FileItem\"; filename=\"C:\\Inetpub\\wwwroot\\Upload\\file1.txt\"");

        AssertJUnit.assertTrue(parser.isFile());
        AssertJUnit.assertEquals(parser.getName(), "FileItem");
        AssertJUnit.assertEquals(parser.getFileName(), "C:\\Inetpub\\wwwroot\\Upload\\file1.txt");

    }
}
