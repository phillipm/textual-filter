/**
 * Collect relevant information surrounding a word appearing in a body of text.
 */

import java.awt.Color;

public class Word {
  // TODO: getters/setters
  private final Color DEFAULT_COLOR = new Color(255, 255, 255);

  public int frequency;
  public String text;
  public Color color;

  public Word(String text, int freq) {
    this.frequency = freq;
    this.text = text;
    this.color = DEFAULT_COLOR;
  }

  public Word(String text, int freq, Color color) {
    this.frequency = freq;
    this.text = text;
    this.color = color;
  }
}
