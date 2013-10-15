package org.abatons.markov.graph;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.abatons.markov.graph.dictionary.Dictionary;
import org.abatons.markov.graph.dictionary.DictionaryLookup;
import org.abatons.markov.graph.dictionary.DictionaryLookupBinarySearch;

public class GraphPersistenceSqlite implements GraphPersistence {
   private final String filename;

   public GraphPersistenceSqlite(final String inFilename) {
      this.filename = inFilename;
   }

   @Override
   public void save(final Graph inGraph) {
      final long startTime = System.currentTimeMillis();
      
      try {
         final Connection conn = getConnection(filename);

         createTables(conn);
         conn.commit();

         final PreparedStatement intoGraph = conn
               .prepareStatement("INSERT INTO histories (id, history) VALUES (?, ?);");
         final PreparedStatement intoTransitions = conn
               .prepareStatement("INSERT INTO transitions (histories_id, target_words_id, sequence, numerator, denominator) VALUES (?, ?, ?, ?, ?);");
         final PreparedStatement intoWord = conn
               .prepareStatement("INSERT INTO words (id, word) VALUES (?, ?);");

         final Dictionary dict = inGraph.getDictionary();

         final int numWords = dict.getNumUniqueWords();
         for (char wordId = 0; wordId < numWords; wordId++) {
            final String word = dict.getWord(wordId);

            intoWord.setInt(1, wordId); // column id
            intoWord.setString(2, word); // column word
            intoWord.addBatch();
         }

         final String allWordHistories[] = inGraph.getWordHistories();
         for (int historiesId = 0; historiesId < allWordHistories.length; historiesId++) {
            final String wordHistory = allWordHistories[historiesId];

            intoGraph.setInt(1, historiesId); // column id
            intoGraph.setString(2, wordHistory); // column history
            intoGraph.addBatch();

            final Iterator<TransitionProbability> it = inGraph.getTransitions(wordHistory).iterator();
            for (int sequence = 0; it.hasNext(); sequence++) {
               final TransitionProbability tp = it.next();

               intoTransitions.setInt(1, historiesId); // column histories_id
               intoTransitions.setInt(2, tp.targetWordId); // column words_id
               intoTransitions.setInt(3, sequence); // column sequence
               intoTransitions.setInt(4, tp.getNumerator()); // column numerator
               intoTransitions.setInt(5, tp.getDenominator()); // column denominator
               intoTransitions.addBatch();
            }
         }

         intoGraph.executeBatch();
         intoWord.executeBatch();
         intoTransitions.executeBatch();

         conn.commit();

         conn.close();
         
         System.out.println("Saving to "+filename+" took " + (System.currentTimeMillis() - startTime) + "ms");
      } catch (final Exception anything) {
         anything.printStackTrace();
      }
   }

   private void createTables(final Connection conn) throws SQLException {
      final Statement stat = conn.createStatement();

      stat.executeUpdate("DROP TABLE IF EXISTS histories;");
      stat.executeUpdate("DROP TABLE IF EXISTS transitions;");
      stat.executeUpdate("DROP TABLE IF EXISTS words;");

      stat.executeUpdate("CREATE TABLE histories (" + "  id INTEGER PRIMARY KEY, "
            + "  history STRING NOT NULL UNIQUE " + ");");

      stat.executeUpdate("CREATE TABLE transitions (" + "  id INTEGER PRIMARY KEY ASC AUTOINCREMENT, "
            + "  histories_id INTEGER NOT NULL, " + "  target_words_id INTEGER NOT NULL, "
            + "  sequence INTEGER NOT NULL, " + "  numerator INTEGER NOT NULL, "
            + "  denominator INTEGER NOT NULL, " + "  FOREIGN KEY (histories_id) REFERENCES histories (id), "
            + "  FOREIGN KEY (target_words_id) REFERENCES words (id) " + ");");

      stat.executeUpdate("CREATE TABLE words (" + "  id INTEGER PRIMARY KEY, "
            + "  word TEXT NOT NULL UNIQUE" + ");");
   }

   private Connection getConnection(final String filename) throws ClassNotFoundException, SQLException {
      Class.forName("org.sqlite.JDBC");

      final Connection conn = DriverManager.getConnection("jdbc:sqlite:" + filename);

      conn.setAutoCommit(false);
      conn.createStatement().executeUpdate("PRAGMA foreign_keys = ON;");
      conn.commit();

      return conn;
   }

   @Override
   public Graph load() {
      final long startTime = System.currentTimeMillis();
      
      if (!(new File(filename)).exists()) {
         return null;
      }

      try {
         final Connection conn = getConnection(filename);

         conn.setAutoCommit(true);

         final Statement stat = conn.createStatement();

         final DictionaryLookup dict = loadDictionary(stat);

         final Map<String, Transitions> wordHistoryToTransitions = loadHistoryTransitionsMap(stat);

         conn.close();

         final Graph reconstituted = new Graph(dict, wordHistoryToTransitions);
         
         System.out.println("Loading from "+filename+" took " + (System.currentTimeMillis() - startTime) + "ms");
         
         return reconstituted;
      } catch (final Exception whatever) {
         return null;
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
            final char numerator = (char) rsTransitions.getInt("numerator");
            final char denominator = (char) rsTransitions.getInt("denominator");

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
