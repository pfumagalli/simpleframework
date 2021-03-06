package org.simpleframework.transport;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.simpleframework.transport.trace.MockTrace;
import org.simpleframework.transport.trace.Trace;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class SocketBufferTest {


    @Test
    public void testBulkWrite() throws Exception {
        final ServerBuffer reader = new ServerBuffer();
        final SocketAddress address = new InetSocketAddress("localhost", reader.getPort());
        final SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false); // underlying socket must be non-blocking
        channel.connect(address);

        while(!channel.finishConnect()) { // wait to finish connection
            Thread.sleep(10);
        };
        final Trace trace = new MockTrace();
        final SocketWrapper wrapper = new SocketWrapper(channel,  trace);
        final SocketBuffer builder = new SocketBuffer(wrapper, 100, 4096);

        for(int i = 0; i < 10000; i++){
            final ByteBuffer buf = ByteBuffer.wrap(("message-"+i+"\n").getBytes());

            if(i > 18) {
                System.err.println("FAIL......."+i);
            }
            if(!builder.write(buf)){
                while(!builder.flush()) {
                    System.err.println("FLUSHING!!!");
                    Thread.sleep(1);
                }
            }
        }
        while(!builder.flush()) {
            System.err.println("FLUSHING!!!");
        }
        builder.close();
        reader.awaitClose();

        final String data = reader.getBuffer().toString();
        final String[] list = data.split("\\n");

        for(int i = 0; i < 10000; i++){
            final String msg = list[i];
            if(!msg.equals("message-"+i)) {
                System.err.println(list[i]);
            }
            AssertJUnit.assertEquals("At index " + i + " value="+list[i] +" expect message-"+i, list[i], "message-"+i);
        }
    }

    @Test
    public void testSimpleWrite() throws Exception {
        final ServerBuffer reader = new ServerBuffer();
        final SocketAddress address = new InetSocketAddress("localhost", reader.getPort());
        final SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false); // underlying socket must be non-blocking
        channel.connect(address);

        while(!channel.finishConnect()) { // wait to finish connection
            Thread.sleep(10);
        };
        final Trace trace = new MockTrace();
        final SocketWrapper wrapper = new SocketWrapper(channel,  trace);
        final SocketBuffer builder = new SocketBuffer(wrapper, 100, 4096);

        builder.write(ByteBuffer.wrap("hello there ".getBytes()));
        builder.write(ByteBuffer.wrap("this ".getBytes()));
        builder.write(ByteBuffer.wrap("is ".getBytes()));
        builder.write(ByteBuffer.wrap("a ".getBytes()));
        builder.write(ByteBuffer.wrap("test".getBytes()));
        builder.flush();
        builder.close();
        reader.awaitClose();

        AssertJUnit.assertEquals(reader.getBuffer().toString(), "hello there this is a test");
    }
}
