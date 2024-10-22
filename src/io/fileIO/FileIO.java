package io.fileIO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import io.consoleIO.ConsoleLogger;

public abstract class FileIO {
    public static String[] read(String path) {
        ArrayList<String> content = new ArrayList<>();

        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                content.add(line);
            }

            bufferedReader.close();

        } catch (IOException e) {
            ConsoleLogger.logError("An error occurred. (reading)", e);
            return null;
        }
        if ((content.isEmpty())) {
            ConsoleLogger.logWhite("The file is empty.");
            return new String[0];
        }

        return content.stream().toArray(String[]::new);
    }

    public static void write(String path, String content) {
        try {
            FileWriter fileWriter = new FileWriter(path);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(content);
            bufferedWriter.close();

            ConsoleLogger.logGreen(String.format("Success! The new file is in %s", path));

        } catch (IOException e) {
            ConsoleLogger.logError("An error occurred. (writing)", e);
        }
    }
}