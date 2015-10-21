package org.simpleframework.http;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class CookieTest {

    @Test
    public void testCookies() throws Exception {
        final Cookie cookie = new Cookie("JSESSIONID", "XXX");

        cookie.setExpiry(10);
        cookie.setPath("/path");

        System.err.println(cookie);

        AssertJUnit.assertTrue(cookie.toString().contains("max-age=10"));
        AssertJUnit.assertTrue(cookie.toString().matches(".*expires=\\w\\w\\w, \\d\\d-\\w\\w\\w-\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d GMT;.*"));
    }

    @Test
    public void testCookieWithoutExpiry() throws Exception {
        final Cookie cookie = new Cookie("JSESSIONID", "XXX");

        cookie.setPath("/path");

        System.err.println(cookie);

        AssertJUnit.assertFalse(cookie.toString().contains("max-age=10"));
        AssertJUnit.assertFalse(cookie.toString().matches(".*expires=\\w\\w\\w, \\d\\d \\w\\w\\w \\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d GMT;.*"));
    }

    @Test
    public void testSecureCookies() throws Exception {
        final Cookie cookie = new Cookie("JSESSIONID", "XXX");

        cookie.setExpiry(10);
        cookie.setPath("/path");
        cookie.setSecure(true);

        System.err.println(cookie);

        AssertJUnit.assertTrue(cookie.toString().contains("max-age=10"));
        AssertJUnit.assertTrue(cookie.toString().matches(".*expires=\\w\\w\\w, \\d\\d-\\w\\w\\w-\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d GMT;.*"));

        cookie.setExpiry(10);
        cookie.setPath("/path");
        cookie.setSecure(false);
        cookie.setProtected(true);

        System.err.println(cookie);

        AssertJUnit.assertTrue(cookie.toString().contains("max-age=10"));
        AssertJUnit.assertTrue(cookie.toString().matches(".*expires=\\w\\w\\w, \\d\\d-\\w\\w\\w-\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d GMT;.*"));

        cookie.setExpiry(10);
        cookie.setPath("/path");
        cookie.setSecure(true);
        cookie.setProtected(true);

        System.err.println(cookie);

        AssertJUnit.assertTrue(cookie.toString().contains("max-age=10"));
        AssertJUnit.assertTrue(cookie.toString().matches(".*expires=\\w\\w\\w, \\d\\d-\\w\\w\\w-\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d GMT;.*"));

    }
}
