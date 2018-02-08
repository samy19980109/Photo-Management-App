package controllers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utility {

    /**
     * This method is used to read file into a String
     * @param filePath
     * @return
     */
    public static List<String> loadExistingTags(String filePath) {
        BufferedReader br = null;
        List<String> tagsList = new ArrayList<>();
        try {
            final String dir = System.getProperty("user.dir");
            System.out.println("current dir = " + dir);
            br = new BufferedReader(new FileReader(filePath));
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                tagsList.add(sCurrentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return tagsList;
    }
}
