package org.abatons.markov;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.abatons.markov.graph.TransitionProbability;
import org.abatons.markov.graph.Transitions;
import org.junit.Test;

public class TransitionsContractTest {
  @Test
  public void iterator_noTransitionsGivesEmptyIterator() {
    final Transitions t = new Transitions();

    final Iterator<TransitionProbability> i = t.iterator();

    assertNotNull(i);
    assertFalse(i.hasNext());
  }

  @Test
  public void iterator_orderedByProbabilityThenById() {
    final Transitions t = new Transitions();

    t.recordTransition('a');
    t.recordTransition('a');

    t.recordTransition('b');
    t.recordTransition('b');
    t.recordTransition('b');

    t.recordTransition('d');

    t.recordTransition('c');

    final Iterator<TransitionProbability> i = t.iterator();

    assertNotNull(i);

    final TransitionProbability tp1 = i.next();
    assertEquals('b', tp1.targetWordId);

    final TransitionProbability tp2 = i.next();
    assertEquals('a', tp2.targetWordId);

    final TransitionProbability tp3 = i.next();
    assertEquals('c', tp3.targetWordId);

    final TransitionProbability tp4 = i.next();
    assertEquals('d', tp4.targetWordId);

    assertFalse(i.hasNext());
  }

  @Test
  public void recordTransition_singleTransition() {
    final Transitions t = new Transitions();

    t.recordTransition('a');

    final TransitionProbability tp = t.iterator().next();

    assertEquals('a', tp.targetWordId);
    assertEquals((char) 1, tp.getNumerator());
    assertEquals((char) 1, tp.getDenominator());
  }

  @Test
  public void recordTransition_twoTransitionsToSameTarget() {
    final Transitions t = new Transitions();

    t.recordTransition('a');
    t.recordTransition('a');

    final TransitionProbability tp = t.iterator().next();

    assertEquals('a', tp.targetWordId);
    assertEquals((char) 2, tp.getNumerator());
    assertEquals((char) 2, tp.getDenominator());
  }

  @Test
  public void recordTransition_twoTransitionsToDifferentTargetsButSameChance() {
    final Transitions t = new Transitions();

    t.recordTransition('a');
    t.recordTransition('b');

    final Iterator<TransitionProbability> i = t.iterator();

    final TransitionProbability tp1 = i.next();
    assertEquals('a', tp1.targetWordId);
    assertEquals((char) 1, tp1.getNumerator());
    assertEquals((char) 2, tp1.getDenominator());

    final TransitionProbability tp2 = i.next();
    assertEquals('b', tp2.targetWordId);
    assertEquals((char) 1, tp2.getNumerator());
    assertEquals((char) 2, tp2.getDenominator());

    assertFalse(i.hasNext());
  }

  @Test
  public void recordTransition_threeTransitionsToTwoDifferentTargets() {
    final Transitions t = new Transitions();

    t.recordTransition('a');
    t.recordTransition('b');
    t.recordTransition('a');

    final Iterator<TransitionProbability> i = t.iterator();

    final TransitionProbability tp1 = i.next();
    assertEquals('a', tp1.targetWordId);
    assertEquals((char) 2, tp1.getNumerator());
    assertEquals((char) 3, tp1.getDenominator());

    final TransitionProbability tp2 = i.next();
    assertEquals('b', tp2.targetWordId);
    assertEquals((char) 1, tp2.getNumerator());
    assertEquals((char) 3, tp2.getDenominator());

    assertFalse(i.hasNext());
  }
  
  @Test
  public void getNumberTransitions_isInitiallyZero() {
    final Transitions t = new Transitions();
    assertEquals(0, t.getNumberTransitions());
  }
  
  @Test
  public void getNumberTransitions_incrementsWithCallTo_addTransition() {
    final Transitions t = new Transitions();
    
    t.addTransition('a', (char)1, (char)1, false);
    assertEquals(1, t.getNumberTransitions());
    
    t.addTransition('b', (char)1, (char)1, true);
    assertEquals(2, t.getNumberTransitions());
  }
  
  @Test
  public void getNumberTransitions_incrementsWithCallTo_recordTransition_whenTargetWordIsNew() {
    final Transitions t = new Transitions();
    
    t.recordTransition('a');
    assertEquals(1, t.getNumberTransitions());
    
    t.recordTransition('b');
    assertEquals(2, t.getNumberTransitions());
  }
  
  @Test
  public void getNumberTransitions_doesNotIncrementsWithCallTo_recordTransition_whenTargetWordIsAlreadyKnown() {
    final Transitions t = new Transitions();
    
    t.recordTransition('a');
    assertEquals(1, t.getNumberTransitions());
    
    t.recordTransition('a');
    assertEquals(1, t.getNumberTransitions());
  }
  
  @Test
  public void addTransition_constructsTransitionProbabilityWithCorrectDetails() {
    final Transitions t = new Transitions();
    
    t.addTransition('a', (char)21, (char)23, true);
    
    final Iterator<TransitionProbability> it = t.iterator();
    
    assertTrue(it.hasNext());
    
    final TransitionProbability iterated = it.next();

    assertEquals('a', iterated.targetWordId);
    assertEquals((char) 21, iterated.getNumerator());
    assertEquals((char) 23, iterated.getDenominator());
  }
  
  @Test
  public void addTransition_transitionsAreOrderedBySequenceOfCallsToMethod() {
    final Transitions t = new Transitions();
    
    t.addTransition('z', (char)1, (char)1, false);
    t.addTransition('m', (char)1, (char)1, false);
    t.addTransition('a', (char)1, (char)1, true);
    
    final Iterator<TransitionProbability> it = t.iterator();
    
    assertTrue(it.hasNext());
    assertEquals('z', it.next().targetWordId);
    
    assertTrue(it.hasNext());
    assertEquals('m', it.next().targetWordId);
    
    assertTrue(it.hasNext());    
    assertEquals('a', it.next().targetWordId);
    
    assertFalse(it.hasNext());
  }
}

