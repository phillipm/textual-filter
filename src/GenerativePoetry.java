import java.io.*;
import javax.swing.SwingUtilities;
import javax.swing.*;

public class GenerativePoetry {

  public static void main(String[] args) {
    //Schedule a job for the event dispatch thread:
    //creating and showing this application's GUI.
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        //Turn off metal's use of bold fonts
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        Gui.createAndShowGUI();
      }
    });
    /*
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
    */
  }
}
