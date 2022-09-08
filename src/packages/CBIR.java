package packages;

/*
* Project 2
* Tony Le and Khoa Tra
* 2/7/22
*/
import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import static javax.swing.UIManager.getColor;
import java.lang.Object;
import java.math.RoundingMode;
import java.text.Format;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import javax.swing.*;
import packages.readImage;

public class CBIR extends JFrame {

	private JLabel photographLabel = new JLabel(); // container to hold a large
	private JButton[] button; // creates an array of JButtons
	private JCheckBox[] checkBox;
	private int[] buttonOrder = new int[101]; // creates an array to keep up with the image order
	private double[] imageSize = new double[101]; // keeps up with the image sizes
	private GridLayout gridLayout1;
	private GridLayout gridLayout2;
	private GridLayout gridLayout3;
	private GridLayout gridLayout4;
	private JPanel panelBottom1;
	private JPanel panelBottom2;
	private JPanel panelTop;
	private JPanel buttonPanel;
	private JCheckBox relevant;
	private boolean isRelevant = false;
	private Double[][] intensityMatrix = new Double[101][26];
	private Double[][] colorCodeMatrix = new Double[101][64];
	private Double[][] colorCodeIntensityMatrix = new Double[101][89];
	private Double[] weightArr = new Double[89];
	private ArrayList<Integer> relevantPics = new ArrayList<Integer>();

