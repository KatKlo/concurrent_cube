package concurrentcube;

import java.util.function.BiConsumer;

public class Cube {

    public Cube(int size,
            BiConsumer<Integer, Integer> beforeRotation,
            BiConsumer<Integer, Integer> afterRotation,
            Runnable beforeShowing,
            Runnable afterShowing) {
    }

    public void rotate(int side, int layer) throws InterruptedException {
    }

    public String show() throws InterruptedException {
        return "W PRZYGOTOWNIU";
    }

}