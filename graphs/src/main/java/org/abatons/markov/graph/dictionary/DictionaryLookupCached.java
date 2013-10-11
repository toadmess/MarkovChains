package org.abatons.markov.graph.dictionary;

import java.util.HashMap;
import java.util.Map;

/**
 * A concrete DictionaryLookup implementation that provides O(1) lookups (after
 * the first lookup, which is O(n)). However, due to the caching, the memory
 * footprint is proportional to the number of words (i.e. like a hashmap) and
 * shouldn't be used on a resource limited device like a mobile.
 */
public class DictionaryLookupCached extends DictionaryLookup {
  private Map<String, Character> wordIndicies;

  public DictionaryLookupCached(final String[] inAllUniqueWordsSortedAlphabetically) {
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
    return getWordIndicies().get(inForThisWord);
  }

  /**
   * Maps each word to the index at which that word appears. This is a slow
   * process the first time it is called, but is cached for each subsequent
   * call. The caching is memory intensive, so don't use it on a limited device
   * like a mobile.
   * 
   * @return A map from the word to the index at which that word can be found
   *         (stored as a character for space purposes, but the numeric value is
   *         used)
   */
  private Map<String, Character> getWordIndicies() {
    if (null != this.wordIndicies) {
      return this.wordIndicies;
    }

    // Choose this to be enough for all the words (and include a little more
    // spare capacity to cover the default load factor of 0.75)
    final int initialCapacity = (int) (getNumUniqueWords() * 1.25F);
    
    final Map<String, Character> indicies = new HashMap<String, Character>(initialCapacity);

    final int numWords = getNumUniqueWords();
    for (char index = 0; index < numWords; index++) {
      indicies.put(getWord(index), index);
    }

    this.wordIndicies = indicies;

    return this.wordIndicies;
  }
}
