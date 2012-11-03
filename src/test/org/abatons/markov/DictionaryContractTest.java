package org.abatons.markov;

import org.abatons.markov.graph.dictionary.Dictionary;
import org.abatons.markov.graph.dictionary.DictionaryLookup;
import org.abatons.markov.graph.dictionary.DictionaryLookupCached;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DictionaryContractTest {
    @Before
    public void before() {
    }
    
	@Test
	public void getNumUniqueWords_noWords() {
	    final Dictionary dict = new Dictionary(new String[]{});
	    
	    assertEquals(0, dict.getNumUniqueWords());
	}
	
    @Test
    public void getNumUniqueWords_oneWord() {
        final Dictionary dict = new Dictionary(new String[]{"word"});
        
        assertEquals(1, dict.getNumUniqueWords());
    }
    
    @Test
    public void getNumUniqueWords_manyWords() {
        final Dictionary dict = new Dictionary(new String[]{"", "3", "two"});
        
        assertEquals(3, dict.getNumUniqueWords());
    }
    
    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void getWord_idIsOutOfBounds() {
        final String[] uniqueSortedWords = new String[]{"word"};
        final Dictionary dict = new Dictionary(uniqueSortedWords);
        
        final char indexBeyondEnd = (char) uniqueSortedWords.length;
        
        dict.getWord(indexBeyondEnd);
    }
    
    @Test
    public void getWord_idIsInBounds() {
        final Dictionary dict = new Dictionary(new String[]{"a", "b"});
        
        assertEquals("a", dict.getWord((char) 0));
        assertEquals("b", dict.getWord((char) 1));
    }
    
    @Test
    public void getWord_getWordId_symmetric() {
        final DictionaryLookup dict = new DictionaryLookupCached(new String[]{"a", "b"});
        
        assertEquals("a", dict.getWord(dict.getWordId("a")));
        assertEquals("b", dict.getWord(dict.getWordId("b")));
        
        assertEquals(new Character((char) 0), dict.getWordId(dict.getWord((char) 0)));
        assertEquals(new Character((char) 1), dict.getWordId(dict.getWord((char) 1)));
    }
    
    @Test
    public void getWordId_whenNotInDictionary() {
        final DictionaryLookup dict = new DictionaryLookupCached(new String[]{"a", "b"});
        
        assertNull(dict.getWordId("z"));
    }
    
    @Test
    public void getWordId_whenInDictionary() {
        final DictionaryLookup dict = new DictionaryLookupCached(new String[]{"a", "b"});
        
        assertEquals(new Character((char) 0), dict.getWordId("a"));
        assertEquals(new Character((char) 1), dict.getWordId("b"));
    }
}

