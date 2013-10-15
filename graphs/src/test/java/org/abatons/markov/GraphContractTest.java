package org.abatons.markov;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.abatons.markov.graph.Graph;
import org.abatons.markov.graph.TransitionProbability;
import org.abatons.markov.graph.Transitions;
import org.abatons.markov.graph.dictionary.DictionaryLookup;
import org.abatons.markov.graph.dictionary.DictionaryLookupCached;
import org.junit.Before;
import org.junit.Test;

public class GraphContractTest {
    private DictionaryLookup dict;
    private char idForWordA;
    private char idForWordB;
    private Transitions justOneTransitionToWordB;
    private Graph singleTransitionGraph;
    
    @Before
    public void before() {
        this.dict = new DictionaryLookupCached(new String[]{"a", "b"});
        
        this.idForWordA = dict.getWordId("a");
        this.idForWordB = dict.getWordId("b");
        
        this.justOneTransitionToWordB = new Transitions();
        this.justOneTransitionToWordB.recordTransition(idForWordB);
        
        final Map<String, Transitions> historyToTransitionsMap = new HashMap<String, Transitions>();
        historyToTransitionsMap.put("" + idForWordA, this.justOneTransitionToWordB);
        
        this.singleTransitionGraph = new Graph(dict, historyToTransitionsMap);
    }
    
	@Test
	public void getDictionary() {
	    final DictionaryLookup dict = new DictionaryLookupCached(new String[]{"a"});
	    
	    final Map<String, Transitions> noTransitions = new HashMap<String, Transitions>();
	    final Graph g = new Graph(dict, noTransitions);
	    
	    assertEquals(dict, g.getDictionary());
	}
	
    @Test
    public void getTransitions_whenThereAreNoTransitions() {
        final DictionaryLookup dict = new DictionaryLookupCached(new String[]{"a"});
        
        final Map<String, Transitions> noTransitions = new HashMap<String, Transitions>();
        final Graph g = new Graph(dict, noTransitions);
        
        final Transitions transitionsFromA = g.getTransitions("a");
        
        assertNotNull(transitionsFromA);
        
        assertFalse(transitionsFromA.iterator().hasNext());
    }
    
    @Test
    public void getTransitions_whenSuppliedAKnownWordHistory() {        
        final Transitions transitionsFromA = singleTransitionGraph.getTransitions("" + idForWordA);
        
        assertNotNull(transitionsFromA);
        
        final Iterator<TransitionProbability> transitions = transitionsFromA.iterator();
        
        final TransitionProbability tp = transitions.next();
        
        assertFalse(transitions.hasNext());
        
        assertEquals(idForWordB, tp.targetWordId);
        assertEquals(1, tp.getNumerator());
        assertEquals(1, tp.getDenominator());
    }
    
    @Test
    public void getTransitions_whenSuppliedAnUnknownWordHistory() {
        final Transitions transitionsFromB = singleTransitionGraph.getTransitions("" + idForWordB);
        
        assertNotNull(transitionsFromB);
        
        assertFalse(transitionsFromB.iterator().hasNext());
    }
}

