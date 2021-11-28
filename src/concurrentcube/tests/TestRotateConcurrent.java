package concurrentcube.tests;

import concurrentcube.Cube;
import concurrentcube.tests.TestUtils.Counter;
import concurrentcube.tests.TestUtils.Pair;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;


public class TestRotateConcurrent {
    private static final Random RANDOM = new Random();
    private final int size;
    private final Cube cubeConcurrent;
    private final Cube cubeSequential;
    private final Counter rotateCounter = new Counter();
    private static final int ROTATE_COUNT = 1000;
    private static final int THREADS_COUNT = 50;
    private final ConcurrentLinkedQueue<Pair> rotationsQueue = new ConcurrentLinkedQueue<>();

    public TestRotateConcurrent(int size) {
        this.size = size;

        this.cubeConcurrent = new Cube(this.size,
                                       (x, y) -> {
                                           rotateCounter.add(2);
                                           rotationsQueue.add(new Pair(x, y));
                                       },
                                       (x, y) -> rotateCounter.add(9),
                                       () -> {},
                                       () -> {}
        );

        this.cubeSequential = new Cube(this.size,
                                       (x, y) -> {},
                                       (x, y) -> {},
                                       () -> {},
                                       () -> {}
        );
    }

    public void testConcurrentRotate() {
        System.out.println("Testing concurrent rotating cube " + size + "x" + size + ":");

        ArrayList<Thread> threads = new ArrayList<>();

        for (int i = 0; i < THREADS_COUNT; i++) {
            Thread t = new Thread(() -> {
                for (int j = 0; j < ROTATE_COUNT; j++) {
                    Assertions.assertDoesNotThrow(() -> this.cubeConcurrent.rotate(RANDOM.nextInt(6), RANDOM.nextInt(this.size)));
                }
            });
            threads.add(t);
            t.start();
        }

        for (int i = 0; i < THREADS_COUNT; i++) {
            int finalI = i;
            Assertions.assertDoesNotThrow(() -> threads.get(finalI).join());
        }

        while (!rotationsQueue.isEmpty()) {
            Pair args = rotationsQueue.poll();
            Assertions.assertDoesNotThrow(() -> cubeSequential.rotate(args.first, args.second));
        }

        String concurrent = Assertions.assertDoesNotThrow(cubeConcurrent::show);
        String sequential = Assertions.assertDoesNotThrow(cubeSequential::show);

        Assertions.assertEquals(sequential, concurrent, "  - strings BAD");
        System.out.println("  + strings OK");

        Assertions.assertEquals(THREADS_COUNT * ROTATE_COUNT * 11, rotateCounter.get(), "  - before/after BAD");
        System.out.println("  + before/after OK");

    }
}
