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

  private String projectName;

  private String textFilename;
  private HashMap<String, Integer> wordHistogram = new HashMap<String, Integer>();

  public Corpus() {
  }

  // create a corpus from a text file
  public Corpus(String filename) {
    loadFile(filename);
  }

  public void loadText(String text) {
    InputStream is = new ByteArrayInputStream(text.getBytes());
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    processText(br);
  }

  public void loadFile(String filename) {
    textFilename = filename;
    projectName = filename.replaceFirst("[.][^.]+$", "");

    try {
      processText(new BufferedReader(new FileReader(textFilename)));
    } catch (IOException e) {
      System.out.format("couldn't open %s%n", textFilename);
    }
  }

  // read in a text file as a corpus, building word histogram
  private void processText(BufferedReader bufReader) {
    ArrayList<String> currentWords = new ArrayList<String>();
    Scanner s = null;

    try {
      s = new Scanner(bufReader);

      String word;
      while (s.hasNext()) {
        word = s.next();
        currentWords.add(word);

        // update word histogram
        boolean flag = wordHistogram.containsKey(word);
        if (flag) {
          wordHistogram.put(word, wordHistogram.get(word) + 1);
        } else {
          wordHistogram.put(word, 1);
        }

      }
    } finally {
      if (s != null) {
        s.close();
      }
    }
    words = currentWords.toArray(new String[0]);
  }

  // return number of words in a corpus
  public int length() {
    return words.length;
  }

  // return a histogram of words in corpus, that is,
  // a map from words to the number of their occurences
  public HashMap<String, Integer> wordFrequency() {
    return wordHistogram;
  }

  // return a string representation of the original corpus
  public String getOriginalText() {
    StringBuffer result = new StringBuffer();

    for (int i = 0; i < words.length; i++) {
      result.append(words[i] + " ");
    }
    return result.toString();
  }

  /**
   * Count how many unique words are in the corpus. Dependent on case-sensitive
   * and punctuation parameter.
   *
   * @return    int representing how many unique words in the corpus' text
   * @since 0.1
   */
  public int uniqueWords() {
    return wordHistogram.size();
  }

  public String getWord(int index) {
    return words[index];
  }
}
