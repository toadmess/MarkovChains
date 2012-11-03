package org.abatons.markov.graph;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.abatons.markov.graph.dictionary.Dictionary;
import org.abatons.markov.graph.dictionary.DictionaryLookup;
import org.abatons.markov.graph.dictionary.DictionaryLookupBinarySearch;
import org.abatons.markov.graph.dictionary.DictionaryLookupCached;

public class GraphPersistenceRaw implements GraphPersistence {
   private final String filename;

   public GraphPersistenceRaw(final String inFilename) {
      this.filename = inFilename;
   }

   @Override
   public void save(final Graph inGraph) {
      final long startTime = System.currentTimeMillis();
      
      DataOutputStream dos = null;
      
      try {
         dos = new DataOutputStream(new FileOutputStream(new File(this.filename)));
         
         final Dictionary dict = inGraph.getDictionary();

         final int numWords = dict.getNumUniqueWords();
         assert(numWords <= Character.MAX_VALUE);
         dos.writeChar(dict.getNumUniqueWords());
         
         for (char wordId = 0; wordId < numWords; wordId++) {
            dos.writeUTF(dict.getWord(wordId));
         }

         final String allWordHistories[] = inGraph.getWordHistories();
         dos.writeInt(allWordHistories.length);
         
         for (int historiesIndex = 0; historiesIndex < allWordHistories.length; historiesIndex++) {
            final String wordHistory = allWordHistories[historiesIndex];
            
            dos.writeUTF(wordHistory);
            
            final Transitions t = inGraph.getTransitions(wordHistory);
            
            final int numTransitions = t.getNumberTransitions();
            assert(numTransitions <= Character.MAX_VALUE);
            dos.writeChar(numTransitions);
             
            for(final TransitionProbability tp : t) {
               dos.writeChar(tp.targetWordId);
               dos.writeByte(tp.getNumerator());
               dos.writeByte(tp.getDenominator());
            }
         }
         
      } catch (final Exception anything) {
         anything.printStackTrace();
      } finally {
         if(dos != null) {
            try {
               dos.close();
            } catch(Throwable anything){}
         }
         System.out.println("Saving to "+filename+" took " + (System.currentTimeMillis() - startTime) + "ms");
      }
   }

   @Override
   public Graph load() {
      final long startTime = System.currentTimeMillis();
      
      final File file = new File(filename);
      
      if (!file.exists()) {
         return null;
      }
      
      DataInputStream dis = null; 

      try {
         dis = new DataInputStream(new FileInputStream(file));
         
         final char numWords = dis.readChar();
         final String uniqueAndSortedWords[] = new String[numWords];
         for (char wordId = 0; wordId < numWords; wordId++) {
            uniqueAndSortedWords[wordId] = dis.readUTF();
         }
         
         final DictionaryLookup dict = new DictionaryLookupBinarySearch(uniqueAndSortedWords);
         
         final int numWordHistories = dis.readInt();
         
         // Choose the hashmap's initial capacity to be enough for all the words (and include 
         // a little more spare capacity to cover the default load factor of 0.75).
         final int initialCapacity = (numWordHistories + (numWordHistories / 4));
         
         final Map<String, Transitions> wordHistoryToTransitions = new HashMap<String, Transitions>(initialCapacity);
         
         for (int historiesIndex = 0; historiesIndex < numWordHistories; historiesIndex++) {
            final Transitions t = new Transitions();
            
            wordHistoryToTransitions.put(dis.readUTF(), t);
            
            for(char stillToRead = dis.readChar(); stillToRead > 0; stillToRead--) {
               final char targetWordId = dis.readChar();
               final byte numerator = dis.readByte();
               final byte denominator = dis.readByte();
               
               t.addTransition(targetWordId, numerator, denominator, stillToRead > 1);
            }
         }
         
         final Graph reconstituted = new Graph(dict, wordHistoryToTransitions);
         
         System.out.println("Loading from "+filename+" took " + (System.currentTimeMillis() - startTime) + "ms");
         
         return reconstituted;
      } catch (final Exception anything) {
         anything.printStackTrace();
         return null;
      } finally {
         if(dis != null) {
            try {
               dis.close();
            } catch(Throwable anything){}
         }
      }
   }

   private Map<String, Transitions> loadHistoryTransitionsMap(final Statement stat) throws SQLException {
      final Map<String, Transitions> wordHistoryToTransitions = new HashMap<String, Transitions>();

      final String sql = "SELECT h.history, t.target_words_id, t.numerator, t.denominator "
            + "FROM histories h, transitions t " + "WHERE h.id = t.histories_id "
            + "ORDER BY h.history, t.sequence ASC;";

      final ResultSet rsTransitions = stat.executeQuery(sql);

      {
         String currentHistory = null;
         Transitions currentHistoryTransitions = null;
         boolean hasResultsLeft = rsTransitions.next();

         while (hasResultsLeft) {
            final String wordHistory = rsTransitions.getString("history");
            final char tagetWordId = (char) rsTransitions.getInt("target_words_id");
            final byte numerator = rsTransitions.getByte("numerator");
            final byte denominator = rsTransitions.getByte("denominator");

            if (!wordHistory.equals(currentHistory)) {
               currentHistory = wordHistory;
               currentHistoryTransitions = new Transitions();

               wordHistoryToTransitions.put(currentHistory, currentHistoryTransitions);
            }

            hasResultsLeft = rsTransitions.next();

            currentHistoryTransitions.addTransition(tagetWordId, numerator, denominator, !hasResultsLeft);
         }
      }

      rsTransitions.close();

      return wordHistoryToTransitions;
   }

   private DictionaryLookup loadDictionary(final Statement stat) throws SQLException {
      final LinkedList<String> wordList = new LinkedList<String>();

      final ResultSet rsWords = stat.executeQuery("SELECT id, word FROM words ORDER BY id ASC;");

      while (rsWords.next()) {
         assert (wordList.size() == rsWords.getInt("id"));

         wordList.add(rsWords.getString("word"));
      }

      rsWords.close();

      return new DictionaryLookupBinarySearch(wordList.toArray(new String[0]));
   }
}
