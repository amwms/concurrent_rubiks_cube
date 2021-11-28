package concurrentcube;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static org.junit.jupiter.api.Assertions.*;

public class CubeTest {

    private Cube onlySizeCube(int size) {
        return new Cube(size, (x, y) -> {}, (x, y) -> {}, () -> {}, () -> {});
    }

    private boolean areCorrectColors(String cube, int size) {
        int[] tab = new int[6];
        for (int i = 0; i < cube.length(); i++) {
            int sideId = cube.charAt(i) - '0';
            tab[sideId]++;
        }

        for (int i = 0; i < 6; i++) {
            if (tab[i] != size * size) {
                return false;
            }
        }

        return true;
    }

    private int random(int min, int max) {
        max++;
        return (int) ((Math.random() * (max - min)) + min);
    }

    private int getSideGroup(int side) {
        if (side < 3)
            return side;

        return (side + 1) % 3;
    }

    private int getLayerGroup(int side, int layer, int size) {
        return side > 2 ? size - layer - 1 : layer;
    }

    private Cube testCubeInit(int size) {
        AtomicReferenceArray<Integer> inRotationLayers = new AtomicReferenceArray<>(new Integer[size]);
        AtomicReferenceArray<Integer> inRotationGroups = new AtomicReferenceArray<>(new Integer[4]);

        for (int i = 0; i < size; i++) {
            inRotationLayers.set(i, 0);
        }
        for (int i = 0; i < 4; i++) {
            inRotationGroups.set(i, 0);
        }

        Cube cube = new Cube(size,
                (x, y) -> {
                    inRotationGroups.getAndUpdate(getSideGroup(x), i -> i + 1);
                    inRotationLayers.getAndUpdate(getLayerGroup(x, y, size), i -> i + 1);

                    // only one thread per layer
                    for (int i = 0; i < size; i++) {
                        if (inRotationLayers.get(i) > 1)
                            fail();
                    }

                    // only one group in rotation
                    for (int i = 0; i < 4; i++) {
                        if (i != getSideGroup(x) && inRotationGroups.get(i) > 0)
                            fail();
                    }
                },
                (x, y) -> {
                    inRotationGroups.getAndUpdate(getSideGroup(x), i -> i - 1);
                    inRotationLayers.getAndUpdate(getLayerGroup(x, y, size), i -> i - 1);

                    // only one thread per layer
                    for (int i = 0; i < size; i++) {
                        if (inRotationLayers.get(i) > 1)
                            fail();
                    }

                    // only one group in rotation
                    for (int i = 0; i < 4; i++) {
                        if (i != getSideGroup(x) && inRotationGroups.get(i) > 0)
                            fail();
                    }
                },
                () -> {inRotationGroups.getAndUpdate(3, i -> i + 1);},
                () -> {inRotationGroups.getAndUpdate(3, i -> i - 1);});

        return cube;
    }

    // ------------ SEQUENTIAL TESTS ------------
    @Test
    @DisplayName("Sequential long test 1 - 3x3x3")
    public void SequentialLongTest1() {
        Cube cube = onlySizeCube(3);
        try {
            cube.rotate(2, 0);
            cube.rotate(3, 0);
            cube.rotate(4, 0);
            cube.rotate(5, 1);
            cube.rotate(2, 2);
            cube.rotate(0, 2);
            cube.rotate(1, 0);
            cube.rotate(4, 1);
        }
        catch (InterruptedException e) {
            fail();
        }

        String result = "542023412221201545023015133050253004153033114244144355";
        assertEquals(result, cube.cubeToString());
    }

    @Test
    @DisplayName("Sequential long test 2 - 3x3x3")
    public void SequentialLongTest2() {
        Cube cube = onlySizeCube(3);
        try {
            cube.rotate(2, 0);
            cube.rotate(3, 0);
            cube.rotate(4, 0);
            cube.rotate(5, 1);
            cube.rotate(2, 2);
            cube.rotate(0, 2);
            cube.rotate(1, 0);
            cube.rotate(4, 1);
            cube.rotate(3, 0);
            cube.rotate(0, 0);
            cube.rotate(1, 1);
            cube.rotate(1, 1);
            cube.rotate(3, 2);
            cube.rotate(0, 0);
            cube.rotate(4, 0);
            cube.rotate(5, 0);
            cube.rotate(0, 1);
            cube.rotate(0, 2);
            cube.rotate(0, 2);
            cube.rotate(3, 0);
            cube.rotate(2, 0);
            cube.rotate(2, 0);
            cube.rotate(1, 0);
            cube.rotate(3, 2);
            cube.rotate(4, 2);
            cube.rotate(4, 2);
        }
        catch (InterruptedException e) {
            fail();
        }

        String result = "353545341010134325250052432422113511534404025101020432";
        assertEquals(result, cube.cubeToString());
    }

