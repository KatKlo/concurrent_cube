package concurrentcube.tests;

import concurrentcube.Cube;
import concurrentcube.tests.TestUtils.Counter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class TestConcurrency {
    private Cube testingCube;
    private Counter showCounter;
    private Counter rotateCounter;
    private Counter interruptedCounter;
    private static final int CUBE_SLEEP_TIME = 200;

    private static final int SIZE = 4;
    private static final int THREADS_COUNT = 4;

    private void setUp() {
        this.rotateCounter = new Counter();
        this.showCounter = new Counter();
        this.interruptedCounter = new Counter();

        this.testingCube = new Cube(SIZE,
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

    public void testShowConcurrency() {
        System.out.println("Testing show concurrency");

        this.setUp();
        ArrayList<Thread> threads = new ArrayList<>();
        Instant start = Instant.now();

        for (int i = 0; i < THREADS_COUNT; i++) {
            Thread t = new Thread(() -> Assertions.assertDoesNotThrow(() -> testingCube.show()));

            threads.add(t);
            t.start();
        }

        for (Thread thread : threads)
            Assertions.assertDoesNotThrow((Executable) thread::join);


        Duration duration = Duration.between(start, Instant.now());

        Assertions.assertEquals(THREADS_COUNT * 11, showCounter.get(), "  - before/after BAD");
        System.out.println("  + before/after OK");

        Assertions.assertEquals(0, interruptedCounter.get(), "  - interruptions in sleep BAD");
        System.out.println("  + interruptions in sleep OK");

        Assertions.assertTrue(duration.compareTo(Duration.of(2 * CUBE_SLEEP_TIME, ChronoUnit.MILLIS)) <= 0, "  - duration BAD");
        System.out.println("  + duration OK");
    }

    public void testRotateConcurrency() {
        System.out.println("Testing rotate concurrency");
        rotateConcurrency(0, 5);
        rotateConcurrency(1, 3);
        rotateConcurrency(2, 4);
    }

    private void rotateConcurrency(int side, int opposite) {
        System.out.println("  rotate side " + side + " and " + opposite + " concurrency");

        this.setUp();
        ArrayList<Thread> threads = new ArrayList<>();
        Instant start = Instant.now();

        for (int i = 0; i < SIZE / 2; i++) {
            int finalI = i;
            Thread t1 = new Thread(() -> Assertions.assertDoesNotThrow(() -> testingCube.rotate(side, finalI)));

            Thread t2 = new Thread(() -> Assertions.assertDoesNotThrow(() -> testingCube.rotate(opposite, finalI)));

            threads.add(t1);
            threads.add(t2);
            t1.start();
            t2.start();
        }

        for (Thread thread : threads)
            Assertions.assertDoesNotThrow((Executable) thread::join);

        Duration duration = Duration.between(start, Instant.now());

        Assertions.assertEquals(THREADS_COUNT * 11, rotateCounter.get(), "- before/after BAD");
        System.out.println("    + before/after OK");

        Assertions.assertEquals(0, interruptedCounter.get(), "  - interruptions in sleep BAD");
        System.out.println("  + interruptions in sleep OK");

        Assertions.assertTrue(duration.compareTo(Duration.of(2 * CUBE_SLEEP_TIME, ChronoUnit.MILLIS)) <= 0, "- duration BAD");
        System.out.println("    + duration OK");
    }
}
