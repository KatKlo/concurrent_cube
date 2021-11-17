package concurrentcube;

public enum NameOfSide {
    UP(0), LEFT(1), FRONT(2), RIGHT(3), BACK(4), BOTTOM(5);

    private final int number;

    NameOfSide(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public int getNumberOfOppositeSide() {
        switch (number) {
            case 0:
                return 5;
            case 1:
                return 3;
            case 2:
                return 4;
            case 3:
                return 1;
            case 4:
                return 2;
            case 5:
                return 0;
            default:
                throw new IllegalStateException("Unexpected value: " + number);
        }
    }
}
