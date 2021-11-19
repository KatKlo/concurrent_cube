package concurrentcube;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

public class Cube {
    private final int numberOfWalls = 6;
    private final int numberOfGroups = 4;

    private final int size;
    private final Side[] sides = new Side[numberOfWalls];

    private final BiConsumer<Integer, Integer> beforeRotation;
    private final BiConsumer<Integer, Integer> afterRotation;
    private final Runnable beforeShowing;
    private final Runnable afterShowing;

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


    public Cube(int size,
                BiConsumer<Integer, Integer> beforeRotation,
                BiConsumer<Integer, Integer> afterRotation,
                Runnable beforeShowing,
                Runnable afterShowing) {
        this.size = size;
        this.beforeRotation = beforeRotation;
        this.afterRotation = afterRotation;
        this.beforeShowing = beforeShowing;
        this.afterShowing = afterShowing;

        for (NameOfSide side : NameOfSide.values()) {
            this.sides[side.getNumber()] = new Side(side, size);
        }

        layersSem = new Semaphore[size];
        for (int i = 0; i < size; i++) {
            layersSem[i] = new Semaphore(1, true);
        }
    }

    private void rotateFor05(int side, int line) {
        int[] toMove;

        if (side == 0) toMove = new int[]{4, 3, 2, 1};
        else if (side == 5) toMove = new int[]{1, 2, 3, 4};
        else return;

        Integer[][] temp = new Integer[4][this.size];

        for (int i = 0; i < 4; i++) {
            temp[i] = this.sides[toMove[i]].getRow(line);
        }

        for (int i = 0; i < 4; i++) {
            int j = i > 0 ? i - 1 : 3;
            this.sides[toMove[i]].setRow(line, temp[j]);
        }
    }

    private void rotateFor13(int side, int column) {
        int[] toMove;

        if (side == 1) toMove = new int[]{0, 2, 5};
        else if (side == 3) toMove = new int[]{5, 2, 0};
        else return;

        Integer[][] temp = new Integer[4][this.size];

        for (int i = 0; i < 3; i++) {
            temp[i] = this.sides[toMove[i]].getColumn(column);
        }
        temp[3] = this.sides[4].getColumn(this.size - 1 - column); // REVERSE!

        Collections.reverse(Arrays.asList(temp[3]));
        Collections.reverse(Arrays.asList(temp[2]));

        for (int i = 0; i < 3; i++) {
            int j = i > 0 ? i - 1 : 3;
            this.sides[toMove[i]].setColumn(column, temp[j]);
        }
        this.sides[4].setColumn(this.size - 1 - column, temp[2]);
    }

    private void rotateFor24(int side, int layer) {
        Integer[][] temp = new Integer[4][this.size];
        temp[0] = this.sides[0].getRow(this.size - 1 - layer);
        temp[1] = this.sides[3].getColumn(layer);
        temp[2] = this.sides[5].getRow(layer);
        temp[3] = this.sides[1].getColumn(this.size - 1 - layer);

        if (side == 2) {
            Collections.reverse(Arrays.asList(temp[1]));
            Collections.reverse(Arrays.asList(temp[3]));

            this.sides[0].setRow(this.size - 1 - layer, temp[3]);
            this.sides[3].setColumn(layer, temp[0]);
            this.sides[5].setRow(layer, temp[1]);
            this.sides[1].setColumn(this.size - 1 - layer, temp[2]);
        } else if (side == 4) {
            Collections.reverse(Arrays.asList(temp[0]));
            Collections.reverse(Arrays.asList(temp[2]));

            this.sides[0].setRow(this.size - 1 - layer, temp[1]);
            this.sides[3].setColumn(layer, temp[2]);
            this.sides[5].setRow(layer, temp[3]);
            this.sides[1].setColumn(this.size - 1 - layer, temp[0]);
        }
    }

    private void rotateNotConcurrent(int side, int layer) {
        this.beforeRotation.accept(side, layer);

        switch (side) {
            case 0:
                rotateFor05(0, layer);
                break;
            case 1:
                rotateFor13(1, layer);
                break;
            case 2:
                rotateFor24(2, layer);
                break;
            case 3:
                rotateFor13(3, this.size - 1 - layer);
                break;
            case 4:
                rotateFor24(4, this.size - 1 - layer);
                break;
            case 5:
                rotateFor05(5, this.size - 1 - layer);
                break;
            default:
                System.out.println("NIE ZAIMPLEMENTOWANO");
        }

        if (layer == 0) this.sides[side].rotateWall(true);
        if (layer == this.size - 1) this.sides[this.sides[side].getNumberOfOppositeSide()].rotateWall(false);

        this.afterRotation.accept(side, layer);
    }

    private void accessProtocol(int groupNumber) throws InterruptedException {
        mutex.acquire();

        int next1 = (groupNumber + 1) % numberOfGroups;
        int next2 = (groupNumber + 2) % numberOfGroups;
        int next3 = (groupNumber + 3) % numberOfGroups;

        if (howManyWorks[next1] + howManyWorks[next2] + howManyWorks[next3] + howManyWait[next1] + howManyWait[next2] + howManyWait[next3] > 0) {
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

        int next1 = (groupNumber + 1) % numberOfGroups;
        int next2 = (groupNumber + 2) % numberOfGroups;
        int next3 = (groupNumber + 3) % numberOfGroups;

        if ((howManyWorks[groupNumber] == 0) & (howManyWait[next1] + howManyWait[next2] + howManyWait[next3] > 0)) {
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

        rotateNotConcurrent(side, layer);

        endProtocolForRotate(side, layer);
    }

    private String showNotConcurrent() {
        this.beforeShowing.run();

        StringBuilder result = new StringBuilder();
        for (int wallNo = 0; wallNo < numberOfWalls; wallNo++) {
            result.append(this.sides[wallNo].toString());
        }

        this.afterShowing.run();

        return result.toString();
    }

    public String show() throws InterruptedException {
        accessProtocol(3);

        if (Thread.interrupted()) {
            endProtocol(3);
            throw new InterruptedException();
        }

        String result = showNotConcurrent();

        endProtocol(3);

        return result;
    }

    public String prettyShow() throws InterruptedException {
        accessProtocol(3);
        if (Thread.interrupted()) {
            endProtocol(3);
            throw new InterruptedException();
        }

        Integer[] temp;

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < this.size; i++) {
            result.append(" ".repeat(2 * this.size + 2));
            temp = this.sides[0].getRow(i);

            for (int j = 0; j < this.size; j++) {
                result.append(temp[j]).append(" ");
            }

            result.append("\n");
        }
        result.append("\n");

        for (int i = 0; i < this.size; i++) {
            for (int cube = 1; cube < 5; cube++) {
                temp = this.sides[cube].getRow(i);

                for (int j = 0; j < this.size; j++) {
                    result.append(temp[j]).append(" ");
                }

                result.append(" ".repeat(2));
            }
            result.append("\n");
        }
        result.append("\n");

        for (int i = 0; i < this.size; i++) {
            result.append(" ".repeat(2 * this.size + 2));
            temp = this.sides[5].getRow(i);

            for (int j = 0; j < this.size; j++) {
                result.append(temp[j]).append(" ");
            }
            result.append("\n");
        }
        result.append("\n");

        endProtocol(3);

        return result.toString();
    }
}