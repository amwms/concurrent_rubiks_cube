package sequentialcube;

import tools.ColorPrinter;

import java.util.function.BiConsumer;

public class SequentialCube {
    private int size;
    private int[][][] cube;

    void initCube(int n) {
        cube = new int[6][n][n];

        for (int i = 0; i < 6; i++) {
            for (int x = 0; x < n; x++) {
                for (int y = 0; y < n; y++) {
                    cube[i][x][y] = i;
                }
            }
        }
    }

    public SequentialCube(int size,
                BiConsumer<Integer, Integer> beforeRotation,
                BiConsumer<Integer, Integer> afterRotation,
                Runnable beforeShowing,
                Runnable afterShowing) {

        initCube(size);
        this.size = size;
    }

    public SequentialCube(int size) {
        initCube(size);
        this.size = size;
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

    public void printCube() {
        for (int i = 0; i < 6; i++) {
            for (int y = size - 1; y >= 0 ; y--) {
                for (int x = 0; x < size; x++) {
//                    System.out.printf("%d", cube[i][x][y]);
                    ColorPrinter.squareColorPrint(cube[i][x][y], cube[i][x][y]);
                }
                System.out.printf("\n");
            }
            System.out.println("");
        }
        System.out.println("-----------------------------");
    }

    public void printNumberedCube() {
        for (int i = 0; i < 6; i++) {
            for (int y = size - 1; y >= 0 ; y--) {
                for (int x = 0; x < size; x++) {
//                    System.out.printf("%d", cube[i][x][y]);
                    ColorPrinter.cubeColorPrint(cube[i][x][y], cube[i][x][y]);
                }
                System.out.printf("\n");
            }
            System.out.println("");
        }
        System.out.println("-----------------------------");
    }

    public static void main(String[] args) {
//        Cube cube = new Cube(3);
//        cube.sequentialRotate(0, 2);
//        cube.sequentialRotate(5, 0);
//        cube.sequentialRotate(1, 1);
//        cube.sequentialRotate(4, 1);
        SequentialCube cube = new SequentialCube(4);
        cube.sequentialRotate(2, 0);
        cube.sequentialRotate(5, 1);

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
}
