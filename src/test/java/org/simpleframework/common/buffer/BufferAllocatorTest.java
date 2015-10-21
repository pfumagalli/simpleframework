package org.simpleframework.common.buffer;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class BufferAllocatorTest {

    @Test
    public void testBuffer() throws Exception {
        Allocator allocator = new ArrayAllocator(1, 2);
        Buffer buffer = new BufferAllocator(allocator, 1, 2);

        buffer.append(new byte[]{'a'}).append(new byte[]{'b'});

        AssertJUnit.assertEquals(buffer.encode(), "ab");
        AssertJUnit.assertEquals(buffer.encode("ISO-8859-1"), "ab");

        boolean overflow = false;

        try {
            buffer.append(new byte[]{'c'});
        } catch(final Exception e) {
            overflow = true;
        }
        AssertJUnit.assertTrue(overflow);

        buffer.clear();

        AssertJUnit.assertEquals(buffer.encode(), "");
        AssertJUnit.assertEquals(buffer.encode("UTF-8"), "");

        allocator = new ArrayAllocator(1024, 2048);
        buffer = new BufferAllocator(allocator, 1024, 2048);
        buffer.append("abcdefghijklmnopqrstuvwxyz".getBytes());

        final Buffer alphabet = buffer.allocate();
        alphabet.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());

        final Buffer digits = buffer.allocate();
        digits.append("0123456789".getBytes());

        AssertJUnit.assertEquals(alphabet.encode(), "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        AssertJUnit.assertEquals(digits.encode(), "0123456789");
        AssertJUnit.assertEquals(buffer.encode(), "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");

        final Buffer extra = digits.allocate();
        extra.append("#@?".getBytes());

        AssertJUnit.assertEquals(extra.encode(), "#@?");
        AssertJUnit.assertEquals(extra.length(), 3);
        AssertJUnit.assertEquals(digits.encode(), "0123456789#@?");
        AssertJUnit.assertEquals(digits.length(), 13);
        AssertJUnit.assertEquals(buffer.encode(), "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789#@?");
        AssertJUnit.assertEquals(buffer.length(), 65);
    }

    @Test
    public void testCascadingBufferAllocator() throws Exception {
        Allocator allocator = new ArrayAllocator(1024, 2048);
        allocator = new BufferAllocator(allocator, 1024, 2048);
        allocator = new BufferAllocator(allocator, 1024, 2048);
        allocator = new BufferAllocator(allocator, 1024, 2048);
        allocator = new BufferAllocator(allocator, 1024, 2048);

        final Buffer buffer = allocator.allocate(1024);

        buffer.append("abcdefghijklmnopqrstuvwxyz".getBytes());

        AssertJUnit.assertEquals(buffer.encode(), "abcdefghijklmnopqrstuvwxyz");

        buffer.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());

        AssertJUnit.assertEquals(buffer.encode(), "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        AssertJUnit.assertEquals(buffer.length(), 52);
    }

}
