package org.simpleframework;

import org.testng.annotations.Test;

public class JavaVersionTest {

    @Test
    public void testJavaVersion() {
        System.err.println("Java Version " + System.getProperty("java.version"));
    }
}
