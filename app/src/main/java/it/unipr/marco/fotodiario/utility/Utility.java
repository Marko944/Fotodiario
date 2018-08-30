package it.unipr.marco.fotodiario.utility;

import android.content.res.Configuration;
import android.view.Display;

import java.util.Arrays;

public class Utility {
    /**
     * =====================================PARTE GENERALE==========================================
     */

    public static String utility = "";

    //Caratteri illegali nei testi che devono essere usati nei path
    private static int[] illegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
            11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
            31, 58, 42, 63, 92, 47};
    static {
        Arrays.sort(illegalChars);
    }

    @SuppressWarnings("deprecation")
    public static int getScreenOrientation(Display display) {
        int orientation;
        if(display.getWidth()==display.getHeight()){
            orientation = Configuration.ORIENTATION_SQUARE;
        } else{
            if(display.getWidth() < display.getHeight()){
                orientation = Configuration.ORIENTATION_PORTRAIT;
            }else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    /**
     * Rimuove i caratteri illegali (per windows ma penso vada bene anche qua) da una stringa
     * */
    public static String cleanFileName(String badFileName) {
        StringBuilder cleanName = new StringBuilder();
        int len = badFileName.codePointCount(0, badFileName.length());
        for (int i=0; i<len; i++) {
            int c = badFileName.codePointAt(i);
            if (Arrays.binarySearch(illegalChars, c) < 0) {
                cleanName.appendCodePoint(c);
            }
        }
        return cleanName.toString();
    }
}
