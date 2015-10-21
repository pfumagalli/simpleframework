package org.simpleframework.transport;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;

import org.simpleframework.common.thread.ConcurrentExecutor;
import org.simpleframework.transport.reactor.ExecutorReactor;
import org.simpleframework.transport.reactor.Reactor;
import org.simpleframework.transport.trace.MockTrace;
import org.simpleframework.transport.trace.Trace;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class SocketTransportTest {


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
        final Executor executor = new ConcurrentExecutor(Runnable.class);
        final Reactor reactor = new ExecutorReactor(executor);
        final SocketTransport transport = new SocketTransport(wrapper,reactor);
        for(int i = 0; i < 10000; i++){
            transport.write(ByteBuffer.wrap(("message-"+i+"\n").getBytes()));
        }
        transport.close();
        reader.awaitClose();

        final String data = reader.getBuffer().toString();
        final String[] list = data.split("\\n");

        for(int i = 0; i < 10000; i++){
            if(!list[i].equals("message-"+i)) {
                System.err.println(list[i]);
            }
            AssertJUnit.assertEquals("At index " + i + " value="+list[i] +" expect message-"+i, list[i], "message-"+i);
        }
    }
}
