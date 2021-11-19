package concurrentcube.tests;

import concurrentcube.Counter;
import concurrentcube.Cube;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestShow {
    private final Cube testingCube;
    private final Counter showCounter = new Counter();

    private final String EXPECTED;
    private static final int THREADS_COUNT = 4;

    public TestShow(int size) {
        testingCube = new Cube(size,
                (x, y) -> {
                },
                (x, y) -> {
                },
                () -> {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        System.out.println("przerwano :)");
                        return;
                    }
                    showCounter.add(1);
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
        System.out.println("Testing showing cube without rotations: ");

        List<Thread> threads = new ArrayList<>();
        List<String> results = new ArrayList<>();

        for (int i = 0; i < THREADS_COUNT; i++) {
            threads.add(new Thread(() -> {
                try {
                    String a = testingCube.show();
                    results.add(a);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        Instant start = Instant.now();

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            Assertions.assertDoesNotThrow(() ->thread.join());
        }

        Duration duration = Duration.between(start, Instant.now());

        for (int i = 0; i < THREADS_COUNT; i++) {
            Assertions.assertEquals(EXPECTED, results.get(i), "- strings BAD");
        }
        System.out.println("+ strings OK");

        Assertions.assertEquals(THREADS_COUNT * 10, showCounter.get(), "- before/after BAD");
        System.out.println("+ before/after OK");

        Assertions.assertTrue(duration.compareTo(Duration.of(2, ChronoUnit.SECONDS)) <= 0, "- duration BAD");
        System.out.println("+ duration OK");
    }
}
