package org.simpleframework.http.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.simpleframework.http.message.MessageHeader;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class MessageTest {

    @Test
    public void testMessage() {
        final MessageHeader message = new MessageHeader();

        message.addValue("Content-Length", "10");
        message.addValue("Connection", "keep-alive");
        message.addValue("Accept", "image/gif, image/jpeg, */*");
        message.addValue("Set-Cookie", "a=b");
        message.addValue("Set-Cookie", "b=c");

        AssertJUnit.assertEquals(message.getValue("CONTENT-LENGTH"), "10");
        AssertJUnit.assertEquals(message.getValue("Content-Length"), "10");
        AssertJUnit.assertEquals(message.getValue("CONTENT-length"), "10");
        AssertJUnit.assertEquals(message.getValue("connection"), "keep-alive");
        AssertJUnit.assertEquals(message.getValue("CONNECTION"), "keep-alive");

        AssertJUnit.assertTrue(message.getValues("CONNECTION") != null);
        AssertJUnit.assertEquals(message.getValues("connection").size(), 1);

        AssertJUnit.assertTrue(message.getValues("set-cookie") != null);
        AssertJUnit.assertEquals(message.getValues("set-cookie").size(), 2);
        AssertJUnit.assertTrue(message.getValues("SET-COOKIE").contains("a=b"));
        AssertJUnit.assertTrue(message.getValues("SET-COOKIE").contains("b=c"));

        AssertJUnit.assertTrue(message.getNames().contains("Content-Length"));
        AssertJUnit.assertFalse(message.getNames().contains("CONTENT-LENGTH"));
        AssertJUnit.assertTrue(message.getNames().contains("Connection"));
        AssertJUnit.assertFalse(message.getNames().contains("CONNECTION"));
        AssertJUnit.assertTrue(message.getNames().contains("Set-Cookie"));
        AssertJUnit.assertFalse(message.getNames().contains("SET-COOKIE"));

        message.setValue("Set-Cookie", "d=e");

        AssertJUnit.assertTrue(message.getValues("set-cookie") != null);
        AssertJUnit.assertEquals(message.getValues("set-cookie").size(), 1);
        AssertJUnit.assertFalse(message.getValues("SET-COOKIE").contains("a=b"));
        AssertJUnit.assertFalse(message.getValues("SET-COOKIE").contains("b=c"));
        AssertJUnit.assertTrue(message.getValues("SET-COOKIE").contains("d=e"));
    }

    @Test
    public void testDates() {
        final MessageHeader message = new MessageHeader();
        final DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        final TimeZone zone = TimeZone.getTimeZone("GMT");
        final long time = System.currentTimeMillis();
        final Date date = new Date(time);

        format.setTimeZone(zone);
        message.setValue("Date", format.format(date));

        AssertJUnit.assertEquals(format.format(date), message.getValue("date"));
        AssertJUnit.assertEquals(new Date(message.getDate("DATE")).toString(), date.toString());

        message.setDate("Date", time);

        AssertJUnit.assertEquals(format.format(date), message.getValue("date"));
        AssertJUnit.assertEquals(new Date(message.getDate("DATE")).toString(), date.toString());
    }

}
