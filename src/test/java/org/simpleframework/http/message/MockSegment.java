package org.simpleframework.http.message;

import java.util.List;

import org.simpleframework.http.ContentDisposition;
import org.simpleframework.http.ContentType;
import org.simpleframework.http.parse.ContentDispositionParser;
import org.simpleframework.http.parse.ContentTypeParser;

public class MockSegment implements Segment {

    private final MessageHeader header;

    public MockSegment() {
        this.header = new MessageHeader();
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public ContentType getContentType() {
        final String value = getValue("Content-Type");

        if(value == null) {
            return null;
        }
        return new ContentTypeParser(value);
    }

    @Override
    public long getContentLength() {
        final String value = getValue("Content-Length");

        if(value != null) {
            return new Long(value);
        }
        return -1;
    }

    @Override
    public String getTransferEncoding() {
        final List<String> list = getValues("Transfer-Encoding");

        if(list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public ContentDisposition getDisposition() {
        final String value = getValue("Content-Disposition");

        if(value == null) {
            return null;
        }
        return new ContentDispositionParser(value);
    }

    @Override
    public List<String> getValues(String name) {
        return header.getValues(name);
    }

    @Override
    public String getValue(String name) {
        return header.getValue(name);
    }

    @Override
    public String getValue(String name, int index) {
        return header.getValue(name, index);
    }

    protected void add(String name, String value) {
        header.addValue(name, value);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getFileName() {
        return null;
    }
}