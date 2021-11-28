package concurrentcube.tests;

import concurrentcube.Cube;
import concurrentcube.tests.TestUtils.Counter;
import org.junit.jupiter.api.Assertions;

public class TestShow {
    private final int size;
    private final Cube testingCube;
    private final Counter showCounter = new Counter();

    private final String EXPECTED;

    public TestShow(int size) {
        this.size = size;

        testingCube = new Cube(size,
                               (x, y) -> {},
                               (x, y) -> {},
                               () -> showCounter.add(2),
                               () -> showCounter.add(9)
        );

        StringBuilder expected = new StringBuilder();
        final int squaresForSide = size * size;

        for (int i = 0; i < 6; i++)
            expected.append(Integer.toString(i).repeat(squaresForSide));

        this.EXPECTED = expected.toString();
    }

    public void testShowWithoutRotations() {
        System.out.println("Testing showing cube " + size + "x" + size + " without rotations:");

        String[] result = new String[1];

        Assertions.assertDoesNotThrow(() -> result[0] = testingCube.show());

        Assertions.assertEquals(EXPECTED, result[0], "  - strings BAD");
        System.out.println("  + string OK");

        Assertions.assertEquals(11, showCounter.get(), "  - before/after BAD");
        System.out.println("  + before/after OK");
    }
}
