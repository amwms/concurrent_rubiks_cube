package concurrentcube;

import sequentialcube.SequentialCube;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

public class Cube {
    private final int size;
    private volatile SequentialCube cube;


    Lock lock = new ReentrantLock(true);
    Condition toExit = lock.newCondition();
    Condition toEnter = lock.newCondition();

    private AtomicInteger amountToEnter = new AtomicInteger(0);
    private AtomicInteger amountToExit = new AtomicInteger(0);
    private AtomicInteger amountRotating = new AtomicInteger(0);
    private AtomicInteger whoIsRotating = new AtomicInteger(-1);

    private Semaphore[] layers;

    private BiConsumer<Integer, Integer> beforeRotation;
    private BiConsumer<Integer, Integer> afterRotation;
    private Runnable beforeShowing;
    private Runnable afterShowing;

    void initSemaphores() {
        layers = new Semaphore[size];

        for (int i = 0; i < size; i++) {
            layers[i] = new Semaphore(1, true);
        }
    }

    public Cube(int size,
                BiConsumer<Integer, Integer> beforeRotation,
                BiConsumer<Integer, Integer> afterRotation,
                Runnable beforeShowing,
                Runnable afterShowing) {
        this.beforeRotation = beforeRotation;
        this.beforeShowing = beforeShowing;
        this.afterRotation = afterRotation;
        this.afterShowing = afterShowing;

        cube = new SequentialCube(size);
        this.size = size;

        initSemaphores();
    }

    private int opositeFace(int id) {
        switch (id) {
            case 0 : return 5;
            case 1 : return  3;
            case 2 : return 4;
            case 3 : return  1;
            case 4 : return  2;
            default : return  0;
        }
    }

    private int opositeLayer(int layer) {
        return size - layer - 1;
    }

    private int getGroup(int side) {
        if (side < 3)
            return side;

        return (side + 1) % 3;
    }

    private void entryProtocol(int id) throws InterruptedException {
        // protokół wejścia
        try {
            lock.lock();

            while (whoIsRotating.get() != id && whoIsRotating.get() != -1) {
                toEnter.await();
            }

            amountRotating.incrementAndGet();
            whoIsRotating.set(id);
        }
        finally {
            lock.unlock();
        }
    }

    private void exitProtocol(int id) throws InterruptedException {
        // protokół wyjścia
        try {
            lock.lock();

            amountRotating.decrementAndGet();
            amountToExit.incrementAndGet();
            while (amountRotating.get() > 0) { // while nie if
                toExit.await();
            }

            whoIsRotating.set(-1000); // żeby przez przypadek nikt nie wszedł

            toExit.signalAll();
        }
        finally {
            amountToExit.decrementAndGet();

            if (amountToExit.get() == 0 && amountRotating.get() == 0) {
                whoIsRotating.set(-1);
                toEnter.signalAll();
            }

            lock.unlock();
        }
    }

    public void rotate(int side, int layer) throws InterruptedException {
        int id = getGroup(side);
        AtomicBoolean flag = new AtomicBoolean(false);

        try {
            entryProtocol(id);
        }
        catch (InterruptedException e) {
            flag.set(true);
            throw e;
        }

        // System.out.println("bede krecil: " + side + " " + layer + " " + Thread.currentThread().getName() + " flag: " + flag.get());

        if (!flag.get()) {
            rotation(side, layer, flag);
        }

        // System.out.println("juz nie: " + side + " " + layer + " " + Thread.currentThread().getName());

        exitProtocol(id);
    }

    private void rotation(int side, int layer, AtomicBoolean flag) throws InterruptedException {
        int layerId = side > 2 ? opositeLayer(layer) : layer; // jeśli side = 3, 4, 5 to liczymy je jak scianki 0,1,2

        try {
            layers[layerId].acquire();
        }
        catch (InterruptedException e) {
            lock.lock();
            flag.set(true);

            if (amountRotating.decrementAndGet() == 0 && amountToExit.get() == 0) {
                whoIsRotating.set(-1);
                toEnter.signalAll();
            }
            else if (amountRotating.get() == 0 && amountToExit.get() > 0) {
                whoIsRotating.set(-1000);
                toExit.signalAll();
            }

            lock.unlock();

            throw e;
        }

        // System.out.println("bede krecil ale tak serio: " + side + " " + layer + " " + Thread.currentThread().getName() + " flag: " + flag.get());

        if (!flag.get()) {
            beforeRotation.accept(side, layer);
//            sequentialRotate(side, layer);
            cube.sequentialRotate(side, layer);
            afterRotation.accept(side, layer);

            layers[layerId].release();
        }
    }

    public void printCube() {
        cube.printCube();
    }

    public void printNumberedCube() {
        cube.printNumberedCube();
    }

    public String show() throws InterruptedException {
        entryProtocol(3);

        beforeShowing.run();

        StringBuilder bob = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            for (int y = size - 1; y >= 0 ; y--) {
                for (int x = 0; x < size; x++) {
//                    bob.append(cube[i][x][y]);
                    bob.append(cube.getCube(i, x, y));
                }
            }
        }

        afterShowing.run();

        exitProtocol(3);

        return bob.toString();
    }

    public String cubeToString() {
        StringBuilder bob = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            for (int y = size - 1; y >= 0 ; y--) {
                for (int x = 0; x < size; x++) {
                    bob.append(cube.getCube(i, x, y));
                }
            }
        }

        return bob.toString();
    }

    public static void main(String[] args) {

//        Cube cube = new Cube(3);
//        cube.sequentialRotate(0, 2);
//        cube.sequentialRotate(5, 0);
//        cube.sequentialRotate(1, 1);
//        cube.sequentialRotate(4, 1);
//        Cube cube = new Cube(4);
//        cube.sequentialRotate(2, 0);
//        cube.sequentialRotate(5, 1);
//        cube.printCube();
//
//        Cube cube2 = new Cube(4);
//        cube2.rotate(2, 0);
//        cube2.rotate(5, 1);
//        Cube cube = new Cube(3);
//
//        cube.sequentialRotate(2, 0);
//        cube.printCube();
//        cube.sequentialRotate(3, 0);
//        cube.printCube();
//        cube.sequentialRotate(4, 0);
//        cube.printCube();
//        cube.sequentialRotate(1, 0);
//        cube.printCube();
//        cube.sequentialRotate(0, 0);
//        cube.printCube();
//        cube.sequentialRotate(5, 0);

//        cube.printCube();

    }
}