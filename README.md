# findwordandreplace
Find a given word and replace all instances with a replacement word provided.

This program implements word dictionary with positions stored. It implements something similar to the
first half of the following link:

http://www.ardendertat.com/2011/12/20/programming-interview-questions-23-find-word-positions-in-text/

and takes it a step further by asking for a word to search and word to replace each instance within a
specified file and saves the resultant text to a new text file.

The structure of the code flow is that a given text file is read incrementally per line and looks at
each word and stores its location/position within the line.

The main data structure is a HashMap that has a key of word and value of another HashMap that has key
as line number and value as positions within that line.

NOTE: This problem stores the first character position of the word within the a line number.
It does not store the occurence position of the entire string word.

For instance,

"test frank giordano" 

test located at line 1, position 0
frank located at line 1, position 5
etc

whereas entire word position would be:
test located at position 0 and frank located at position 1 and giordano located at position 3
