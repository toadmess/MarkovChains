package org.abatons.markov.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SentenceReader {
    private final WordReader mReader;
    
    private static final String[] abbreviations; 
    static {
        abbreviations = new String[] {
            "Mr.", "Mrs.", "Ms."    
        };
        Arrays.sort(abbreviations);
    }
    
    public SentenceReader(final WordReader inWords) throws IOException {
        mReader = inWords;
    }
    	
	public String[] readSentence() throws IOException {
		final List<String> words = new ArrayList<String>();
		
		for(String word = null; (word = mReader.readWord()) != null;) {
		    if(isEndingWord(word)) {
		        final String endingPunctuation = getSentenceEnd(word);
		        
		        final String lastWord = word.substring(0, word.length() - endingPunctuation.length());
		        
		        words.add(lastWord);
		        words.add(endingPunctuation);

		        break;
		    } else {
		        words.add(word);
		    }
		}
		
		return words.toArray(new String[0]);
	}
	
	private static boolean isEndingWord(final String word) {
        return !"".equals(getSentenceEnd(word));
    }
    
    private static String getSentenceEnd(final String fromThisWord) {
        if(isAbbrWithPeriod(fromThisWord)) return "";
        
        if(fromThisWord.endsWith(".")) return ".";
        if(fromThisWord.endsWith("?")) return "?";
        if(fromThisWord.endsWith("!")) return "!";
        
        if(fromThisWord.endsWith(".\"")) return ".\"";
        if(fromThisWord.endsWith("?\"")) return "?\"";
        if(fromThisWord.endsWith("!\"")) return "!\"";
        
        return "";
    }
    
    private static boolean isAbbrWithPeriod(final String word) {
        return Arrays.binarySearch(abbreviations, word) >= 0;
    }
}
