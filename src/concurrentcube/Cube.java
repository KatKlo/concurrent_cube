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
            new Semaphore(0, true)
    };
    Semaphore[] layersSem;

    Semaphore releasedGroupMutex = new Semaphore(1, true);
    Semaphore additionalProtection = new Semaphore(1, true);
    int releasedGroup = -1;

    public Cube(int size,
                BiConsumer<Integer, Integer> beforeRotation, BiConsumer<Integer, Integer> afterRotation,
                Runnable beforeShowing, Runnable afterShowing) {
        this.size = size;
        this.cube = new NotConcurrentCube(size, beforeRotation, afterRotation, beforeShowing, afterShowing);
        layersSem = new Semaphore[size];

        for (int i = 0; i < size; i++)
            layersSem[i] = new Semaphore(1, true);
    }

    private int calculateWaitingAndWorking(int startingGroup) {
        int temp, result = 0;

        for (int i = 1; i < 4; i++) {
            temp = (startingGroup + i) % GROUPS_COUNT;
            result += howManyWorks[temp] + howManyWait[temp];
        }

        return result;
    }

    private void accessProtocol(int groupNumber) throws InterruptedException {
        additionalProtection.acquire();
        mutex.acquire();

        if (calculateWaitingAndWorking(groupNumber) > 0) {
            howManyWait[groupNumber]++;
            mutex.release();
            additionalProtection.release();

            try {
                waitingSem[groupNumber].acquire();
            } catch (InterruptedException e) {
                releasedGroupMutex.acquireUninterruptibly();

                if (releasedGroup == groupNumber) {
                    waitingSem[groupNumber].acquireUninterruptibly();
                }
                else {
                    mutex.acquireUninterruptibly();
                    howManyWait[groupNumber]--;
                    mutex.release();
                    releasedGroupMutex.release();
                    throw new InterruptedException();
                }

                Thread.currentThread().interrupt();
                releasedGroupMutex.release();
            }
            howManyWait[groupNumber]--;
        }

        howManyWorks[groupNumber]++;

        mutex.release();
        releasedGroupMutex.acquireUninterruptibly();
        mutex.acquireUninterruptibly();

        if (howManyWait[groupNumber] > 0) {
            waitingSem[groupNumber].release();
        } else {
            releasedGroup = -1;
            mutex.release();
            additionalProtection.release();
        }

        releasedGroupMutex.release();

        if (Thread.currentThread().isInterrupted()) {
            endProtocol(groupNumber);
            throw new InterruptedException();
        }

    }

    private int calculateWaiting() {
        int result = 0;

        for (int i = 0; i < 4; i++) {
            result += howManyWait[i];
        }

        return result;
    }

    private void endProtocol(int groupNumber) {
        additionalProtection.acquireUninterruptibly();
        releasedGroupMutex.acquireUninterruptibly();
        mutex.acquireUninterruptibly();

        howManyWorks[groupNumber]--;

        if (howManyWorks[groupNumber] == 0 && calculateWaiting() > 0) {
            for (int i = 1; i <= GROUPS_COUNT; i++) {
                int temp = (groupNumber + i) % GROUPS_COUNT;

                if (howManyWait[temp] > 0) {
                    releasedGroup = temp;
                    waitingSem[temp].release();
                    break;
                }
            }
        } else {
            mutex.release();
            additionalProtection.release();
        }

        releasedGroupMutex.release();
    }

    private void accessProtocolForRotate(int side, int layer) throws InterruptedException {
        int group;
        if (side == 0 || side == 5)
            group = 0;
        else if (side == 1 || side == 3)
            group = 1;
        else //side == 2 || side == 4
            group = 2;

        accessProtocol(group);

        int layerToAcquire = side < 3 ? layer : this.size - 1 - layer;

        try {
            layersSem[layerToAcquire].acquire();
        } catch (InterruptedException e) {
            endProtocol(group);

            throw new InterruptedException();
        }

    }

    private void endProtocolForRotate(int side, int layer) {
        int layerToRelease = side < 3 ? layer : this.size - 1 - layer;
        layersSem[layerToRelease].release();

        if (side == 0 || side == 5)
            endProtocol(0);
        else if (side == 1 || side == 3)
            endProtocol(1);
        else //side == 2 || side == 4
            endProtocol(2);
    }

    public void rotate(int side, int layer) throws InterruptedException {
        assert (side >= 0 && side < 6);
        assert (layer >= 0 && layer < this.size);

        accessProtocolForRotate(side, layer);

        this.cube.rotate(side, layer);

        endProtocolForRotate(side, layer);
    }

    public String show() throws InterruptedException {
        accessProtocol(3);

        String result = this.cube.show();

        endProtocol(3);

        return result;
    }
}