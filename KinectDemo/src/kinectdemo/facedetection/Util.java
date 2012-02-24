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

public class Util {
	/* Morphologia matem�tica Estrutura Esf�rica - Eye Map Luma */
	public static final boolean morphologia[][] = {
			{ false, false, false, true, false, false, false },
			{ false, true, true, true, true, true, false },
			{ false, true, true, true, true, true, false },
			{ true, true, true, true, true, true, true },
			{ false, true, true, true, true, true, false },
			{ false, true, true, true, true, true, false },
			{ false, false, false, true, false, false, false } };

	public static BufferedImage equalizeGrayScale(BufferedImage input) {
		final BufferedImage output = new BufferedImage(input.getWidth(), input
				.getHeight(), input.getType());
		final int[] frequency = new int[256];
		final int maxIntensity = 255;
		for (int x = 0; x < input.getWidth(); x++) {
			for (int y = 0; y < input.getHeight(); y++) {
				double[] pixel = new double[1];
				pixel = input.getRaster().getPixel(x, y, pixel);
				frequency[(int) pixel[0]]++;
			}
		}
		int sum = 0;
		for (int x = 0; x < frequency.length; x++) {
			sum += frequency[x];
			frequency[x] = sum * maxIntensity
					/ (input.getWidth() * input.getHeight());
		}
		for (int x = 0; x < input.getWidth(); x++) {
			for (int y = 0; y < input.getHeight(); y++) {
				double[] pixel = new double[1];
				pixel = input.getRaster().getPixel(x, y, pixel);
				pixel[0] = frequency[(int) pixel[0]];
				output.getRaster().setPixel(x, y, pixel);
			}
		}
		return output;
	}

	public static boolean isFace(int x, int y, BufferedImage img) {
		final Color col = new Color(img.getRGB(x, y));
		final int R = col.getRed();
		final int G = col.getGreen();
		final int B = col.getBlue();

		final double Cb = -0.169 * R - 0.332 * G + 0.500 * B + 128;
		final double Cr = 0.500 * R - 0.419 * G - 0.081 * B + 128;
		double t;

		if (Cr > 137 && Cr < 177) {
			if (Cb < 127 && Cb > 77) {
				t = Cb + 0.6 * Cr;
				if (t > 190 && t < 215)
					return true;
			}
		}
		return false;
	}

	public static double normaliza(double max, double min, double value) {
		return (value - min) * 255 / (max - min);
	}
}
