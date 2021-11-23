package concurrentcube;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

public class Cube {
    private final int size;
    volatile private int[][][] cube;

    private Semaphore ochrona = new Semaphore(1);
    private Semaphore reprezentanci = new Semaphore(0); // jesli nie mozemy wejsc, to sie na nim ustawiamy
    private Semaphore[] pozostali = new Semaphore[4]; //binary_semaphore pozostali[N] = {0, ..., 0};
    private Semaphore doWyjscia = new Semaphore(0);
    private volatile int ileDoWyjscia = 0;
    private volatile int ileReprezentantow = 0;
    private volatile int[] ileZGrupy = new int[4]; //{0, ..., 0};
    private volatile int ktoOblicza = -1;
    private volatile int ileOblicza = 0;

    private Semaphore[] layers;
    private volatile int[] layersWaitingToTurn;
    private volatile Boolean[] isLayerRotating;

    private BiConsumer<Integer, Integer> beforeRotation;
    private BiConsumer<Integer, Integer> afterRotation;
    private Runnable beforeShowing;
    private Runnable afterShowing;

    void inintCube(int n) {
        cube = new int[6][n][n];

        for (int i = 0; i < 6; i++) {
            for (int x = 0; x < n; x++) {
                for (int y = 0; y < n; y++) {
                    cube[i][x][y] = i;
                }
            }
        }
    }

    void initSemaphores() {
        pozostali[0] = new Semaphore(0);
        pozostali[1] = new Semaphore(0);
        pozostali[2] = new Semaphore(0);
        pozostali[3] = new Semaphore(0);

        layers = new Semaphore[size];

        for (int i = 0; i < size; i++) {
            layers[i] = new Semaphore(0);
        }
    }

    void initArrays() {
        ileZGrupy[0] = 0;
        ileZGrupy[1] = 0;
        ileZGrupy[2] = 0;
        ileZGrupy[3] = 0;

        layersWaitingToTurn = new int[size];
        isLayerRotating = new Boolean[size];

        for (int i = 0 ; i < size; i++) {
            layersWaitingToTurn[i] = 0;
            isLayerRotating[i] = false;
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

        inintCube(size);
        this.size = size;

        initSemaphores();
        initArrays();
    }

    public Cube(int size) {
        inintCube(size);
        this.size = size;

        initSemaphores();
        initArrays();
    }

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
        switch (id) {
            case 0: return 5;
            case 1: return 3;
            case 2: return 4;
            case 3: return 1;
            case 4: return 2;
            default: return 0;
        }
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
            case 0:
                faceZeroTurn(layer);
                break;
            case 1:
                faceOneTurn(layer);
                break;
            case 2:
                faceTwoTurn(layer);
                break;
            case 3:
                faceThreeTurn(layer);
                break;
            case 4:
                faceFourTurn(layer);
                break;
            default:
                faceFiveTurn(layer);
        }
    }

