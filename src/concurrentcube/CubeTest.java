package concurrentcube;

import concurrentcube.tests.TestRotateSequential;
import concurrentcube.tests.TestShow;
import org.junit.jupiter.api.Test;

public class CubeTest {
    private final int size = 4;

    @Test
    public void testShowWithoutRotations() {
        TestShow test = new TestShow(size);
        test.test();
    }

    @Test
    public void testRotateWithoutRotations() {
        TestRotateSequential test = new TestRotateSequential();
        test.test();
    }
}
