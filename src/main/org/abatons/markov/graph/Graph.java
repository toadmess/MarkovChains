package org.abatons.markov.graph;

import java.util.Arrays;
import java.util.Map;

import org.abatons.markov.graph.dictionary.DictionaryLookup;

public class Graph {
    private final Map<String, Transitions> wordHistoryToTransitions;
    private final DictionaryLookup dictionary;
    
    public Graph(final DictionaryLookup inDictionary, final Map<String, Transitions> inWordHistoryToTransitions) {
        this.dictionary = inDictionary;

        this.wordHistoryToTransitions = inWordHistoryToTransitions; 
    }
    
    public DictionaryLookup getDictionary() {
        return this.dictionary;
    }
    
    /**
     * Get the transitions to words following on from a certain point in the graph (pinpointed by the history of words)
     * 
     * @param inFollowingThisWordHistory The history of words (which will be equal to the graph order) leading up to the current point
     * @return Never null - if the word history is unknown, an empty list of transitions is returned. Otherwise, all of the words that are known to come after the history of words are returned.
     */
    public Transitions getTransitions(final String inFollowingThisWordHistory) {
        final Transitions foundTransitions = this.wordHistoryToTransitions.get(inFollowingThisWordHistory);
        
        if(foundTransitions == null) {
            // Just return an empty list of transitions
            return new Transitions();
        }
        
        return foundTransitions;
    }
    
    public String getAWordHistoryForTesting() {
        return this.wordHistoryToTransitions.keySet().iterator().next();
    }
    
    /**
     * Slow and memory intensive. Not for use on a resource limited device like a mobile.
     * 
     * @return Array of word histories, for use in persisting this graph.
     */
    public String[] getWordHistories() {
      return this.wordHistoryToTransitions.keySet().toArray(new String[]{});
    }
    
    /**
     * Slow and memory intensive. Not for use on a resource limited device like a mobile.
     */ 
    public void printGraphStats() {
        int targetWordCount = 0;
        int maxTargetWords = 0;
        
        final String[] sortedHistorySigs = wordHistoryToTransitions.keySet().toArray(new String[0]);
        Arrays.sort(sortedHistorySigs);
        for(final String indexHistory : sortedHistorySigs) {
            int numTargetWords = 0;
            for(final TransitionProbability transition : wordHistoryToTransitions.get(indexHistory)) {
                numTargetWords++;
            }
            
            targetWordCount += numTargetWords;
            maxTargetWords = Math.max(maxTargetWords, numTargetWords);
        }
        
        System.out.println("  maximum target words: " + maxTargetWords);
        System.out.println("  average target words per node: " + ((float)targetWordCount / (float)sortedHistorySigs.length));
    }
}
