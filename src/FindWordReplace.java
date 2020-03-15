import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/*
 * This program implements word dictionary with positions stored. It implements something similar to the
 * first half of the following link:
 * http://www.ardendertat.com/2011/12/20/programming-interview-questions-23-find-word-positions-in-text/
 * and takes it a step further by asking for a word to search and wrd to replace each instance within a
 * hard coded specified file located in current running directory and saves the resultant text to a new
 * text file.
 *
 * The structure of the code flow is that a text file is read from the current running location and reads
 * in every line and looks at each word and stores it location/position within the line.
 *
 * The main data structure is a HashMap that has a key of word and value of another HashMap that has key
 * as line number and value as positions within that line.
 *
 * author: Frank Giordano 10/24/2015
 */
public class FindWordReplace {

    // string = word, HashMap<Integer, ArrayList<Integer>> = line #, list of position numbers within the line
    private static HashMap<String, HashMap<Integer, ArrayList<Integer>>> dictionary;
    private static final String fileName = "t3.txt";
    private static final String outputFileName = "t3.results.txt";
    private static ArrayList<String> newFile;

    private static void processFile() {

        int posFound = 0;
        int lineCount = 0;
        String line = "";

        // HashMap<Integer, ArrayList<Integer>> = line #, list of position numbers within the line
        HashMap<Integer, ArrayList<Integer>> wordLinePosInfo;
        dictionary = new HashMap<>();
        newFile = new ArrayList<>();
        try {
            // create a reader which reads our file.
            BufferedReader bReader = new BufferedReader(new FileReader(fileName));

            // while we loop through the file, read each line until there is nothing left to read.
            // this assumes we have carriage returns ending each text line.
            newFile.add("");
            while ((line = bReader.readLine()) != null) {

                lineCount++;

                newFile.add(line);
                line = toLowerCase(line);
                String[] words = line.replaceAll("[^a-z0-9]", " ").split(" ");
                for (String word : words) {
                    if ("".equals(word))
                        continue;

                    int startIndex = 0;
                    while ((posFound = line.indexOf(word, startIndex)) != -1) {

                        // ignore finding the word text within another large word...
                        // we dont need to store the position. No text replacement.
                        if (isWordEmbedded(posFound, word, line)) {
                            startIndex = posFound + word.length();  // move search index ahead
                            continue; // skip this position
                        }

                        if (dictionary.containsKey(word)) {
                            wordLinePosInfo = dictionary.get(word);
                            if (wordLinePosInfo.get(lineCount) != null) {
                                // find the position of this word from the last saved known position
                                ArrayList<Integer> currWordLinePositions = wordLinePosInfo.get(lineCount);
                                // get the last word position from the list
                                int lastKnownPos = currWordLinePositions.get(currWordLinePositions.size() - 1);
                                // increase last known position to the end of the word so it starts searching there
                                lastKnownPos = lastKnownPos + word.length();
                                // search for the next word occurrence from current read in line file
                                posFound = line.indexOf(word, lastKnownPos);
                                if (posFound == -1)
                                    break;
                                if (!currWordLinePositions.contains(Integer.valueOf(posFound)))
                                    wordLinePosInfo.get(lineCount).add(posFound);
                            } else {
                                ArrayList<Integer> position = new ArrayList<>();
                                position.add(posFound);
                                wordLinePosInfo.put(lineCount, position);
                            }
                        } else {
                            wordLinePosInfo = new HashMap<>();
                            ArrayList<Integer> position = new ArrayList<>();
                            position.add(posFound);
                            wordLinePosInfo.put(lineCount, position);
                            dictionary.put(word, wordLinePosInfo);
                        }
                        startIndex = posFound + word.length();
                    }
                }
            }

            // close the reader.
            bReader.close();
        } catch (IOException e) {
            System.out.print("Error reading file. Error message = " + e.getMessage());
            System.exit(-1);
        }
    }

    public static boolean isWordEmbedded(int position, String word, String line) {
        if (position != 0 && position != line.length() -1 && position + (word.length() - 1) != (line.length() - 1)) {
            boolean isAlphabeticRightSide = Character.isAlphabetic(line.charAt(position + (word.length())));
            boolean isAlphabeticLeftSide = Character.isAlphabetic(line.charAt(position - 1));
            if (isAlphabeticRightSide || isAlphabeticLeftSide) {
                return true;
            }
        }
        return false;
    }

    public static void wordReplace(String[] parameters) {

        String searchWord = toLowerCase(parameters[0]);
        String replaceWord = parameters[1];

        processFile();

        if (!dictionary.containsKey(searchWord)) {
            System.out.println(" The following word " + searchWord + " was not found.");
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
        try {
            writer = new BufferedWriter(new FileWriter(outputFileName));
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

    public static void main(String args[]) {

        String words = null;
        byte[] input;

        do {
            input = new byte[80];
            System.out.println("Enter two words separated by a space for word search and replacement");
            System.out.print(">\t");
            try {
                System.in.read(input);
            } catch (IOException e) {
                System.out.print("Error reading given input. Error message = " + e.getMessage());
                System.exit(-1);
            }
            words = (new String(input, 0, input.length)).trim();
            if (words.length() > 0) {
                String[] parameters = words.split(" ");
                FindWordReplace.wordReplace(parameters);
            } else if (words.length() == 1 || words.length() > 2) {
                System.out.print("Two inputs not provided.");
                System.exit(-1);
            } else {
                System.exit(-1);
            }
        } while (words.length() > 0);
    }

}