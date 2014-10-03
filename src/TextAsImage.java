/**
 * Logic for interpreting a Corpus of text as an image.
 *
 * Uses a Corpus object to create a mapping from words to colors.
 * This mapping can be used to create images from text or
 * interpret images as text.
 *
 * @author Phillip Mates
 * @version 0.1
 */

import java.io.IOException;
import java.io.File;
import java.util.HashMap;
import java.awt.image.*;

// for writing images
import javax.imageio.*;

import java.awt.Color;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

// WordFreqEntryComparator
import java.util.Comparator;
import java.util.Map;
import java.util.Collections;

public class TextAsImage {
  /**
   * Constants
   */
  private final int DEFAULT_COLOR = new Color(255, 255, 255).getRGB();

  // maps words found in a text to a color value
  private HashMap<String, Integer> wordToColor = new HashMap<String, Integer>();
  // corpus of text that includes stats like lenght & word frequency.
  private Corpus corpus;


  /**
  * Image Creation Parameters
  */
  // should the images created contain a uniformly colored padding?
  private boolean toPadImage = false;
  // The padding size, in terms of the image's width
  private float padRatio = 0.2f;
  // should each new vertical line of pixels correspond to a new line from the text?
  private boolean syncNewlineAndVertical = false;

 // TODO: eventually remove
  private int imagePadding;
  // dimensions of the image that actually represent text
  private int textImageSize;

  /**
   * Image properties derived from text stats
   */
  // if we have less than 256 words, we need to map color ranges to unique words
  int colorGradient;
  // mask to eliminate parts of colors that don't have word mappings
  int indexMask;
  // size of the image: textImageSize + (2 * imagePadding)
  int imageSize = 100; // XXX

  /**
   * Sole constructor; loads corpus of text and builds word to color mapping
   * @param corpus      Corpus object containing text and statistical information
   *                    needed to create a mapping from words to colors
   */
  public TextAsImage(Corpus corpus) {
    this.corpus = corpus;
    // TODO: create word->color mapping

  // number of unique words in the corpus
    int uniqueWords = this.corpus.uniqueWords();
    // compute the index mask, so that we don't try to interpret colors as
    // indices if they lie out of the word range
    int shiftedWordCount = uniqueWords;
    while (shiftedWordCount > 0) {
      indexMask = (indexMask << 1) | 0x1;
      shiftedWordCount = shiftedWordCount >> 1;
    }

    // TODO: this will change depending on image creation flags
    textImageSize = (int) Math.ceil(Math.sqrt((double) this.corpus.length()));
    imagePadding = textImageSize / 4;
    imageSize = textImageSize + (2 * imagePadding);

    if (uniqueWords >= 256) {
      // When dealing with more than 256 unique words,
      // each color value (0-255) will be assigned a unique word.
      // Hence, we don't need to map ranges of color values to a given word.
      colorGradient = 1;
    } else {
      colorGradient = 256 / uniqueWords;
    }

    System.out.format("Colorgradient: %d%n", colorGradient);

    // build the map from words to pixel colors
    Set<Map.Entry<String, Integer>> histoSet = this.corpus.wordFrequency().entrySet();
    List<Map.Entry<String, Integer>> histoKVList =
      new ArrayList<Map.Entry<String, Integer>>(histoSet);
    Collections.sort(histoKVList, new WordFreqEntryComparator());

    // map each word to a distinct color
    if (colorGradient == 1) {
      // words won't fit in one hue (blue)
      for (int i = 0; i < uniqueWords; i++) {
        // XXX: creating a new color out of bit shifting and then converting it
        // to rgb might be a round about, that is, "i" might be that exact value...
        wordToColor.put(histoKVList.get(i).getKey(),
            new Color((i >> 16) & 0x11111111,
              (i >> 8) & 0x11111111,
              i & 0x11111111).getRGB());
      }
    } else {
      // words fit in one hue (blue), must use the cologradient to map color ranges
      // to a specific word
      for (int i = 0; i < uniqueWords; i++) {
        wordToColor.put(histoKVList.get(i).getKey(),
            new Color(0, 0, i * colorGradient).getRGB());
      }
    }

  }

