package org.simpleframework.transport.trace;

import java.text.DecimalFormat;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import org.simpleframework.common.thread.ConcurrentExecutor;
import org.testng.annotations.Test;

public class CompareQueueTest {

    private static final int TEST_DURATION = 10000;
    private static final int THREAD_COUNT = 100;

    private final Executor blockingReadExecutor = new ConcurrentExecutor(BlockingConsumer.class, THREAD_COUNT);
    private final Executor concurrentReadExecutor = new ConcurrentExecutor(ConcurrentConsumer.class, THREAD_COUNT);
    private final Executor writeExecutor = new ConcurrentExecutor(Producer.class, THREAD_COUNT);

    @Test
    public void testLinkedBlockingQueue() throws Exception {
        final BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
        final AtomicBoolean active = new AtomicBoolean(true);
        final AtomicLong writeCount = new AtomicLong();
        final AtomicLong readCount = new AtomicLong();
        final CountDownLatch startLatch = new CountDownLatch(THREAD_COUNT);
        final CountDownLatch stopLatch = new CountDownLatch(THREAD_COUNT);
        final DecimalFormat format = new DecimalFormat("###,###,###");

        for(int i = 0; i < THREAD_COUNT; i++) {
            final BlockingConsumer consumer = new BlockingConsumer(queue, stopLatch, active, readCount);
            blockingReadExecutor.execute(consumer);
        }
        Thread.sleep(1000);

        for(int i = 0; i < THREAD_COUNT; i++) {
            final Producer producer = new Producer(queue, startLatch, active, writeCount);
            writeExecutor.execute(producer);
        }
        Thread.sleep(TEST_DURATION);
        active.set(false);
        stopLatch.await();

        System.err.printf("read=%s write=%s%n", format.format(readCount.get()), format.format(writeCount.get()));
    }

    @Test
    public void testConcurrentQueue() throws Exception {
        final Queue<Object> queue = new ConcurrentLinkedQueue<Object>();
        final AtomicBoolean active = new AtomicBoolean(true);
        final AtomicLong writeCount = new AtomicLong();
        final AtomicLong readCount = new AtomicLong();
        final CountDownLatch startLatch = new CountDownLatch(THREAD_COUNT);
        final CountDownLatch stopLatch = new CountDownLatch(THREAD_COUNT);
        final DecimalFormat format = new DecimalFormat("###,###,###");

        for(int i = 0; i < THREAD_COUNT; i++) {
            final ConcurrentConsumer consumer = new ConcurrentConsumer(queue, stopLatch, active, readCount);
            concurrentReadExecutor.execute(consumer);
        }
        Thread.sleep(1000);

        for(int i = 0; i < THREAD_COUNT; i++) {
            final Producer producer = new Producer(queue, startLatch, active, writeCount);
            writeExecutor.execute(producer);
        }
        Thread.sleep(TEST_DURATION);
        active.set(false);
        stopLatch.await();

        System.err.printf("read=%s write=%s%n", format.format(readCount.get()), format.format(writeCount.get()));
    }

    private static class Producer implements Runnable {

        private final Queue<Object> queue;
        private final AtomicBoolean active;
        private final AtomicLong count;
        private final CountDownLatch latch;

        public Producer(Queue<Object> queue, CountDownLatch latch, AtomicBoolean active, AtomicLong count) {
            this.queue = queue;
            this.active = active;
            this.count = count;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                latch.countDown();
                latch.await();

                while(active.get()) {
                    final Long value = count.getAndIncrement();
                    queue.offer(value);
                }
            } catch(final Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static class ConcurrentConsumer implements Runnable {

        private final Queue<Object> queue;
        private final AtomicBoolean active;
        private final AtomicLong count;
        private final CountDownLatch latch;

        public ConcurrentConsumer(Queue<Object> queue, CountDownLatch latch, AtomicBoolean active, AtomicLong count) {
            this.queue = queue;
            this.active = active;
            this.count = count;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                while(active.get()) {
                    final Object value = queue.poll();
                    if(value != null) {
                        count.getAndIncrement();
                    } else {
                        LockSupport.parkNanos(100);
                    }
                }
                latch.countDown();
                latch.await();
            }catch(final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class BlockingConsumer implements Runnable {

        private final BlockingQueue<Object> queue;
        private final AtomicBoolean active;
        private final AtomicLong count;
        private final CountDownLatch latch;

        public BlockingConsumer(BlockingQueue<Object> queue, CountDownLatch latch, AtomicBoolean active, AtomicLong count) {
            this.queue = queue;
            this.active = active;
            this.count = count;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                while(active.get()) {
                    try {
                        final Object value = queue.take();
                        if(value != null) {
                            count.getAndIncrement();
                        }
                    }catch(final Exception e) {
                        e.printStackTrace();
                    }
                }
                latch.countDown();
                latch.await();
            }catch(final Exception e) {
                e.printStackTrace();
            }
        }
    }
}
