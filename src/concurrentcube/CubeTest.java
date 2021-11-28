package concurrentcube;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

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

    /*
    @Test
    @DisplayName("Sequential simple test 1 - 3x3x3")
    public void SequentialSimpleTest1() {
        Cube cube1 = onlySizeCube(3);
        cube1.sequentialRotate(0, 2);
        cube1.sequentialRotate(5, 0);
        cube1.sequentialRotate(1, 1);
        cube1.sequentialRotate(4, 1);
        cube1.printCube();

        Cube cube2 = onlySizeCube(4);
        cube2.sequentialRotate(2, 0);
        cube2.sequentialRotate(5, 1);
        cube2.printCube();

        Cube cube3 = onlySizeCube(4);
        try {
            cube3.rotate(2, 0);
            cube3.rotate(5, 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cube3.printCube();

        Cube cube = onlySizeCube(3);
        cube.sequentialRotate(2, 0);
//        cube.printCube();
        cube.sequentialRotate(3, 0);
//        cube.printCube();
        cube.sequentialRotate(4, 0);
//        cube.printCube();
        cube.sequentialRotate(1, 0);
//        cube.printCube();
        cube.sequentialRotate(0, 0);
//        cube.printCube();
        cube.sequentialRotate(5, 0);
        cube.printCube();

        assertTrue(true);
    }
    */

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
    @DisplayName("Small concurrent test 1 - 3x3x3")
    public void smallConcurrentTest1() {
        final String[] result = {""};
        Cube cube = onlySizeCube(3);

        Thread t1 = new Thread(() -> {
            try {
                cube.rotate(2, 1); //1, 4, 3, 4, 1,
            } catch (InterruptedException e) {
                fail();
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            try {
                cube.rotate(4, 1);  //2, 1, 2, 2, 3,
            } catch (InterruptedException e) {
                fail();
            }
            try {
                cube.rotate(3, 2); //3, 2, 4, 3, 4,
            } catch (InterruptedException e) {
                fail();
            }
        }, "t2");
        Thread t3 = new Thread(() -> {
            try {
                cube.rotate(3, 0); //4, 3, 1, 1, 2,
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

        String result1 = "202202202" + "111111111" + "525525525" + "333333333" + "040040040" + "454454454";
        String result2 = "202101202" + "141010141" + "525121525" + "323535323" + "040343040" + "454353454";
        String result3 = "202455202" + "131230121" + "525125525" + "313313343" + "040043040" + "454101454";
        String result4 = "202203202" + "111211111" + "525523525" + "343030333" + "040143040" + "454455454";

        assertTrue(cube.cubeToString().equals(result1)
                || cube.cubeToString().equals(result2)
                || cube.cubeToString().equals(result3)
                || cube.cubeToString().equals(result4));
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
    @DisplayName("concurrent test 1 - 10x10x10")
    public void concurrentTest1() {
        int numberOfTests = 6;
        int maxSize = 10;
        int maxThreads = 10;

        threadsTest(numberOfTests, maxSize, maxThreads);
    }

    @Test
    @DisplayName("concurrent test 2 - nxnxn")
    public void concurrentTest2() {
        int numberOfTests = 100;
        int maxSize = 10;
        int maxThreads = 100;

        threadsTest(numberOfTests, maxSize, maxThreads);
    }

    @Test
    @DisplayName("Nazwa testu")
    public void testCube() {
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
                e.printStackTrace();
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            try {
                cube.rotate(4, 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                cube.rotate(3, 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t2");
        Thread t3 = new Thread(() -> {
            try {
                cube.rotate(2, 1);
            } catch (InterruptedException e) {
//                e.printStackTrace();
            }
        }, "t3");
        Thread t4 = new Thread(() -> {
            try {
                cube.rotate(3, 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t4");
        Thread t5 = new Thread(() -> {
            try {
                cube.rotate(3, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                result[0] = cube.show();
                System.out.println(result[0]);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, "t5");

        t1.start();
        t4.start();
        t2.start();
        t3.start();
        t5.start();

        t3.interrupt();
//        t6.start();


        try {
            t1.join();
            t2.join();
            t3.join();
            t4.join();
            t5.join();
//            cube.printCube();
//            cube.printNumberedCube();
            // assertEquals("502312502151151151425423425333000333042142042054134054", result[0]);
        }
        catch (InterruptedException e) {
//            error(4);
        }
    }


}
