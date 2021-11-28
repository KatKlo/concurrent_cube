package concurrentcube;

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

        for (int sideNumber = 0; sideNumber < WALLS_COUNT; sideNumber++)
            this.sides[sideNumber] = new Side(sideNumber, size);
    }

    public void rotate(int side, int layer) {
        assert (side >= 0 && side < WALLS_COUNT);
        assert (layer >= 0 && layer < this.size);

        this.beforeRotation.accept(side, layer);

        switch (side) {
            case 0:
                rotateYAxis(layer, true);
                break;
            case 1:
                rotateXAxis(layer, true);
                break;
            case 2:
                rotateZAxis(layer, true);
                break;
            case 3:
                rotateXAxis(oppositeLayer(layer), false);
                break;
            case 4:
                rotateZAxis(oppositeLayer(layer), false);
                break;
            case 5:
                rotateYAxis(oppositeLayer(layer), false);
                break;
            default:
                throw new IllegalStateException("There's no side like " + side);
        }

        if (layer == 0)
            this.sides[side].rotateWall(true);
        if (layer == this.size - 1)
            this.sides[oppositeSites[side]].rotateWall(false);

        this.afterRotation.accept(side, layer);
    }

    public String show() {
        this.beforeShowing.run();

        StringBuilder result = new StringBuilder();

        for (int sideNumber = 0; sideNumber < WALLS_COUNT; sideNumber++)
            result.append(this.sides[sideNumber].show());

        this.afterShowing.run();

        return result.toString();
    }

    private int oppositeLayer(int layer) {
        return this.size - 1 - layer;
    }

    private void rotateYAxis(int line, boolean clockwise) {
        assert (line >= 0 && line < this.size);

        int[] toMove;
        if (clockwise) toMove = new int[]{4, 3, 2, 1};
        else toMove = new int[]{1, 2, 3, 4};

        Integer[] temp = this.sides[toMove[0]].getRow(line);
        temp = this.sides[toMove[1]].replaceRow(line, temp, false);
        temp = this.sides[toMove[2]].replaceRow(line, temp, false);
        temp = this.sides[toMove[3]].replaceRow(line, temp, false);
        this.sides[toMove[0]].setRow(line, temp, false);
    }

    private void rotateXAxis(int column, boolean clockwise) {
        assert (column >= 0 && column < this.size);

        int[] toMove;
        if (clockwise) toMove = new int[]{0, 2, 5, 4};
        else toMove = new int[]{5, 2, 0, 4};

        Integer[] temp = this.sides[toMove[0]].getColumn(column);
        temp = this.sides[toMove[1]].replaceColumn(column, temp, false);
        temp = this.sides[toMove[2]].replaceColumn(column, temp, false);
        temp = this.sides[toMove[3]].replaceColumn(oppositeLayer(column), temp, true);
        this.sides[toMove[0]].setColumn(column, temp, true);
    }

    private void rotateZAxis(int layer, boolean clockwise) {
        assert (layer >= 0 && layer < this.size);

        Integer[] temp = this.sides[0].getRow(oppositeLayer(layer));

        if (clockwise) {
            temp = this.sides[3].replaceColumn(layer, temp, false);
            temp = this.sides[5].replaceRow(layer, temp, true);
            temp = this.sides[1].replaceColumn(oppositeLayer(layer), temp, false);
            this.sides[0].setRow(oppositeLayer(layer), temp, true);
        } else {
            temp = this.sides[1].replaceColumn(oppositeLayer(layer), temp, true);
            temp = this.sides[5].replaceRow(layer, temp, false);
            temp = this.sides[3].replaceColumn(layer, temp, true);
            this.sides[0].setRow(oppositeLayer(layer), temp, false);
        }
    }
}
