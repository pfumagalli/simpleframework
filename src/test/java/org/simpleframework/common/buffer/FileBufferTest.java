package org.simpleframework.common.buffer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class FileBufferTest {

    @Test
    public void  testFileBuffer() throws Exception {
        final File tempFile = File.createTempFile(FileBufferTest.class.getSimpleName(), null);
        final Buffer buffer = new FileBuffer(tempFile);
        buffer.append("abcdefghijklmnopqrstuvwxyz".getBytes());

        final Buffer alphabet = buffer.allocate();
        alphabet.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());

        final Buffer digits = buffer.allocate();
        digits.append("0123456789".getBytes());

        expect(buffer, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".getBytes());
        expect(alphabet, "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
        expect(digits, "0123456789".getBytes());
    }

    private void expect(Buffer buffer, byte[] expect) throws IOException {
        final InputStream result = buffer.open();

        for(int i  =0; i < expect.length; i++) {
            final byte octet = expect[i];
            final int value = result.read();

            if(value < 0) {
                throw new IOException("Buffer exhausted too early");
            }
            AssertJUnit.assertEquals(octet, (byte)value);
        }
        AssertJUnit.assertEquals(-1, result.read());
    }

}
