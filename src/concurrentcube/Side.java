package concurrentcube;

import java.util.Arrays;
import java.util.Collections;

public class Side {
    private final int size;
    private final Integer[][] wall;

    public Side(int number, int size) {
        this.size = size;
        this.wall = new Integer[size][size];

        for (int row = 0; row < size; row++)
            for (int column = 0; column < size; column++)
                this.wall[row][column] = number;
    }

    public Integer[] getColumn(int layer) {
        assert(layer >= 0 && layer < this.size);

        Integer[] result = new Integer[this.size];

        for (int row = 0; row < this.size; row++)
            result[row] = this.wall[row][layer];

        return result;
    }

    public Integer[] getRow(int layer) {
        assert(layer >= 0 && layer < this.size);

        Integer[] result = new Integer[this.size];
        System.arraycopy(this.wall[layer], 0, result, 0, this.size);

        return result;
    }

    public void setColumn(int layer, Integer[] newColumn, boolean inReverse) {
        assert(layer >= 0 && layer < this.size);
        assert(newColumn.length == this.size);

        if (inReverse) Collections.reverse(Arrays.asList(newColumn));

        for (int row = 0; row < this.size; row++)
            this.wall[row][layer] = newColumn[row];
    }

    public void setRow(int layer, Integer[] newRow, boolean inReverse) {
        assert(layer >= 0 && layer < this.size);
        assert(newRow.length == this.size);

        if (inReverse) Collections.reverse(Arrays.asList(newRow));

        System.arraycopy(newRow, 0, this.wall[layer], 0, this.size);
    }

    public Integer[] replaceColumn(int layer, Integer[] newColumn, boolean inReverse) {
        Integer[] oldColumn = getColumn(layer);
        setColumn(layer, newColumn, inReverse);

        return oldColumn;
    }

    public Integer[] replaceRow(int layer, Integer[] newRow, boolean inReverse) {
        Integer[] oldRow = getRow(layer);
        setRow(layer, newRow, inReverse);

        return oldRow;
    }

    public void rotateWall(boolean clockwise) {
        Integer[][] newWall = new Integer[this.size][this.size];

        for (int line = 0; line < this.size; line++) {
            for (int column = 0; column < this.size; column++) {
                if (clockwise) newWall[column][this.size - 1 - line] = this.wall[line][column];
                else newWall[this.size - 1 - column][line] = this.wall[line][column];
            }
        }

        for (int line = 0; line < this.size; line++)
            System.arraycopy(newWall[line], 0, this.wall[line], 0, this.size);
    }

    public String show() {
        StringBuilder result = new StringBuilder();

        for (int row = 0; row < this.size; row++)
            for (int column = 0; column < this.size; column++)
                result.append(this.wall[row][column]);

        return result.toString();
    }
}
