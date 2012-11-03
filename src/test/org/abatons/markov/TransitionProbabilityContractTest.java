package org.abatons.markov;

import static org.junit.Assert.*;

import org.abatons.markov.graph.TransitionProbability;
import org.junit.Test;

public class TransitionProbabilityContractTest {
	@Test
	public void construction() {
	    final TransitionProbability tp = new TransitionProbability('a', (byte)1, (byte)1);
	    
	    assertEquals('a', tp.targetWordId);
	    assertEquals(1, tp.getNumerator());
	    assertEquals(1, tp.getDenominator());
	}
	
	@Test
    public void setChance() {
	    final TransitionProbability tp = new TransitionProbability('a', (byte)1, (byte)1);
	    
	    tp.setChance((byte) 23, (byte) 50);
	    
	    assertEquals((byte) 23, tp.getNumerator());
        assertEquals((byte) 50, tp.getDenominator());
        assertEquals('a', tp.targetWordId);
	}
}

