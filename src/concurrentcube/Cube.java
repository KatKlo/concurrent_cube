package concurrentcube;

import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

public class Cube {
    private final NotConcurrentCube cube;
    private final int size;

    private static final int GROUPS_COUNT = 4;

    int[] howManyWorks = {0, 0, 0, 0};
    int[] howManyWait = {0, 0, 0, 0};

    Semaphore mutex = new Semaphore(1, true);
    Semaphore[] waitingSem = {
            new Semaphore(0, true),
            new Semaphore(0, true),
            new Semaphore(0, true),
            new Semaphore(0, true)};
    Semaphore[] layersSem;


    public Cube(int size,
                BiConsumer<Integer, Integer> beforeRotation, BiConsumer<Integer, Integer> afterRotation,
                Runnable beforeShowing, Runnable afterShowing) {
        this.size = size;
        this.cube = new NotConcurrentCube(size, beforeRotation, afterRotation, beforeShowing, afterShowing);
        layersSem = new Semaphore[size];

        for (int i = 0; i < size; i++) {
            layersSem[i] = new Semaphore(1, true);
        }
    }

    private void accessProtocol(int groupNumber) throws InterruptedException {
        mutex.acquire();

        int next1 = (groupNumber + 1) % GROUPS_COUNT;
        int next2 = (groupNumber + 2) % GROUPS_COUNT;
        int next3 = (groupNumber + 3) % GROUPS_COUNT;

        if (howManyWorks[next1] + howManyWorks[next2] + howManyWorks[next3]
                + howManyWait[next1] + howManyWait[next2] + howManyWait[next3] > 0) {
            howManyWait[groupNumber]++;
            mutex.release();
            waitingSem[groupNumber].acquireUninterruptibly();
            howManyWait[groupNumber]--;
        }

        howManyWorks[groupNumber]++;

        if (howManyWait[groupNumber] > 0) {
            waitingSem[groupNumber].release();
        } else {
            mutex.release();
        }

    }

    private void endProtocol(int groupNumber) {
        mutex.acquireUninterruptibly();

        howManyWorks[groupNumber]--;

        int next1 = (groupNumber + 1) % GROUPS_COUNT;
        int next2 = (groupNumber + 2) % GROUPS_COUNT;
        int next3 = (groupNumber + 3) % GROUPS_COUNT;

        if ((howManyWorks[groupNumber] == 0)
                && (howManyWait[next1] + howManyWait[next2] + howManyWait[next3] > 0)) {
            if (howManyWait[next1] > 0) {
                waitingSem[next1].release();
            } else if (howManyWait[next2] > 0) {
                waitingSem[next2].release();
            } else { // iluCzeka[a3] > 0
                waitingSem[next3].release();
            }
        } else {
            mutex.release();
        }
    }

    private void accessProtocolForRotate(int side, int layer) throws InterruptedException {
        if (side == 0 || side == 5) {
            accessProtocol(0);
        } else if (side == 1 || side == 3) {
            accessProtocol(1);
        } else if (side == 2 || side == 4) {
            accessProtocol(2);
        }

        if (side < 3) {
            layersSem[layer].acquireUninterruptibly();
        } else {
            layersSem[this.size - 1 - layer].acquireUninterruptibly();
        }

    }

    private void endProtocolForRotate(int side, int layer) {
        if (side < 3) {
            layersSem[layer].release();
        } else {
            layersSem[this.size - 1 - layer].release();
        }

        if (side == 0 || side == 5) {
            endProtocol(0);
        } else if (side == 1 || side == 3) {
            endProtocol(1);
        } else {
            endProtocol(2);
        }
    }

    public void rotate(int side, int layer) throws InterruptedException {
        accessProtocolForRotate(side, layer);

        if (Thread.interrupted()) {
            endProtocolForRotate(side, layer);
            throw new InterruptedException();
        }

        this.cube.rotate(side, layer);

        endProtocolForRotate(side, layer);
    }

    public String show() throws InterruptedException {
        accessProtocol(3);

        if (Thread.interrupted()) {
            endProtocol(3);
            throw new InterruptedException();
        }

        String result = this.cube.show();

        endProtocol(3);

        return result;
    }
}