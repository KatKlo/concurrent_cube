package concurrentcube;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.BiConsumer;

public class NotConcurrentCube {
    private final int WALLS_COUNT = 6;

    private final int size;
    private final Side[] sides = new Side[WALLS_COUNT];
    private final static int[] oppositeSites = {5, 3, 4, 1, 2, 0};

    private final BiConsumer<Integer, Integer> beforeRotation;
    private final BiConsumer<Integer, Integer> afterRotation;
    private final Runnable beforeShowing;
    private final Runnable afterShowing;

    public NotConcurrentCube(int size, BiConsumer<Integer, Integer> beforeRotation, BiConsumer<Integer, Integer> afterRotation, Runnable beforeShowing, Runnable afterShowing) {
        this.size = size;
        this.beforeRotation = beforeRotation;
        this.afterRotation = afterRotation;
        this.beforeShowing = beforeShowing;
        this.afterShowing = afterShowing;

        for (int sideNumber = 0; sideNumber < WALLS_COUNT; sideNumber++) {
            this.sides[sideNumber] = new Side(sideNumber, size);
        }
    }

    private void rotateFor05(int side, int line) {
        assert(side == 0 || side == 5);
        assert(line >= 0 && line < this.size);

        int[] toMove;
        Integer[][] temp = new Integer[4][this.size];

        if (side == 0) toMove = new int[]{4, 3, 2, 1};
        else toMove = new int[]{1, 2, 3, 4};

        for (int i = 0; i < 4; i++) {
            temp[i] = this.sides[toMove[i]].getRow(line);
        }

        for (int i = 0; i < 4; i++) {
            int j = i > 0 ? i - 1 : 3;
            this.sides[toMove[i]].setRow(line, temp[j]);
        }
    }

    private void rotateFor13(int side, int column) {
        assert(side == 1 || side == 3);
        assert(column >= 0 && column < this.size);

        int[] toMove;
        Integer[][] temp = new Integer[4][this.size];

        if (side == 1) toMove = new int[]{0, 2, 5};
        else toMove = new int[]{5, 2, 0};

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
        assert(side == 2 || side == 4);
        assert(layer >= 0 && layer < this.size);

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
        } else {
            Collections.reverse(Arrays.asList(temp[0]));
            Collections.reverse(Arrays.asList(temp[2]));

            this.sides[0].setRow(this.size - 1 - layer, temp[1]);
            this.sides[3].setColumn(layer, temp[2]);
            this.sides[5].setRow(layer, temp[3]);
            this.sides[1].setColumn(this.size - 1 - layer, temp[0]);
        }
    }

    public void rotate(int side, int layer) {
        assert(side >= 0 && side < WALLS_COUNT);
        assert(layer >= 0 && layer < this.size);

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
                throw new IllegalStateException("There's no side like " + side);
        }

        if (layer == 0) this.sides[side].rotateWall(true);
        if (layer == this.size - 1) this.sides[oppositeSites[side]].rotateWall(false);

        this.afterRotation.accept(side, layer);
    }

    public String show() {
        this.beforeShowing.run();

        StringBuilder result = new StringBuilder();

        for (int sideNumber = 0; sideNumber < WALLS_COUNT; sideNumber++) {
            result.append(this.sides[sideNumber].show());
        }

        this.afterShowing.run();

        return result.toString();
    }
}
