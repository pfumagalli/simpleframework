package org.simpleframework.http.socket.table;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

import org.simpleframework.http.socket.FrameChannel;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.WebSocketAnalyzer;
import org.simpleframework.http.socket.service.Service;
import org.simpleframework.transport.trace.TraceAnalyzer;

public class WebSocketTableUpdater extends Thread implements Service {

    private final Set<WebSocketTableSubscription> subscriptions;
    private final WebSocketTableListener listener;
    private final WebSocketTableRowChanger changer;
    private final WebSocketTableSweeper sweeper;
    private final WebSocketTable table;
    private final AtomicLong time;

    public WebSocketTableUpdater(String key, WebSocketTableSchema schema, WebSocketTableRowAnnotator annotator) {
        this.subscriptions = new CopyOnWriteArraySet<WebSocketTableSubscription>();
        this.table = new WebSocketTable(key, schema, annotator);
        this.sweeper = new WebSocketTableSweeper(table);
        this.changer = new WebSocketTableRowChanger(table);
        this.listener = new WebSocketTableListener(this);
        this.time = new AtomicLong();
    }

    public void refresh(Session session) {
        for(final WebSocketTableSubscription subscription : subscriptions) {
            final FrameChannel socket = subscription.getSocket();
            final FrameChannel other = session.getChannel();

            if(socket == other) {
                final AtomicLong timeStamp = subscription.getTimeStamp();
                timeStamp.set(0);
            }
        }
    }

    @Override
    public void run() {
        changer.start();

        while(true) {
            try {
                Thread.sleep(200);

                for(final WebSocketTableSubscription subscription : subscriptions) {
                    final FrameChannel socket = subscription.getSocket();
                    final AtomicLong timeStamp = subscription.getTimeStamp();
                    final AtomicLong sendCount = subscription.getSendCount();
                    final long before = System.currentTimeMillis();
                    final long time = timeStamp.get();
                    final long count = sendCount.get();

                    try {
                        final Map<WebSocketTableUpdateType, String> messages = sweeper.sweep(time - 1000, count);
                        final Set<WebSocketTableUpdateType> updates = messages.keySet();

                        for(final WebSocketTableUpdateType update : updates) {
                            final String message = messages.get(update);

                            if(message != null) {
                                socket.send(update.code + message);
                            }
                        }
                    } catch(final Exception e) {
                        e.printStackTrace();
                        subscriptions.remove(subscription);
                        socket.close();
                    } finally {
                        sendCount.getAndIncrement();
                        timeStamp.set(before);
                    }
                }
            } catch(final Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void connect(Session connection) {
        final FrameChannel socket = connection.getChannel();

        try {
            final WebSocketTableSubscription subscription = new WebSocketTableSubscription(socket);

            socket.register(listener);
            subscriptions.add(subscription);
            time.set(0);
            Thread.sleep(1000); // crap
            time.set(0);
        } catch(final Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] list) throws Exception {
        final TraceAnalyzer agent = new WebSocketAnalyzer();
        final Map<String, WebSocketTableColumnStyle> columns = new LinkedHashMap<String, WebSocketTableColumnStyle>();

        final WebSocketTableSchema schema = new WebSocketTableSchema(columns);
        columns.put("id", new WebSocketTableColumnStyle("id", "Id", "{id}", true, true));
        columns.put("bidOutrightVolume", new WebSocketTableColumnStyle("bidOutrightVolume", "$ B", "<div style='font-weight: bold; color: #0000ff; text-decoration: underline;'>{bidOutrightVolume}</a>", true, false));
        columns.put("bidOutright", new WebSocketTableColumnStyle("bidOutright", "Bid", "<div style='font-weight: bold; color: #0000ff; text-decoration: underline;'>{bidOutright}</a>", true, false));
        columns.put("offerOutright", new WebSocketTableColumnStyle("offerOutright", "Offer", "<div style='font-weight: bold; color: #ff0000; text-decoration: underline;'>{offerOutright}</a>", true, false));
        columns.put("offerOutrightVolume", new WebSocketTableColumnStyle("offerOutrightVolume", "$ O", "<div style='font-weight: bold; color: #ff0000; text-decoration: underline;'>{offerOutrightVolume}</a>", true, false));
        columns.put("product", new WebSocketTableColumnStyle("product", "Security", "<div style='font-weight: bold;'>{product}</div>", true, true));
        columns.put("bidEFPVolume", new WebSocketTableColumnStyle("bidEFPVolume", "$ B", "<div style='font-weight: bold; color: #0000ff; text-decoration: underline;'>{bidEFPVolume}</a>", true, false));
        columns.put("bidEFP", new WebSocketTableColumnStyle("bidEFP", "Bid", "<div style='font-weight: bold; color: #0000ff; text-decoration: underline;'>{bidEFP}</a>", true, false));
        columns.put("offerEFP", new WebSocketTableColumnStyle("offerEFP", "Offer", "<div style='font-weight: bold; color: #ff0000; text-decoration: underline;'>{offerEFP}</a>", true, false));
        columns.put("offerEFPVolume", new WebSocketTableColumnStyle("offerEFPVolume", "$ O", "<div style='font-weight: bold; color: #ff0000; text-decoration: underline;'>{offerEFPVolume}</a>", true, false));
        columns.put("reference", new WebSocketTableColumnStyle("reference", "Ref", "{reference}", true, true));
        final WebSocketTableRowAnnotator annotator = new WebSocketTableRowAnnotator(schema);
        final WebSocketTableUpdater application = new WebSocketTableUpdater("product", schema, annotator);

        final WebSocketTableUpdaterApplication container = new WebSocketTableUpdaterApplication(application, agent, 6060);
        application.start();
        container.connect();
    }
}