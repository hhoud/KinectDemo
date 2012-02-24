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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class EyeMap {

	private double maxCR, minCR, maxCB, minCB, maxCB2, minCB2, maxCR2, minCR2,
			maxCR2Negado, minCR2Negado;
	static int cont = 0;

	public double calculaG(double x) {
		final double parameter = 2; // Dado pelo artigo
		return Math.abs(parameter)
				* (Math.pow(1 - Math.pow(Math.abs(x / parameter), 2), 0.5) - 1);
	}

	public double calculaNMouthMap(double nPixels, BufferedImage img) {
		double CB, CR, R, G, B;
		Color col;

		double somatorioCr2 = 0;
		double somatorioCrCb = 0;

		maxCB = 0;
		minCB = 255;

		maxCR = 0;
		minCR = 255;

		maxCB2 = 0;
		minCB2 = 255 * 255;

		maxCR2 = 0;
		minCR2 = 255 * 255;

		maxCR2Negado = 0;
		minCR2Negado = 255 * 255;
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				col = new Color(img.getRGB(x, y));
				R = col.getRed();
				G = col.getGreen();
				B = col.getBlue();
				CB = (int) (-0.169 * R - 0.332 * G + 0.500 * B + 128);
				CR = (int) (0.500 * R - 0.419 * G - 0.081 * B + 128);
				if (minCB > CB) {
					minCB = CB;
				}
				if (maxCB < CB) {
					maxCB = CB;
				}

				if (minCR > CR) {
					minCR = CR;
				}
				if (maxCR < CR) {
					maxCR = CR;
				}

				if (maxCB2 < CB * CB) {
					maxCB2 = CB * CB;
				}
				if (minCB2 > CB * CB) {
					minCB2 = CB * CB;
				}

				if (maxCR2 < CR * CR) {
					maxCR2 = CR * CR;
				}
				if (minCR2 > CR * CR) {
					minCR2 = CR * CR;
				}

				final double CRNegado = 255 - CR;
				final double CR2N = CRNegado * CRNegado;

				if (maxCR2Negado < CR2N) {
					maxCR2Negado = CR2N;
				}
				if (minCR2Negado > CR2N) {
					minCR2Negado = CR2N;
				}
			}
		}
		double n = 0.0;
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				col = new Color(img.getRGB(x, y));
				R = col.getRed();
				G = col.getGreen();
				B = col.getBlue();
				CB = (int) (-0.169 * R - 0.332 * G + 0.500 * B + 128);
				CR = (int) (0.500 * R - 0.419 * G - 0.081 * B + 128);

				// TODO Verificar se devo incluir somente pixels da FACE
				double CR2 = CR * CR;
				CR2 = Util.normaliza(maxCR2, minCR2, CR2);

				CR = Util.normaliza(maxCR, minCR, CR);
				CB = Util.normaliza(maxCB, minCB, CB);
				final double minA = minCR / maxCB;
				final double maxA = maxCR / minCB;
				final double merda = Util.normaliza(maxA, minA, CR / CB);

				somatorioCr2 += CR2;
				somatorioCrCb += merda;
				n++;

			}
		}
		return 0.95 * somatorioCr2 / n / (somatorioCrCb / n);

	}

	public double calculaR(double x, double y) {
		return Math.sqrt(x * x + y * y);
	}

	public boolean extractFaces(BufferedImage img, int minX, int minY,
			int maxX, int maxY) {

		/* Realiza a corre��o de ilumina��o */
		img = new CorrectLight().correctLigth(img);

		/* Componentes do espa�o de cores RGB */
		double R, G, B;

		/* Componentes do espa�o de cores YCbCr */
		double CR, CB;

		/* Componentes das f�rmulas utilzadas */
		double CR2, CB2, CRNegado, CRNegado2, CbDCr, L1, L2, CrDCb, mouthMap;

		/* Vari�vel de manipula��o do mapa de olhos da cromin�ncia */
		double eyeMapC;

		/* N�mero total de pixels da imagem */
		final double nPixels = numberOfPixels(img);

		/* C�lculo do n da f�rmula do mapa da boca */
		final double n = calculaNMouthMap(nPixels, img);

		/* Obtem o m�nimo e m�ximo da divis�o CB/CR */
		final double minCbCr = minCB / maxCR;
		final double maxCbCr = maxCB / minCR;

		/* Obtem o m�nimo e m�ximo da divis�o CR/CB */
		final double minCrCb = minCR / maxCB;
		final double maxCrCb = maxCR / minCB;

		/* Inicializa as imagens resultantes */
		final BufferedImage imgEyeMapC = new BufferedImage(img.getWidth(), img
				.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		final BufferedImage imgEyeMapL = new BufferedImage(img.getWidth(), img
				.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		final BufferedImage imgEyeMapL1 = new BufferedImage(img.getWidth(), img
				.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		final BufferedImage imgEyeMapL2 = new BufferedImage(img.getWidth(), img
				.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		final BufferedImage imgMouthMap = new BufferedImage(img.getWidth(), img
				.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

		/* Mapa final dos olhos */
		final BufferedImage imgEyeCombined = new BufferedImage(img.getWidth(),
				img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

		/* Percorre a imagem pixel a pixel */
		for (int x = minX; x < maxX; x++) {
			for (int y = minY; y < maxY; y++) {
				/* Obtem a cor RGB do pixel atual */
				final Color col = new Color(img.getRGB(x, y));

				/* Atribui��o dos Componentes */
				R = col.getRed();
				G = col.getGreen();
				B = col.getBlue();

				/* Convers�o para o espa�o de Cores YCbCr */
				// Y =( 0.257 * R + 0.504 * G + 0.098 * B );
				CB = -0.169 * R - 0.331 * G + 0.499 * B + 128;
				CR = 0.499 * R - 0.418 * G - 0.0813 * B + 128;

				/* C�lculo do mapa de olhos Cromin�ncia */
				{
					/* Obtem CR� */
					CB2 = Math.pow(CR, 2);
					CB2 = Util.normaliza(maxCB2, minCB2, CB2);

					/* Obtem CR negado */
					CRNegado = 255 - CR;

					/* Obtem CRNegado� */
					CRNegado2 = Math.pow(CRNegado, 2);
					CRNegado2 = Util.normaliza(maxCR2Negado, minCR2Negado,
							CRNegado2);

					/* Obtem Cb/Cr */
					CbDCr = Util.normaliza(maxCbCr, minCbCr, CB / CR);

					/* Joga na formula 1/3{Cb2/3 + CrNegado�/3 + (Cb/Cr)/3} */
					eyeMapC = CB2 / 3 + CRNegado2 / 3 + CbDCr / 3;

					/* Atribui o pixel � imagem */
					imgEyeMapC.getRaster().setPixel(x, y,
							new int[] { (int) eyeMapC });
				}

				/* C�lculo do mapa de olhos Lumin�ncia */
				{

					/* Obtem a dilata��o do pixel */
					L1 = (int) getMax(x, y, img);

					/* Obtem a eros�o do pixel */
					L2 = (int) getMin(x, y, img);

					/* Atribui os pixels as imagens */
					imgEyeMapL1.getRaster().setPixel(x, y,
							new int[] { (int) L1 });
					imgEyeMapL2.getRaster().setPixel(x, y,
							new int[] { (int) L2 });

					if (imgEyeMapL2.getRaster().getPixel(x, y, new int[1])[0] > 20) {
						if (L1 / (L2 + 1) * 5 > 10) {
							imgEyeMapL.getRaster().setPixel(x, y,
									new double[] { 255 });
							if (imgEyeMapC.getRaster().getPixel(x, y,
									new int[1])[0] > 100) {
								imgEyeCombined.getRaster().setPixel(x, y,
										new double[] { 255 });
							}
						}

					}

				}

				/* C�lculo do Mapa da Boca */
				{

					/* Obtem o quadrado do CR e normaliza */
					CR2 = Math.pow(CR, 2);
					CR2 = Util.normaliza(maxCR2, minCR2, CR2);

					CrDCb = Util.normaliza(maxCrCb, minCrCb, CR / CB);

					mouthMap = CR2 * (CR2 - n * CrDCb) * (CR2 - n * CrDCb);

					mouthMap = mouthMap / (255 * 255);
					if (mouthMap > 240) {
						mouthMap = 255;
					} else {
						mouthMap = 0;
					}
					imgMouthMap.getRaster().setPixel(x, y,
							new int[] { (int) mouthMap });

				}
			}

		}
	
		/* jun��o dos mapas de olhos */
		boolean temBoca = false;
		boolean lEye = false;
		boolean rEye = false;
		int minXBoca = img.getWidth();
		int maxXBoca = 0;
		int minXEyeL = img.getWidth();
		int maxXEyeL = 0;
		int minXEyeR = img.getWidth();
		int maxXEyeR = 0;
		int minYBoca = img.getHeight();
		int maxYBoca = 0;
		int minYEyeL = img.getHeight();
		int maxYEyeL = 0;
		int minYEyeR = img.getHeight();
		int maxYEyeR = 0;

		double somatorioXBoca = 0;
		double somatorioXEyeL = 0;
		double somatorioXEyeR = 0;
		double somatorioYBoca = 0;
		double somatorioYEyeL = 0;
		double somatorioYEyeR = 0;

		int pixCountEyeL = 0;
		int pixCountEyeR = 0;
		int pixCountMouth = 0;

		for (int x = minX; x < maxX; x++) {
			for (int y = minY; y < maxY; y++) {
				
				/* Valida��o da boca */
				{
					if (y > minY && y < maxY * 1.3)// ((double)(maxY - minY)/2 -
													// 500) && y < maxY)
					{
						if (x > minX && x < maxX) {
							if (imgMouthMap.getRaster().getPixel(x, y,
									new int[1])[0] == 255) {
								imgMouthMap.getRaster().setPixel(x, y,
										new int[] { 150 });
								temBoca = true;
								if (x > maxXBoca) {
									maxXBoca = x;
								}
								if (x < minXBoca) {
									minXBoca = x;
								}
								if (maxYBoca < y) {
									maxYBoca = y;
								}
								if (minYBoca > y) {
									minYBoca = y;
								}
								somatorioXBoca += x;
								somatorioYBoca += y;
								pixCountMouth++;
							} else {
								imgMouthMap.getRaster().setPixel(x, y,
										new int[] { 0 });
							}
						} else {
							imgMouthMap.getRaster().setPixel(x, y,
									new int[] { 0 });
						}
					} else {
						imgMouthMap.getRaster().setPixel(x, y, new int[] { 0 });
					}
				}

				/* Valida��o dos olhos */
				{

					if (x < (maxX - minX) / 2 + minX && x > minX
							&& y < (maxY - minY) / 2 + minY && y > minY) // por��o
																			// superior
																			// esquerda
					{
						if (imgEyeCombined.getRaster().getPixel(x, y,
								new int[1])[0] == 255) {
							imgEyeCombined.getRaster().setPixel(x, y,
									new int[] { 150 });
							lEye = true;
							if (maxXEyeL < x) {
								maxXEyeL = x;
							}
							if (minXEyeL > x) {
								minXEyeL = x;
							}
							if (maxYEyeL < y) {
								maxYEyeL = y;
							}
							if (minYEyeL > y) {
								minYEyeL = y;
							}

							somatorioXEyeL += x;
							somatorioYEyeL += y;
							pixCountEyeL++;
						} else {
							imgEyeCombined.getRaster().setPixel(x, y,
									new int[] { 0 });
						}
					} else if (x > (maxX - minX) / 2 + minX && x < maxX
							&& y < (maxY - minY) / 2 + minY && y > minY) // por��o
																			// superior
																			// direita
					{
						if (imgEyeCombined.getRaster().getPixel(x, y,
								new int[1])[0] == 255) {
							imgEyeCombined.getRaster().setPixel(x, y,
									new int[] { 150 });
							rEye = true;
							if (maxXEyeR < x) {
								maxXEyeR = x;
							}
							if (minXEyeR > x) {
								minXEyeR = x;
							}
							if (maxYEyeR < y) {
								maxYEyeR = y;
							}
							if (minYEyeR > y) {
								minYEyeR = y;
							}
							somatorioXEyeR += x;
							somatorioYEyeR += y;
							pixCountEyeR++;
						} else {
							imgEyeCombined.getRaster().setPixel(x, y,
									new int[] { 0 });
						}
					} else {
						imgEyeCombined.getRaster().setPixel(x, y,
								new int[] { 0 });
					}

				}
				// mouthMap = (int)getMax(x, y, imgMouthMap);
				// imgMouthMap.getRaster().setPixel(x, y, new
				// int[]{(int)mouthMap});

			}

		}
		final double mediaXBoca = somatorioXBoca / pixCountMouth;// ((maxXBoca -
																	// minXBoca
																	// )/
																	// 2+minXBoca
																	// );
		final double mediaXOlhoEsquerdo = somatorioXEyeL / pixCountEyeL;// ((
																		// maxXEyeL
																		// -
																		// minXEyeL
																		// )/2 +
																		// minXEyeL
																		// );
		final double mediaXOlhoDireito = somatorioXEyeR / pixCountEyeR;// ((
																		// maxXEyeR
																		// -
																		// minXEyeR
																		// )/2 +
																		// minXEyeR
																		// );

		final double mediaYBoca = somatorioYBoca / pixCountMouth;// ((maxYBoca -
																	// minYBoca
																	// )/
																	// 2+minYBoca
																	// );
		final double mediaYOlhoEsquerdo = somatorioYEyeL / pixCountEyeL;// ((
																		// maxYEyeL
																		// -
																		// minYEyeL
																		// )/2 +
																		// minYEyeL
																		// );
		final double mediaYOlhoDireito = somatorioYEyeR / pixCountEyeR; // ((
																		// maxYEyeR
																		// -
																		// minYEyeR
																		// )/2 +
																		// minYEyeR
																		// );

		final double mediaYOlhos = (mediaYOlhoDireito + mediaYOlhoEsquerdo) / 2.0;
		
		boolean face = false;
		
		if (pixCountEyeL > pixCountEyeR
				&& (double) pixCountEyeL / pixCountEyeR > 4) {
			rEye = false;
			lEye = false;
		} else if (pixCountEyeR > pixCountEyeL
				&& (double) pixCountEyeR / pixCountEyeL > 4) {
			rEye = false;
			lEye = false;
		}

		// Verifica espa�os entre olhos
		int pixCount = 0;
		for (int x = maxXEyeL; x < minXEyeR; x++) {
			if (Util.isFace(x, (int) mediaYOlhos, img)) {
				pixCount++;
			}
		}
		if (pixCount < (maxXEyeL - minXEyeR) * 1.1) {
			lEye = false;
		}

		// verifica espa�os entre boca e olhos
		pixCount = 0;
		for (int y = (int) mediaYOlhos; y < minYBoca; y++) {
			if (Util.isFace((int) mediaXBoca, y, img)) {
				pixCount++;
			}
		}
		if (pixCount < (minYBoca - mediaYOlhos) * 0.14) {
			lEye = false;
		}

		if (lEye && rEye && temBoca && mediaXBoca > mediaXOlhoEsquerdo * 1.0
				&& mediaXBoca < mediaXOlhoDireito * 1.2
				&& mediaYOlhos < mediaYBoca) {
			
			face = true;
			// ImageIO.write(img, "jpeg", new
			// File("d:\\temp\\images\\out"+cont+++".jpeg"));
		}

		// imgMouthMap = Util.equalizeGrayScale(imgMouthMap);
		final JFrame frame = new JFrame();
		// frame.setLayout(new GridLayout(2,2));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new LoadImage(img));
		// frame.add(new LoadImage(imgEyeMapC));
		// frame.add(new LoadImage(imgEyeMapL1));
		// frame.add(new LoadImage(imgEyeMapL2));
		// frame.add(new LoadImage(imgEyeMapL));
		// frame.add(new LoadImage(imgMouthMap));
		// frame.add(new LoadImage(imgEyeCombined));
		frame.setSize(img.getWidth() + 20, img.getHeight() + 40);
		// if(face)
		// frame.setVisible(true);
		// else
		// frame.dispose();
		return face;

	}

	public void extractFaces(File file) throws IOException {
		ImageIO.read(file);
	}

	public double getMax(int x, int y, BufferedImage img) {
		double max = 0;
		double R, G, B, Y;
		for (int a = 0; a < Util.morphologia.length; a++) {
			for (int b = 0; b < Util.morphologia.length; b++) {
				if (x + a < img.getWidth() && y + b < img.getHeight()) {
					final Color col = new Color(img.getRGB(x + a, y + b));

					R = col.getRed();
					G = col.getGreen();
					B = col.getBlue();
					Y = 0.257 * R + 0.504 * G + 0.098 * B;
					if (Util.morphologia[a][b] && Y > max) {
						max = Y;
					}
				}
			}

		}
		return max;
	}

	public double getMin(int x, int y, BufferedImage img) {
		double min = 255;
		double R, G, B, Y;
		for (int a = 0; a < Util.morphologia.length; a++) {
			for (int b = 0; b < Util.morphologia.length; b++) {
				if (x + a < img.getWidth() && y + b < img.getHeight()) {
					final Color col = new Color(img.getRGB(x + a, y + b));

					R = col.getRed();
					G = col.getGreen();
					B = col.getBlue();
					Y = 0.257 * R + 0.504 * G + 0.098 * B;
					if (Util.morphologia[a][b] && Y < min) {
						min = Y;
					}
				}
			}

		}
		return min;
	}

	public boolean isFacePixels(double Y, double CR, double CB) {
		if (CR > 137 && CR < 177) {
			if (CB < 127 && CB > 77) {
				final double t = CB + 0.6 * CR;
				if (t > 190 && t < 215)
					return true;
			}
		}
		return false;
	}

	public int numberOfPixels(BufferedImage img) {
		// TODO Verificar inicialmente retornava apenas o n�mero de pixels de
		// segmentos de pele
		return img.getHeight() * img.getWidth();
	}

}
