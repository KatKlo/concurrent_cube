package concurrentcube;

import concurrentcube.tests.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CubeTest {
    private final static int SIZE = 5;
    private static TestShow showT;
    private static TestRotateSequential rotateSequentialT;
    private static TestConcurrency concurrencyT;
    private static TestRotateConcurrent rotateConcurrentT;
    private static TestInterruptions interruptionsT;

    @BeforeAll
    public static void setUp() {
        showT = new TestShow(SIZE);
        rotateSequentialT = new TestRotateSequential();
        concurrencyT = new TestConcurrency();
        rotateConcurrentT = new TestRotateConcurrent(SIZE);
        interruptionsT = new TestInterruptions(SIZE);
    }

    @Test
    public void testShowWithoutRotations() {
        showT.testShowWithoutRotations();
    }

    @Test
    public void testSequentialRotations() {
        rotateSequentialT.testRotateAndShowSequential();
    }

    @Test
    public void testShowConcurrency() {
        concurrencyT.testShowConcurrency();
    }

    @Test
    public void testRotateConcurrency() {
        concurrencyT.testRotateConcurrency();
    }

    @Test
    public void testConcurrentRotations() {
        rotateConcurrentT.testConcurrentRotate();
    }

    @Test
    public void testInterruptions() {
        interruptionsT.test();
    }
}