    @Test
    @DisplayName("Sequential long random test 3 - nxnxn")
    public void SequentialLongTest3() {
        int size = random(3, 100);
        Cube cube = onlySizeCube(size);

        try {
            int i = 1000;
            while (i --> 0) {
                int side = random(0, 5);
                int layer = random(0, size - 1);
                cube.rotate(side, layer);
            }
        }
        catch (InterruptedException e) {
            fail();
        }

        assertTrue(areCorrectColors(cube.cubeToString(), size));
    }

    // ------------ CONCURRENT TESTS ------------
    @Test
    @DisplayName("Small concurrent test 1 - 3x3x3")
    public void smallConcurrentTest1() {
        final String[] result = {""};
        int size = 3;
        Cube cube = onlySizeCube(size);

        Thread t1 = new Thread(() -> {
            try {
                cube.rotate(2, 1);
            } catch (InterruptedException e) {
                fail();
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            try {
                cube.rotate(3, 2);
            } catch (InterruptedException e) {
                fail();
            }
        }, "t2");
        Thread t3 = new Thread(() -> {
            try {
                cube.rotate(3, 0);
            } catch (InterruptedException e) {
                fail();
            }
            try {
                result[0] = cube.show();
            } catch (InterruptedException e) {
                fail();
            }
        }, "t3");

        t1.start();
        t2.start();
        t3.start();

        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            fail();
        }

        String result1 = "202212202" + "111555111" + "525323525" + "333000333" + "040141040" + "454434454";
        String result2 = "205202112" + "114115115" + "553225225" + "002333333" + "140040040" + "334454454";
        String result3 = "202202511" + "554111111" + "322522555" + "033033233" + "041040040" + "433454454";
        String result4 = "202202111" + "114115114" + "555222555" + "233033233" + "040040040" + "333454454";
        String result5 = "205202112" + "114115115" + "553225225" + "002333333" + "140040040" + "334454454";

        assertTrue(cube.cubeToString().equals(result1)
                || cube.cubeToString().equals(result2)
                || cube.cubeToString().equals(result3)
                || cube.cubeToString().equals(result4)
                || cube.cubeToString().equals(result5));
        assertTrue(areCorrectColors(result[0], size));
    }

    @Test
    @DisplayName("Small concurrent test 2 - 3x3x3")
    public void smallConcurrentTest2() {
        final String[] result = {""};
        Cube cube = onlySizeCube(3);

        Thread t1 = new Thread(() -> {
            try {
                cube.rotate(2, 1);
            } catch (InterruptedException e) {
                fail();
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            try {
                cube.rotate(4, 1);
            } catch (InterruptedException e) {
                fail();
            }
            try {
                cube.rotate(3, 2);
            } catch (InterruptedException e) {
                fail();
            }
        }, "t2");
        Thread t3 = new Thread(() -> {
            try {
                cube.rotate(3, 0);
            } catch (InterruptedException e) {
                fail();
            }
            try {
                result[0] = cube.show();
            } catch (InterruptedException e) {
                fail();
            }
        }, "t3");

        t1.start();
        t2.start();
        t3.start();

        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            fail();
        }

        assertTrue(areCorrectColors(cube.cubeToString(), 3));
    }

