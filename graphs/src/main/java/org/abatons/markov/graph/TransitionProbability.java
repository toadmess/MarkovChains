package org.abatons.markov.graph;


public class TransitionProbability {
    public final char targetWordId;
    
    private byte chanceNumerator;
    private byte chanceDenominator;
    
    public TransitionProbability(final char inTargetWordId, final byte inNumerator, final byte inDenominator) {
        this.targetWordId = inTargetWordId;
        setChance(inNumerator, inDenominator);
    }

    public byte getNumerator() { return chanceNumerator; }
    public byte getDenominator() { return chanceDenominator; }
    
    public void setChance(final byte inNumerator, final byte inDenominator) {
        this.chanceNumerator = inNumerator;
        this.chanceDenominator = inDenominator;
    }
}