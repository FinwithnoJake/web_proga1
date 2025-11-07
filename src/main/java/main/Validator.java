package main;

public class Validator {
    //radio
    public static boolean validateX(int x) {
        return x >= -4 && x <= 4;
    }

    //text
    public static boolean validateY(float y) {
        return y >= -3 && y <= 5;
    }

    //button
    public static boolean validateR(float r) {

        return r == 1 || r == 1.5 || r == 2 || r == 2.5 || r == 3;
    }
}
