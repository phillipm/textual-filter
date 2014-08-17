import java.io.*;
import java.awt.image.*;
import java.awt.Color;
import javax.imageio.*;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

public class Corpus {
  private final int DEFAULT_COLOR = new Color(255, 255, 255).getRGB();
  // size of the image: textImageSize + (2 * imagePadding)
  private int imageSize;
  // dimensions of the image that actually represent text
  private int textImageSize;
  private int imagePadding;
  // if we have less than 256 words, we need to map color ranges to unique words
  private int colorGradient;
  // number of unique words in the corpus
  private int uniqueWords;
  // mask to eliminate parts of colors that don't have word mappings
  private int indexMask;
  // the corpus split by words
  private String[] words;

  private String textFilename;
  private String origImageFilename;
  private String modImageFilename;
  // image created from the array of words
  private BufferedImage image;
  private HashMap<String, Integer> wordHistogram = new HashMap<String, Integer>();
  private HashMap<String, Integer> wordToPixelMap = new HashMap<String, Integer>();

  public Corpus() {
  }

  // create a corpus from a text file
  public Corpus(String filename) {
    load(filename);
  }

  public void load(String filename) {
    this.textFilename = filename + ".txt";
    this.origImageFilename = filename + ".bmp";
    this.modImageFilename = filename + "_filtered.bmp";

    try {
      openText();
    } catch (IOException e) {
      System.out.format("couldn't open %s%n", textFilename);
    }

    if (uniqueWords >= 256) {
      // When dealing with more than 256 unique words,
      // each color value (0-255) will be assigned a unique word.
      // Hence, we don't need to map ranges of color values to a given word.
      colorGradient = 1;
    } else {
      colorGradient = 256 / uniqueWords;
    }
    buildWordToPixelMap();

    textImageSize = (int) Math.ceil(Math.sqrt((double) words.length));
    imagePadding = textImageSize / 4;
    imageSize = textImageSize + (2 * imagePadding);
  }

  // read in a text file as a corpus, building word histogram
  private void openText() throws IOException {
    ArrayList<String> currentWords = new ArrayList<String>();
    Scanner s = null;

    try {
      s = new Scanner(new BufferedReader(new FileReader(textFilename)));

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
    uniqueWords = wordHistogram.size();

    // compute the index mask, so that we don't try to interpret colors as indices if they lie out of the word range
    int shiftedWordCount = uniqueWords;
    while (shiftedWordCount > 0) {
      indexMask = (indexMask << 1) | 0x1;
      shiftedWordCount = shiftedWordCount >> 1;
    }
  }

  // open an image for interpretation as a textual corpus
  public void openImage(String filename) throws IOException {
    BufferedImage img;
    try {
      img = ImageIO.read(new File(filename));
    } catch (IOException e) {
      // TODO
    }
  }

  // serialize corpus into a bitmap image using the word-to-pixel map
  public void createImage() throws IOException {
    image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);

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
          if (wordIndex < words.length) {
            image.setRGB(x, y, wordToColor(words[wordIndex]));
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

    System.out.format("drew %d pixels and there were %d words%n", pixelCount, words.length);

    // write image
    File outputfile = new File(origImageFilename);
    ImageIO.write(image, "bmp", outputfile);
  }

  // map a bitmap image to text using the word-to-pixel map
  public String imageToText() throws IOException {
    File outputfile = new File(modImageFilename);
    BufferedImage loadedImage = ImageIO.read(outputfile);

    StringBuffer result = new StringBuffer();

    // build image
    for (int y = 0; y < imageSize; y++) {
      for (int x = 0; x < imageSize; x++) {
        result.append(colorToWord(loadedImage.getRGB(x, y)) + " ");
      }
    }
    return result.toString();
  }

  // return the rgb int value that has been assigned to a word in the corpus
  public int wordToColor(String word) {
    return wordToPixelMap.get(word);
  }


  // converts an rgb color to a word
  public String colorToWord(int rgb) {
    if (rgb == DEFAULT_COLOR) {
      return "";
    }
    Color color = new Color(rgb);
    int index = 0;
    if (colorGradient == 1) {
      index = indexMask & ((color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue());
    } else {
      index = color.getBlue() / colorGradient;
    }

    Set<Map.Entry<String, Integer>> histoSet = wordHistogram.entrySet();
    List<Map.Entry<String, Integer>> histoKVList = new ArrayList<Map.Entry<String, Integer>>(histoSet);
    Collections.sort(histoKVList, new WordFreqEntryComparator());

    return histoKVList.get(index).getKey();
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

  // create a mapping from words to pixels so that an image can be created
  public HashMap<String, Integer> buildWordToPixelMap() {
    // return word to pixel color map if it has already been generated
    if (wordToPixelMap.size() > 0) {
      return wordToPixelMap;
    }

    // build the map from words to pixel colors
    Set<Map.Entry<String, Integer>> histoSet = wordHistogram.entrySet();
    List<Map.Entry<String, Integer>> histoKVList = new ArrayList<Map.Entry<String, Integer>>(histoSet);
    Collections.sort(histoKVList, new WordFreqEntryComparator());

    // map each word to a distinct color
    if (colorGradient == 1) {
      // words won't fit in one hue (blue)
      for (int i = 0; i < uniqueWords; i++) {
        // XXX: creating a new color out of bit shifting and then converting it to rgb might be a round about,
        //      that is, "i" might be that exact value...
        wordToPixelMap.put(histoKVList.get(i).getKey(), new Color((i >> 16) & 0x11111111, (i >> 8) & 0x11111111, i & 0x11111111).getRGB());
      }
    } else {
      // words fit in one hue (blue), must use the cologradient to map color ranges to a specific word
      for (int i = 0; i < uniqueWords; i++) {
        wordToPixelMap.put(histoKVList.get(i).getKey(), new Color(0, 0, i * colorGradient).getRGB());
      }
    }

    return wordToPixelMap;
  }

  // Compare Map.Entry's based on the size of the integer value
  // used to sort words by their occurence frequencies.
  class WordFreqEntryComparator implements Comparator<Map.Entry<String, Integer>> {
    @Override
    public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
        return a.getValue().compareTo(b.getValue());
    }
  }

  // return a string representation of the original corpus
  public String getOriginalText() {
    StringBuffer result = new StringBuffer();

    for (int i = 0; i < words.length; i++) {
      result.append(words[i] + " ");
    }
    return result.toString();
  }
}
