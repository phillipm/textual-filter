package net.quined.textual_filters;

/**
 * A Corpus of text is an object for organizing a piece of text.
 * It includes stats and accessors for the text.
 *
 * @author Phillip Mates
 * @version 0.1
 */

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Corpus {
  // the corpus split by words
  private String[] words;

  private HashMap<String, Integer> wordHistogram = new HashMap<String, Integer>();

  // longest length of words between linebreaks
  private int longestLine;
  private int newlineCount;

  /**
   *
   */
  public Corpus() {
  }

  /**
   *
   */
  public Corpus(String filename) {
    loadFile(filename);
  }

  /**
   * Load a string and compute its word frequency histogram.
   *
   * @param text    String to set corpus text to
   * @since 0.1
   */
  public void loadText(String text) {
    InputStream is = new ByteArrayInputStream(text.getBytes());
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    processText(br);
  }

  /**
   * Load a text file and process it.
   *
   * @param filename    Name of file to open
   * @since 0.1
   */
  public void loadFile(String filename) {
    // projectName = filename.replaceFirst("[.][^.]+$", "");

    try {
      processText(new BufferedReader(new FileReader(filename)));
    } catch (IOException e) {
      System.out.format("couldn't open %s%n", filename);
    }
  }

  /**
   * Import a text from a BufferedReader file and build the word histogram for that text
   *
   * @param bufReader     reader with text to import into the corpus
   * @since 0.1
   */
  private void processText(BufferedReader bufReader) {
    // TODO: make is so this can only be called once per object instantiation
    ArrayList<String> currentWords = new ArrayList<String>();
    Scanner lineScanner = null;

    try {
      lineScanner = new Scanner(bufReader);

      String word;
      while (lineScanner.hasNextLine()) {
        int wordsOnLine = 0;
        Scanner wordScanner = new Scanner(lineScanner.nextLine());
        while (wordScanner.hasNext()) {
          word = wordScanner.next();
          currentWords.add(word);
          wordsOnLine++;

          // update word histogram
          addWordToHistogram(word);
        }
        this.newlineCount++;
        if (lineScanner.hasNextLine()) {
          // add a newline and count as a word
          wordsOnLine++;
          currentWords.add("\n");
          addWordToHistogram("\n");
        }
        if (wordsOnLine > this.longestLine) {
          longestLine = wordsOnLine;
        }
      }
    } finally {
      if (lineScanner != null) {
        lineScanner.close();
      }
    }
    // convert ArrayList of Strings into an immutable Array of Strings
    words = currentWords.toArray(new String[0]);
  }

  /**
   * The word count of a corpus
   *
   * @return    word count of a corpus
   * @since 0.1
   */
  public int length() {
    return words.length;
  }

  /**
   * The maximum number of words on a line
   *
   * @return    word count on the line with the most words
   * @since 0.1
   */
  public int maxLineLength() {
    return longestLine;
  }

  /**
   * Number of newlines in the corpus
   *
   * @return    newline count in text
   * @since 0.1
   */
  public int newlineCount() {
    return newlineCount;
  }

  /**
   * Provide access to a copy of the histogram of words in corpus.
   * This is a map from words to the number of their occurences
   *
   * @return    map from words to the times they occur
   * @since 0.1
   */
  public HashMap<String, Integer> wordFrequency() {
    return wordHistogram;
  }

  /**
   * Add a word or increment word occurence in histogram
   *
   * @param word that occurs in corpus
   * @since 0.1
   */
  private void addWordToHistogram(String word) {
    if (wordHistogram.containsKey(word)) {
      wordHistogram.put(word, wordHistogram.get(word) + 1);
    } else {
      wordHistogram.put(word, 1);
    }
  }


  /**
   * Generate the String representation of the original corpus
   *
   * @return    the corpus' text as a String
   * @since 0.1
   */
  public String getOriginalText() {
    StringBuffer result = new StringBuffer();

    result.append(words[0]);
    for (int i = 1; i < words.length; i++) {
      if (words[i-1] == "\n" || words[i] == "\n") {
        result.append(words[i]);
      } else {
        result.append(" " + words[i]);
      }
    }
    return result.toString();
  }

  /**
   * Count how many unique words are in the corpus. Dependent on how words are
   * partitions, which takes case-sensitivity and punctuation parameters into
   * account.
   *
   * @return    int representing how many unique words in the corpus' text
   * @since 0.1
   */
  public int uniqueWordCount() {
    return wordHistogram.size();
  }

  /**
   * Lookup the i-th word in the text
   *
   * @param index   int index into the text of a corpus
   * @return        the i-th word in the text
   * @since 0.1
   */
  public String getWord(int index) {
    return words[index];
  }
}
