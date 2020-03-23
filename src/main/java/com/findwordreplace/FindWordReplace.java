package com.findwordreplace;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/*
 * This program implements word dictionary with positions stored. It implements something similar to the
 * first half of the following link:
 *
 * http://www.ardendertat.com/2011/12/20/programming-interview-questions-23-find-word-positions-in-text/
 *
 * and takes it a step further by asking for a word to search and word to replace each instance within a
 * specified file and saves the resultant text to a new text file.
 *
 * The structure of the code flow is that a given text file is read incrementally per line and looks at
 * each word and stores its location/position within the line.
 *
 * The main data structure is a HashMap that has a key of word and value of another HashMap that has key
 * as line number and value as positions within that line.
 *
 * author: Frank Giordano 10/24/2015
 */
public class FindWordReplace {

    // string = word, HashMap<Integer, ArrayList<Integer>> = line #, list of position numbers within the line
    private static HashMap<String, HashMap<Integer, ArrayList<Integer>>> dictionary;
    private static String fileName;
    private static String outputFile;
    private static ArrayList<String> newFile;
    private static boolean verbose = false;

    private static void processFile() {

        int position = 0;
        int lineCount = 0;
        String line = "";

        // HashMap<Integer, ArrayList<Integer>> = line #, list of position numbers within the line
        HashMap<Integer, ArrayList<Integer>> wordLinePosInfo;
        dictionary = new HashMap<>();
        newFile = new ArrayList<>();
        newFile.add("");
        try {
            BufferedReader bReader = new BufferedReader(new FileReader(fileName));

            // while we loop through the file, read each line until there is nothing left to read.
            // this assumes we have carriage returns ending each text line.
            while ((line = bReader.readLine()) != null) {

                lineCount++;

                newFile.add(line);
                line = toLowerCase(line);
                String[] words = line.replaceAll("[^a-z0-9]", " ").split(" ");
                for (String word : words) {
                    if ("".equals(word))
                        continue;

                    int startIndex = 0;
                    while ((position = line.indexOf(word, startIndex)) != -1) {
                        startIndex = position + word.length();
                        // ignore finding the word text within another larger word
                        if (isWordEmbedded(position, word, line)) {
                            continue; // skip this position
                        }

                        // first time this word is seen
                        if (!dictionary.containsKey(word)) {
                            wordLinePosInfo = new HashMap<>();
                            addFirstLinePosition(position, lineCount, wordLinePosInfo);
                            dictionary.put(word, wordLinePosInfo);
                            break;
                        }

                        // first time adding a position for this word for this line
                        if (dictionary.containsKey(word) && dictionary.get(word).get(lineCount) == null) {
                            wordLinePosInfo = dictionary.get(word);
                            addFirstLinePosition(position, lineCount, wordLinePosInfo);
                            break;
                        }

                        // at this point, it is obvious add position to existing word/line storage
                        wordLinePosInfo = dictionary.get(word);
                        // retrieve all current positions of the word found so far for this line
                        ArrayList<Integer> currWordLinePositions = wordLinePosInfo.get(lineCount);
                        // get the last word position from the list
                        int lastKnownPos = currWordLinePositions.get(currWordLinePositions.size() - 1);
                        // increase last known position to the end of the word so it starts searching there
                        lastKnownPos = lastKnownPos + word.length();
                        // search for the next word occurrence from current read in line file
                        position = line.indexOf(word, lastKnownPos);
                        if (position == -1)
                            break;
                        if (!currWordLinePositions.contains(Integer.valueOf(position))) {
                            wordLinePosInfo.get(lineCount).add(position);
                            startIndex = position + word.length();
                        }
                    }
                }
            }

            bReader.close();
        } catch (IOException e) {
            System.out.print("Error reading file. Error message = " + e.getMessage());
            System.exit(-1);
        }
    }

    private static void addFirstLinePosition(int position, int lineCount, HashMap<Integer, ArrayList<Integer>> wordLinePosInfo) {
        ArrayList<Integer> positions = new ArrayList<>();
        positions.add(position);
        wordLinePosInfo.put(lineCount, positions);
    }

    private static boolean isWordEmbedded(int position, String word, String line) {
        if (position != 0 && position != line.length() - 1 && position + (word.length() - 1) != (line.length() - 1)) {
            boolean isAlphabeticRightSide = Character.isAlphabetic(line.charAt(position + (word.length())));
            boolean isAlphabeticLeftSide = Character.isAlphabetic(line.charAt(position - 1));
            if (isAlphabeticRightSide || isAlphabeticLeftSide) {
                return true;
            }
        }

        if (position == 0 && (position + (word.length()) < line.length())) {
            boolean isAlphabeticRightSide = Character.isAlphabetic(line.charAt(position + (word.length())));
            if (isAlphabeticRightSide)
                return true;
        }

        return false;
    }

