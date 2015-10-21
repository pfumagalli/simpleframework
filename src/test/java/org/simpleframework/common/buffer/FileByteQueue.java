package org.simpleframework.common.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FileByteQueue {

    private final BlockingQueue<Block> blocks;
    private final BlockAllocator allocator;
    private Block source;

    public FileByteQueue(Allocator allocator) throws IOException {
        this.blocks = new LinkedBlockingQueue<Block>();
        this.allocator = new BlockAllocator(allocator);
    }

    public int read(byte[] array, int off, int size) throws Exception {
        final int left = blocks.size();
        final int mark = size;

        for(int i = 0; (source != null) || (i < left); i++) {
            if(source == null) {
                source = blocks.take();
            }
            final int remain = source.remaining();
            final int read = Math.min(remain, size);

            if(read > 0) {
                source.read(array, off, size);
                size -= read;
                off += read;
            }
            if(remain == 0) {
                source.close(); // clear up file handles
                source = null;
            }
            if(size <= 0) {
                return mark;
            }
        }
        return mark - size;
    }

    public void write(byte[] array, int off, int size) throws Exception {
        final Block buffer = allocator.allocate(array, off, size);

        if(size > 0) {
            blocks.offer(buffer);
        }
    }

    private class BlockAllocator {

        private final Allocator allocator;

        public BlockAllocator(Allocator allocator) {
            this.allocator = new BufferAllocator(allocator);
        }

        public Block allocate(byte[] array, int off, int size) throws IOException {
            final Buffer buffer = allocator.allocate();

            if(size > 0) {
                buffer.append(array, off, size);
            }
            return new Block(buffer, size);
        }
    }

    private class Block {

        private final InputStream source;
        private int remaining;
        private final int size;

        public Block(Buffer buffer, int size) throws IOException {
            this.source = buffer.open();
            this.remaining = size;
            this.size = size;
        }

        public int read(byte[] array, int off, int size) throws IOException {
            final int count = source.read(array, off, size);

            if(count > 0) {
                remaining -= size;
            }
            return count;
        }

        public void close() throws IOException {
            source.close();
        }

        public int remaining() {
            return remaining;
        }

        public int size() {
            return size;
        }
    }
}