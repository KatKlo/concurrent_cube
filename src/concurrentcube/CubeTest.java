package concurrentcube;

import concurrentcube.tests.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

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
    @Timeout(2)
    public void testShowConcurrency() {
        concurrencyT.testShowConcurrency();
    }

    @Test
    @Timeout(4)
    public void testRotateConcurrency() {
        concurrencyT.testRotateConcurrency();
    }

    @Test
    @Timeout(1)
    public void testConcurrentRotations() {
        rotateConcurrentT.testConcurrentRotate();
    }

    @Test
    @Timeout(1)
    public void testInterruptOnShow() {
        interruptionsT.testShowInterrupted();
    }

    @Test
    @Timeout(1)
    public void testInterruptOnRotationWaitingOnGroup() {
        interruptionsT.testRotationInterruptedWaitingOnGroup();
    }

    @Test
    @Timeout(1)
    public void testInterruptOnRotationWaitingOnLayer() {
        interruptionsT.testRotationInterruptedWaitingOnLayer();
    }

    @Test
    @Timeout(1)
    public void testArrangementAfterRotationInterrupted() {
        interruptionsT.testArrangementAfterRotationInterrupted();
    }
}
