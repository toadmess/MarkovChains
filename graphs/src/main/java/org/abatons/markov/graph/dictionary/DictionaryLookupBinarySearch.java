package org.abatons.markov.graph.dictionary;

import java.util.Arrays;

/**
 * A concrete DictionaryLookup implementation that provides O(1og n) lookups of word IDs.
 * Space complexity is O(n), i.e. it's stored in an array. 
 */
public class DictionaryLookupBinarySearch extends DictionaryLookup {
  public DictionaryLookupBinarySearch(final String[] inAllUniqueWordsSortedAlphabetically) {
    super(inAllUniqueWordsSortedAlphabetically);
  }

  /**
   * In this implementaton, this is a slow process the first time it is called,
   * but is cached for each subsequent call. The caching is memory intensive, so
   * don't use it on a limited device like a mobile.
   * 
   * @param inForThisWord
   * @return A unique ID identifying the word. Null if the word does not exist
   *         in the dictionary.
   */
  public Character getWordId(final String inForThisWord) {
    final int index = Arrays.binarySearch(this.allUniqueSortedWords, inForThisWord);
    
    if(index < 0 || index >= this.allUniqueSortedWords.length) {
       // Not found
       return null;
    }
    
    return (char) index; 
  }
}
