package concurrentcube;

import sequentialcube.SequentialCube;
import tools.ColorPrinter;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

public class Cube {
    private final int size;
//    volatile private int[][][] cube;
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

//    void inintCube(int n) {
//        cube = new int[6][n][n];
//
//        for (int i = 0; i < 6; i++) {
//            for (int x = 0; x < n; x++) {
//                for (int y = 0; y < n; y++) {
//                    cube[i][x][y] = i;
//                }
//            }
//        }
//    }

    void initSemaphores() {
//        pozostali[0] = new Semaphore(0, true);
//        pozostali[1] = new Semaphore(0, true);
//        pozostali[2] = new Semaphore(0, true);
//        pozostali[3] = new Semaphore(0, true);

        layers = new Semaphore[size];

        for (int i = 0; i < size; i++) {
            layers[i] = new Semaphore(1, true);
        }
    }

    void initArrays() {
//        ileZGrupy[0] = 0;
//        ileZGrupy[1] = 0;
//        ileZGrupy[2] = 0;
//        ileZGrupy[3] = 0;

//        layersWaitingToTurn = new int[size];
//        isLayerRotating = new Boolean[size];
//
//        for (int i = 0 ; i < size; i++) {
//            layersWaitingToTurn[i] = 0;
//            isLayerRotating[i] = false;
//        }
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

//        inintCube(size);
        cube = new SequentialCube(size);
        this.size = size;

        initSemaphores();
        initArrays();
    }

//    public Cube(int size) {
//        inintCube(size);
//        this.size = size;
//
//        initSemaphores();
//        initArrays();
//    }
/*
    private void turnFace(int id) {
        for (int i = 0; i < size / 2; i++) {
            for (int j = i; j < size - i - 1; j++) {

                int temp = cube[id][i][j];
                cube[id][i][j] = cube[id][size - 1 - j][i];
                cube[id][size - 1 - j][i] = cube[id][size - 1 - i][size - 1 - j];
                cube[id][size - 1 - i][size - 1 - j] = cube[id][j][size - 1 - i];
                cube[id][j][size - 1 - i] = temp;
            }
        }
    }

    private int opositeFace(int id) {
        return switch (id) {
            case 0 -> 5;
            case 1 -> 3;
            case 2 -> 4;
            case 3 -> 1;
            case 4 -> 2;
            default -> 0;
        };
    }

    private int opositeLayer(int layer) {
        return size - layer - 1;
    }

    private int[] getRowY(int face, int y) {
        int[] row = new int[size];
        for (int i = 0; i < size; i++) {
            row[i] = cube[face][i][y];
        }

        return row;
    }

    private int[] getRowX(int face, int x) {
        int[] row = new int[size];
        for (int i = 0; i < size; i++) {
            row[i] = cube[face][x][i];
        }

        return row;
    }

    private int[] moveRowY(int face, int y, int[] row) {
        int[] r = getRowY(face, y);

        for (int i = 0; i < size; i++) {
            cube[face][i][y] = row[i];
        }

        return r;
    }

    private int[] moveRowX(int face, int x, int[] row) {
        int[] r = getRowX(face, x);

        for (int i = 0; i < size; i++) {
            cube[face][x][i] = row[i];
        }

        return r;
    }

    private int[] moveRowXReverseOrder(int face, int x, int[] row) {
        int[] r = getRowX(face, x);

        for (int i = 0; i < size; i++) {
            cube[face][x][i] = row[size - i - 1];
        }

        return r;
    }

    private void faceZeroTurn(int layer) {
        if (layer == 0)
            turnFace(0);
        if (layer == size - 1) {
            turnFace(opositeFace(0));
            turnFace(opositeFace(0));
            turnFace(opositeFace(0));
        }


        // turn edges around face
        int[] movingRow = getRowY(4, opositeLayer(layer));
        movingRow = moveRowY(3, opositeLayer(layer), movingRow);
        movingRow = moveRowY(2, opositeLayer(layer), movingRow);
        movingRow = moveRowY(1, opositeLayer(layer), movingRow);
        moveRowY(4, opositeLayer(layer), movingRow);
    }

    private void faceOneTurn(int layer) {
        if (layer == 0)
            turnFace(1);
        if (layer == size - 1) {
            turnFace(opositeFace(1));
            turnFace(opositeFace(1));
            turnFace(opositeFace(1));
        }

        // turn edges around face
        int[] movingRow = getRowX(0, layer);
        movingRow = moveRowX(2, layer, movingRow);
        movingRow = moveRowX(5, layer, movingRow);
        movingRow = moveRowXReverseOrder(4, opositeLayer(layer), movingRow);
        moveRowXReverseOrder(0, layer, movingRow);
    }

    private void faceTwoTurn(int layer) {
        if (layer == 0)
            turnFace(2);
        if (layer == size - 1) {
            turnFace(opositeFace(2));
            turnFace(opositeFace(2));
            turnFace(opositeFace(2));
        }

        // turn edges around face
        int[] movingRow = getRowY(0, layer);
        movingRow = moveRowXReverseOrder(3, layer, movingRow);
        movingRow = moveRowY(5, opositeLayer(layer), movingRow);
        movingRow = moveRowXReverseOrder(1, opositeLayer(layer), movingRow);
        moveRowY(0, layer, movingRow);
    }

    private void faceThreeTurn(int layer) {
        faceOneTurn(opositeLayer(layer));
        faceOneTurn(opositeLayer(layer));
        faceOneTurn(opositeLayer(layer));
    }

    private void faceFourTurn(int layer) {
        faceTwoTurn(opositeLayer(layer));
        faceTwoTurn(opositeLayer(layer));
        faceTwoTurn(opositeLayer(layer));
    }

    private void faceFiveTurn(int layer) {
        faceZeroTurn(opositeLayer(layer));
        faceZeroTurn(opositeLayer(layer));
        faceZeroTurn(opositeLayer(layer));
    }

    public void sequentialRotate(int side, int layer) {
        switch (side) {
            case 0 -> faceZeroTurn(layer);
            case 1 -> faceOneTurn(layer);
            case 2 -> faceTwoTurn(layer);
            case 3 -> faceThreeTurn(layer);
            case 4 -> faceFourTurn(layer);
            default -> faceFiveTurn(layer);
        }
    }
*/

    private int opositeFace(int id) {
        return switch (id) {
            case 0 -> 5;
            case 1 -> 3;
            case 2 -> 4;
            case 3 -> 1;
            case 4 -> 2;
            default -> 0;
        };
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
//        for (int i = 0; i < 6; i++) {
//            for (int y = size - 1; y >= 0 ; y--) {
//                for (int x = 0; x < size; x++) {
//                    ColorPrinter.squareColorPrint(cube[i][x][y], cube[i][x][y]);
//                }
//                System.out.printf("\n");
//            }
//            System.out.println("");
//        }
//        System.out.println("-----------------------------");
    }

    public void printNumberedCube() {
        cube.printNumberedCube();
//        for (int i = 0; i < 6; i++) {
//            for (int y = size - 1; y >= 0 ; y--) {
//                for (int x = 0; x < size; x++) {
//                    ColorPrinter.cubeColorPrint(cube[i][x][y], cube[i][x][y]);
//                }
//                System.out.printf("\n");
//            }
//            System.out.println("");
//        }
//        System.out.println("-----------------------------");
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

    // sequential cube to string
    public String cubeToString() {
        StringBuilder bob = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            for (int y = size - 1; y >= 0 ; y--) {
                for (int x = 0; x < size; x++) {
//                    bob.append(cube[i][x][y]);
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