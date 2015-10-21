package org.simpleframework.common.lease;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

public class ContractQueueTest extends TimeTestCase {

    @Test
    public void testTimeUnits() throws Exception {
        final ContractQueue<Long> queue = new ContractQueue<Long>();
        final List<String> complete = new ArrayList<String>();

        for(long i = 0; i < 10000; i++) {
            final long random = (long)(Math.random() * 1000);
            final Contract<Long> contract = new Expiration(random, random, TimeUnit.NANOSECONDS);

            queue.offer(contract);
        }
        for(int i = 0; i < 10000; i++) {
            final Contract<Long> contract = queue.take();

            assertGreaterThanOrEqual(contract.getDelay(TimeUnit.NANOSECONDS), contract.getDelay(TimeUnit.NANOSECONDS));
            assertGreaterThanOrEqual(contract.getDelay(TimeUnit.MILLISECONDS), contract.getDelay(TimeUnit.MILLISECONDS));
            assertGreaterThanOrEqual(contract.getDelay(TimeUnit.SECONDS), contract.getDelay(TimeUnit.SECONDS));

            final long nanoseconds = contract.getDelay(TimeUnit.NANOSECONDS);
            final long milliseconds = contract.getDelay(TimeUnit.MILLISECONDS);

            complete.add(String.format("index=[%s] nano=[%s] milli=[%s]", i, nanoseconds, milliseconds));
        }
        for(int i = 0; i < 10000; i++) {
            System.err.println("expiry=[" + complete.get(i)+ "]");
        }
    }

    @Test
    public void testAccuracy() throws Exception {
        final ContractQueue<Long> queue = new ContractQueue<Long>();

        for(long i = 0; i < 10000; i++) {
            final long random = (long)(Math.random() * 1000);
            final Contract<Long> contract = new Expiration(random, random, TimeUnit.NANOSECONDS);

            queue.offer(contract);
        }
        for(int i = 0; i < 10000; i++) {
            final Contract<Long> contract = queue.take();

            assertLessThanOrEqual(-2000, contract.getDelay(TimeUnit.MILLISECONDS));
        }
    }

}

