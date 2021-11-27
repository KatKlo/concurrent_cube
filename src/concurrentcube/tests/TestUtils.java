package concurrentcube.tests;

import java.util.concurrent.atomic.AtomicInteger;

public class TestUtils {
    public static class Counter {
        AtomicInteger value;

        public Counter() {
            this.value = new AtomicInteger(0);
        }

        public void add(int n) {
            this.value.addAndGet(n);
        }

        public int get() {
            return this.value.get();
        }
    }

    public static class Pair {
        public final int first;
        public final int second;

        public Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }
    }
}
