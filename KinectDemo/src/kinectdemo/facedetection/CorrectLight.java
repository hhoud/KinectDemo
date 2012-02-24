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
import java.util.ArrayList;

public class CorrectLight {
	public BufferedImage correctLigth(BufferedImage img) {
		double R, G, B, Y;
		final ArrayList<Double> mediaR = new ArrayList<Double>();
		final ArrayList<Double> mediaG = new ArrayList<Double>();
		final ArrayList<Double> mediaB = new ArrayList<Double>();
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				final Color col = new Color(img.getRGB(x, y));
				R = col.getRed();
				G = col.getGreen();
				B = col.getBlue();

				Y = 0.299 * R + 0.587 * G + 0.114 * B;
				// 95% de 255 = 200.65
				if (Y > 200.65) {
					mediaR.add(R);
					mediaG.add(G);
					mediaB.add(B);
				}
			}
		}
		double mediaTotalR = 0;
		double mediaTotalG = 0;
		double mediaTotalB = 0;
		for (int x = 0; x < mediaR.size(); x++) {

			mediaTotalR += mediaR.get(x);
			mediaTotalG += mediaG.get(x);
			mediaTotalB += mediaB.get(x);
		}
		mediaTotalR = mediaTotalR / mediaR.size();
		mediaTotalG = mediaTotalG / mediaR.size();
		mediaTotalB = mediaTotalB / mediaR.size();

		// TODO Verificar porque antes estava 227
		final double multiplierR = 255d / mediaTotalR;
		final double multiplierG = 255d / mediaTotalG;
		final double multiplierB = 255d / mediaTotalB;

		final BufferedImage resultado = new BufferedImage(img.getWidth(), img
				.getHeight(), img.getType());

		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				final Color col = new Color(img.getRGB(x, y));
				R = col.getRed();
				G = col.getGreen();
				B = col.getBlue();

				R = (int) (R * multiplierR);
				G = (int) (G * multiplierG);
				B = (int) (B * multiplierB);
				if (R > 255) {
					R = 255;
				}
				if (G > 255) {
					G = 255;
				}
				if (B > 255) {
					B = 255;
				}

				resultado.setRGB(x, y, new Color((int) R, (int) G, (int) B)
						.getRGB());
			}
		}
		return resultado;

	}

}
