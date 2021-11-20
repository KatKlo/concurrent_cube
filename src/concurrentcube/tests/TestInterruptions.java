package concurrentcube.tests;

import concurrentcube.Cube;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import java.util.Random;


public class TestInterruptions {
    private static final Random RANDOM = new Random();
    private final int size;
    private Cube cubeConcurrent;
    private Counter rotateCounter;
    private Counter showCounter;
    private Counter interruptedCounter;
    private static final int CUBE_SLEEP_TIME = 50;
    private static final int TEST_SLEEP_TIME = 5;


    private void setUp() {
        this.rotateCounter = new Counter();
        this.showCounter = new Counter();
        this.interruptedCounter = new Counter();

        this.cubeConcurrent = new Cube(this.size,
                                       (x, y) -> {
                                           this.rotateCounter.add(2);
                                           try {
                                               Thread.sleep(CUBE_SLEEP_TIME);
                                           } catch (InterruptedException e) {
                                               this.interruptedCounter.add(1);
                                           }
                                       },
                                       (x, y) -> this.rotateCounter.add(9),
                                       () -> {
                                           this.showCounter.add(2);
                                           try {
                                               Thread.sleep(CUBE_SLEEP_TIME);
                                           } catch (InterruptedException e) {
                                               this.interruptedCounter.add(1);
                                           }
                                       },
                                       () -> this.showCounter.add(9)
        );
    }

    public TestInterruptions(int size) {
        this.size = size;
    }

    public void test() {
        System.out.println("Testing interruptions handling in " + size + "x" + size + " cube:");
        testShowInterrupted();
        testRotationInterruptedWaitingOnGroup();
        testRotationInterruptedWaitingOnLayer();
        testArrangementAfterRotationInterrupted();
    }

    private void testShowInterrupted() {
        System.out.println("  Show interrupted:");
        setUp();

        Thread rotateThread1 = new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.rotate(RANDOM.nextInt(6), RANDOM.nextInt(size))));
        Thread rotateThread2 = new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.rotate(RANDOM.nextInt(6), RANDOM.nextInt(size))));
        Thread showThread1 = new Thread(() -> Assertions.assertThrows(InterruptedException.class, () -> cubeConcurrent.show()));
        Thread showThread2 = new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.show()));

        rotateThread1.start();

        try {
            Thread.sleep(TEST_SLEEP_TIME);
        } catch (InterruptedException e) {
            return;
        }

        showThread1.start();
        showThread1.interrupt();
        showThread2.start();
        rotateThread2.start();

        Assertions.assertDoesNotThrow((Executable) rotateThread1::join);
        Assertions.assertDoesNotThrow((Executable) showThread1::join);
        Assertions.assertDoesNotThrow((Executable) showThread2::join);
        Assertions.assertDoesNotThrow((Executable) rotateThread2::join);
        System.out.println("    + throwing exceptions OK");

        Assertions.assertEquals(11, showCounter.get(), "    - before/after show BAD");
        System.out.println("    + before/after show OK");

        Assertions.assertEquals(2 * 11, rotateCounter.get(), "    - before/after rotation BAD");
        System.out.println("    + before/after rotation OK");

    }

    private void testRotationInterruptedWaitingOnGroup() {
        System.out.println("  One rotate interrupted while waiting on group:");
        setUp();

        Thread showThread = new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.show()));
        Thread rotateThread1 = new Thread(() -> Assertions.assertThrows(InterruptedException.class, () -> cubeConcurrent.rotate(1, RANDOM.nextInt(size))));
        Thread rotateThread2 = new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.rotate(1, RANDOM.nextInt(size))));
        Thread rotateThread3 = new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.rotate(2, RANDOM.nextInt(size))));

        showThread.start();

        try {
            Thread.sleep(TEST_SLEEP_TIME);
        } catch (InterruptedException e) {
            return;
        }

        rotateThread1.start();
        rotateThread1.interrupt();
        rotateThread2.start();
        rotateThread3.start();

        Assertions.assertDoesNotThrow((Executable) showThread::join);
        Assertions.assertDoesNotThrow((Executable) rotateThread1::join);
        Assertions.assertDoesNotThrow((Executable) rotateThread2::join);
        Assertions.assertDoesNotThrow((Executable) rotateThread3::join);
        System.out.println("    + throwing exceptions OK");

        Assertions.assertEquals(11, showCounter.get(), "    - before/after show BAD");
        System.out.println("    + before/after show OK");

        Assertions.assertEquals(2 * 11, rotateCounter.get(), "    - before/after rotation BAD");
        System.out.println("    + before/after rotation OK");

    }

    private void testRotationInterruptedWaitingOnLayer() {
        System.out.println("  One rotate interrupted while waiting on layer:");
        setUp();

        Thread rotateThread1 = new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.rotate(1, 0)));
        Thread rotateThread2 = new Thread(() -> Assertions.assertThrows(InterruptedException.class,() -> cubeConcurrent.rotate(3, this.size - 1)));
        Thread rotateThread3 = new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.rotate(1, 0)));
        Thread rotateThread4 = new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.rotate(2, RANDOM.nextInt(size))));

        rotateThread1.start();

        try {
            Thread.sleep(TEST_SLEEP_TIME);
        } catch (InterruptedException e) {
            return;
        }

        rotateThread2.start();
        rotateThread2.interrupt();
        rotateThread3.start();
        rotateThread4.start();

        Assertions.assertDoesNotThrow((Executable) rotateThread1::join);
        Assertions.assertDoesNotThrow((Executable) rotateThread2::join);
        Assertions.assertDoesNotThrow((Executable) rotateThread3::join);
        Assertions.assertDoesNotThrow((Executable) rotateThread4::join);
        System.out.println("    + throwing exceptions OK");

        Assertions.assertEquals(0, showCounter.get(), "    - before/after show BAD");
        System.out.println("    + before/after show OK");

        Assertions.assertEquals(3 * 11, rotateCounter.get(), "    - before/after rotation BAD");
        System.out.println("    + before/after rotation OK");

    }

    private void testArrangementAfterRotationInterrupted() {
        System.out.println("  Cube stays the same after one rotate interrupted:");
        setUp();

        StringBuilder expected = new StringBuilder();
        final int squaresForSide = size * size;

        for (int i = 0; i < 6; i++) {
            expected.append(Integer.toString(i).repeat(squaresForSide));
        }

        Thread showThread = new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.show()));
        Thread rotateThread = new Thread(() -> Assertions.assertThrows(InterruptedException.class,() -> cubeConcurrent.rotate(RANDOM.nextInt(6), RANDOM.nextInt(size))));

        showThread.start();

        try {
            Thread.sleep(TEST_SLEEP_TIME);
        } catch (InterruptedException e) {
            return;
        }

        rotateThread.start();
        rotateThread.interrupt();

        Assertions.assertDoesNotThrow((Executable) showThread::join);
        Assertions.assertDoesNotThrow((Executable) rotateThread::join);
        System.out.println("    + throwing exceptions OK");

        String result = Assertions.assertDoesNotThrow(() -> cubeConcurrent.show());
        Assertions.assertEquals(expected.toString(), result, "    - strings BAD");
        System.out.println("    + strings OK");

        Assertions.assertEquals(2 * 11, showCounter.get(), "    - before/after show BAD");
        System.out.println("    + before/after show OK");

        Assertions.assertEquals(0, rotateCounter.get(), "    - before/after rotation BAD");
        System.out.println("    + before/after rotation OK");

    }
}
