import java.io.*;

public class GenerativePoetry {

  public static void main(String[] args) {
    System.out.println(args.length);
    for (String arg : args) {
      System.out.format("%s, ", arg);
    }
    System.out.format("%n");
    Corpus c = new Corpus(args[0]);
    try {
      if (args[1].equals("create")) {
        c.createImage();
        System.out.println("created " + args[0] + ".bmp");
      } else if (args[1].equals("load")) {
        c.openImage(args[0] + ".bmp");
        System.out.println("Original Text");
        System.out.println(c.getOriginalText());
        String alteredText = c.imageToText();
        System.out.println("Filtered Text");
        System.out.println(alteredText);
      }
    } catch (IOException e) {
      System.out.println("Problem reading or writing" + args[0] + ".txt/bmp");
    }
  }
}
