package org.simpleframework.http.socket.table;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketTableChanger {

    private final Map<String, Integer> currentRows;
    private final WebSocketValueEncoder encoder;
    private final WebSocketTable table;

    public WebSocketTableChanger(WebSocketTable table) {
        this.currentRows = new ConcurrentHashMap<String, Integer>();
        this.encoder = new WebSocketValueEncoder();
        this.table = table;
    }

    public void onChange(Map<String, Object> values) {
        final Map<String, String> row = new HashMap<String, String>();
        final Map<String, String> header = new HashMap<String, String>();
        final Set<String> columns = values.keySet();

        for (final String column : columns) {
            final Object value = values.get(column);
            final String encoded = encoder.encode(value);

            row.put(column, encoded);
            header.put(column, column);
        }
        final WebSocketTableRow headerRow = table.getRow(0);

        if (headerRow == null) {
            table.addRow(header);
        } else {
            for (final String column : columns) {
                final String name = header.get(column);
                headerRow.setValue(column, name);
            }
        }
        final String key = table.getKey();
        final Object keyAttribute = values.get(key);

        if (keyAttribute != null) {
            final String tableKey = String.valueOf(keyAttribute);
            Integer index = currentRows.get(tableKey);

            if (index == null) {
                final WebSocketTableRow newRow = table.addRow(row);
                index = newRow.getIndex();
                currentRows.put(tableKey, index);
            } else {
                table.updateRow(index, row);
            }
        }
    }
}
