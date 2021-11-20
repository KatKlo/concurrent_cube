package concurrentcube.tests;

import concurrentcube.Cube;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class TestShow {
    private final int size;
    private final Cube testingCube;
    private final Counter showCounter = new Counter();

    private final String EXPECTED;
    private static final int THREADS_COUNT = 4;

    public TestShow(int size) {
        this.size = size;

        testingCube = new Cube(size,
                               (x, y) -> {},
                               (x, y) -> {},
                               () -> {
                                   try {
                                       TimeUnit.SECONDS.sleep(1);
                                   } catch (InterruptedException e) {
                                       System.out.println("przerwano :)");
                                       return;
                                   }
                                   showCounter.add(2);
                               },
                               () -> showCounter.add(9)
        );

        StringBuilder expected = new StringBuilder();
        final int squaresForSide = size * size;

        for (int i = 0; i < 6; i++) {
            expected.append(Integer.toString(i).repeat(squaresForSide));
        }

        this.EXPECTED = expected.toString();
    }

    public void test() {
        System.out.println("Testing showing cube " + size + "x" + size + " without rotations:");

        ArrayList<Thread> threads = new ArrayList<>();
        ConcurrentLinkedQueue<String> results = new ConcurrentLinkedQueue<>();
        Instant start = Instant.now();

        for (int i = 0; i < THREADS_COUNT; i++) {
            Thread t = new Thread(() -> {
                final String[] a = new String[1];
                Assertions.assertDoesNotThrow(() -> a[0] = testingCube.show());
                results.add(a[0]);
            });

            threads.add(t);
            t.start();
        }

        for (Thread thread : threads) {
            Assertions.assertDoesNotThrow((Executable) thread::join);
        }


        Duration duration = Duration.between(start, Instant.now());

        for (int i = 0; i < THREADS_COUNT; i++) {
            Assertions.assertEquals(EXPECTED, results.poll(), "  - strings BAD");
        }
        System.out.println("  + strings OK");

        Assertions.assertEquals(THREADS_COUNT * 11, showCounter.get(), "  - before/after BAD");
        System.out.println("  + before/after OK");

        Assertions.assertTrue(duration.compareTo(Duration.of(2, ChronoUnit.SECONDS)) <= 0, "  - duration BAD");
        System.out.println("  + duration OK");
    }
}
