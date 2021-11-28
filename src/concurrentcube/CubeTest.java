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

    /**
     * Poprawność sekwencyjna show bez obrotów
     */
    @Test
    public void testShowWithoutRotations() {
        showT.testShowWithoutRotations();
    }

    /**
     * Poprawność sekwencyjna rotate i show
     */
    @Test
    public void testSequentialRotations() {
        rotateSequentialT.testRotateAndShowSequential();
    }

    /**
     * Poprawność współbieżnych rotate
     */
    @Test
    @Timeout(5)
    public void testConcurrentRotations() {
        rotateConcurrentT.testConcurrentRotate();
    }

    /**
     * Współbieżność samego show
     */
    @Test
    @Timeout(1)
    public void testShowConcurrency() {
        concurrencyT.testShowConcurrency();
    }

    /**
     * Współbieżność samego rotate
     */
    @Test
    @Timeout(1)
    public void testRotateConcurrency() {
        concurrencyT.testRotateConcurrency();
    }

    /**
     * Przerwanie show
     */
    @Test
    @Timeout(1)
    public void testInterruptOnShow() {
        interruptionsT.testShowInterrupted();
    }

    /**
     * Przerwanie rotate czekającego na dostęp do osi
     */
    @Test
    @Timeout(1)
    public void testInterruptOnRotationWaitingOnGroup() {
        interruptionsT.testRotationInterruptedWaitingOnGroup();
    }

    /**
     * Przerwanie rotate czekającego na dostęp do warstwy
     */
    @Test
    @Timeout(1)
    public void testInterruptOnRotationWaitingOnLayer() {
        interruptionsT.testRotationInterruptedWaitingOnLayer();
    }

    /**
     * Odpowiedni układ kostki po przerwaniu rotate
     */
    @Test
    @Timeout(1)
    public void testArrangementAfterRotationInterrupted() {
        interruptionsT.testArrangementAfterRotationInterrupted();
    }

    /**
     * Przerwania natepują w miare szybko (niezależnie od czasu wykonania before i after)
     */
    @Test
    @Timeout(1)
    public void testInterruptionsTime() {
        interruptionsT.testTimeInterrupted();
    }
}
