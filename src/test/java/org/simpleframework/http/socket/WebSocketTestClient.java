package org.simpleframework.http.socket;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class WebSocketTestClient {

    public static void main(String[] list) throws Exception {
        final Socket socket = new Socket("localhost", 80);
        final OutputStream out = socket.getOutputStream();
        final byte[] request = ("GET / HTTP/1.0\r\n\r\n").getBytes("ISO-8859-1");
        out.write(request);
        final InputStream in = socket.getInputStream();
        final byte[] chunk = new byte[1024];
        int count = 0;

        while((count = in.read(chunk)) != -1) {
            Thread.sleep(1000);
            System.err.write(chunk, 0, count);
        }


    }
}
