/*
* Project 2
* Tony Le and Khoa Tra
* 2/7/22
*/
package packages;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.Object.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

public class readImage {
  int imageCount = 1;
  double intensityBins[] = new double[26];
  double intensityMatrix[][] = new double[101][26];
  double colorCodeBins[] = new double[64];
  double colorCodeMatrix[][] = new double[101][64];
  double imageSizeList[] = new double[101];

  /*
   * Each image is retrieved from the file. The height and width are found for the
   * image and the getIntensity and
   * getColorCode methods are called.
   */
  public readImage() {
    BufferedImage image = null;
    while (imageCount < 101) {
      try {
        // reading in images, using imageIO.
        String filename = "images/" + imageCount + ".jpg";
        image = ImageIO.read(new File(filename));
        // initalizing the intensityBins.
        for (int i = 1; i < 26; i++) {
          intensityBins[i] = 0;
        }
        // initalizing the colorCodeBins.
        for (int i = 0; i < 64; i++) {
          colorCodeBins[i] = 0;
        }
        // getting width and height of the image to get imageSize
        int width = image.getWidth();
        int height = image.getHeight();
        // the image width and height is stored inside array
        imageSizeList[imageCount] = width * height;
        // get insenity and colorCode from the image, height, and width
        getIntensity(image, height, width);
        getColorCode(image, height, width);
        // increment imageCount
        imageCount++;
      } catch (IOException e) {
        System.out.println("Error occurred when reading the file.");
      }

    }

    writeIntensity(); // writes the intensity file
    writeColorCode(); // writes the color code file
    writeSize(); // writes the file size file
  }

  // intensity method
  public void getIntensity(BufferedImage image, int height, int width) {
    // i represent rows
    for (int i = 0; i < height; i++) {
      // j represents the column
      for (int j = 0; j < width; j++) {
        // Color object c gets image's rgb
        Color c = new Color(image.getRGB(j, i));
        int red = c.getRed();
        int green = c.getGreen();
        int blue = c.getBlue();
        // (I = 0.299R + 0.587G + 0.114B)
        double intensity = (0.299 * (double) red + 0.587 * (double) green
            + 0.114 * (double) blue);
        // Histogram bins from 0-240 is increments by 1 every 10, and 240-255 increments
        // 1 for 15.
        if (intensity >= 0 && intensity < 240) {
          intensityBins[(int) (intensity / 10) + 1]++;
        } else if (intensity >= 240 && intensity < 255) {
          intensityBins[25]++;
        }
      }
    }
    // Puts the intensityBins into intensityMatrix 2d array
    for (int i = 1; i < 26; i++) {
      intensityMatrix[imageCount][i] = intensityBins[i];
    }

  }

  // color code method
  public void getColorCode(BufferedImage image, int height, int width) {
    // i represent rows
    for (int i = 0; i < height; i++) {
      // j represents the column
      for (int j = 0; j < width; j++) {
        // Color object c gets image's rgb
        Color c = new Color(image.getRGB(j, i));
        // color code equals the codebit
        int code = getCodeBit(c);
        // increments bin count by 1 for that color code
        if (code >= 0 && code < 64) {
          colorCodeBins[code]++;
        }
      }
    }
    // Puts the colorCodeBins into colorCodeMatrix 2darray
    for (int i = 0; i < 64; i++) {
      colorCodeMatrix[imageCount][i] = colorCodeBins[i];
    }
  }

  /*
   * Getting the two most leftmost digits from RGB
   * and merging into 6 digit binary representation
   */
  private int getCodeBit(Color c) {
    int red = c.getRed();
    int green = c.getGreen();
    int blue = c.getBlue();

    String colorCode = "";

    int bit = getBit(red, 7);
    colorCode += bit;
    bit = getBit(red, 6);
    colorCode += bit;
    bit = getBit(green, 7);
    colorCode += bit;
    bit = getBit(green, 6);
    colorCode += bit;
    bit = getBit(blue, 7);
    colorCode += bit;
    bit = getBit(blue, 6);
    colorCode += bit;

    int base = 2;
    // parses the colorCode, which is string, into an int, base is the radix
    int code = Integer.parseInt(colorCode, base);
    return code;
  }

  // gets bit, right shift by index
  private int getBit(int number, int i) {
    return (number >> i) & 1;
  }

  // This method writes the contents of the colorCode matrix to a file named
  // colorCodes.txt.
  public void writeColorCode() {
    try {

      FileWriter fstream = new FileWriter("colorCodes.txt", true);

      BufferedWriter out = new BufferedWriter(fstream);

      for (int i = 1; i < 101; i++) {
        for (int j = 0; j < 64; j++) {
          out.write(colorCodeMatrix[i][j] + " ");
        }
        out.newLine();
      }

      out.close();
    } catch (IOException e) {
      System.out.print("Error occurred when creating the file.");
    }
  }

  // This method writes the contents of the intensity matrix to a file called
  // intensity.txt
  public void writeIntensity() {
    try {

      FileWriter fstream = new FileWriter("intensity.txt", true);

      BufferedWriter out = new BufferedWriter(fstream);

      for (int i = 1; i < 101; i++) {
        for (int j = 1; j < 26; j++) {
          out.write(intensityMatrix[i][j] + " ");
        }
        out.newLine();
      }

      out.close();
    } catch (IOException e) {
      System.out.print("Error occurred when creating the file.");
    }
  }

  // This method writes the contents of the ImageSizeList to a file called
  // fileSize.txt
  public void writeSize() {
    try {

      FileWriter fstream = new FileWriter("fileSize.txt", true);

      BufferedWriter out = new BufferedWriter(fstream);

      for (int i = 1; i < 101; i++) {
        out.write("" + imageSizeList[i]);
        out.newLine();
      }

      out.close();
    } catch (IOException e) {
      System.out.print("Error occurred when creating the file.");
    }
  }
}