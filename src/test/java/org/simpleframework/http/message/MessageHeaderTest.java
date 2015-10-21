package org.simpleframework.http.message;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class MessageHeaderTest {

    @Test
    public void testMessage() {
        final MessageHeader header = new MessageHeader();
        header.addValue("A", "a");
        header.addValue("A", "b");
        header.addValue("A", "c");

        AssertJUnit.assertEquals(header.getValue("A"), "a");
        AssertJUnit.assertEquals(header.getValue("A", 0), "a");
        AssertJUnit.assertEquals(header.getValue("A", 1), "b");
        AssertJUnit.assertEquals(header.getValue("A", 2), "c");

        header.setValue("A", null);

        AssertJUnit.assertEquals(header.getValue("A"), null);
        AssertJUnit.assertEquals(header.getValue("A", 0), null);
        AssertJUnit.assertEquals(header.getValue("A", 1), null);
        AssertJUnit.assertEquals(header.getValue("A", 2), null);
        AssertJUnit.assertEquals(header.getValue("A", 3), null);
        AssertJUnit.assertEquals(header.getValue("A", 4), null);
        AssertJUnit.assertEquals(header.getValue("A", 5), null);

        header.setValue("A", "X");

        AssertJUnit.assertEquals(header.getValue("A"), "X");
        AssertJUnit.assertEquals(header.getValue("A", 0), "X");
        AssertJUnit.assertEquals(header.getValue("A", 1), null);

        header.addInteger("A", 1);

        AssertJUnit.assertEquals(header.getValue("A"), "X");
        AssertJUnit.assertEquals(header.getValue("A", 0), "X");
        AssertJUnit.assertEquals(header.getValue("A", 1), "1");
        AssertJUnit.assertEquals(header.getValue("A", 2), null);

        header.addValue("A", null);

        AssertJUnit.assertEquals(header.getValue("A"), "X");
        AssertJUnit.assertEquals(header.getValue("A", 0), "X");
        AssertJUnit.assertEquals(header.getValue("A", 1), "1");
        AssertJUnit.assertEquals(header.getValue("A", 2), null);
    }
}
