package org.abatons.markov.graph;


public class TransitionProbability {
    public final char targetWordId;
    
    private char chanceNumerator;
    private char chanceDenominator;
    
    public TransitionProbability(final char inTargetWordId, final char inNumerator, final char inDenominator) {
        this.targetWordId = inTargetWordId;
        setChance(inNumerator, inDenominator);
    }

    public char getNumerator() { return chanceNumerator; }
    public char getDenominator() { return chanceDenominator; }
    
    public void setChance(final char inNumerator, final char inDenominator) {
        this.chanceNumerator = inNumerator;
        this.chanceDenominator = inDenominator;
    }
}