    @Test
    @DisplayName("2 vs 1 concurrent test - 7x7x7")
    public void twoVsOneConcurrentTest() {
        int size = 7;
        int starvingSide = 1;
        AtomicInteger notStarved = new AtomicInteger(0);
        Cube cube = new Cube(size,
                (x, y) -> {if (x == starvingSide) notStarved.incrementAndGet();},
                (x, y) -> {if (x == starvingSide) notStarved.incrementAndGet();},
                () -> {},
                () -> {});

        Thread t1 = new Thread(() -> {
            try {
                int i = 10000;
                while (i --> 0) {cube.rotate(2, 0);}
            } catch (InterruptedException e) {
                fail();
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            try {
                int i = 10000;
                while (i --> 0) {cube.rotate(2, 2);}
            } catch (InterruptedException e) {
                fail();
            }
        }, "t2");
        Thread t3 = new Thread(() -> {
            try {
                cube.rotate(1, 1);
            } catch (InterruptedException e) {
                fail();
            }
        }, "t3");

        t1.start();
        t3.start();
        t2.start();

        try {
            t1.join();
            t3.join();
            t2.join();
        } catch (InterruptedException e) {
            fail();
        }

        assertEquals(notStarved.get(), 2);
    }

    @Test
    @DisplayName("only one group and layer at a time - 10x10x10")
    public void onlyOneAtATime() {
        int size = 10;
        int numberOfThreads = 10000;
        AtomicReferenceArray<Integer> inRotationLayers = new AtomicReferenceArray<>(new Integer[size]);
        AtomicReferenceArray<Integer> inRotationGroups = new AtomicReferenceArray<>(new Integer[4]);

        for (int i = 0; i < size; i++) {
            inRotationLayers.set(i, 0);
        }
        for (int i = 0; i < 4; i++) {
            inRotationGroups.set(i, 0);
        }

        Cube cube = new Cube(size,
                (x, y) -> {
                    inRotationGroups.getAndUpdate(getSideGroup(x), i -> i + 1);
                    inRotationLayers.getAndUpdate(getLayerGroup(x, y, size), i -> i + 1);

                    // only one thread per layer
                    for (int i = 0; i < size; i++) {
                        if (inRotationLayers.get(i) > 1)
                            fail();
                    }

                    // only one group in rotation
                    for (int i = 0; i < 4; i++) {
                        if (i != getSideGroup(x) && inRotationGroups.get(i) > 0)
                            fail();
                    }
                },
                (x, y) -> {
                    inRotationGroups.getAndUpdate(getSideGroup(x), i -> i - 1);
                    inRotationLayers.getAndUpdate(getLayerGroup(x, y, size), i -> i - 1);

                    // only one thread per layer
                    for (int i = 0; i < size; i++) {
                        if (inRotationLayers.get(i) > 1)
                            fail();
                    }

                    // only one group in rotation
                    for (int i = 0; i < 4; i++) {
                        if (i != getSideGroup(x) && inRotationGroups.get(i) > 0)
                            fail();
                    }
                },
                () -> {inRotationGroups.getAndUpdate(3, i -> i + 1);},
                () -> {inRotationGroups.getAndUpdate(3, i -> i - 1);});

        ArrayList<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            int side = random(0, 5);
            int layer = random(0, size - 1);
            threads.add(new Thread(() -> {
                try {
                    cube.rotate(side, layer);
                    cube.show();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));
        }

        for (int i = 0; i < numberOfThreads; i++) {
            threads.get(i).start();
        }

        for (int i = 0; i < numberOfThreads; i++) {
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                fail();
            }
        }

        assertTrue(areCorrectColors(cube.cubeToString(), size));
    }

    private void threadsTest(int numberOfTests, int maxSize, int maxThreads) {
        boolean passed = true;

        for (int t = 0; t < numberOfTests; t++) {
            int size = random(1, maxSize);
            int numberOfThreads = random(2, maxThreads);

            Cube cube = onlySizeCube(size);

            ArrayList<Thread> threads = new ArrayList<>();
            for (int i = 0; i < numberOfThreads; i++) {
                int side = random(0, 5);
                int layer = random(0, size - 1);
                threads.add(new Thread(() -> {
                    try {
                        cube.rotate(side, layer);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }));
            }

            for (int i = 0; i < numberOfThreads; i++) {
                threads.get(i).start();
            }

            for (int i = 0; i < numberOfThreads; i++) {
                try {
                    threads.get(i).join();
                } catch (InterruptedException e) {
                    fail();
                }
            }

            passed &= (areCorrectColors(cube.cubeToString(), size));
        }

        assertTrue(passed);
    }

    @Test
    @DisplayName("concurrent random test 1 - nxnxn")
    public void concurrentTest1() {
        int numberOfTests = 6;
        int maxSize = 10;
        int maxThreads = 10;

        threadsTest(numberOfTests, maxSize, maxThreads);
    }

    @Test
    @DisplayName("concurrent random test 2 - nxnxn")
    public void concurrentTest2() {
        int numberOfTests = 100;
        int maxSize = 10;
        int maxThreads = 100;

        threadsTest(numberOfTests, maxSize, maxThreads);
    }

    @Test
    @DisplayName("concurrent random big test 3 - nxnxn")
    public void concurrentTest3() {
        int numberOfTests = 100;
        int maxSize = 1000;
        int maxThreads = 1000;

        threadsTest(numberOfTests, maxSize, maxThreads);
    }

    @Test
    @DisplayName("Small interrupt test 1 - 3x3x3")
    public void smallInterruptTest1() {
        final String[] result = {""};
        var counter = new Object() { int value = 0; };

        Cube cube = new Cube(3,
                (x, y) -> { ++counter.value; System.out.println("before " + x + ", " + y); },
                (x, y) -> { ++counter.value; System.out.println("after " + x + ", " + y); },
                () -> { ++counter.value; },
                () -> { ++counter.value; }
        );

        Thread t1 = new Thread(() -> {
            try {
                cube.rotate(2, 1);
            } catch (InterruptedException e) {
                fail();
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            try {
                cube.rotate(4, 1);
            } catch (InterruptedException e) {
                fail();
            }
            try {
                cube.rotate(3, 2);
            } catch (InterruptedException e) {
                fail();
            }
        }, "t2");
        Thread t3 = new Thread(() -> {
            try {
                cube.rotate(2, 1);
            } catch (InterruptedException e) {

            }
        }, "t3");
        Thread t4 = new Thread(() -> {
            try {
                cube.rotate(3, 2);
            } catch (InterruptedException e) {
                fail();
            }
        }, "t4");
        Thread t5 = new Thread(() -> {
            try {
                cube.rotate(3, 0);
            } catch (InterruptedException e) {
                fail();
            }
            try {
                result[0] = cube.show();
            } catch (InterruptedException e) {
                fail();
            }
        }, "t5");

        t1.start();
        t4.start();
        t2.start();
        t3.start();
        t5.start();

        t3.interrupt();

        try {
            t1.join();
            t2.join();
            t3.join();
            t4.join();
            t5.join();
        }
        catch (InterruptedException e) {
            fail();
        }

        assertTrue(areCorrectColors(result[0], 3));
    }

    private void threadsInterruptTest(int numberOfTests, int maxSize, int maxThreads) {
        boolean passed = true;

        for (int t = 0; t < numberOfTests; t++) {
            int size = random(1, maxSize);
            int numberOfThreads = random(2, maxThreads);

            Cube cube = onlySizeCube(size);

            ArrayList<Thread> threads = new ArrayList<>();
            for (int i = 0; i < numberOfThreads; i++) {
                int side = random(0, 5);
                int layer = random(0, size - 1);

                threads.add(new Thread(() -> {
                    try {
                        cube.rotate(side, layer);
                    } catch (InterruptedException e) {

                    }
                }));
            }

            for (int i = 0; i < numberOfThreads; i++) {
                threads.get(i).start();
            }

            for (int i = 0; i < numberOfThreads; i++) {
                if (random(0, 1) == 1 && random(0, 1) == 1 && random(0, 1) == 1 && random(0, 1) == 1) {
                    threads.get(i).interrupt();
                }
            }

            for (int i = 0; i < numberOfThreads; i++) {
                try {
                    threads.get(i).join();
                } catch (InterruptedException e) {
                    fail();
                }
            }

            passed &= (areCorrectColors(cube.cubeToString(), size));
        }

        assertTrue(passed);
    }

    @Test
    @DisplayName("concurrent interrupt random test 1 - nxnxn")
    public void concurrentInterruptTest1() {
        int numberOfTests = 6;
        int maxSize = 10;
        int maxThreads = 10;

        threadsInterruptTest(numberOfTests, maxSize, maxThreads);
    }

    @Test
    @DisplayName("concurrent interrupt random test 2 - nxnxn")
    public void concurrentInterruptTest2() {
        int numberOfTests = 100;
        int maxSize = 100;
        int maxThreads = 100;

        threadsInterruptTest(numberOfTests, maxSize, maxThreads);
    }

    private void threadsAllTest(int numberOfTests, int maxSize, int maxThreads) {
        boolean passed = true;

        for (int t = 0; t < numberOfTests; t++) {
            int size = random(1, maxSize);
            int numberOfThreads = random(2, maxThreads);

            Cube cube = testCubeInit(size);

            ArrayList<Thread> threads = new ArrayList<>();
            for (int i = 0; i < numberOfThreads; i++) {
                int side = random(0, 5);
                int layer = random(0, size - 1);

                if (random(0, 4) == 1) {
                    threads.add(new Thread(() -> {
                        try {
                            cube.show();
                        } catch (InterruptedException e) {

                        }
                    }));
                }
                else {
                    threads.add(new Thread(() -> {
                        try {
                            cube.rotate(side, layer);
                        } catch (InterruptedException e) {

                        }
                    }));
                }
            }

            for (int i = 0; i < numberOfThreads; i++) {
                threads.get(i).start();
            }

            for (int i = 0; i < numberOfThreads; i++) {
                if (random(0, 1) == 1 && random(0, 1) == 1 && random(0, 1) == 1 && random(0, 1) == 1) {
                    threads.get(i).interrupt();
                }
            }

            for (int i = 0; i < numberOfThreads; i++) {
                try {
                    threads.get(i).join();
                } catch (InterruptedException e) {
                    fail();
                }
            }

            passed &= (areCorrectColors(cube.cubeToString(), size));
        }

        assertTrue(passed);
    }

    @Test
    @DisplayName("concurrent all random test 1 - nxnxn")
    public void concurrentAllTest1() {
        int numberOfTests = 100;
        int maxSize = 10;
        int maxThreads = 1000;

        threadsAllTest(numberOfTests, maxSize, maxThreads);
    }

}
