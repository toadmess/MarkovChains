package org.abatons.markov;

import static org.junit.Assert.*;

import org.abatons.markov.graph.TransitionProbability;
import org.junit.Test;

public class TransitionProbabilityContractTest {
	@Test
	public void construction() {
	    final TransitionProbability tp = new TransitionProbability('a', (char)1, (char)1);
	    
	    assertEquals('a', tp.targetWordId);
	    assertEquals(1, tp.getNumerator());
	    assertEquals(1, tp.getDenominator());
	}
	
	@Test
    public void setChance() {
	    final TransitionProbability tp = new TransitionProbability('a', (char)1, (char)1);
	    
	    tp.setChance((char) 23, (char) 50);
	    
	    assertEquals((char) 23, tp.getNumerator());
        assertEquals((char) 50, tp.getDenominator());
        assertEquals('a', tp.targetWordId);
	}
}

