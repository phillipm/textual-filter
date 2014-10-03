import java.awt.Color;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.GroupLayout.*;
import javax.swing.filechooser.*;
import javax.swing.Box;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

// for ImagePanel
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Gui extends JPanel implements ActionListener {
  private static final String DEFAULT_FILE_FIELD = "Choose a file to open or provide a new filename:";
  private JButton openButton;
  private JButton generateImageButton;
  private JButton interpretImageButton;
  private JFileChooser fc;
  private File file;

  private JScrollPane origTextScrollPane;
  private ImagePanel origImagePanel;
  private JTextArea origTextArea;

  private JScrollPane modTextScrollPane;
  private ImagePanel modImagePanel;
  private JTextArea modTextArea;

  private JTextField nameField;
  private String projectName;

  private Corpus corpus;
  private TextAsImage tai;

  final static Color  HILIT_COLOR = Color.LIGHT_GRAY;
  final static Color  ERROR_COLOR = Color.PINK;
  final static String CANCEL_ACTION = "cancel-search";

  public Gui() {
    super(new BorderLayout());
    initComponents();
    corpus = new Corpus();
  }

  /** This method is called from within the constructor to
   * initialize the form.
   */

  private void initComponents() {
    origTextArea = new JTextArea();
    modTextArea = new JTextArea();
    nameField = new JTextField();
    nameField.setText("default_project");

    JLabel nameLabel = new JLabel("Project name:", JLabel.LEFT);

    //Create a file chooser
    fc = new JFileChooser();

    openButton = new JButton("Open text file...");
    openButton.addActionListener(this);

    //For layout purposes, put the buttons in a separate panel
    JPanel filePanel = new JPanel(); //use FlowLayout
    filePanel.add(nameLabel);
    filePanel.add(nameField);
    filePanel.add(openButton);

    origTextArea.setColumns(50);
    origTextArea.setLineWrap(true);
    origTextArea.setRows(20);
    origTextArea.setWrapStyleWord(true);
    origTextArea.setEditable(true);

    modTextArea.setColumns(50);
    modTextArea.setLineWrap(true);
    modTextArea.setRows(20);
    modTextArea.setWrapStyleWord(true);
    modTextArea.setEditable(false);

    JPanel textPanel = new JPanel();

    Box origBox = Box.createHorizontalBox();
    generateImageButton = new JButton("Convert to Image");
    generateImageButton.addActionListener(this);
    origBox.add(generateImageButton);
    origTextScrollPane = new JScrollPane(origTextArea);
    origImagePanel = new ImagePanel();
    origImagePanel.setPreferredSize(new Dimension(256, 256));
    origBox.add(origTextScrollPane);
    origBox.add(origImagePanel);
    textPanel.add(origBox);

    Box modBox = Box.createHorizontalBox();
    interpretImageButton = new JButton("Load Filtered Image");
    interpretImageButton.addActionListener(this);
    modBox.add(interpretImageButton);
    modTextScrollPane = new JScrollPane(modTextArea);
    modImagePanel = new ImagePanel();
    modImagePanel.setPreferredSize(new Dimension(256, 256));
    modBox.add(modTextScrollPane);
    modBox.add(modImagePanel);
    textPanel.add(modBox);

    add(filePanel, BorderLayout.PAGE_START);
    add(textPanel, BorderLayout.CENTER);
  }

  public void actionPerformed(ActionEvent e) {
    // choosing a text file to open
    if (e.getSource() == openButton) {
      int returnVal = fc.showOpenDialog(Gui.this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        file = fc.getSelectedFile();
        try {
          InputStream in = new FileInputStream(file.getAbsolutePath());
          origTextArea.read(new InputStreamReader(in), null);
        } catch (IOException excep) {
          excep.printStackTrace();
        }
      }
    } else if (e.getSource() == generateImageButton) {
      // generate image from content in text field
      projectName = nameField.getText();
      corpus.loadText(origTextArea.getText());
      tai = new TextAsImage(corpus);
      BufferedImage img = tai.createImage();
      try {
        // save image
        File outputfile = new File(projectName+".bmp");
        ImageIO.write(img, "bmp", outputfile);
        // display image
        origImagePanel.setImage(img);
      } catch (IOException excep) {
        System.out.println("unable to write " + projectName  + ".bmp");
      }
    } else if (e.getSource() == interpretImageButton) {
      modImagePanel.loadImage(projectName+ "_filtered.bmp");
      String filteredText;
      try {
        Corpus c = tai.openImage(projectName+"_filtered.bmp");
        filteredText = c.getOriginalText();
      } catch (IOException excep) {
        filteredText = "error loading filtered text";
      }
      modTextArea.setText(filteredText);
    }
  }

  /**
   * Create the GUI and show it.  For thread safety,
   * this method should be invoked from the
   * event dispatch thread.
   */
  public static void createAndShowGUI() {
    //Create and set up the window.
    JFrame frame = new JFrame("FileChooserDemo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //Add content to the window.
    frame.add(new Gui());

    //Display the window.
    frame.pack();
    frame.setVisible(true);
  }
}

// For opening and drawing an image file onto the GUI
class ImagePanel extends JPanel {
    private BufferedImage image;

    public void setImage(BufferedImage img) {
      image = img;
      repaint();
    }

    public void loadImage (String filename) {
       try {
          image = ImageIO.read(new File(filename));
          repaint();
       } catch (IOException ex) {
            // handle exception...
       }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, 256, 256, null);
    }
}

