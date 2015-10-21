package org.simpleframework.http.core;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.simpleframework.transport.Socket;
import org.simpleframework.transport.SocketProcessor;

class TicketProcessor implements SocketProcessor {

    private final SocketProcessor delegate;

    public TicketProcessor(SocketProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void process(Socket pipe) throws IOException {
        final SocketChannel channel = pipe.getChannel();
        final int port = channel.socket().getPort();

        pipe.getAttributes().put(Ticket.KEY,new Ticket(port));
        delegate.process(pipe);
    }

    @Override
    public void stop() throws IOException {
        delegate.stop();
    }
}