	private TreeMap<Double, LinkedList<Integer>> map; // tree map because keys are sorted
	int picNo = 0;
	int imageCount = 1; // keeps up with the number of images displayed since the first page.
	int pageNo = 1;

	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new readImage();
				CBIR app = new CBIR();
				app.setVisible(true);
			}
		});
	}

	public CBIR() {
		// The following lines set up the interface including the layout of the buttons
		// and JPanels.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("CBIR");
		panelBottom1 = new JPanel();
		panelBottom2 = new JPanel();
		panelTop = new JPanel();
		buttonPanel = new JPanel();
		gridLayout1 = new GridLayout(4, 5, 5, 5);
		gridLayout2 = new GridLayout(2, 1, 5, 5);
		gridLayout3 = new GridLayout(1, 2, 5, 5);
		gridLayout4 = new GridLayout(5, 1, 5, 5);
		setLayout(gridLayout2);
		panelBottom1.setLayout(gridLayout1);
		panelBottom2.setLayout(gridLayout1);
		panelTop.setLayout(gridLayout3);
		add(panelTop);
		add(panelBottom1);
		photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
		photographLabel.setHorizontalTextPosition(JLabel.CENTER);
		photographLabel.setHorizontalAlignment(JLabel.CENTER);
		photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonPanel.setLayout(gridLayout4);
		panelTop.add(photographLabel);

		panelTop.add(buttonPanel);
		JButton previousPage = new JButton("Previous Page");
		JButton nextPage = new JButton("Next Page");
		JButton intensity = new JButton("Intensity");
		JButton colorCode = new JButton("Color Code");
		JButton Reset = new JButton("Reset");
		JButton ColorCodeAndIntensity = new JButton("Color Code & Intensity");
		relevant = new JCheckBox("Relevant");
		relevant.setVisible(false);
		buttonPanel.add(previousPage);
		buttonPanel.add(nextPage);
		buttonPanel.add(intensity);
		buttonPanel.add(colorCode);
		buttonPanel.add(ColorCodeAndIntensity);
		buttonPanel.add(Reset);
		buttonPanel.add(relevant);

		nextPage.addActionListener(new nextPageHandler());
		previousPage.addActionListener(new previousPageHandler());
		intensity.addActionListener(new intensityHandler());
		colorCode.addActionListener(new colorCodeHandler());
		ColorCodeAndIntensity.addActionListener((new colorCodeIntensityHandler()));
		relevant.addItemListener(new checkBoxHandler());
		Reset.addActionListener(new ResetHandler());
		setSize(1100, 750);
		// this centers the frame on the screen
		setLocationRelativeTo(null);

		button = new JButton[101];
		checkBox = new JCheckBox[101];
		/*
		 * This for loop goes through the images in the database and stores them as
		 * icons and adds
		 * the images to JButtons and then to the JButton array
		 */
		for (int i = 1; i < 101; i++) {
			ImageIcon icon;
			icon = new ImageIcon("images/" + i + ".jpg");
			if (icon != null) {
				ImageIcon scaledIcon = new ImageIcon(icon.getImage().getScaledInstance(200, 70,
						java.awt.Image.SCALE_SMOOTH));
				button[i] = new JButton(scaledIcon);
				button[i].setIcon(scaledIcon);
				button[i].setText("" + i);
				button[i].setFont(new Font("lato", Font.PLAIN, 20));
				button[i].setHorizontalTextPosition(SwingConstants.CENTER);
				button[i].setForeground(Color.BLACK);
				button[i].addActionListener(new IconButtonHandler(i, icon));
				buttonOrder[i] = i;
				checkBox[i] = new JCheckBox();
				checkBox[i].setIcon(scaledIcon);
				checkBox[i].setText("" + i);
				checkBox[i].setFont(new Font("lato", Font.PLAIN, 20));
				checkBox[i].setHorizontalTextPosition(SwingConstants.CENTER);
				checkBox[i].setForeground(Color.BLACK);
				checkBox[i].addItemListener(new checkBoxHandler());
			}
		}
		for (int i = 0; i < 89; i++) {
			weightArr[i] = (double) 1.0 / 89;
		}
		readIntensityFile();
		readColorCodeFile();
		readFileSize();
		normalizeimages();
		displayFirstPage();
	}

	/*
	 * This method opens the intensity text file containing the intensity matrix
	 * with the histogram bin values for each image.
	 * The contents of the matrix are processed and stored in a two dimensional
	 * array called intensityMatrix.
	 */
	public void readIntensityFile() {
		Scanner read;
		Double intensityBin;
		try {
			read = new Scanner(new File("intensity.txt"));
			for (int i = 1; i < 101; i++) {
				for (int j = 1; j < 26; j++) {
					if (read.hasNext()) {
						intensityBin = read.nextDouble();
						intensityMatrix[i][j] = intensityBin;
					}
				}
			}
		} catch (FileNotFoundException EE) {
			System.out.println("The file intensity.txt does not exist");
		}
	}

	/*
	 * This method opens the color code text file containing the color code matrix
	 * with the histogram bin values for each image.
	 * The contents of the matrix are processed and stored in a two dimensional
	 * array called colorCodeMatrix.
	 */
	private void readColorCodeFile() {
		Scanner read;
		Double colorCodeBin;

		try {
			read = new Scanner(new File("colorCodes.txt"));
			for (int i = 1; i < 101; i++) {
				for (int j = 0; j < 64; j++) {
					if (read.hasNext()) {
						colorCodeBin = read.nextDouble();
						colorCodeMatrix[i][j] = colorCodeBin;
					}
				}
			}
		} catch (FileNotFoundException EE) {
			System.out.println("The file colorCodes.txt does not exist");
		}

	}

	/*
	 * This method opens the FileSize to read the image sizes for each file
	 * The size is stored in an array called imageSize
	 */
	private void readFileSize() {
		Scanner read;
		Double currentImageSize;
		try {
			read = new Scanner(new File("fileSize.txt"));
			for (int i = 1; i < 101; i++) {
				if (read.hasNext()) {
					currentImageSize = read.nextDouble();
					imageSize[i] = currentImageSize;
				}

			}
		} catch (FileNotFoundException EE) {
			System.out.println("The file colorCodes.txt does not exist");
		}

	}

	/*
	 * This method displays the first twenty images in the panelBottom. The for loop
	 * starts at number one and gets the image
	 * number stored in the buttonOrder array and assigns the value to imageButNo.
	 * The button associated with the image is
	 * then added to panelBottom1. The for loop continues this process until twenty
	 * images are displayed in the panelBottom1
	 */
	private void displayFirstPage() {
		int imageButNo = 0;
		panelBottom1.removeAll();
		for (int i = 1; i < 21; i++) {
			// System.out.println(button[i]);
			imageButNo = buttonOrder[i];
			if (isRelevant) {
				panelBottom1.add(checkBox[imageButNo]);
			} else {
				panelBottom1.add(button[imageButNo]);
			}
			imageCount++;
		}
		panelBottom1.revalidate();
		panelBottom1.repaint();

	}

	/*
	 * This class implements an ActionListener for each iconButton. When an icon
	 * button is clicked, the image on the
	 * the button is added to the photographLabel and the picNo is set to the image
	 * number selected and being displayed.
	 */
	private class IconButtonHandler implements ActionListener {
		int pNo = 0;
		ImageIcon iconUsed;

		IconButtonHandler(int i, ImageIcon j) {
			pNo = i;
			iconUsed = j; // sets the icon to the one used in the button
		}

		public void actionPerformed(ActionEvent e) {
			photographLabel.setIcon(iconUsed);
			picNo = pNo;
		}

	}

	/*
	 * This class implements an ActionListener for the nextPageButton. The last
	 * image number to be displayed is set to the
	 * current image count plus 20. If the endImage number equals 101, then the next
	 * page button does not display any new
	 * images because there are only 100 images to be displayed. The first picture
	 * on the next page is the image located in
	 * the buttonOrder array at the imageCount
	 */
	private class nextPageHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			int imageButNo = 0;
			int endImage = imageCount + 20;
			if (endImage <= 101) {
				panelBottom1.removeAll();
				for (int i = imageCount; i < endImage; i++) {
					imageButNo = buttonOrder[i];
					if (isRelevant) {
						panelBottom1.add(checkBox[imageButNo]);
					} else {
						panelBottom1.add(button[imageButNo]);
					}
					imageCount++;
				}
				panelBottom1.revalidate();
				panelBottom1.repaint();
			}
		}

	}

	/*
	 * This class implements an ActionListener for the previousPageButton. The last
	 * image number to be displayed is set to the
	 * current image count minus 40. If the endImage number is less than 1, then the
	 * previous page button does not display any new
	 * images because the starting image is 1. The first picture on the next page is
	 * the image located in
	 * the buttonOrder array at the imageCount
	 */
	private class previousPageHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			int imageButNo = 0;
			int startImage = imageCount - 40;
			int endImage = imageCount - 20;
			if (startImage >= 1) {
				panelBottom1.removeAll();
				/*
				 * The for loop goes through the buttonOrder array starting with the startImage
				 * value
				 * and retrieves the image at that place and then adds the button to the
				 * panelBottom1.
				 */
				for (int i = startImage; i < endImage; i++) {
					imageButNo = buttonOrder[i];
					if (isRelevant) {
						panelBottom1.add(checkBox[imageButNo]);
					} else {
						panelBottom1.add(button[imageButNo]);
					}
					imageCount--;
				}
				panelBottom1.revalidate();
				panelBottom1.repaint();
			}
		}
	}

	private class ResetHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			setTitle("CBIR");
			int imageButNo = 0;
			panelBottom1.removeAll();
			photographLabel.setIcon(null);
			setAllChecked(false);
			for (int i = 1; i < 101; i++) {
				buttonOrder[i] = i;
			}
			for (int i = 1; i < 21; i++) {
				imageButNo = buttonOrder[i];
				panelBottom1.add(button[imageButNo]);
				imageCount++;
			}
			relevant.setSelected(false);
			panelBottom1.revalidate();
			panelBottom1.repaint();
			panelTop.revalidate();
			panelTop.repaint();
		}
	}

	/*
	 * This class implements an ActionListener when the user selects the
	 * intensityHandler button. The image number that the
	 * user would like to find similar images for is stored in the variable pic. pic
	 * takes the image number associated with
	 * the image selected and subtracts one to account for the fact that the
	 * intensityMatrix starts with zero and not one.
	 * The size of the image is retrieved from the imageSize array. The selected
	 * image's intensity bin values are
	 * compared to all the other image's intensity bin values and a score is
	 * determined for how well the images compare.
	 * The images are then arranged from most similar to the least.
	 */
	private class intensityHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			setTitle("CBIR Intensity");
			double[] distance = new double[101];
			map = new TreeMap<Double, LinkedList<Integer>>();
			double d = 0;
			int buttonNo = 1;
			// first forloop goes through each image
			for (int i = 1; i < 101; i++) {
				d = 0;
				// second forloop goes through each intensity bin
				for (int j = 1; j < 26; j++) {
					// calculates the manhattan distance between selected image and other images
					double x = Math.abs((intensityMatrix[picNo][j] - intensityMatrix[i][j]));
					double y = Math.abs((imageSize[picNo] + imageSize[i]));
					d += Math.abs(x / y);
				}
				distance[i] = d;
				// checks the map for key, if no key exists, then make new LinkedList for key
				if (map.get(d) == null) {
					map.put(d, new LinkedList<Integer>());
				}
				// add image number to distance key
				map.get(d).add(i);
			}
			// for-each loop to sort the map so closest distance shows up first
			for (Map.Entry<Double, LinkedList<Integer>> en : map.entrySet()) {
				LinkedList<Integer> list = en.getValue();
				while (list.size() > 0) {
					buttonOrder[buttonNo] = list.pop();
					buttonNo++;
				}
			}
			imageCount = 1;
			relevant.setSelected(false);
			relevant.setVisible(false);
			displayFirstPage();
		}

	}

	/*
	 * This class implements an ActionListener when the user selects the colorCode
	 * button. The image number that the
	 * user would like to find similar images for is stored in the variable pic. pic
	 * takes the image number associated with
	 * The size of the image is retrieved from the imageSize array. The selected
	 * image's intensity bin values are
	 * compared to all the other image's intensity bin values and a score is
	 * determined for how well the images compare.
	 * The images are then arranged from most similar to the least.
	 */
	private class colorCodeHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			setTitle("CBIR ColorCode");
			double[] distance = new double[101];
			map = new TreeMap<Double, LinkedList<Integer>>();
			double d = 0;
			int buttonNo = 1;
			// first forloop goes through each image
			for (int i = 1; i < 101; i++) {
				d = 0;
				// second forloop goes through each color code bin
				for (int j = 0; j < 64; j++) {
					// calculates the manhattan distance between selected image and other images
					double x = Math.abs((colorCodeMatrix[picNo][j] - colorCodeMatrix[i][j]));
					double y = Math.abs((imageSize[picNo] + imageSize[i]));
					d += Math.abs(x / y);
				}
				distance[i] = d;
				// checks the map for key, if no key exists, then make new LinkedList for key
				if (map.get(d) == null) {
					map.put(d, new LinkedList<Integer>());
				}
				// add image number to distance key
				map.get(d).add(i);
			}
			// for-each loop to sort the map so closest distance shows up first
			for (Map.Entry<Double, LinkedList<Integer>> en : map.entrySet()) {
				LinkedList<Integer> list = en.getValue();
				while (list.size() > 0) {
					buttonOrder[buttonNo] = list.pop();
					buttonNo++;
				}
			}
			imageCount = 1;
			relevant.setSelected(false);
			relevant.setVisible(false);
			displayFirstPage();
		}
	}

	private double normalizeImagesStdevHelper(double average, double numofImages, int bin) {
		double stdev = 0.0;
		double sqrt = 0.0;
		double total = 0.0;
		for (int i = 1; i < 101; i++) {
			total += Math.pow(colorCodeIntensityMatrix[i][bin] - average, 2);
		}
		sqrt = total / numofImages;
		stdev = Math.sqrt(sqrt);
		return stdev;
	}

	private double normalizeimagesAverageHelper(double numofImages, int bin) {
		double total = 0.0;
		for (int i = 1; i < 101; i++) {
			total += colorCodeIntensityMatrix[i][bin];
		}
		return total / numofImages;
	}

	private void normalizeimages() {
		// initalizing
		for (int i = 1; i < 101; i++) {
			for (int j = 0; j < 89; j++) {
				if (j >= 25) {
					colorCodeIntensityMatrix[i][j] = colorCodeMatrix[i][j - 25]
							/ (double) imageSize[i];
				} else {
					colorCodeIntensityMatrix[i][j] = intensityMatrix[i][j + 1]
							/ (double) imageSize[i];
				}
			}
		}
		// loops through each bin
		for (int i = 0; i < 89; i++) {
			int imageNum = 100;
			double stdev = 0.0;
			double avg = 0.0;
			avg = normalizeimagesAverageHelper(imageNum, i);
			stdev = normalizeImagesStdevHelper(avg, imageNum, i);
			// normalize the colorCodeIntesnity matrix
			for (int j = 1; j < 101; j++) {
				if (stdev != 0) {
					colorCodeIntensityMatrix[j][i] = (colorCodeIntensityMatrix[j][i] - avg) / stdev;
				}
			}
		}
	}

	private double getWeightSTDEVHelper(double average, int bin) {
		double stdev = 0.0;
		for (int i = 0; i < relevantPics.size(); i++) {
			stdev += Math.pow(colorCodeIntensityMatrix[relevantPics.get(i)][bin]
					- average, 2);
		}
		return Math.sqrt(stdev / relevantPics.size());
	}

	private double getAverage(int n, int bin) {
		double total = 0.0;
		for (int i = 0; i < n; i++) {
			total += colorCodeIntensityMatrix[relevantPics.get(i)][bin];
		}
		return total / n;
	}

	private void getWeights() {
		double totalWeight = 0.0;
		double stdevArr[] = new double[89];
		double averageArr[] = new double[89];
		double minStdev = Double.MAX_VALUE;
		for (int i = 0; i < 89; i++) {
			double average = 0.0;
			double stdev = 0.0;
			// used to check if all of the relevant images would have the same stdev, if
			// they do then stdev is avg
			boolean equalZero = (relevantPics.size() == 0);
			// calculates the avg values
			if (!equalZero) {
				average = getAverage(relevantPics.size(), i);
			}
			if (!equalZero) {
				stdev = getWeightSTDEVHelper(average, i);
			}
			stdevArr[i] = stdev;
			averageArr[i] = average;
			if (stdev != 0) {
				minStdev = Math.min(stdev, minStdev);
			}
		}
		// update the weight
		for (int i = 0; i < 89; i++) {
			if (stdevArr[i] == 0) {
				if (averageArr[i] == 0) {
					weightArr[i] = 0.0;
				} else {
					weightArr[i] = minStdev / 2;
				}
			} else {
				weightArr[i] = 1 / stdevArr[i];
			}
			totalWeight += weightArr[i];
		}
		// normalize the weight arr
		for (int i = 0; i < 89; i++) {
			weightArr[i] = weightArr[i] / totalWeight;
		}
	}

	private void setAllChecked(boolean checked) {
		for (int i = 1; i < 101; i++) {
			checkBox[i].setSelected(checked);
		}
	}

	/*
	 * This class implements an ActionListener when the user selects the
	 * Color Code & Intensity button.
	 * The size of the image is retrieved from the imageSize array.
	 * The selected image's intensity and color code bin values are
	 * compared to all the other image's intensity and colod code bin values and a
	 * score is determined for how well the images compare.
	 * The images are then arranged from most similar to the least.
	 */
	private class colorCodeIntensityHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			setTitle("CBIR ColorCode and Intensity");
			if (picNo != 0) {
				double[] distance = new double[101];
				map = new TreeMap<Double, LinkedList<Integer>>();
				double d = 0;
				int buttonNo = 1;
				if (isRelevant) {
					getWeights();
				}
				for (int i = 1; i < 101; i++) {
					d = 0;
					for (int j = 0; j < 89; j++) {
						d += weightArr[j] * Math.abs((colorCodeIntensityMatrix[picNo][j]
								- colorCodeIntensityMatrix[i][j]));
					}
					distance[i] = d;
					if (map.get(d) == null) {
						map.put(d, new LinkedList<Integer>());
					}
					map.get(d).add(i);
				}
				for (Map.Entry<Double, LinkedList<Integer>> entry : map.entrySet()) {
					LinkedList<Integer> list = entry.getValue();
					while (list.size() > 0) {
						buttonOrder[buttonNo] = list.pop();
						buttonNo++;
					}
				}
				imageCount = 1;
				relevant.setVisible(true);
				displayFirstPage();
			}
		}
	}

	/*
	 * This class is a listener class for the checkboxes. when the relevance toggle
	 * is checked we turn on all the other checkboxes for the image. You click
	 * on the image to select. Weights are also initialized when the RF is turned
	 * off and all the check boxes are removed.
	 */
	private class checkBoxHandler implements ItemListener {
		// Listens to the check boxes
		public void itemStateChanged(ItemEvent e) {
			Object s = e.getItemSelectable();
			if (s == relevant) {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					isRelevant = false;
					setAllChecked(false);
					for (int i = 0; i < 89; i++) {
						weightArr[i] = (double) 1 / 89;
					}
					displayFirstPage();
				} else if (e.getStateChange() == ItemEvent.SELECTED) {
					isRelevant = true;
					displayFirstPage();
				}
			}

			// adds the relevant images to the relevantPics arr when their respective
			// checkbox is selected
			for (int i = 1; i < 101; i++) {
				if (s == checkBox[i]) {
					if (e.getStateChange() == ItemEvent.DESELECTED) {
						checkBox[i].setBackground(getColor(Color.WHITE));
						relevantPics.remove((Integer) i);
					} else if (e.getStateChange() == ItemEvent.SELECTED) {
						checkBox[i].setBackground(Color.BLACK);
						relevantPics.add(i);
					}
				}
			}
		}
	}
}
