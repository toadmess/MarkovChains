package org.abatons.markov.graph.dictionary;

/**
 * Intended to be a space efficient storage for all the words, and a time 
 * efficient lookup for words by ID.  
 */
public class Dictionary {
    protected final String[] allUniqueSortedWords;
    
    public Dictionary(final String[] inAllUniqueWordsSortedAlphabetically) {
        // We're using chars to store the indicies of the words (to keep the serialized size down).. 
        // Be sure they won't overflow.
        assert(Character.MAX_VALUE > inAllUniqueWordsSortedAlphabetically.length);
        
        this.allUniqueSortedWords = inAllUniqueWordsSortedAlphabetically;
    }

    public String getWord(final Character inWordId) {
        return this.allUniqueSortedWords[inWordId];
    }
    
    public int getNumUniqueWords() {
        return this.allUniqueSortedWords.length;
    }
}
