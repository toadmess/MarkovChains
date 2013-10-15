package org.abatons.markov.graph;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.abatons.markov.graph.dictionary.Dictionary;
import org.abatons.markov.graph.dictionary.DictionaryLookup;
import org.abatons.markov.graph.dictionary.DictionaryLookupBinarySearch;

public class GraphPersistenceRaw implements GraphPersistence {
   private final String filename;
   private final InputStream inputStream;
   private final OutputStream outputStream;

   public GraphPersistenceRaw(final String inFilename) {
      this.filename = inFilename;
      
      this.inputStream = null;
      this.outputStream = null;
   }

   public GraphPersistenceRaw(final InputStream inInputStream, final OutputStream inOutputStream) {
      this.filename = null;
      
      this.inputStream = inInputStream;
      this.outputStream = inOutputStream;
   }
   
   @Override
   public void save(final Graph inGraph) {
      final long startTime = System.currentTimeMillis();
      
      DataOutputStream dos = null;
      
      try {
         {
            final OutputStream outputStreamToUse;
            
            assert(this.filename != null || this.outputStream != null);
            if(this.outputStream != null) {
               outputStreamToUse = this.outputStream;
            } else {
               outputStreamToUse = new FileOutputStream(new File(this.filename));
            }
            
            dos = new DataOutputStream(outputStreamToUse);
         }
         
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
               dos.writeChar(tp.getNumerator());
               dos.writeChar(tp.getDenominator());
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
      
      DataInputStream dis = null; 
      try {
         {         
            final InputStream inputStreamToUse;
            
            assert(this.filename != null || this.inputStream != null);
            if(this.inputStream != null) {
               inputStreamToUse = this.inputStream;
            } else {
               final File file = new File(filename);
               
               if (!file.exists()) {
                  return null;
               }
               
               inputStreamToUse = new FileInputStream(file);
            }
            
            dis = new DataInputStream(inputStreamToUse);
         }
         
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
               final char numerator = dis.readChar();
               final char denominator = dis.readChar();
               
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
}
