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
        String color = switch (colorId) {
            case 0 -> WHITE;
            case 1 -> RED;
            case 2 -> BLUE;
            case 3 -> ORANGE;
            case 4 -> GREEN;
            default -> YELLOW;
        };

        System.out.printf(color + "%d " + RESET, number);
    }

    public static void squareColorPrint(int number, int colorId) {
        String color = switch (colorId) {
            case 0 -> BACKGROUND_WHITE + WHITE;
            case 1 -> BACKGROUND_RED + RED;
            case 2 -> BACKGROUND_BLUE + BLUE;
            case 3 -> BACKGROUND_ORANGE + ORANGE;
            case 4 -> BACKGROUND_GREEN + "\u001b[38;5;34m";
            default -> BACKGROUND_YELLOW + YELLOW;
        };

        System.out.printf(color + "%d " + RESET, number);
    }

}
