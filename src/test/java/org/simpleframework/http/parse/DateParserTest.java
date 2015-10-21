package org.simpleframework.http.parse;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class DateParserTest {

    /**
     * Sun, 06 Nov 2009 08:49:37 GMT ; RFC 822, updated by RFC 1123 Sunday,
     * 06-Nov-09 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036 Sun Nov 6 08:49:37
     * 2009 ; ANSI C's asctime() format
     */
    @Test
    public void testDate() {
        final DateParser rfc822 = new DateParser("Sun, 06 Nov 2009 08:49:37 GMT");
        final DateParser rfc850 = new DateParser("Sunday, 06-Nov-09 08:49:37 GMT");
        final DateParser asctime = new DateParser("Sun Nov  6 08:49:37 2009");

        AssertJUnit.assertEquals(rfc822.toLong() >> 10, rfc850.toLong() >> 10); // shift out
        // seconds
        AssertJUnit.assertEquals(rfc822.toLong() >> 10, asctime.toLong() >> 10); // shift out
        // seconds
        AssertJUnit.assertEquals(rfc822.toString(), rfc850.toString());
        AssertJUnit.assertEquals(rfc822.toString(), asctime.toString());
        AssertJUnit.assertEquals(rfc850.toString(), "Sun, 06 Nov 2009 08:49:37 GMT");
        AssertJUnit.assertEquals(rfc850.toString().length(), 29);
        AssertJUnit.assertEquals(rfc822.toString(), "Sun, 06 Nov 2009 08:49:37 GMT");
        AssertJUnit.assertEquals(rfc822.toString().length(), 29);
        AssertJUnit.assertEquals(asctime.toString(), "Sun, 06 Nov 2009 08:49:37 GMT");
        AssertJUnit.assertEquals(asctime.toString().length(), 29);
    }

    @Test
    public void testLong() throws Exception {
        final String date = "Thu, 20 Jan 2011 16:43:08 GMT";

        final DateParser dp1 = new DateParser(date);
        System.out.println("value a: " + dp1.toLong());
        Thread.sleep(50);

        final DateParser dp2 = new DateParser(date);
        System.out.println("value b: " + dp2.toLong());
        Thread.sleep(50);

        final DateParser dp3 = new DateParser(date);
        System.out.println("value c: " + dp3.toLong());

        AssertJUnit.assertEquals(dp1.toLong(), dp2.toLong());
        AssertJUnit.assertEquals(dp2.toLong(), dp3.toLong());
        AssertJUnit.assertEquals(dp1.toString(), dp2.toString());
        AssertJUnit.assertEquals(dp2.toString(), dp3.toString());


    }
}
