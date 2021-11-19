package concurrentcube;

import concurrentcube.tests.TestRotateConcurrent;
import concurrentcube.tests.TestRotateSequential;
import concurrentcube.tests.TestShow;
import org.junit.jupiter.api.Test;

public class CubeTest {
    private final int size = 5;

    @Test
    public void testShowWithoutRotations() {
        TestShow test = new TestShow(size);
        test.test();
    }

    @Test
    public void testSequentialRotations() {
        TestRotateSequential test = new TestRotateSequential();
        test.test();
    }

    @Test
    public void testConcurrentRotations() {
        TestRotateConcurrent test = new TestRotateConcurrent(size);
        test.test();
    }
}
