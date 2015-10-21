package org.simpleframework.transport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class TransportCursorTest {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final String SOURCE = ALPHABET + "\r\n";

    @Test
    public void testCursor() throws IOException {
        final byte[] data = SOURCE.getBytes("ISO-8859-1");
        final InputStream source = new ByteArrayInputStream(data);
        final Transport transport = new StreamTransport(source, System.out);
        final ByteCursor cursor = new TransportCursor(transport);
        final byte[] buffer = new byte[1024];

        AssertJUnit.assertEquals(cursor.ready(), data.length);
        AssertJUnit.assertEquals(26, cursor.read(buffer, 0, 26));
        AssertJUnit.assertEquals(26, cursor.reset(26));
        AssertJUnit.assertEquals(new String(buffer, 0, 26), ALPHABET);

        AssertJUnit.assertEquals(cursor.ready(), data.length);
        AssertJUnit.assertEquals(26, cursor.read(buffer, 0, 26));
        AssertJUnit.assertEquals(26, cursor.reset(26));
        AssertJUnit.assertEquals(new String(buffer, 0, 26), ALPHABET);

        AssertJUnit.assertEquals(cursor.ready(), data.length);
        AssertJUnit.assertEquals(4, cursor.read(buffer, 0, 4));
        AssertJUnit.assertEquals(4, cursor.reset(26));
        AssertJUnit.assertEquals(new String(buffer, 0, 4), "abcd");

        AssertJUnit.assertEquals(cursor.ready(), data.length);
        AssertJUnit.assertEquals(4, cursor.read(buffer, 0, 4));
        AssertJUnit.assertEquals(4, cursor.reset(26));
        AssertJUnit.assertEquals(new String(buffer, 0, 4), "abcd");

        AssertJUnit.assertEquals(cursor.ready(), data.length);
        AssertJUnit.assertEquals(4, cursor.read(buffer, 0, 4));
        AssertJUnit.assertEquals(new String(buffer, 0, 4), "abcd");

        AssertJUnit.assertEquals(cursor.ready(), data.length - 4);
        AssertJUnit.assertEquals(4, cursor.read(buffer, 0, 4));
        AssertJUnit.assertEquals(new String(buffer, 0, 4), "efgh");

        AssertJUnit.assertEquals(cursor.ready(), data.length - 8);
        AssertJUnit.assertEquals(4, cursor.read(buffer, 0, 4));
        AssertJUnit.assertEquals(new String(buffer, 0, 4), "ijkl");

        AssertJUnit.assertEquals(cursor.ready(), data.length - 12);
        AssertJUnit.assertEquals(12, cursor.reset(12));
        AssertJUnit.assertEquals(10, cursor.read(buffer, 0, 10));
        AssertJUnit.assertEquals(new String(buffer, 0, 10), "abcdefghij");

        cursor.push("1234".getBytes("ISO-8859-1"));
        cursor.push("5678".getBytes("ISO-8859-1"));
        cursor.push("90".getBytes("ISO-8859-1"));

        AssertJUnit.assertEquals(cursor.ready(), 10);
        AssertJUnit.assertEquals(2, cursor.read(buffer, 0, 2));
        AssertJUnit.assertEquals(new String(buffer, 0, 2), "90");

        AssertJUnit.assertEquals(cursor.ready(), 8);
        AssertJUnit.assertEquals(4, cursor.read(buffer, 0, 4));
        AssertJUnit.assertEquals(new String(buffer, 0, 4), "5678");

        AssertJUnit.assertEquals(cursor.ready(), 4);
        AssertJUnit.assertEquals(4, cursor.read(buffer, 0, 4));
        AssertJUnit.assertEquals(new String(buffer, 0, 4), "1234");

        AssertJUnit.assertEquals(4, cursor.reset(4));
        AssertJUnit.assertEquals(cursor.ready(), 4);
        AssertJUnit.assertEquals(4, cursor.read(buffer, 0, 4));
        AssertJUnit.assertEquals(new String(buffer, 0, 4), "1234");

        AssertJUnit.assertEquals(8, cursor.read(buffer, 0, 8));
        AssertJUnit.assertEquals(new String(buffer, 0, 8), "klmnopqr");
    }

}
