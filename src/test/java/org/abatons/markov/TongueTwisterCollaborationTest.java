package org.abatons.markov;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;

import java.io.IOException;

import org.abatons.markov.compiler.GraphCompiler;
import org.abatons.markov.graph.Graph;
import org.abatons.markov.graph.Parody;
import org.abatons.markov.graph.TransitionProbability;
import org.abatons.markov.graph.Transitions;
import org.abatons.markov.graph.dictionary.DictionaryLookup;
import org.junit.Before;
import org.junit.Test;

public class TongueTwisterCollaborationTest {
  Graph graphOrder1;

  char idShe;
  char idSells;
  char idSea;
  char idShells;
  char idBy;
  char idThe;
  char idShore;
  char idFullStop;

  @Before
  public void setupTongueTwister() throws IOException {
    final String tongueTwister = "She sells sea shells by the sea shore.";

    graphOrder1 = GraphCompiler.createGraph(tongueTwister, 1);

    final DictionaryLookup dictionary = graphOrder1.getDictionary();

    idShe = dictionary.getWordId("She");
    idSells = dictionary.getWordId("sells");
    idSea = dictionary.getWordId("sea");
    idShells = dictionary.getWordId("shells");
    idBy = dictionary.getWordId("by");
    idThe = dictionary.getWordId("the");
    idShore = dictionary.getWordId("shore");

    idFullStop = dictionary.getWordId(".");
  }

  @Test
  public void transitionsSummedProbabilitesAllGood() {
    final char[] allIds = new char[] { idShe, idSells, idSea, idShells, idBy, idThe, idShore, idFullStop };

    for (final char id : allIds) {
      final Transitions t = graphOrder1.getTransitions("" + id);
      checkSummedProbability(t);
    }
  }

  @Test
  public void checkListSizes() {
    checkNumTransitions(idShe, 1);
    checkNumTransitions(idSells, 1);
    checkNumTransitions(idSea, 2);
    checkNumTransitions(idShells, 1);
    checkNumTransitions(idBy, 1);
    checkNumTransitions(idThe, 1);
    checkNumTransitions(idShore, 1);
    checkNumTransitions(idFullStop, 0);
  }

  private void checkNumTransitions(final char inId, final int inExpectedNumTransitions) {
    final String order1WordHistory = "" + inId;
    final Transitions t = graphOrder1.getTransitions(order1WordHistory);

    assertEquals(inExpectedNumTransitions, t.getNumberTransitions());
  }
  
  @Test
  public void isCaseSensitive() {
    final DictionaryLookup dict = graphOrder1.getDictionary();
      
    assertEquals(1, graphOrder1.getTransitions(""+dict.getWordId("She")).getNumberTransitions());
    assertEquals(0, graphOrder1.getTransitions(""+dict.getWordId("she")).getNumberTransitions());
    assertEquals(0, graphOrder1.getTransitions(""+dict.getWordId("shE")).getNumberTransitions());
    
    assertEquals(0, graphOrder1.getTransitions(""+dict.getWordId("SEA")).getNumberTransitions());
  }
  
  @Test
  public void getNextWord_hasTransitionsToFullstops() {
    assertTrue(contains(graphOrder1, ".", graphOrder1.getTransitions(""+idShore)));
  }
  
  @Test
  public void getNextWord_noTransitionsFromFullstops() {
    assertEquals(0, graphOrder1.getTransitions(""+idFullStop).getNumberTransitions());
  }

  @Test
  public void getNextWord_checkAllNextWords() {
    final NonRandomNG controllableRNG = new NonRandomNG();
    final String startingWordHistory = "" + graphOrder1.getDictionary().getWordId("She");
    final Parody parody = new Parody(graphOrder1, startingWordHistory, controllableRNG);

    assertEquals("sells", parody.getNextWord());
    assertEquals("sea", parody.getNextWord());
    assertEquals("shells", parody.getNextWord());
    assertEquals("by", parody.getNextWord());
    assertEquals("the", parody.getNextWord());
    assertEquals("sea", parody.getNextWord());

    controllableRNG.unrandomTarget = 1;
    assertEquals("shore", parody.getNextWord());

    controllableRNG.unrandomTarget = 0;
    assertEquals(".", parody.getNextWord());

    // There are no transitions from the word "." as it is the last word in the whole text.
    assertNull(parody.getNextWord());
  }

  @Test
  public void nextWordFromUnknownWordHistoryIsNull() {
    final NonRandomNG controllableRNG = new NonRandomNG();
    final Parody parody = new Parody(graphOrder1, "rascal", controllableRNG);

    assertNull(parody.getNextWord());
  }

  @Test
  public void transitionProbabilites() throws IOException {
    final String source = "And there came out of the smoke locusts upon the earth and unto them was given power, as the scorpions of the earth have power.";
    // transitions from "the" are: "earth" "earth" "smoke" "scorpions"

    final Graph g = GraphCompiler.createGraph(source, 1);

    final NonRandomNG rng = new NonRandomNG();

    final char wordIdThe = g.getDictionary().getWordId("the");

    rng.unrandomTarget = 0;
    Parody p = new Parody(g, "" + wordIdThe, rng);
    assertEquals("earth", p.getNextWord());

    rng.unrandomTarget = 1;
    p = new Parody(g, "" + wordIdThe, rng);
    assertEquals("earth", p.getNextWord());

    rng.unrandomTarget = 2;
    p = new Parody(g, "" + wordIdThe, rng);
    assertNotSame("earth", p.getNextWord());

    rng.unrandomTarget = 3;
    p = new Parody(g, "" + wordIdThe, rng);
    assertNotSame("earth", p.getNextWord());
  }

  @Test
  public void transitionProbabilites_more() throws IOException {
    final String source = "And there came out of the smoke locusts upon the earth and unto them was given power, as the scorpions of the earth have power.";
    // transitions from "the" are: "earth" "earth" "smoke" "scorpions"

    final Graph g = GraphCompiler.createGraph(source, 1);

    final char wordIdThe = g.getDictionary().getWordId("the");
    final char wordIdEarth = g.getDictionary().getWordId("earth");

    final Transitions theTransitions = g.getTransitions("" + wordIdThe);

    checkSummedProbability(theTransitions);

    final TransitionProbability mostLikelyTransition = theTransitions.iterator().next();

    assertEquals(wordIdEarth, mostLikelyTransition.targetWordId);
    assertEquals(4, mostLikelyTransition.getDenominator());
    assertEquals(2, mostLikelyTransition.getNumerator());

    assertTrue(contains(g, "smoke", theTransitions));
    assertTrue(contains(g, "scorpions", theTransitions));
  }
  
  protected void checkSummedProbability(final Transitions transitions) {
    if(transitions.getNumberTransitions() == 0) {
        return;
    }
    
    int cumulNumerators = 0;
    final int denominator = transitions.iterator().next().getDenominator();
    
    for(final TransitionProbability t : transitions) {
        cumulNumerators += t.getNumerator();
        
        assertEquals(denominator, t.getDenominator());
    }
    
    assertEquals(denominator, cumulNumerators);
  }

  protected boolean contains(final Graph inGraph, final String inThisWord, final Transitions inThislist) {
      for(final TransitionProbability transition : inThislist) {
          final String targetWord = inGraph.getDictionary().getWord(transition.targetWordId);
          
          if(inThisWord.equalsIgnoreCase(targetWord)) {
              return true; 
          }
      }
      
      return false;
  }
}
