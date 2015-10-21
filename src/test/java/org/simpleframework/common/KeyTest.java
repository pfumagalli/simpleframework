package org.simpleframework.common;

import java.util.HashMap;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Test for fast case insensitive mapping for headers that have been taken
 * from the request HTTP header or added to the response HTTP header.
 *
 * @author Niall Gallagher
 */
public class KeyTest {

    public class Index implements Name {

        private final String value;

        public Index(String value) {
            this.value = value.toLowerCase();
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public boolean equals(Object key) {
            if(key instanceof Name) {
                return key.equals(value);
            }
            if(key instanceof String) {
                return key.equals(value);
            }
            return false;
        }
    }

    public interface Name {

        @Override
        public int hashCode();
        @Override
        public boolean equals(Object value);
    }

    public class ArrayName implements Name {

        private String cache;
        private final byte[] array;
        private final int off;
        private final int size;
        private int hash;

        public ArrayName(byte[] array) {
            this(array, 0, array.length);
        }

        public ArrayName(byte[] array, int off, int size) {
            this.array = array;
            this.size = size;
            this.off = off;
        }

        @Override
        public boolean equals(Object value) {
            if(value instanceof String) {
                final String text = value.toString();

                return equals(text);
            }
            return false;
        }

        public boolean equals(String value) {
            final int length = value.length();

            if(length != size) {
                return false;
            }
            for(int i = 0; i < size; i++) {
                final int left = value.charAt(i);
                int right = array[off + i];

                if((right >= 'A') && (right <= 'Z')) {
                    right = (right - 'A') + 'a';
                }
                if(left != right) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            int code = hash;

            if(code == 0) {
                int pos = off;

                for(int i = 0; i < size; i++) {
                    int next = array[pos++];

                    if((next >= 'A') && (next <= 'Z')) {
                        next = (next - 'A') + 'a';
                    }
                    code = (31*code) + next;
                }
                hash = code;
            }
            return code;
        }
    }

    public class StringName implements Name {

        private final String value;
        private final String key;

        public StringName(String value) {
            this.key = value.toLowerCase();
            this.value = value;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object value) {
            return value.equals(key);
        }
    }

    public class NameTable<T> {

        private final Map<Name, T> map;

        public NameTable() {
            this.map = new HashMap<Name, T>();
        }

        public void put(Name key, T value) {
            map.put(key, value);
        }

        public void put(String text, T value) {
            final Name key = new StringName(text);

            map.put(key, value);
        }

        public T get(String key) {
            final Index index = new Index(key);

            return map.get(index);
        }

        public T remove(String key) {
            final Index index = new Index(key);

            return map.remove(index);
        }
    }

    @Test
    public void testName() {
        final Name contentLength = new ArrayName("Content-Length".getBytes());
        final Name contentType = new ArrayName("Content-Type".getBytes());
        final Name transferEncoding = new ArrayName("Transfer-Encoding".getBytes());
        final Name userAgent = new ArrayName("User-Agent".getBytes());
        final NameTable<String> map = new NameTable<String>();

        AssertJUnit.assertEquals(contentLength.hashCode(), "Content-Length".toLowerCase().hashCode());
        AssertJUnit.assertEquals(contentType.hashCode(), "Content-Type".toLowerCase().hashCode());
        AssertJUnit.assertEquals(transferEncoding.hashCode(), "Transfer-Encoding".toLowerCase().hashCode());
        AssertJUnit.assertEquals(userAgent.hashCode(), "User-Agent".toLowerCase().hashCode());

        map.put(contentLength, "1024");
        map.put(contentType, "text/html");
        map.put(transferEncoding, "chunked");
        map.put(userAgent, "Mozilla/4.0");
        map.put("Date", "18/11/1977");
        map.put("Accept", "text/plain, text/html, image/gif");

        AssertJUnit.assertEquals(map.get("Content-Length"), "1024");
        AssertJUnit.assertEquals(map.get("CONTENT-LENGTH"), "1024");
        AssertJUnit.assertEquals(map.get("content-length"), "1024");
        AssertJUnit.assertEquals(map.get("Content-length"), "1024");
        AssertJUnit.assertEquals(map.get("Content-Type"), "text/html");
        AssertJUnit.assertEquals(map.get("Transfer-Encoding"), "chunked");
        AssertJUnit.assertEquals(map.get("USER-AGENT"), "Mozilla/4.0");
        AssertJUnit.assertEquals(map.get("Accept"), "text/plain, text/html, image/gif");
        AssertJUnit.assertEquals(map.get("ACCEPT"), "text/plain, text/html, image/gif");
        AssertJUnit.assertEquals(map.get("accept"), "text/plain, text/html, image/gif");
        AssertJUnit.assertEquals(map.get("DATE"), "18/11/1977");
        AssertJUnit.assertEquals(map.get("Date"), "18/11/1977");
        AssertJUnit.assertEquals(map.get("date"), "18/11/1977");
    }
}
