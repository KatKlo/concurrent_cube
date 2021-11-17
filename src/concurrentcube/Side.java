package concurrentcube;

public class Side {
    private final NameOfSide name;
    private final int size;
    private final Integer[][] wall;

    public Side(NameOfSide name, int size) {
        this.name = name;
        this.size = size;
        this.wall = new Integer[size][size];

        int number = name.getNumber();
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                this.wall[row][column] = number;
            }
        }
    }

    public Integer[] getColumn(int layer) {
        Integer[] result = new Integer[this.size];

        for (int row = 0; row < this.size; row++) {
            result[row] = this.wall[row][layer];
        }

        return result;
    }

    public Integer[] getRow(int layer) {
        Integer[] result = new Integer[this.size];

        if (this.size >= 0) System.arraycopy(this.wall[layer], 0, result, 0, this.size);

        return result;
    }

    public void setColumn(int layer, Integer[] newColumn) {
        for (int row = 0; row < this.size; row++) {
            this.wall[row][layer] = newColumn[row];
        }
    }

    public void setRow(int layer, Integer[] newRow) {
        if (this.size >= 0) System.arraycopy(newRow, 0, this.wall[layer], 0, this.size);
    }

    public void rotateWall(boolean clockwise) {
        Integer[][] newSide = new Integer[this.size][this.size];

        for (int line = 0; line < this.size; line++) {
            for (int column = 0; column < this.size; column++) {
                if (clockwise) newSide[column][this.size - 1 - line] = this.wall[line][column];
                else newSide[this.size - 1 - column][line] = this.wall[line][column];
            }
        }

        for (int line = 0; line < this.size; line++) {
            System.arraycopy(newSide[line], 0, this.wall[line], 0, this.size);
        }
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int row = 0; row < this.size; row++) {
            for (int column = 0; column < this.size; column++) {
                result.append(this.wall[row][column]);
            }
        }

        return result.toString();
    }

    public int getNumberOfOppositeSide() {
        return this.name.getNumberOfOppositeSide();
    }
}
