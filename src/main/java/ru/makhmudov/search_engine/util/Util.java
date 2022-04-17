package ru.makhmudov.search_engine.util;


import java.io.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Util {
    private static final String INVALID_URL_ENDINGS = "src/main/resources/invalid_url_endings.txt";

    public static String loadInvalidUrlEndings() {
        BufferedReader br = null;
        String s = "";
        try {
            br = new BufferedReader(new FileReader(INVALID_URL_ENDINGS));
            s = br.readLine();
            br.close();
        } catch (FileNotFoundException e) {
            createNewFile(INVALID_URL_ENDINGS);
        } catch (IOException exception) {
            System.err.println(exception.getLocalizedMessage());
        }
        return s == null ? "" : s;
    }

    public static void main(String[] args) {
        System.out.println(loadInvalidUrlEndings());
    }

    public static File createNewFile(String path) {
        File file = null;
        try {
            file = new File(INVALID_URL_ENDINGS);
            file.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return file;
    }

    public static void saveInvalidUrlEndings(String endings) {
        endings = Arrays.stream(endings.split(";")).filter(e -> !e.equals("")).distinct().collect(Collectors.joining(";"));
        if (!endings.equals("")) endings += ";";
        try {
            FileWriter w = new FileWriter(INVALID_URL_ENDINGS);
            w.write(endings);
            w.flush();
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

