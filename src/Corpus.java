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
  private int imageSize;
  private int colorGradient;
  private String[] words;
  private String textFilename;
  private String imageFilename;
  private BufferedImage image;
  private HashMap<String, Integer> wordHistogram = new HashMap<String, Integer>();
  private HashMap<String, Integer> wordToPixelMap = new HashMap<String, Integer>();

  // create a corpus from a text file
  public Corpus(String filename) {
    this.textFilename = filename + ".txt";
    this.imageFilename = filename + ".bmp";

    try {
      openText();
      colorGradient = 256 / uniqueWords(); // XXX: assumes uniqueWords is less than 256
      buildWordToPixelMap();

      imageSize = (int) Math.ceil(Math.sqrt((double) words.length));
    } catch (IOException e) {
      System.out.format("couldn't open %s%n", textFilename);
    }
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
    for (int y = 0; y < imageSize; y++) {
      for (int x = 0; x < imageSize; x++) {
        wordIndex = x + (y * imageSize);
        if (wordIndex < words.length) {
          image.setRGB(x, y, wordColor(words[wordIndex]));
        } else {
          image.setRGB(x, y, DEFAULT_COLOR);
        }
      }
    }

    // write image
    File outputfile = new File(imageFilename);
    ImageIO.write(image, "bmp", outputfile);
  }

  // map a bitmap image to text using the word-to-pixel map
  public String imageToText() throws IOException {
    File outputfile = new File(imageFilename);
    BufferedImage loadedImage = ImageIO.read(outputfile);

    StringBuffer result = new StringBuffer();

    // build image
    int index;
    for (int y = 0; y < imageSize; y++) {
      for (int x = 0; x < imageSize; x++) {
        index = loadedImage.getRGB(x, y)/colorGradient;

        result.append(colorToWord(loadedImage.getRGB(x, y)) + " ");
      }
    }
    return result.toString();
  }

  // converts an rgb color to a word
  public String colorToWord(int rgb) {
    Color color = new Color(rgb);
    // TODO: round up or down if it is not exact color
    int index = color.getBlue() / colorGradient;

    Set<Map.Entry<String, Integer>> histoSet = wordHistogram.entrySet();
    List<Map.Entry<String, Integer>> histoKVList = new ArrayList<Map.Entry<String, Integer>>(histoSet);
    Collections.sort(histoKVList, new WordFreqEntryComparator());

    return histoKVList.get(index).getKey();
  }

  // return number of words in a corpus
  public int length() {
    return words.length;
  }

  // how many unique words occur in the corpus
  public int uniqueWords() {
    return wordHistogram.size();
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
    for (int i = 0; i < histoKVList.size(); i++) {
      wordToPixelMap.put(histoKVList.get(i).getKey(), new Color(0, 0, i * colorGradient).getRGB());
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

  // return the rgb int value that has been assigned to a word in the corpus
  public int wordColor(String word) {
    return wordToPixelMap.get(word);
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
