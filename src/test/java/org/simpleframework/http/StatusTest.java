package org.simpleframework.http;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class StatusTest {

    private static final int ITERATIONS = 100000;

    @Test
    public void testStatus() {
        testStatus(200, "OK");
        testStatus(404, "Not Found");
    }

    private void testStatus(int code, String expect) {
        for(int i = 0; i < ITERATIONS; i++) {
            AssertJUnit.assertEquals(expect, Status.getDescription(code));
        }
    }

}
