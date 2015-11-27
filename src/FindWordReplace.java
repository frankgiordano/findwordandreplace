import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/*
 * author: Frank Giordano 10/24/2015
 * program implements word dictionary with positions stored
 * implements first half of the following link
 * http://www.ardendertat.com/2011/12/20/programming-interview-questions-23-find-word-positions-in-text/
 * and takes it a step further by asking for a word to search and a word to replace
 * each instance in the text file and save the resultant text in a new text file
 * 
 */
public class FindWordReplace {
	
	private static HashMap<String, HashMap<Integer, ArrayList<Integer>>> dictionary;
	private static HashMap<Integer, ArrayList<Integer>> wordLinesPositionsInfo;
    private static String fileName = "/Users/FrankGiordano/Documents/workspace/FindWordAndReplace/t3.txt";
    private static String outputFileName = "/Users/FrankGiordano/Documents/workspace/FindWordAndReplace/t3.results.txt";
    private static ArrayList<String> newFile;
	
    public static void main(String args[]) {
    
        String words = null;
		byte[] input;
		
		do {
			input = new byte[80];
			System.out.println("Enter two words separated by a space for word search and replacement from the following file located here");
			System.out.println(fileName);
			System.out.print(":");
			try {
				System.in.read(input);
			} catch (IOException e) {
				e.printStackTrace();
			}
			words = (new String(input, 0, input.length)).trim();
			if (words.length() > 0)
		    {
				String[] parameters = words.split(" ");
		    	processFileWordSearch();
		    	wordReplace(parameters);
		    	writeOutPutFile(outputFileName);
		    }
		} while (words.length() > 0);
		
    }

	private static void writeOutPutFile(String outputFileName) {
		
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(outputFileName));
			for (String line: newFile) {
				if (line != null) {
					writer.write(line);
					writer.newLine();
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private static void wordReplace(String[] parameters) {
		
		String searchWord = toLowerCase(parameters[0]);
		String replaceWord = parameters[1];
		
		if (!dictionary.containsKey(searchWord)) {
			System.out.println(" The following word " + searchWord + " was not found.");
		}
		else
		{
			for (Entry<Integer, ArrayList<Integer>> entry : dictionary.get(searchWord).entrySet()) {
				
				System.out.println("The following word " + searchWord + 
									" was found at line number " + entry.getKey() + 
									" at position(s) " + entry.getValue().toString());
				System.out.println("Changing Line number      = " + entry.getKey() + ", with string = " + newFile.get(entry.getKey()));
				
				StringBuilder temp = new StringBuilder();
				temp.append(newFile.get(entry.getKey()));
				
				// start from the end of the positions array to avoid overwriting.. 
				// replace words starting from the end of the string not the front..
				for (int i = entry.getValue().size() - 1; i >= 0; i--) {
//					System.out.println("p1 = " + entry.getValue().get(i) + " p2 = " + entry.getValue().get(i)+searchWord.length() + " searchWord = " + replaceWord);
					temp.replace(entry.getValue().get(i), entry.getValue().get(i)+searchWord.length(), replaceWord);		
				}
				
				newFile.set(entry.getKey(), temp.toString());
				
				System.out.println("Changed Line number       = " + entry.getKey() + ", with string = " + newFile.get(entry.getKey()));	
			}
		}
	}

	private static void processFileWordSearch() {
		
	     try {
	        	int posFound = 0;
	            int lineCount = 0;
	            String line = "";
	            dictionary = new HashMap<String, HashMap<Integer, ArrayList<Integer>>>();
	            newFile = new ArrayList<String>();

	            // create a reader which reads our file. 
	            BufferedReader bReader = new BufferedReader(new FileReader(fileName));

	            // while we loop through the file, read each line until there is nothing left to read.
	            // this assumes we have carriage returns ending each text line.
	            newFile.add("");
	            while ((line = bReader.readLine()) != null) {
	            	
	                 lineCount++;
	                 newFile.add(line);
	                 line = toLowerCase(line);
	                 String[] words = line.split(" ");
	                    
	                 for (String word: words) {
	                    	
	                    posFound = line.indexOf(word);
	                    if (dictionary.containsKey(word)) {
	                    	wordLinesPositionsInfo = dictionary.get(word);
	                    	if (wordLinesPositionsInfo.get(lineCount) != null) {
	                    		// find the position of this word from the last saved known position
	                    		// within the array
	                    		posFound = line.indexOf(word, (wordLinesPositionsInfo.get(lineCount).get(wordLinesPositionsInfo.get(lineCount).size()-1) + word.length()));
	                    		wordLinesPositionsInfo.get(lineCount).add(posFound);
	                    	} 
	                    	else 
	                    	{
	                    		ArrayList<Integer> position = new ArrayList<Integer>();
	                    		position.add(posFound);
	                    		wordLinesPositionsInfo.put(lineCount, position);
	                    	}                 	
	                    }
	                    else
	                    {
	                    	wordLinesPositionsInfo = new HashMap<Integer, ArrayList<Integer>>();
	                    	ArrayList<Integer> position = new ArrayList<Integer>();
	                		position.add(posFound);
	                    	wordLinesPositionsInfo.put(lineCount, position);
	                    	dictionary.put(word, wordLinesPositionsInfo);
	                    }
	                    			
	                 } 

	             }
	             // close the reader.
	             bReader.close(); 
	       }
	       catch (IOException e) {
	                // we encountered an error with the file, print it to the user.
	                System.out.println("Error: " + e.toString());
	       }
	}

	private static String toLowerCase(String input) {

		    StringBuilder stringBuffer = new StringBuilder(input);

		    String result = "";
		    
		    for (int i = 0; i < stringBuffer.length(); i++) {
		        Character charAt = stringBuffer.charAt(i);
		        if (Character.isAlphabetic(charAt)) {
		        	result = result + Character.toLowerCase(charAt);
		        }
		        else
		        {
		        	result = result + charAt;
		        }
		    }

		    return result;
	}

}