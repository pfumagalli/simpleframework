package org.simpleframework.http.parse;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.http.Cookie;
import org.testng.annotations.Test;

public class CookieParserTest {

    @Test
    public void testParse() throws Exception {
        final CookieParser parser = new CookieParser("blackbird={\"pos\": 1, \"size\": 0, \"load\": null}; JSESSIONID=31865d30-e252-4729-ac6f-9abdd1fb9071");
        final List<Cookie> cookies = new ArrayList<Cookie>();

        for(final Cookie cookie : parser) {
            System.out.println(cookie.toClientString());
            cookies.add(cookie);
        }
    }

}
