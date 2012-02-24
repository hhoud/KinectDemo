package kinectdemo.facedetection;
/**
 * This file is part of Face Recogtion In Color Images based on Skin tone (FaceRecColImages).

    FaceRecColImages is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FaceRecColImages is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FaceRecColImages.  If not, see <http://www.gnu.org/licenses/>.

	author: Breno Santos Ara�jo
	email: brenopuc@gmail.com
 */
import java.awt.Color;
import java.awt.Point;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

public class DetectFace {
	private static void addListenerConvertButton(final JButton convertButton,
			final JTextField path, final JCheckBox check) {
		convertButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				final String arquivo = path.getText();
				final String output = arquivo.substring(0, arquivo
						.lastIndexOf('.'))
						+ "_detected"
						+ arquivo.substring(arquivo.indexOf('.'), arquivo
								.length());
				final File arquivoImagem = new File(arquivo);
				if (arquivoImagem.exists()) {
					try {
						final DetectFace detect = new DetectFace(ImageIO.read(arquivoImagem));
						detect.detectFaces();
						final BufferedImage resultado = detect.getImg();
						final JFrame frameResult = new JFrame("Resultado");
						frameResult
								.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
						frameResult.setSize(resultado.getWidth() + 10,
								resultado.getHeight() + 10);
						frameResult.add(new LoadImage(resultado));
						frameResult.setVisible(true);
						if (check.isSelected()) {
							ImageIO.write(resultado, "jpeg", new File(output));
						}
					} catch (final IOException e2) {
						JOptionPane.showMessageDialog(
								convertButton.getParent(), "ERRO: "
										+ e2.getMessage(), "Erro",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

	}

	private static void addListenerOpenButton(final JButton openButton,
			final JTextField path) {
		openButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileFilter() {

					@Override
					public boolean accept(File f) {
						if (f.getName().endsWith(".jpg")
								|| f.getName().endsWith(".jpeg")
								|| f.getName().endsWith(".bmp")
								|| f.isDirectory()
								|| f.getName().endsWith(".JPG")
								|| f.getName().endsWith(".JPEG")
								|| f.getName().endsWith(".BMP")
								|| f.isDirectory())
							return true;
						else
							return false;

					}

					@Override
					public String getDescription() {
						// TODO Auto-generated method stub
						return null;
					}
				});
				if (fileChooser.showOpenDialog(openButton.getParent()) == JFileChooser.APPROVE_OPTION) {
					path.setText(fileChooser.getSelectedFile()
							.getAbsolutePath());
				}

			}
		});
	}

	public static BufferedImage convertToGrayscale(BufferedImage source) {
		final BufferedImageOp op = new ColorConvertOp(ColorSpace
				.getInstance(ColorSpace.CS_PYCC), null);
		return op.filter(source, null);
	}

	private static void extractSkin(BufferedImage img, BufferedImage imgD) {
		int R, G, B, Cb, Cr;
		double t;
		Color col;
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				col = new Color(img.getRGB(x, y));
				R = col.getRed();
				G = col.getGreen();
				B = col.getBlue();

				Cb = (int) (-0.169 * R - 0.332 * G + 0.500 * B + 128);
				Cr = (int) (0.500 * R - 0.419 * G - 0.081 * B + 128);

				if (Cr > 137 && Cr < 177) {
					if (Cb < 127 && Cb > 77) {
						t = Cb + 0.6 * Cr;
						if (t > 190 && t < 215) {

							imgD.setRGB(x, y, Color.WHITE.getRGB());
							// imgD.setRGB(x, y, col.getRGB());
						} else {
							imgD.setRGB(x, y, 0);
						}

					} else {
						imgD.setRGB(x, y, 0);
					}
				} else {
					imgD.setRGB(x, y, 0);
				}
			}
		}
	}

	public static BufferedImage getBinaryImage(BufferedImage image) {
		final BufferedImage bwImage = new BufferedImage(image.getWidth(), image
				.getHeight(), BufferedImage.TYPE_USHORT_GRAY);
		bwImage.createGraphics().drawImage(image, 0, 0, null);
		final BufferedImage imageRes = new BufferedImage(image.getWidth(),
				image.getHeight(), image.getType());
		for (int x = 0; x < bwImage.getWidth(); x++) {
			for (int y = 0; y < bwImage.getHeight(); y++) {
				final int[] pixel = bwImage.getData()
						.getPixel(x, y, new int[1]);
				if (pixel[0] < 13000) {
					imageRes.setRGB(x, y, Color.WHITE.getRGB());
				} else {
					imageRes.setRGB(x, y, Color.BLACK.getRGB());
				}
			}
		}
		return imageRes;
	}

	public static List<PontoImage> getFaces(BufferedImage input,
			HashMap<Integer, HashMap<Integer, List<Point>>> map,
			BufferedImage original) {
		final List<PontoImage> imgs = new ArrayList<PontoImage>();

		for (final int k : map.keySet()) {
			final PontoImage pontoImage = new PontoImage();

			final BufferedImage img = new BufferedImage(input.getWidth(), input
					.getHeight(), input.getType());
			final HashMap<Integer, Point> listaFronteiras = new HashMap<Integer, Point>();
			int maxXGlobal = 0;
			int minXGlobal = input.getWidth();
			int minYGlobal = input.getHeight();
			int maxYGlobal = 0;
			for (final int kIn : map.get(k).keySet()) {
				double maxX = 0;
				double minX = input.getWidth() - 1;

				if (minYGlobal > kIn) {
					minYGlobal = kIn;
				}
				if (maxYGlobal < kIn) {
					maxYGlobal = kIn;
				}
				for (final Point ponto : map.get(k).get(kIn)) {
					if (minX > ponto.getX()) {
						minX = ponto.getX();
					}
					if (maxX < ponto.getX()) {
						maxX = ponto.getX();
					}

				}

				if (maxXGlobal < maxX) {
					maxXGlobal = (int) maxX;
				}
				if (minXGlobal > minX) {
					minXGlobal = (int) minX;
				}

				/*
				 * for(Point ponto: pontos) { img.setRGB(ponto.x, ponto.y,
				 * Color.WHITE.getRGB()); }
				 */

				double[] arrayPixel = new double[((int) (maxX - minX) + 1) * 5];
				arrayPixel = original.getRaster().getPixels((int) minX, kIn,
						(int) (maxX - minX) + 1, 1, arrayPixel);
				listaFronteiras.put(kIn, new Point((int) minX, (int) maxX));
				if (maxX - minX > 0) {
					img.getRaster().setPixels((int) minX, kIn,
							(int) (maxX - minX) + 1, 1, arrayPixel);
				}

				// ntoImage.pontos = pontos.toArray(new Point[pontos.size()]);

			}
			pontoImage.image = img;
			pontoImage.pontos = listaFronteiras;
			pontoImage.maxX = maxXGlobal;
			pontoImage.minX = minXGlobal;
			pontoImage.minY = minYGlobal;
			pontoImage.maxY = maxYGlobal;
			imgs.add(pontoImage);
		}

		return imgs;
	}

	public static BufferedImage getImageCross(BufferedImage output,
			BufferedImage roberts, Area area) {

		final BufferedImage res = new BufferedImage(output.getWidth(), output
				.getHeight(), output.getType());
		for (int x = 0; x < output.getWidth(); x++) {
			for (int y = 0; y < output.getHeight(); y++) {
				if (output.getRGB(x, y) == roberts.getRGB(x, y)
						&& output.getRGB(x, y) == Color.WHITE.getRGB()) {
					res.setRGB(x, y, Color.WHITE.getRGB());
				} else {
					res.setRGB(x, y, Color.BLACK.getRGB());
				}
			}
		}
		final Erosao erode = new Erosao();
		erode.erode2(res);
		return res;
	}

	private static void initializeGUI() {
		final JFrame frame = new JFrame(
				"Face Detection in color images - Breno Santos Ara�jo - brenopuc@gmail.com");
		frame.setSize(400, 150);
		frame.setLayout(null);
		final JLabel label = new JLabel("Input File: ");
		label.setBounds(10, 10, 70, 20);
		frame.add(label);
		final JTextField textField = new JTextField(200);
		textField.setBounds(80, 10, 160, 20);
		frame.add(textField);
		final JButton addButton = new JButton("Open...");
		addButton.setBounds(250, 10, 90, 20);
		frame.add(addButton);
		final JCheckBox checkBox = new JCheckBox("Save output file");
		checkBox.setBounds(10, 40, 140, 20);
		frame.add(checkBox);
		final JButton detectButton = new JButton("Detect");
		detectButton.setBounds(250, 40, 90, 20);
		frame.add(detectButton);
		addListenerOpenButton(addButton, textField);
		addListenerConvertButton(detectButton, textField, checkBox);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	BufferedImage img;

	static boolean debug = true;

	public static void main(String[] args) throws IOException {
		initializeGUI();

	}

	//public DetectFace(File arqImg) throws IOException
        public DetectFace(BufferedImage arqImg) throws IOException
	{
		// File arquivoImagem = new File("d:\\temp\\images\\image18.jpg");

		//img = ImageIO.read(arqImg);
                img = arqImg;
	}

	public boolean detectFaces() {
		final BufferedImage imgD = new BufferedImage(img.getWidth(), img
				.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		extractSkin(img, imgD);
		BufferedImage buf1 = new BufferedImage(imgD.getWidth(), imgD
				.getHeight(), imgD.getType());
		buf1.setData(imgD.getData());
		final Area area = new Area(img);
		buf1 = area.coloreContorno(buf1);
		// BufferedImage buf2 = new Erosao().erode(buf1);
		final LoadImage panel0 = new LoadImage(img);
		// LoadImage panel1 = new LoadImage(imgD);
		// LoadImage panel2 = new LoadImage(buf1);
		// LoadImage panel3 = new LoadImage(area.edge);

		/*
		 * RobertsCrossOp roberts = new RobertsCrossOp(convertToGrayscale(img));
		 * BufferedImage buffRoberts =
		 * getBinaryImage(roberts.getBufferedImage()); LoadImage panel4 = new
		 * LoadImage(buffRoberts);
		 */
		final BufferedImage imgCross = getImageCross(buf1, area.edge, area);
		new LoadImage(imgCross);
		final RemoveArea2 r = new RemoveArea2();
		new LoadImage(r.coloreContorno(imgCross));
		final List<PontoImage> segments = getFaces(imgCross, r.getRotulos(),
				img);

		final JFrame frame = new JFrame();
		// frame.setSize(img.getWidth()+40,img.getHeight()+70);
		// frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(panel0);
		// frame.add(panel1);
		// frame.add(panel2);
		// frame.add(panel3);
		// frame.add(panel4);
		// frame.add(panel5);
		// frame.add(panel6);
		final EyeMap map = new EyeMap();
                boolean gotFace=false;
		for (final PontoImage imagens : segments) {
			// FindFace.locateFaces(imagens);

			// frame.add(new LoadImage(imagens.image));
			final boolean hasFace = map.extractFaces(imagens.image,
					imagens.minX, imagens.minY, imagens.maxX, imagens.maxY);
			if (hasFace) {
				drawRect(imagens.minX, imagens.minY, imagens.maxX
						- imagens.minX, imagens.maxY - imagens.minY, img);
                                gotFace=true;
			}
			// Point[] res = new Point[imagens.pontos.length];
			// new ConvexHull().chainHull_2D(imagens.pontos,
			// imagens.pontos.length, res);
			// BufferedImage hull = new BufferedImage(imagens.image.getWidth(),
			// imagens.image.getHeight(), imagens.image.getType());
			/*
			 * for(Point p: res) { if(p != null ) hull.setRGB(p.x,
			 * p.y,Color.RED.getRGB()); }
			 */
			// frame.add(new LoadImage(hull));
		}
                return gotFace;
		// frame.add(panel7);

		// frame.setSize(img.getWidth() + 20, img.getHeight() + 50);

		// panel.setBounds(5,5,img.getWidth(), img.getHeight());
		// panel.setBounds(5,5,img.getWidth(), img.getHeight());
		// panel.setBounds(5,5,img.getWidth(), img.getHeight());
		// frame.setVisible(true);
		// /ImageIO.write(imgD, "JPEG", new File("d:\\temp\\res_eu2.jpg"));
	}

	private void drawRect(int x, int y, int width, int height, BufferedImage img) {
		for (int a = x; a < x + width; a++) {
			if (y - 1 > 0) {
				img.setRGB(a, y - 1, Color.RED.getRGB());
			}
			img.setRGB(a, y, Color.RED.getRGB());
			if (y + 1 < img.getHeight()) {
				img.setRGB(a, y + 1, Color.RED.getRGB());
			}

			if (y + height - 1 > 0) {
				img.setRGB(a, y + height - 1, Color.RED.getRGB());
			}
			img.setRGB(a, (y + height), Color.RED.getRGB());
			if (y + height + 1 < img.getHeight()) {
				img.setRGB(a, y + height + 1, Color.RED.getRGB());
			}
		}

		for (int a = y; a < y + height; a++) {
			if (x - 1 > 0) {
				img.setRGB(x - 1, a, Color.RED.getRGB());
			}
			img.setRGB(x, a, Color.RED.getRGB());
			if (x + 1 < img.getWidth()) {
				img.setRGB(x + 1, a, Color.RED.getRGB());
			}

			if (x + width - 1 > 0) {
				img.setRGB(x + width - 1, a, Color.RED.getRGB());
			}
			img.setRGB((x + width), a, Color.RED.getRGB());
			if (x + width + 1 < img.getWidth()) {
				img.setRGB(x + width + 1, a, Color.RED.getRGB());
			}
		}
	}

	public BufferedImage getImg() {
		return img;
	}

}

class PontoImage {
	public BufferedImage image;
	public HashMap<Integer, Point> pontos;
	public int maxX, minX, minY, maxY;
}
