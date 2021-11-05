package concurrentcube;

import java.util.function.BiConsumer;

public class Cube {
    private int size;
    private BiConsumer<Integer, Integer> beforeRotation;
    private BiConsumer<Integer, Integer> afterRotation;
    private Runnable beforeShowing;
    private Runnable afterShowing;
    private int[][][] walls;
    private final int numberOfWalls = 6;

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
        this.walls = new int[numberOfWalls][size][size];

        for (int wallNo = 0; wallNo < numberOfWalls; wallNo++) {
            for (int line = 0; line < size; line++) {
                for (int column = 0; column < size; column++) {
                    this.walls[wallNo][line][column] = wallNo;
                }
            }
        }
    }

    private int oppositeWall(int side) {
        int result;

        switch (side) {
            case 0:
                result = 5;
                break;
            case 1:
                result = 3;
                break;
            case 2:
                result = 4;
                break;
            case 3:
                result = 1;
                break;
            case 4:
                result = 2;
                break;
            case 5:
                result = 0;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + side);
        }

        return result;
    }

    private void rotateFor05(int side, int line) {
        int[] toMove;

        if (side == 0)  toMove = new int[] {4, 3, 2, 1};
        else if (side == 5)  toMove = new int[] {1, 2, 3, 4};
        else return;

        for (int column = 0; column < this.size; column++) {
            int[] temp = new int[4];

            for (int i = 0; i < 4; i++) {
                temp[i] = this.walls[toMove[i]][line][column];
            }

            for (int i = 0; i < 4; i++) {
                int j = i > 0 ? i - 1 : 3;
                this.walls[toMove[i]][line][column] = temp[j];
            }
        }
    }

    private void rotateFor13(int side, int column) {
        int[] toMove;

        if (side == 1)  toMove = new int[] {0, 2, 5};
        else if (side == 3)  toMove = new int[] {5, 2, 0};
        else return;

        for (int line = 0; line < this.size; line++) {
            int[] temp = new int[4];

            for (int i = 0; i < 3; i++) {
                temp[i] = this.walls[toMove[i]][line][column];
            }
            temp[3] = this.walls[4][this.size - 1 - line][this.size - 1 - column];

            for (int i = 0; i < 3; i++) {
                int j = i > 0 ? i - 1 : 3;
                this.walls[toMove[i]][line][column] = temp[j];
            }
            this.walls[4][this.size - 1 - line][this.size - 1 - column] = temp[2];
        }
    }

    private void rotateFor24(int side, int layer) {
        for (int number = 0; number < this.size; number++) {
            int[] temp = new int[4];

            temp[0] = this.walls[0][this.size - 1 - layer][number];
            temp[1] = this.walls[3][number][layer];
            temp[2] = this.walls[5][layer][this.size - 1 - number];
            temp[3] = this.walls[1][this.size - 1 - number][this.size - 1 - layer];

            if (side == 2) {
                this.walls[0][this.size - 1 - layer][number] = temp[3];
                this.walls[3][number][layer] = temp[0];
                this.walls[5][layer][this.size - 1 - number] = temp[1];
                this.walls[1][this.size - 1 - number][this.size - 1 - layer] = temp[2];
            }
            else if (side == 4) {
                this.walls[0][this.size - 1 - layer][number] = temp[1];
                this.walls[3][number][layer] = temp[2];
                this.walls[5][layer][this.size - 1 - number] = temp[3];
                this.walls[1][this.size - 1 - number][this.size - 1 - layer] = temp[4];
            }
            else {
                return;
            }
        }
    }

    private void rotateWall(int side, boolean right) {
        int[][] newSide = new int[this.size][this.size];

        for (int line = 0; line < this.size; line++) {
            for (int column = 0; column < this.size; column++) {
                if (right) newSide[column][this.size - 1 - line] = this.walls[side][line][column];
                else newSide[this.size - 1 - column][line] = this.walls[side][line][column];
            }
        }

        for (int line = 0; line < this.size; line++) {
            System.arraycopy(newSide[line], 0, this.walls[side][line], 0, this.size);
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
                rotateFor24(4, this.size - layer);
                break;
            case 5:
                rotateFor05(5, this.size - 1 - layer);
                break;
            default:
                System.out.println("NIE ZAIMPLEMENTOWANO");
        }

        if (layer == 0) rotateWall(side, true);
        if (layer == side - 1) rotateWall(oppositeWall(side), false);

        this.afterRotation.accept(side, layer);
    }

    public String show() throws InterruptedException {
        this.beforeShowing.run();

        String result = "";
        for (int wallNo = 0; wallNo < numberOfWalls; wallNo++) {
            for (int line = 0; line < this.size; line++) {
                for (int column = 0; column < this.size; column++) {
                    result += walls[wallNo][line][column];
                }
            }
        }

        this.afterShowing.run();

        return result;
    }

}