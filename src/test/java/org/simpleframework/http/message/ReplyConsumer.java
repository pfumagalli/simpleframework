package org.simpleframework.http.message;

import org.simpleframework.http.Cookie;
import org.simpleframework.http.ResponseHeader;
import org.simpleframework.http.Status;

public class ReplyConsumer extends RequestConsumer implements ResponseHeader {

    private String text;
    private int code;

    public ReplyConsumer() {
        super();
    }

    private void status() {
        while(pos < count) {
            if(!digit(array[pos])) {
                break;
            }
            code *= 10;
            code += array[pos];
            code -= '0';
            pos++;
        }
    }

    private void text() {
        final StringBuilder builder = new StringBuilder();

        while(pos < count) {
            if(terminal(array[pos])) {
                pos += 2;
                break;
            }
            builder.append((char) array[pos]);
            pos++;
        }
        text = builder.toString();
    }

    @Override
    public String getDescription() {
        return text;
    }

    @Override
    public void setDescription(String text) {
        this.text = text;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public void setCode(int status) {
        this.code = status;
    }

    @Override
    public Status getStatus() {
        return Status.getStatus(code);
    }

    @Override
    public void setStatus(Status status) {
        code = status.code;
        text = status.description;
    }

    @Override
    protected void add(String name, String value) {
        if(equal("Set-Cookie", name)) { // A=b; version=1; path=/;
            final String[] list = value.split(";"); // "A=b", "version=1", "path=/"

            if(list.length > 0) {
                final String[] pair = list[0].split("=");

                if(pair.length > 1) {
                    header.setCookie(pair[0], pair[1]); // "A", "b"
                }
            }
        }
        super.add(name, value);
    }

    @Override
    protected void process() {
        version(); // HTTP/1.1
        adjust();
        status(); // 200
        adjust();
        text(); // OK
        adjust();
        headers();
    }

    @Override
    public void setMajor(int major) {
        this.major = major;

    }

    @Override
    public void setMinor(int minor) {
        this.minor = minor;

    }

    @Override
    public void addValue(String name, String value) {
        header.addValue(name, value);
    }

    @Override
    public void addInteger(String name, int value) {
        header.addInteger(name, value);

    }

    @Override
    public void addDate(String name, long date) {
        header.addDate(name, date);
    }

    @Override
    public void setValue(String name, String value) {
        header.setValue(name, value);
    }

    @Override
    public void setInteger(String name, int value) {
        header.setInteger(name, value);
    }

    @Override
    public void setLong(String name, long value) {
        header.setLong(name, value);
    }

    @Override
    public void setDate(String name, long date) {
        header.setDate(name, date);
    }

    @Override
    public Cookie setCookie(Cookie cookie) {
        return header.setCookie(cookie);
    }

    @Override
    public Cookie setCookie(String name, String value) {
        return header.setCookie(name, value);
    }
}
