package org.simpleframework.http.message;

import java.io.ByteArrayInputStream;

import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.http.core.DribbleCursor;
import org.simpleframework.http.core.StreamCursor;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BoundaryConsumerTest {

    private static final byte[] TERMINAL = { '-', '-', 'A', 'a', 'B', '0', '3', 'x', '-', '-', '\r', '\n', 'X', 'Y' };

    private static final byte[] NORMAL = { '-', '-', 'A', 'a', 'B', '0', '3', 'x', '\r', '\n', 'X', 'Y' };

    private static final byte[] BOUNDARY = { 'A', 'a', 'B', '0', '3', 'x' };

    private BoundaryConsumer boundary;

    @BeforeMethod
    public void setUp() {
        boundary = new BoundaryConsumer(new ArrayAllocator(), BOUNDARY);
    }

    @Test
    public void testBoundary() throws Exception {
        final StreamCursor cursor = new StreamCursor(new ByteArrayInputStream(NORMAL));

        while(!boundary.isFinished()) {
            boundary.consume(cursor);
        }
        AssertJUnit.assertEquals(cursor.read(), 'X');
        AssertJUnit.assertEquals(cursor.read(), 'Y');
        AssertJUnit.assertTrue(boundary.isFinished());
        AssertJUnit.assertFalse(boundary.isEnd());
        AssertJUnit.assertFalse(cursor.isReady());
    }

    @Test
    public void testTerminal() throws Exception {
        final StreamCursor cursor = new StreamCursor(new ByteArrayInputStream(TERMINAL));

        while(!boundary.isFinished()) {
            boundary.consume(cursor);
        }
        AssertJUnit.assertEquals(cursor.read(), 'X');
        AssertJUnit.assertEquals(cursor.read(), 'Y');
        AssertJUnit.assertTrue(boundary.isFinished());
        AssertJUnit.assertTrue(boundary.isEnd());
        AssertJUnit.assertFalse(cursor.isReady());
    }

    @Test
    public void testDribble() throws Exception {
        DribbleCursor cursor = new DribbleCursor(new StreamCursor(new ByteArrayInputStream(TERMINAL)), 3);

        while(!boundary.isFinished()) {
            boundary.consume(cursor);
        }
        AssertJUnit.assertEquals(cursor.read(), 'X');
        AssertJUnit.assertEquals(cursor.read(), 'Y');
        AssertJUnit.assertTrue(boundary.isFinished());
        AssertJUnit.assertTrue(boundary.isEnd());
        AssertJUnit.assertFalse(cursor.isReady());

        boundary.clear();

        cursor = new DribbleCursor(new StreamCursor(new ByteArrayInputStream(TERMINAL)), 1);

        while(!boundary.isFinished()) {
            boundary.consume(cursor);
        }
        AssertJUnit.assertEquals(cursor.read(), 'X');
        AssertJUnit.assertEquals(cursor.read(), 'Y');
        AssertJUnit.assertTrue(boundary.isFinished());
        AssertJUnit.assertTrue(boundary.isEnd());
        AssertJUnit.assertFalse(cursor.isReady());
    }
}
