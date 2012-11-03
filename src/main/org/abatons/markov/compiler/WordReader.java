package org.abatons.markov.compiler;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.SortedSet;
import java.util.TreeSet;

public class WordReader {
    private final Reader mReader;
    
    public WordReader(final Reader inInputStream) throws FileNotFoundException {
        mReader = inInputStream;
    }
    
    public static class FileWordReader extends WordReader {
        public FileWordReader(final String inPath) throws FileNotFoundException {
            super(new FileReader(inPath));
        }
    }
    
    public static class StringWordReader extends WordReader {
        public StringWordReader(final String inString) throws FileNotFoundException {
            super(new StringReader(inString));
        }
    }
    	
	public String readWord() throws IOException {
		final StringBuilder oneWordSB = new StringBuilder();
		
		for(int intLetter = mReader.read(); intLetter != -1; intLetter = mReader.read()) {
			final char charLetter = (char) intLetter;
			
			if(Character.isWhitespace(charLetter)) {
				if(oneWordSB.length() > 0) {
					break;
				}
			} else {
				oneWordSB.append(charLetter);
			}
		}
		
		final String oneWord = oneWordSB.toString();
		
		if(oneWord.isEmpty()) {
			return null;
		}
		
		return oneWord;
	}
	
	/**
	 * Reads all words remaining in the file, removes any duplicates and sorts them according to their natural order
	 * 
	 * @return All remaining unique words, sorted by their natural order. There will be no duplicates.
	 * @throws IOException 
	 */
	public String[] readAllUniqueWordsAndSort() throws IOException {
	    final SortedSet<String> uniqueWords = new TreeSet<String>();
	    
	    for(String nextWord = null; (nextWord = readWord()) != null;) {
	        uniqueWords.add(nextWord);
	    }
	    
	    return uniqueWords.toArray(new String[0]);
	}

	/**
	 * Calls close() on the underlying reader to free up resources.
	 * @throws IOException 
	 */
    public void close() throws IOException {
        mReader.close();
    }
}