//    Semaphore ochrona = new Semaphore(1);
//    Semaphore reprezentanci = new Semaphore(0); // jesli nie mozemy wejsc, to sie na nim ustawiamy
//    Semaphore[] pozostali = new Semaphore[3]; //binary_semaphore pozostali[N] = {0, ..., 0};
//    Semaphore doWyjscia = new Semaphore(0);
//    int ileDoWyjscia = 0;
//    int ileReprezentantow = 0;
//    int[] ileZGrupy = new int[3]; //{0, ..., 0};
//    int ktoOblicza = -1;
//    int ileOblicza = 0;

    private int getGroup(int side) {
        if (side < 3)
            return side;

        return (side + 1) % 3;
    }

    private void entryProtocol(int id) throws InterruptedException {
        // protokół wejścia
        ochrona.acquire(); // P(ochrona);
        if (ktoOblicza != id && ktoOblicza != -1) { // ktoOblicza != id <- to chyba powoduje że nawet jak jakiś wątek dojdzie później to jest odrazu wykonymwany jeślio jego grupa jest w miejscu obliczania
            ileZGrupy[id]++;
            if (ileZGrupy[id] == 1) {
                ileReprezentantow++;
                ochrona.release(); // V(ochrona);
                reprezentanci.acquire();  //P(reprezentanci); // dziedziczenie ochrony
                ileReprezentantow--;
                ktoOblicza = id;
            }
            else {
                ochrona.release(); //V(ochrona);
                pozostali[id].acquire(); //P(pozostali[id]); // dziedziczenie ochrony
            }

            ileZGrupy[id]--;
            ileOblicza++; // chyba tak xd

            if (ileZGrupy[id] > 0) {
                pozostali[id].release(); //V(pozostali[id]); // przekazanie ochrony
            }
            else {
                ochrona.release(); //V(ochrona); // jak jestesmy ostatni z grupy to musimy
                // oddac ochrone
            }
        }
        else {
            ktoOblicza = id;
            ileOblicza++;
            ochrona.release(); //V(ochrona);
        }
    }

    private void exitProtocol(int id) throws InterruptedException {
        // protokół wyjścia
        ochrona.acquire(); //P(ochrona);
        ileOblicza--;
        if (ileOblicza > 0) {
            ileDoWyjscia++;
            ochrona.release();  //V(ochrona);
            doWyjscia.acquire(); //P(doWyjscia); // dziedziczenie ochrony
            ileDoWyjscia--;
        }
        if (ileDoWyjscia > 0) {
            doWyjscia.release(); //V(doWyjscia);
        }
        else if (ileReprezentantow > 0) { //jesteśmy ostatnim wychodzącym jeśli ileDOWyjścia = 0
            reprezentanci.release(); //V(reprezentanci);
        }
        else {
            ktoOblicza = -1;
            ochrona.release(); //V(ochrona);
        }
    }

    public void rotate(int side, int layer) throws InterruptedException {
        int id = getGroup(side);
        entryProtocol(id);

        rotation(side, layer);

        exitProtocol(id);
    }

//    Semaphore[] layers = new Semaphore[size];
//    int[] layersWaitingToTurn = new int[size];
//    Boolean[] isLayerRotating = new Boolean[size];

    private void rotation(int side, int layer) throws InterruptedException {
        int layerId = side > 2 ? opositeLayer(layer) : layer; // jeśli side = 3, 4, 5 to liczymy je jak scianki 0,1,2

        ochrona.acquire();
        if (isLayerRotating[layerId]) {
            layersWaitingToTurn[layerId]++;
            ochrona.release();
            layers[layerId].acquire(); // diedzicczenie ochrony
            isLayerRotating[layerId] = true;
        }
        ochrona.release();

        beforeRotation.accept(size, layer);
        sequentialRotate(side, layer);
        afterRotation.accept(size, layer);

        ochrona.acquire();
        isLayerRotating[layerId] = false;

        if (layersWaitingToTurn[layerId] > 0) {
            layersWaitingToTurn[layerId]--;
            layers[layerId].release();
        }
        else {
            ochrona.release();
        }

    }

    public void printCube() {
        for (int i = 0; i < 6; i++) {
            for (int y = size - 1; y >= 0 ; y--) {
                for (int x = 0; x < size; x++) {
                    System.out.printf("%d", cube[i][x][y]);
                }
                System.out.printf("\n");
            }
            System.out.println("");
        }
        System.out.println("-----------------------------");
    }

    public String show() throws InterruptedException {
        entryProtocol(3);

        beforeShowing.run();

        StringBuilder bob = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            for (int y = size - 1; y >= 0 ; y--) {
                for (int x = 0; x < size; x++) {
                    bob.append(cube[i][x][y]);
                }
            }
        }

        afterShowing.run();

        exitProtocol(3);

        return bob.toString();
    }
    public static void main(String[] args) {
        try {
//        Cube cube = new Cube(3);
//        cube.sequentialRotate(0, 2);
//        cube.sequentialRotate(5, 0);
//        cube.sequentialRotate(1, 1);
//        cube.sequentialRotate(4, 1);
            Cube cube = new Cube(4);
            cube.sequentialRotate(2, 0);
            cube.sequentialRotate(5, 1);
            cube.printCube();

            Cube cube2 = new Cube(4);
            cube2.rotate(2, 0);
            cube2.rotate(5, 1);
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

            cube.printCube();
        }
        catch (InterruptedException e) {
            System.out.println("dksjfhskljhflsk");
        }
    }
}