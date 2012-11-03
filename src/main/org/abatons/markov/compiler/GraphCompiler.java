package org.abatons.markov.compiler;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.abatons.markov.graph.Graph;
import org.abatons.markov.graph.GraphPersistenceRaw;
import org.abatons.markov.graph.GraphPersistenceSqlite;
import org.abatons.markov.graph.Parody;
import org.abatons.markov.graph.Transitions;
import org.abatons.markov.graph.dictionary.DictionaryLookup;
import org.abatons.markov.graph.dictionary.DictionaryLookupCached;
import org.apache.commons.io.FileUtils;

public class GraphCompiler {
   /**
    * @param args The paths of the text files to read in an generate graphs for
    *  
    * @throws IOException
    * @throws ClassNotFoundException
    * @throws SQLException
    */
   public static void main(final String[] args) throws IOException, ClassNotFoundException, SQLException {
      for (final String filename : args) {
         final String allText = FileUtils.readFileToString(new File(filename));

         for (int order = 1; order < 5; order++) {
            final Graph graph = createGraph(allText, order);

            graph.printGraphStats();

            System.out.println("  example parody: " + new Parody(graph).generateParody(200));
            
            new GraphPersistenceSqlite(filename + "_order_" + order + ".sqlite.db").save(graph);
            new GraphPersistenceRaw(filename + "_order_" + order + ".raw").save(graph);
         }
      }
   }

   public static Graph createGraph(final String inAllText, final int inGraphOrder) throws IOException {
      // Read all the words and construct the dictionary containing all unique words and their IDs.
      // i.e. Scan words and create lexicon
      final DictionaryLookup dict = new DictionaryLookupCached(getSortedUniqueWords(inAllText));

      // Read in all the words again, but map their IDs to lists of all following words.
      // Basically build up the guts of the graph itself.
      final WordReader allTextReader = new WordReader.StringWordReader(inAllText);
      try {
         final Map<String, Transitions> wordTransitions = mapTransitions(dict, inGraphOrder, allTextReader);

         return new Graph(dict, wordTransitions);
      } finally {
         allTextReader.close();
      }
   }

   /**
    * @param wordIndicies
    *           Maps a key word to the index at which that word appears in the ordered list of all words in
    *           the file. The index is stored in a character, the numeric value of which is used.
    * @param order
    *           Number of words used in the history
    * @param filename
    *           The path of the file containing all of the plain text to extract the word statistics graph for
    * @return a map of the history signature to a list the words which follow (containing each target word's
    *         ID and chance of appearing, greatest chance ordered first). A history signature is a String of
    *         characters, where each character's numeric value is the index of the word in the allWords array.
    *         The first character is the oldest word in the history.
    * @throws IOException
    */
   private static Map<String, Transitions> mapTransitions(final DictionaryLookup inDict, final int order,
                                                          final WordReader inAllText) throws IOException {
      final Map<String, Transitions> wordHistoryToTransitionList = new HashMap<String, Transitions>();

      final StringBuffer wordIndexHistory = new StringBuffer();

      final SentenceReader sr = new SentenceReader(inAllText);
      for (String[] sentence = null; (sentence = sr.readSentence()).length > 0;) {
         for (final String word : sentence) {
            final char wordId = inDict.getWordId(word);

            if (wordIndexHistory.length() == order) {
               // The history of preceeding words is complete (i.e. we have elements equal to the wanted
               // order)
               final String historyIndexTrail = wordIndexHistory.toString();

               if (!wordHistoryToTransitionList.containsKey(historyIndexTrail)) {
                  wordHistoryToTransitionList.put(historyIndexTrail, new Transitions());
               }

               final Transitions listOfFollowingWords = wordHistoryToTransitionList.get(historyIndexTrail);

               listOfFollowingWords.recordTransition(wordId);

               wordIndexHistory.deleteCharAt(0);
            }

            wordIndexHistory.append(wordId);
         }
      }

      System.out.println("Order " + order + " graph contains " + wordHistoryToTransitionList.size()
            + " histories");

      return wordHistoryToTransitionList;
   }

   private static String[] getSortedUniqueWords(final String inAllText) throws IOException {
      final String[] allWordArray;

      WordReader wr = null;
      try {
         int wordCount = 0;
         wr = new WordReader.StringWordReader(inAllText);

         final Set<String> allWords = new HashSet<String>();

         final SentenceReader sr = new SentenceReader(wr);
         for (String sentence[] = null; (sentence = sr.readSentence()).length > 0;) {
            for (final String word : sentence) {
               allWords.add(word);
               wordCount++;
            }
         }

         allWordArray = allWords.toArray(new String[0]);

         System.out.println("The text contains " + wordCount + " words, of which " + allWordArray.length
               + " are unique(ish)");
      } finally {
         if (wr != null) {
            try {
               wr.close();
            } catch (final IOException e) {
            }
         }
      }

      Arrays.sort(allWordArray);

      return allWordArray;
   }
}
