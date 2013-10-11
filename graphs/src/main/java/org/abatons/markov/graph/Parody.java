package org.abatons.markov.graph;

import java.util.Random;

public class Parody {
  private final Graph graph;
  private final Random rng;

  private final StringBuilder currentWordHistory;

  public Parody(final Graph inGraph, final String inStartingWordHistory, final Random inRng) {
    this.graph = inGraph;
    this.rng = inRng;

    this.currentWordHistory = new StringBuilder(inStartingWordHistory);
  }

  public Parody(final Graph inGraph, final String inStartingWordHistory) {
    this(inGraph, inStartingWordHistory, new Random());
  }

  public Parody(final Graph inGraph) {
    this(inGraph, inGraph.getAWordHistoryForTesting());
  }

  public Parody(final Graph inGraph, final Random inRng) {
    this(inGraph, inGraph.getAWordHistoryForTesting(), inRng);
  }

  /**
   * <p>Gets the next word in the current parody.</p>
   * 
   * <p>Chooses a random chance and runs a roulette wheel selection algorithm to
   * select the next word from the ordered list of potential words following the
   * current word history.</p>
   * 
   * <p>This new selected word's ID is then appended to the word current word
   * history.</p>
   * 
   * @return The next word in the parody, or null if either of the two
   *         conditions occur:<br/>
   *         <ol>
   *         <li>If the current word history represents the very last sequence
   *         of words appearing in the entire original text, and this sequence
   *         of words is unique throughout the whole original text</li>
   *         <li>If the current word history represents a sequence of words that
   *         could never have appeared in the original text. i.e. The Parody
   *         object was instantiated with an unknown word history.</li>
   *         </ol>
   */
  public String getNextWord() {
    final Transitions transitions = this.graph.getTransitions(this.currentWordHistory.toString());

    final int numTargets = transitions.getNumberTransitions();

    if (numTargets < 1) {
      // There are no words following on from the this word history. This means
      // one of two things:
      // 1) This sequence of words never appeared in the original text (Parody
      // was instantiated with an unknown word history?)
      // or 2) This sequence of words is actually the last few words of the
      // entire original text!
      return null;
    }

    final int chosenChanceNumerator = rng.nextInt(numTargets);
    int culuativeNumerators = 0;
    for (final TransitionProbability t : transitions) {
      culuativeNumerators += t.getNumerator();
      if (culuativeNumerators > chosenChanceNumerator) {
        // We're choosing this target word for the next word in the sentence
        this.currentWordHistory.append(t.targetWordId);
        this.currentWordHistory.deleteCharAt(0);

        return this.graph.getDictionary().getWord(t.targetWordId);
      }
    }

    return null; // Fail
  }

  public String generateParody(final int inNumWords) {
    final StringBuilder parodySentences = new StringBuilder();
    for (int i = 0; i < inNumWords; i++) {
      parodySentences.append(getNextWord());
      parodySentences.append(" ");
    }

    return parodySentences.toString();
  }
}