    private static void resetNewFile() {
        String line = "";
        newFile = new ArrayList<>();
        newFile.add("");
        try {
            BufferedReader bReader = new BufferedReader(new FileReader(fileName));
            while ((line = bReader.readLine()) != null) {
                newFile.add(line);
            }
            bReader.close();
        } catch (IOException e) {
            System.out.print("Error reading file. Error message = " + e.getMessage());
            System.exit(-1);
        }
    }

    public static void wordReplace(String[] parameters, String file) {

        String searchWord = toLowerCase(parameters[0]);
        String replaceWord = parameters[1];

        fileName = file;
        if (dictionary == null)
            processFile();
        else
            resetNewFile();

        if (!dictionary.containsKey(searchWord)) {
            System.out.println("The following word " + searchWord + " was not found.");
        } else {
            for (Entry<Integer, ArrayList<Integer>> entry : dictionary.get(searchWord).entrySet()) {
                foundMsg(searchWord, entry);
                changingMsg(entry);

                String line = newFile.get(entry.getKey());
                StringBuilder tmp = new StringBuilder();
                int index = 0;
                for (int i = 0; i < entry.getValue().size(); i++) {
                    int position = entry.getValue().get(i);

                    // get the substring of the line text upto the save position location
                    // then append the replacement word to the end of the extracted substring
                    // and store and append it into a tmp StringBuilder...
                    tmp.append(line.substring(index, position) + replaceWord);
                    // set index to the next starting location to extract the next substring
                    // this index will be increased by previous "position" plus the length of the
                    // searchword so it starts searching for the next occurrence; the index will be
                    // beyond the previous search range... value searchWord length
                    index = position + searchWord.length();
                }

                tmp.append(line.substring(index));

                newFile.set(entry.getKey(), tmp.toString());
                changedMsg(entry);
            }
        }

        writeOutputFile();
    }

    private static void foundMsg(String searchWord, Entry<Integer, ArrayList<Integer>> entry) {
        if (!verbose)
            return;
        StringBuilder message = new StringBuilder();
        message.append("The following word ");
        message.append("\"");
        message.append(searchWord);
        message.append("\"");
        message.append(" was found at line number ");
        message.append(entry.getKey());
        message.append(" at position(s): ");
        message.append((entry.getValue().toString()));
        System.out.println(message);
    }

    private static void changingMsg(Entry<Integer, ArrayList<Integer>> entry) {
        if (!verbose)
            return;
        StringBuilder message = new StringBuilder();
        message.append("Changing Line number = ");
        message.append(entry.getKey());
        message.append(", with string = ");
        message.append("\"");
        message.append(newFile.get(entry.getKey()));
        message.append("\"");
        System.out.println(message);
    }

    private static void changedMsg(Entry<Integer, ArrayList<Integer>> entry) {
        if (!verbose)
            return;
        StringBuilder message = new StringBuilder();
        message.append("Changed Line number = ");
        message.append(entry.getKey());
        message.append(", with string = ");
        message.append("\"");
        message.append(newFile.get(entry.getKey()));
        message.append("\"");
        System.out.println(message);
    }

    private static void writeOutputFile() {
        BufferedWriter writer = null;
        outputFile = fileName + ".out";
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            for (String line : newFile) {
                if (line != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            writer.close();
        } catch (IOException e) {
            System.out.print("Error writing to output file. Error message = " + e.getMessage());
            System.exit(-1);
        }
    }

    private static String toLowerCase(String input) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            Character charAt = input.charAt(i);
            if (Character.isAlphabetic(charAt)) {
                result.append(Character.toLowerCase(charAt));
            } else {
                result.append(charAt);
            }
        }
        return result.toString();
    }

    private static void setVerbosity(String args[]) {
        if (args.length == 0)
            return;

        if ("-verbose".equals(args[0]))
            verbose = true;
    }

    private static String getInputFile() {
        byte[] input;
        input = new byte[80];

        System.out.println("Enter filename to read");
        System.out.print("> ");
        try {
            System.in.read(input);
            fileName = (new String(input, 0, input.length)).trim();
            if (fileName.isEmpty()) {
                System.exit(0);
            }
        } catch (IOException e) {
            System.out.print("Error reading given input. Error message = " + e.getMessage());
            System.exit(-1);
        }
        return fileName;
    }

    public static void main(String args[]) {

        String words = null;
        byte[] input;

        setVerbosity(args);
        String fileName = getInputFile();

        do {
            input = new byte[80];
            System.out.println("Enter two words separated by a space for word search and replacement");
            System.out.print("> ");
            try {
                System.in.read(input);
            } catch (IOException e) {
                System.out.print("Error reading given input. Error message = " + e.getMessage());
                System.exit(-1);
            }
            words = (new String(input, 0, input.length)).trim();
            if (words.length() > 0) {
                String[] parameters = words.split(" ");
                if (parameters.length < 2 || parameters.length > 2) {
                    System.out.print("Two inputs not provided.");
                    System.exit(-1);
                }
                FindWordReplace.wordReplace(parameters, fileName);
            } else {
                System.exit(0);
            }
            System.out.println("Resultant file is " + outputFile);
        } while (words.length() > 0);
    }

}