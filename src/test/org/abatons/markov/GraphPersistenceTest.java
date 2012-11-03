package org.abatons.markov;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.abatons.markov.compiler.GraphCompiler;
import org.abatons.markov.graph.Graph;
import org.abatons.markov.graph.GraphPersistenceRaw;
import org.abatons.markov.graph.GraphPersistenceSqlite;
import org.abatons.markov.graph.TransitionProbability;
import org.abatons.markov.graph.Transitions;
import org.abatons.markov.graph.dictionary.DictionaryLookup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GraphPersistenceTest {
   private final String filenameSqlite = "DictionaryPersistenceTest.sqlite";
   private final String filenameRaw = "GraphPersistenceTest.raw";

   private Graph graph;
   private GraphPersistenceSqlite sqliteImpl;
   private GraphPersistenceRaw rawImpl;

   @Before
   public void setup() throws IOException {
      graph = GraphCompiler.createGraph("A foo foo bar.", 1);

      if ((new File(filenameSqlite)).exists()) {
         assertTrue((new File(filenameSqlite)).delete());
      }
      if ((new File(filenameRaw)).exists()) {
         assertTrue((new File(filenameRaw)).delete());
      }

      sqliteImpl = new GraphPersistenceSqlite(filenameSqlite);
      rawImpl = new GraphPersistenceRaw(filenameRaw);
   }

   @After
   public void tearDown() {
      (new File(filenameSqlite)).delete();
   }

   @Test
   public void save_and_load() {
      sqliteImpl.save(graph);
      assertSameGraph(graph, sqliteImpl.load());
      
      rawImpl.save(graph);
      assertSameGraph(graph, rawImpl.load());
   }

   private void assertSameGraph(final Graph g1, final Graph g2) {
      assertNotNull(g1);
      assertNotNull(g2);
      
      assertSameDictionary(g1.getDictionary(), g2.getDictionary());

      final String[] g1WordHistories = g1.getWordHistories();
      final String[] g2WordHistories = g2.getWordHistories();

      assertArrayEquals(g1WordHistories, g2WordHistories);

      for (final String wordHistory : g1WordHistories) {
         final Transitions t1 = g1.getTransitions(wordHistory);
         final Transitions t2 = g2.getTransitions(wordHistory);

         assertSameTransitions(t1, t2);
      }
   }

   private void assertSameTransitions(final Transitions t1, final Transitions t2) {
      assertEquals(t1.getNumberTransitions(), t2.getNumberTransitions());

      final Iterator<TransitionProbability> it1 = t1.iterator();
      final Iterator<TransitionProbability> it2 = t2.iterator();

      while (it1.hasNext()) {
         assertTrue(it2.hasNext());

         final TransitionProbability tp1 = it1.next();
         final TransitionProbability tp2 = it2.next();

         assertSameTransitionProbability(tp1, tp2);
      }

      assertFalse(it1.hasNext());
      assertFalse(it2.hasNext());
   }

   private void assertSameTransitionProbability(final TransitionProbability tp1,
                                                final TransitionProbability tp2) {
      assertEquals(tp1.targetWordId, tp2.targetWordId);
      assertEquals(tp1.getNumerator(), tp2.getNumerator());
      assertEquals(tp1.getDenominator(), tp2.getDenominator());
   }

   private void assertSameDictionary(final DictionaryLookup d1, final DictionaryLookup d2) {
      final int numWords = d1.getNumUniqueWords();

      assertEquals(d1.getNumUniqueWords(), d2.getNumUniqueWords());

      for (char wordId = 0; wordId < numWords; wordId++) {
         assertEquals(d1.getWord(wordId), d2.getWord(wordId));

         final String word = d1.getWord(wordId);

         assertEquals(d1.getWordId(word), d2.getWordId(word));
      }
   }

   @Test
   public void load_returnsNullIfFileNotFound() {
      final String fileNotFound = "somefilethatcouldn'tpossiblyexist.no_way_man!";
      assertTrue(!new File(fileNotFound).exists());

      final GraphPersistenceSqlite sqlite = new GraphPersistenceSqlite(fileNotFound);
      assertNull(sqlite.load());

      assertTrue(!new File(fileNotFound).exists());
   }
}
