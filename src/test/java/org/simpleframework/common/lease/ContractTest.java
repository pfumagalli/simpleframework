package org.simpleframework.common.lease;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

public class ContractTest extends TimeTestCase {

    @Test
    public void testContract()  throws Exception {
        final Contract ten = new Expiration(this, 10, TimeUnit.MILLISECONDS);
        final Contract twenty = new Expiration(this, 20, TimeUnit.MILLISECONDS);
        final Contract thirty= new Expiration(this, 30, TimeUnit.MILLISECONDS);

        assertGreaterThanOrEqual(twenty.getDelay(TimeUnit.NANOSECONDS), ten.getDelay(TimeUnit.NANOSECONDS));
        assertGreaterThanOrEqual(thirty.getDelay(TimeUnit.NANOSECONDS), twenty.getDelay(TimeUnit.NANOSECONDS));

        assertGreaterThanOrEqual(twenty.getDelay(TimeUnit.MILLISECONDS), ten.getDelay(TimeUnit.MILLISECONDS));
        assertGreaterThanOrEqual(thirty.getDelay(TimeUnit.MILLISECONDS), twenty.getDelay(TimeUnit.MILLISECONDS));

        ten.setDelay(0, TimeUnit.MILLISECONDS);
        twenty.setDelay(0, TimeUnit.MILLISECONDS);

        assertLessThanOrEqual(ten.getDelay(TimeUnit.MILLISECONDS), 0);
        assertLessThanOrEqual(twenty.getDelay(TimeUnit.MILLISECONDS), 0);

        ten.setDelay(10, TimeUnit.MILLISECONDS);
        twenty.setDelay(20, TimeUnit.MILLISECONDS);
        thirty.setDelay(30, TimeUnit.MILLISECONDS);

        assertGreaterThanOrEqual(twenty.getDelay(TimeUnit.NANOSECONDS), ten.getDelay(TimeUnit.NANOSECONDS));
        assertGreaterThanOrEqual(thirty.getDelay(TimeUnit.NANOSECONDS), twenty.getDelay(TimeUnit.NANOSECONDS));

        assertGreaterThanOrEqual(twenty.getDelay(TimeUnit.MILLISECONDS), ten.getDelay(TimeUnit.MILLISECONDS));
        assertGreaterThanOrEqual(thirty.getDelay(TimeUnit.MILLISECONDS), twenty.getDelay(TimeUnit.MILLISECONDS));

        ten.setDelay(0, TimeUnit.MILLISECONDS);
        twenty.setDelay(0, TimeUnit.MILLISECONDS);
    }
}
