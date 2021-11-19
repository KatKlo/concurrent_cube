package concurrentcube.tests;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
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
