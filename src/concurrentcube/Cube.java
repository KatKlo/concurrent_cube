package concurrentcube;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.BiConsumer;

public class Cube {
    private final int numberOfWalls = 6;

    private final int size;
    private final Side[] walls = new Side[numberOfWalls];

    private final BiConsumer<Integer, Integer> beforeRotation;
    private final BiConsumer<Integer, Integer> afterRotation;
    private final Runnable beforeShowing;
    private final Runnable afterShowing;

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
            this.walls[side.getNumber()] = new Side(side, size);
        }
    }

    private void rotateFor05(int side, int line) {
        int[] toMove;

        if (side == 0) toMove = new int[]{4, 3, 2, 1};
        else if (side == 5) toMove = new int[]{1, 2, 3, 4};
        else return;

        Integer[][] temp = new Integer[4][this.size];

        for (int i = 0; i < 4; i++) {
            temp[i] = this.walls[toMove[i]].getRow(line);
        }

        for (int i = 0; i < 4; i++) {
            int j = i > 0 ? i - 1 : 3;
            this.walls[toMove[i]].setRow(line, temp[j]);
        }
    }

    private void rotateFor13(int side, int column) {
        int[] toMove;

        if (side == 1) toMove = new int[]{0, 2, 5};
        else if (side == 3) toMove = new int[]{5, 2, 0};
        else return;

        Integer[][] temp = new Integer[4][this.size];

        for (int i = 0; i < 3; i++) {
            temp[i] = this.walls[toMove[i]].getColumn(column);
        }
        temp[3] = this.walls[4].getColumn(this.size - 1 - column); // REVERSE!

        Collections.reverse(Arrays.asList(temp[3]));
        Collections.reverse(Arrays.asList(temp[2]));

        for (int i = 0; i < 3; i++) {
            int j = i > 0 ? i - 1 : 3;
            this.walls[toMove[i]].setColumn(column, temp[j]);
        }
        this.walls[4].setColumn(this.size - 1 - column, temp[2]);
    }

    private void rotateFor24(int side, int layer) {
        Integer[][] temp = new Integer[4][this.size];
        temp[0] = this.walls[0].getRow(this.size - 1 - layer);
        temp[1] = this.walls[3].getColumn(layer);
        temp[2] = this.walls[5].getRow(layer);
        temp[3] = this.walls[1].getColumn(this.size - 1 - layer);

        if (side == 2) {
            Collections.reverse(Arrays.asList(temp[1]));
            Collections.reverse(Arrays.asList(temp[3]));

            this.walls[0].setRow(this.size - 1 - layer, temp[3]);
            this.walls[3].setColumn(layer, temp[0]);
            this.walls[5].setRow(layer, temp[1]);
            this.walls[1].setColumn(this.size - 1 - layer, temp[2]);
        } else if (side == 4) {
            Collections.reverse(Arrays.asList(temp[0]));
            Collections.reverse(Arrays.asList(temp[2]));

            this.walls[0].setRow(this.size - 1 - layer, temp[1]);
            this.walls[3].setColumn(layer, temp[2]);
            this.walls[5].setRow(layer, temp[3]);
            this.walls[1].setColumn(this.size - 1 - layer, temp[0]);
        }
    }

    public void rotate(int side, int layer) throws InterruptedException {
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

        if (layer == 0) this.walls[side].rotateWall(true);
        if (layer == this.size - 1) this.walls[this.walls[side].getNumberOfOppositeSide()].rotateWall(false);

        this.afterRotation.accept(side, layer);
    }

    public String show() throws InterruptedException {
        this.beforeShowing.run();

        StringBuilder result = new StringBuilder();
        for (int wallNo = 0; wallNo < numberOfWalls; wallNo++) {
            result.append(this.walls[wallNo].toString());
        }

        this.afterShowing.run();

        return result.toString();
    }

    public void prettyShow() {
        Integer[] temp;

        for (int i = 0; i < this.size; i++) {
            System.out.print(" ".repeat(2 * this.size + 2));
            temp = this.walls[0].getRow(i);

            for (int j = 0; j < this.size; j++) {
                System.out.printf("%d ", temp[j]);
            }

            System.out.println();
        }
        System.out.println();

        for (int i = 0; i < this.size; i++) {
            for (int cube = 1; cube < 5; cube++) {
                temp = this.walls[cube].getRow(i);

                for (int j = 0; j < this.size; j++) {
                    System.out.printf("%d ", temp[j]);
                }

                System.out.print(" ".repeat(2));
            }
            System.out.println();
        }
        System.out.println();

        for (int i = 0; i < this.size; i++) {
            System.out.print(" ".repeat(2 * this.size + 2));
            temp = this.walls[5].getRow(i);

            for (int j = 0; j < this.size; j++) {
                System.out.printf("%d ", temp[j]);
            }
            System.out.println();
        }
    }
}