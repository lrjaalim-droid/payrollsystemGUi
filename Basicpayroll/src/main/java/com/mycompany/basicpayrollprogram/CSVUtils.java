package com.mycompany.basicpayrollprogram;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CSVUtils {

    public static void showCSVContent(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            System.out.println("Laman ng CSV file:");
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error sa pagbasa ng file: " + e.getMessage());
        }
    }
}