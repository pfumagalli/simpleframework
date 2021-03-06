package org.simpleframework.http.socket.table;

import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameListener;
import org.simpleframework.http.socket.FrameType;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.http.socket.Session;

public class WebSocketTableListener implements FrameListener {

    private final WebSocketTableUpdater updater;

    public WebSocketTableListener(WebSocketTableUpdater updater) {
        this.updater = updater;
    }

    @Override
    public void onFrame(Session socket, Frame frame) {
        final FrameType type = frame.getType();

        if((type != FrameType.PONG) && (type != FrameType.PING)) {

            if(type == FrameType.TEXT) {
                final String text = frame.getText();
                final String[] command = text.split(":");
                final String operation = command[0];
                final String parameters = command[1];
                final String[] values = parameters.split(",");

                if(values.length > 0) {
                    for(final String value : values) {
                        final String[] pair = value.split("=");

                        if(operation.equals("refresh")) {
                            updater.refresh(socket);
                        }else if(operation.equals("status")) {
                            System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + value);

                            if(pair[0].equals("sequence")) {
                                if(pair[1].indexOf("@") != -1) {
                                    final String time = pair[1].split("@")[1];
                                    final Long sent = Long.parseLong(time);

                                    System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> TIME RTT: " + (System.currentTimeMillis() - sent));
                                }
                            }
                        }
                    }
                }
            }
            System.err.println("onFrame(");
            System.err.println(frame.getText());
            System.err.println(")");
        }
    }

    @Override
    public void onError(Session socket, Exception cause) {
        System.err.println("onError(");
        cause.printStackTrace();
        System.err.println(")");
    }

    public void onOpen(Session socket) {
        System.err.println("onOpen(" + socket +")");
    }

    @Override
    public void onClose(Session session, Reason reason) {
        System.err.println("onClose(" + reason +" reason="+reason.getText()+" code="+reason.getCode()+")");
    }
}