  /**
   * Convert a color into a word from the internal Corpus text.
   *
   * @param rgb     numeric RGB reperesentation of a color
   * @return        a word from the corpus of text
   */
  private String colorToWord(Integer rgb) {
    if (rgb == DEFAULT_COLOR) {
      return "";
    }
    Color color = new Color(rgb);
    int index = 0;
    if (colorGradient == 1) {
      index = indexMask & ((color.getRed() << 16) |
                           (color.getGreen() << 8) |
                           color.getBlue());
    } else {
      index = indexMask & (color.getBlue() / colorGradient);
    }

    Set<Map.Entry<String, Integer>> histoSet = this.corpus.wordFrequency().entrySet();
    List<Map.Entry<String, Integer>> histoKVList =
      new ArrayList<Map.Entry<String, Integer>>(histoSet);
    Collections.sort(histoKVList, new WordFreqEntryComparator());

    if (index >= histoKVList.size()) {
      return histoKVList.get(histoKVList.size()-1).getKey();
    } else {
      return histoKVList.get(index).getKey();
    }
  }

  /**
   * Create an image that visually displays the word to color mapping.
   *
   * @return              BufferedImage showing words and colors they are mapped to
   */
  public BufferedImage createInfoImage() {
    // TODO
    int infoWidth = 100;
    int infoHeight = 100;
    return new BufferedImage(infoWidth, infoHeight, BufferedImage.TYPE_INT_RGB);
  }

  /**
   * Show what colors in an image are used when mapping an image to text. This
   * is done by masking out all the portions of colors that don't play a part
   * in the color-to-word mapping logic.
   *
   * @param image         An image that we want to interpret as text
   * @return              BufferedImage showing parts of colors that have word interpretations
   * @since 0.1
   */
  public BufferedImage createInterpretableImage(BufferedImage image) {
    // TODO
    return image;
  }

  /**
   * Builds an image from text in the private Corpus object, where each word in
   * that text is mapped to a pixel of a specific color.  Image dimensions and
   * pixel color are decided by properties set in the TextAsImage object.
   *
   * @return              BufferedImage built by visual interpretation of text
   * @since 0.1
   */
  public BufferedImage createImage() {
    // TODO
    BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);

    // build image
    int wordIndex = 0;
    int pixelCount = 0;
    for (int y = 0; y < imageSize; y++) {
      for (int x = 0; x < imageSize; x++) {
        if (x >= imagePadding &&
            y >= imagePadding &&
            x < (textImageSize + imagePadding) &&
            y < (textImageSize + imagePadding)) {
          wordIndex = ((x - imagePadding) + ((y - imagePadding) * textImageSize));
          if (wordIndex < this.corpus.length()) {
            image.setRGB(x, y, wordToColor.get(this.corpus.getWord(wordIndex)));
            pixelCount++;
          } else {
            // fill in the rest of the text image square with the default color
            image.setRGB(x, y, DEFAULT_COLOR);
          }
        } else {
          // fill in the image padding area with the default color
          image.setRGB(x, y, DEFAULT_COLOR);
        }
      }
    }

    System.out.format("drew %d pixels and there were %d words%n", pixelCount, this.corpus.length());

    return image;
    // write image
    // File outputfile = new File("foo.jpg"); // XXX
    // ImageIO.write(image, "bmp", outputfile);
  }

  /**
   * Using the internal color to word mapping this function interprets an image
   * as a piece of text. This piece of text is represented as a Corpus object.
   *
   * @param filename    Filename of image that will be interpreted as text
   * @return Corpus     Representation of text interpreted from the image
   * @throws IOException  If an input or output
   * @since 0.1
   */
  public Corpus openImage(String filename) throws IOException {
    File outputfile = new File(filename);
    BufferedImage loadedImage = ImageIO.read(outputfile);

    StringBuffer result = new StringBuffer();

    // build image
    for (int y = 0; y < loadedImage.getHeight(); y++) {
      for (int x = 0; x < loadedImage.getWidth(); x++) {
        result.append(colorToWord(loadedImage.getRGB(x, y)) + " ");
      }
    }
    Corpus c = new Corpus();
    c.loadText(result.toString());
    return c;
  }

  // TODO: move to some other file
  // Compare Map.Entry's based on the size of the integer value
  // used to sort words by their occurence frequencies.
  class WordFreqEntryComparator implements Comparator<Map.Entry<String, Integer>> {
    @Override
    public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
        return a.getValue().compareTo(b.getValue());
    }
  }
}
