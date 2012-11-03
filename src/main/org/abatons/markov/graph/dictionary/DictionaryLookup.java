package org.abatons.markov.graph.dictionary;

/**
 * Provides an interface for looking up the word ID for a word.
 */
public abstract class DictionaryLookup extends Dictionary {
  public DictionaryLookup(final String[] inAllUniqueWordsSortedAlphabetically) {
    super(inAllUniqueWordsSortedAlphabetically);
  }

  /**
   * Gets the ID for a specific case sensitive word. This ID can then be used
   * for a very quick recall of the word from the Dictionary, and can be used
   * as a space efficient mnemonic for the word.
   * 
   * @param inForThisWord
   * @return A unique ID identifying the word. Null if the word does not exist
   *         in the dictionary.
   */
  public abstract Character getWordId(final String inForThisWord);
  
  /**
   * Finds all the IDs of words matching the given word, when matching without case sensitivity.
   *   
   * @param inForThisWord
   * @return All the IDs for the different words, in no particular order. e.g. {"She", "she", "SHE", "shE"} 
   */
  //public abstract Character[] getWordIdsIgnoreCase(final String inForThisWord);
}
