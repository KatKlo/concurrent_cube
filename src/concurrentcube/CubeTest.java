package concurrentcube;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CubeTest {
    private final int size = 4;
    private Cube testingCube;
    private Counter showCounter;
    private Counter rotateCounter;

    private static class Counter {
        AtomicInteger value = new AtomicInteger(0);
    }

    @BeforeEach
    public void setUp() {
        showCounter = new Counter();
        rotateCounter = new Counter();

        testingCube = new Cube(size,
                (x, y) -> {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        System.out.println("przerwano :)");
                        return;
                    }
                    rotateCounter.value.addAndGet(10 * x + y);
                },
                (x, y) -> {
                    rotateCounter.value.addAndGet(100 * x + 10 * y);
                },
                () -> {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        System.out.println("przerwano :)");
                        return;
                    }
                    showCounter.value.addAndGet(1);
                },
                () -> {
                    showCounter.value.addAndGet(9);
                }
        );
    }

    @Test
    public void testCorrectnessShow() {
        System.out.println("Testing correctness of showing solved cube: ");

        StringBuilder expectedString = new StringBuilder();
        int squaresForSide = size * size;

        for (int i = 0; i < 6; i++) {
            expectedString.append(Integer.toString(i).repeat(squaresForSide));
        }

        String result = null;
        try {
            result = testingCube.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assertions.assertEquals(expectedString.toString(), result, "- string BAD");
        System.out.println("+ string OK");

        Assertions.assertEquals(10, showCounter.value.get(), "- before/after BAD");
        System.out.println("+ before/after OK");
    }

    @Test
    public void testConcurrentShow() {
        System.out.println("Testing concurrent of only showing solved cube: ");

        StringBuilder expectedString = new StringBuilder();
        final int squaresForSide = size * size;
        final int NumberOfThreads = 4;

        for (int i = 0; i < 6; i++) {
            expectedString.append(Integer.toString(i).repeat(squaresForSide));
        }

        List<Thread> threads = new ArrayList<>();
        List<String> results = new ArrayList<>();

        for (int i = 0; i < NumberOfThreads; i++) {
            threads.add(new Thread(() -> {
                try {
                    String a = testingCube.show();
                    results.add(a);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));
        }

        Instant start = Instant.now();

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Main thread interrupted");
            }
        }

        Duration duration = Duration.between(start, Instant.now());

        for (int i = 0; i < NumberOfThreads; i++) {
            Assertions.assertEquals(expectedString.toString(), results.get(i), "- strings BAD");
        }
        System.out.println("+ stringi OK");

        Assertions.assertEquals(NumberOfThreads * 10, showCounter.value.get(), "- before/after BAD");
        System.out.println("+ before/after OK");

        Assertions.assertTrue(duration.compareTo(Duration.of(2, ChronoUnit.SECONDS)) <= 0, "- duration BAD");
        System.out.println("+ duration OK");
    }
}
