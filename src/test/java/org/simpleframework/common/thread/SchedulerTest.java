package org.simpleframework.common.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

public class SchedulerTest {

    private static final int ITERATIONS = 10000;

    @Test
    public void testScheduler() throws Exception {
        final ConcurrentScheduler queue = new ConcurrentScheduler(Runnable.class, 10);
        final LinkedBlockingQueue<Timer> list = new LinkedBlockingQueue<Timer>();

        for(int i = 0; i < ITERATIONS; i++) {
            queue.execute(new Task(list, new Timer(i)), i, TimeUnit.MILLISECONDS);
        }
        for(Timer timer = list.take(); timer.getValue() < (ITERATIONS - 10); timer = list.take()) {
            System.err.println("value=["+timer.getValue()+"] delay=["+timer.getDelay()+"] expect=["+timer.getExpectation()+"]");
        }
    }

    public class Timer {

        private final Integer value;

        private final long time;

        public Timer(Integer value) {
            this.time = System.currentTimeMillis();
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }

        public long getDelay() {
            return System.currentTimeMillis() - time;
        }

        public int getExpectation() {
            return value.intValue();
        }
    }

    public class Task implements Runnable {

        private final LinkedBlockingQueue<Timer> queue;

        private final Timer timer;

        public Task(LinkedBlockingQueue<Timer> queue, Timer timer) {
            this.queue = queue;
            this.timer = timer;
        }

        @Override
        public void run() {
            queue.offer(timer);
        }
    }
}
