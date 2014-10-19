package net.quined.textual_filters;

/**
 * Test basic functionality of importing text into a Corpus.
 *
 * @author Phillip Mates
 * @version 0.1
 */

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import java.util.Map;

public class CorpusTest {
  private Corpus c;

  @Before
  public void setUp() {
    c = new Corpus();
  }

  @After
  public void tearDown() {
    c = null;
  }

  /**
   * Since there is only one line, no newlines are added.
   * Punctuation is not thought of as individual words
   */
  @Test
  public void loadOneLineString() {
    String inputStr = "hello world, every word should only occur once.";
    c.loadText(inputStr);
    assertEquals(8, c.length());
    assertEquals(8, c.maxLineLength());
    assertEquals(1, c.newlineCount());
    for(Map.Entry<String, Integer> keyValuePair : c.wordFrequency().entrySet()) {
      assertEquals(1, (int) keyValuePair.getValue());
    }
    assertEquals(inputStr, c.getOriginalText());
    assertEquals(8, c.uniqueWordCount());
  }

  /**
   * Newlines are thought of as words.
   * Punctuation is not thought of as individual words
   */
  @Test
  public void loadMultiLineString() {
    String inputStr = "Hello world!\n"
                    + "If only there was someone to talk to.\n"
                    + "Is anyone there?";

    c.loadText(inputStr);
    assertEquals(15, c.length());
    assertEquals(9, c.maxLineLength());
    assertEquals(3, c.newlineCount());
    for(Map.Entry<String, Integer> keyValuePair : c.wordFrequency().entrySet()) {
      if (keyValuePair.getKey() != "\n") {
        assertEquals(1, (int) keyValuePair.getValue());
      }
    }
    assertEquals(inputStr, c.getOriginalText());
    assertEquals(14, c.uniqueWordCount());
  }
}
