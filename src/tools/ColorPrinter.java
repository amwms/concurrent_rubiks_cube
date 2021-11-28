package tools;

public class ColorPrinter {
    public static final String RESET = "\u001B[0m";

    public static final String BLACK =	"\u001B[30m";
    public static final String RED = "\u001b[31;1m"; //"\u001B[31m";
    public static final String GREEN = "\u001b[38;5;76m"; //"\u001B[32m";
    public static final String YELLOW = "\u001b[38;5;184m"; //"\u001b[38;5;226m";
    public static final String BLUE = "\u001b[38;5;33m";
    public static final String WHITE = "\u001b[38;5;255m";
    public static final String ORANGE = "\u001b[38;5;208m";

    public static final String BACKGROUND_BLACK = "\u001B[30m";
    public static final String BACKGROUND_RED = "\u001b[41;1m"; //"\u001B[31m";
    public static final String BACKGROUND_GREEN = "\u001b[48;5;34m"; //"\u001B[32m";
    public static final String BACKGROUND_YELLOW = "\u001b[48;5;184m"; //"\u001b[38;5;226m";
    public static final String BACKGROUND_BLUE = "\u001b[48;5;33m";
    public static final String BACKGROUND_WHITE = "\u001b[48;5;255m";
    public static final String BACKGROUND_ORANGE = "\u001b[48;5;208m";

    public ColorPrinter() {
    }

    public static void cubeColorPrint(int number, int colorId) {
        String color;

        switch (colorId) {
            case 0 : color = WHITE; break;
            case 1 : color = RED; break;
            case 2 : color = BLUE; break;
            case 3 : color = ORANGE; break;
            case 4 : color = GREEN; break;
            default : color = YELLOW; break;
        }

        System.out.printf(color + "%d " + RESET, number);
    }

    public static void squareColorPrint(int number, int colorId) {
        String color;

        switch (colorId) {
            case 0 : color = BACKGROUND_WHITE + WHITE; break;
            case 1 : color = BACKGROUND_RED + RED; break;
            case 2 : color = BACKGROUND_BLUE + BLUE; break;
            case 3 : color = BACKGROUND_ORANGE + ORANGE; break;
            case 4 : color = BACKGROUND_GREEN + "\u001b[38;5;34m"; break;
            default : color = BACKGROUND_YELLOW + YELLOW; break;
        }

        System.out.printf(color + "%d " + RESET, number);
    }

}
