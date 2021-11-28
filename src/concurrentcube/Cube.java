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
        mutex.acquire();

        if (calculateWaitingAndWorking(groupNumber) > 0) {
            howManyWait[groupNumber]++;
            mutex.release();

            try {
                waitingSem[groupNumber].acquire();
            } catch (InterruptedException e) {
                releasedGroupMutex.acquireUninterruptibly();

                if (releasedGroup == groupNumber)
                    waitingSem[groupNumber].acquireUninterruptibly();
                else
                    mutex.acquireUninterruptibly();

                howManyWait[groupNumber]--;
                mutex.release();
                releasedGroupMutex.release();

                throw new InterruptedException();
            }

            howManyWait[groupNumber]--;
        }

        howManyWorks[groupNumber]++;

        if (howManyWait[groupNumber] > 0) {
            waitingSem[groupNumber].release();
        } else {
            mutex.release();
            releasedGroupMutex.acquireUninterruptibly();
            releasedGroup = -1;
            releasedGroupMutex.release();
        }

    }

    private void endProtocol(int groupNumber) {
        releasedGroupMutex.acquireUninterruptibly();
        mutex.acquireUninterruptibly();

        howManyWorks[groupNumber]--;

        int next1 = (groupNumber + 1) % GROUPS_COUNT;
        int next2 = (groupNumber + 2) % GROUPS_COUNT;
        int next3 = (groupNumber + 3) % GROUPS_COUNT;

        if ((howManyWorks[groupNumber] == 0)
                && (howManyWait[next1] + howManyWait[next2] + howManyWait[next3] > 0)) {
            if (howManyWait[next1] > 0) {
                releasedGroup = next1;
                waitingSem[next1].release();
            } else if (howManyWait[next2] > 0) {
                releasedGroup = next2;
                waitingSem[next2].release();
            } else { // iluCzeka[next3] > 0
                releasedGroup = next3;
                waitingSem[next3].release();
            }
        } else {
            mutex.release();
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