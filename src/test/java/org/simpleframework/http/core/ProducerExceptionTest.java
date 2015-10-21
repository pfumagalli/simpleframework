package org.simpleframework.http.core;

import java.io.IOException;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class ProducerExceptionTest {

    @Test
    public void testException() {
        try {
            throw new IOException("Error");
        }catch(final Exception main) {
            try {
                throw new BodyEncoderException("Wrapper", main);
            }catch(final Exception cause) {
                cause.printStackTrace();

                AssertJUnit.assertEquals(cause.getCause(), main);
            }
        }
    }

}
