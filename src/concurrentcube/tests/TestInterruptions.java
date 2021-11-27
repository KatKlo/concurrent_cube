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

    public void testShowInterrupted() {
        System.out.println("Testing handling 'show interrupted' in " + size + "x" + size + " cube:");
        setUp();

        Thread[] threadsOrder = {
                new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.rotate(RANDOM.nextInt(6), RANDOM.nextInt(size)))),
                new Thread(() -> Assertions.assertThrows(InterruptedException.class, () -> cubeConcurrent.show())),
                new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.show())),
                new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.rotate(RANDOM.nextInt(6), RANDOM.nextInt(size))))
        };

        test4Threads(threadsOrder, 11, 2 * 11);
    }

    public void testRotationInterruptedWaitingOnGroup() {
        System.out.println("Testing handling 'one rotate interrupted while waiting on group' in " + size + "x" + size + " cube:");
        setUp();

        Thread[] threadsOrder = {
                new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.show())),
                new Thread(() -> Assertions.assertThrows(InterruptedException.class, () -> cubeConcurrent.rotate(1, RANDOM.nextInt(size)))),
                new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.rotate(1, RANDOM.nextInt(size)))),
                new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.rotate(2, RANDOM.nextInt(size))))
        };

        test4Threads(threadsOrder, 11, 2 * 11);
    }

    public void testRotationInterruptedWaitingOnLayer() {
        System.out.println("Testing handling 'one rotate interrupted while waiting on layer' in " + size + "x" + size + " cube:");
        setUp();

        Thread[] threadsOrder = {
                new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.rotate(1, 0))),
                new Thread(() -> Assertions.assertThrows(InterruptedException.class,() -> cubeConcurrent.rotate(3, this.size - 1))),
                new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.rotate(1, 0))),
                new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.rotate(2, RANDOM.nextInt(size))))
        };

        test4Threads(threadsOrder, 0, 3 * 11);
    }

    public void testArrangementAfterRotationInterrupted() {
        System.out.println("Testing if cube stays the same after one rotate interrupted in " + size + "x" + size + " cube:");
        setUp();

        StringBuilder expected = new StringBuilder();
        final int squaresForSide = size * size;

        for (int i = 0; i < 6; i++) {
            expected.append(Integer.toString(i).repeat(squaresForSide));
        }

        Thread showThread = new Thread(() -> Assertions.assertDoesNotThrow(() -> cubeConcurrent.show()));
        Thread rotateThread = new Thread(() -> Assertions.assertThrows(InterruptedException.class,() -> cubeConcurrent.rotate(RANDOM.nextInt(6), RANDOM.nextInt(size))));

        startWithDelay(showThread);
        startWithDelay(rotateThread);
        rotateThread.interrupt();

        Assertions.assertDoesNotThrow((Executable) showThread::join);
        Assertions.assertDoesNotThrow((Executable) rotateThread::join);
        System.out.println("  + throwing exceptions OK");

        String result = Assertions.assertDoesNotThrow(() -> cubeConcurrent.show());
        Assertions.assertEquals(expected.toString(), result, "  - strings BAD");
        System.out.println("  + strings OK");

        Assertions.assertEquals(2 * 11, showCounter.get(), "  - before/after show BAD");
        System.out.println("  + before/after show OK");

        Assertions.assertEquals(0, rotateCounter.get(), "  - before/after rotation BAD");
        System.out.println("  + before/after rotation OK");
    }

    private void delay() {
        try {
            Thread.sleep(TEST_SLEEP_TIME);
        } catch (InterruptedException ignored) {}
    }

    private void startWithDelay(Thread t) {
        t.start();
        delay();
    }

    private void test4Threads(Thread[] threads, int showC, int rotateC) {
        startWithDelay(threads[0]);
        startWithDelay(threads[1]);
        threads[1].interrupt();
        delay();
        startWithDelay(threads[2]);
        threads[3].start();

        for (int i = 0; i < 4; i++) {
            Assertions.assertDoesNotThrow((Executable) threads[i]::join);
        }
        System.out.println("  + throwing exceptions OK");

        Assertions.assertEquals(showC, showCounter.get(), "  - before/after show BAD");
        System.out.println("  + before/after show OK");

        Assertions.assertEquals(rotateC, rotateCounter.get(), "  - before/after rotation BAD");
        System.out.println("  + before/after rotation OK");
    }
}
