package org.ivan2m;

import java.io.File;
import java.io.FileFilter;

public class TextFileFilter{
    public static boolean accept(File pathname) {
        return pathname.getName().toLowerCase().endsWith(".json");
    }

    /**
     * Para tratar el texto antes de indexarlo o ponerlo en una consulta
     * @param original
     * @return
     */
    public static String treatText(String original){
        String newText = original.replaceAll(",", "");
        newText = newText.replaceAll("\\.", "");
        newText = newText.replaceAll("¿", "");
        newText = newText.replaceAll("\\?", "");
        newText = newText.replaceAll(";", "");
        newText = newText.replaceAll(":", "");
        newText = newText.replaceAll("\\(", "");
        newText = newText.replaceAll("\\)", "");

        return newText;
    }
}
