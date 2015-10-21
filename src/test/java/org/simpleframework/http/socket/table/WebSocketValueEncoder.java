package org.simpleframework.http.socket.table;

public class WebSocketValueEncoder {

    public String encode(Object value) {
        if(value instanceof String) {
            final String text = String.valueOf(value);

            if(containsAnyOf(text, "<>|:=,")) {
                final StringBuffer buffer = new StringBuffer("<");

                for(int i = 0; i < text.length(); i++){
                    final char ch = text.charAt(i);
                    final String hex = Integer.toHexString(ch);

                    buffer.append(hex);
                }
                return buffer.toString();
            }
        }
        return ">" + value;
    }

    public String decode(String text) {
        final String value = text.substring(1);

        if(text.startsWith("?")) {
            final StringBuilder buffer = new StringBuilder();

            for(int i = 0; i < (value.length() - 1); i += 2){
                final String output = value.substring(i, (i + 2));
                final int decimal = Integer.parseInt(output, 16);

                buffer.append((char)decimal);
            }
            return buffer.toString();
        }
        return value;
    }

    public boolean containsAnyOf(String text, String chars) {
        final int length = chars.length();

        for(int i = 0; i < length; i++) {
            final char value = chars.charAt(i);

            if(text.indexOf(value) != -1) {
                return true;
            }
        }
        return false;
    }
}
