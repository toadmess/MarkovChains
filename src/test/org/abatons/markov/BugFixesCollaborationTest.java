package org.abatons.markov;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.abatons.markov.compiler.GraphCompiler;
import org.abatons.markov.graph.Graph;
import org.abatons.markov.graph.Parody;
import org.abatons.markov.graph.dictionary.DictionaryLookup;
import org.junit.Test;

public class BugFixesCollaborationTest {

  @Test
  public void sentenceBeginningWithAbbreviation() throws IOException {
    final Graph g = GraphCompiler.createGraph("Yes. Mr. Foo.", 1);
    
    final DictionaryLookup d = g.getDictionary();
    
    final String startingWordHistory = "" + d.getWordId("Yes");
    
    final Parody p = new Parody(g, startingWordHistory, new NonRandomNG());
    
    assertEquals(".", p.getNextWord());
    assertEquals("Mr.", p.getNextWord());
    assertEquals("Foo", p.getNextWord());
  }
  
  @Test
  public void quotedSentenceEnd() throws IOException {
    final Graph g = GraphCompiler.createGraph("A dance!\" cried Marianne. \"Impossible! Who is to dance?\"", 1);
    
    final DictionaryLookup d = g.getDictionary();
    
    final String startingWordHistory = "" + d.getWordId("A");
    
    final NonRandomNG rng = new NonRandomNG();
    
    final Parody p = new Parody(g, startingWordHistory, rng);
    
    rng.unrandomTarget = 0;
    assertEquals("dance", p.getNextWord());
    assertEquals("!\"", p.getNextWord());
    assertEquals("cried", p.getNextWord());
    assertEquals("Marianne", p.getNextWord());
    assertEquals(".", p.getNextWord());
    assertEquals("\"Impossible", p.getNextWord());
    assertEquals("!", p.getNextWord());
    assertEquals("Who", p.getNextWord());
    assertEquals("is", p.getNextWord());
    assertEquals("to", p.getNextWord());
    assertEquals("dance", p.getNextWord());
    
    rng.unrandomTarget = 1; // As the transitions from "dance" are first "!\"" then "?\""
    assertEquals("?\"", p.getNextWord());
    
    rng.unrandomTarget = 0;
    assertNull(p.getNextWord());
  }
}
