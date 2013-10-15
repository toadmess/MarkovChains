package org.abatons.markov.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * <p>
 * This builds up a list of transitions, and their probabilities, from a single word history to the possible subsequent target words that can occur next.
 * As each transition is added, all existing transitions have their respective probabilities recalculated.
 * </p>
 * 
 * <p>Whilst compiling a new graph, if a transition is noted for a new target word that has not been seen before, the new transition is given a probability of 1 out of the total number of observed transitions.</p>
 * 
 * <p>Whilst compiling a new graph, If a transition is noted for a target word that has been seen before, the existing transition for that target word has its probability's numerator incremented.</p>
 * 
 * <p>Whilst compiling a new graph, whenever any transition is noted, all transitions then have their probability's denominator incremented.</p>
 */
public class Transitions implements Iterable<TransitionProbability> {
    private final ArrayList<TransitionProbability> transitions;

    public Transitions() {
        this.transitions = new ArrayList<TransitionProbability>(0);
    }
    
    @Override
    public Iterator<TransitionProbability> iterator() {
        return this.transitions.iterator();
    }
    
    public int getNumberTransitions() {
        return this.transitions.size();
    }
    
    /**
     * <p>This method is for use whilst building up a graph during the compilation of some text. Certainly not to be used whilst deserialising a saved graph.</p>
     * 
     * <p>This takes note of an occurrence of a transition to a word. All transitions then have their transition probabilities recalculated and the Iterator's order is re-sorted.</p>
     * 
     * Transition probabilities are recalculated as follows:
     * <ul>
     * <li>If there are no existing transitions, this new word transition will have a probability of 1/1</li>
     * <li>If there are existing transitions, but none are for this word, then this new word transition will have a numerator of 1. The denominator will be set to the same as all other transitions, plus one. Then all other transitions will have their denominators incremented by one.</li> 
     * <li>If a transition to this word has already been observed, it's probability will have its numerator incremented by 1. Then all transitions, including this one just observed, will have their denominators incremented by one.</li> 
     * </ul> 
     *  
     * @param inTargetWordId The ID identifying the word that follows.
     */
    public void recordTransition(final char inTargetWordId) {
        final char newDenom;
        if(this.getNumberTransitions() > 0) {
            newDenom = (char) (this.transitions.get(0).getDenominator() + 1);
        } else {
            newDenom = (char) 1;
        }
        
        boolean seenTargetWordBefore = false;
        for(final TransitionProbability t : this.transitions) {
            final char newNumer; 
            if(inTargetWordId == t.targetWordId) {
                newNumer = (char) (t.getNumerator() + 1);
                seenTargetWordBefore = true;
            } else {
                newNumer = t.getNumerator();
            }
            
            t.setChance(newNumer, newDenom);
        }
        
        if(!seenTargetWordBefore) {
            final TransitionProbability transition = new TransitionProbability(inTargetWordId, (char) 1, newDenom);
            this.transitions.add(transition);
        }
        
        sortAndTrim();
    }
    
    private void sortAndTrim() {
        Collections.sort(this.transitions, new Comparator<TransitionProbability>() {
            @Override
            public int compare(final TransitionProbability a, final TransitionProbability b) {
                // First compare chance, highest comes first
                if(a.getNumerator() < b.getNumerator()) return 1;
                if(a.getNumerator() > b.getNumerator()) return -1;
                
                // Chance is the same, now compare by word ID, lowest comes first
                if(a.targetWordId > b.targetWordId) return 1;
                if(a.targetWordId < b.targetWordId) return -1;
                
                // They're the same.
                return 0;
            }
        });
        
        this.transitions.trimToSize();
    }
    
    /**
     * <p>This method is for use whilst reconstructing a persisted graph. Certainly not to be used whilst compiling a new graph.</p>
     * 
     * <p>Adds a transition to a target word with the given probability. Its order in the Iterator will be after all other transitions added before it (i.e. the sequence of calls to this method dictates the sorting of the TransitionProbabilities).</p>
     * 
     * @param isLastToBeAdded False if there further transitions are expected to be added. True if this is thought to be the last to be added. If true, the internal data structures will then be optimised for space. 
     */
    public void addTransition(final char inTargetWordId, final char inNumerator, final char inDenominator, final boolean isLastToBeAdded) {
      this.transitions.add(new TransitionProbability(inTargetWordId, inNumerator, inDenominator));
      
      if(isLastToBeAdded) {
        this.transitions.trimToSize();
      }
    }
    
    @Override
    public String toString() {
      final StringBuilder sb = new StringBuilder("Transitions[");
      
      for(int i = 0; i < this.transitions.size(); i++) {
        if(i > 0) {
          sb.append(", ");
        }
        final TransitionProbability tp = this.transitions.get(i);
        
        sb.append(tp.toString());
      }
      
      sb.append("]");
      
      return sb.toString();
    }
}