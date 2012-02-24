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

public class Erosao {
	public BufferedImage erode(BufferedImage img) {
		final BufferedImage imgResult = new BufferedImage(img.getWidth(), img
				.getHeight(), img.getType());
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				erodePixel(img, imgResult, x, y);

			}
		}
		return imgResult;
	}

	public BufferedImage erode2(BufferedImage img) {
		final BufferedImage imgResult = new BufferedImage(img.getWidth(), img
				.getHeight(), img.getType());
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				if (new Color(img.getRGB(x, y)).equals(Color.BLACK)) {
					imgResult.setRGB(x, y, Color.BLACK.getRGB());
					if (x < img.getWidth() - 1) {
						imgResult.setRGB(x + 1, y, Color.BLACK.getRGB());
					}
					if (x < img.getWidth() - 1 && y < img.getHeight() - 1) {
						imgResult.setRGB(x + 1, y + 1, Color.BLACK.getRGB());
					}
					if (x < img.getWidth() - 1 && y > 0) {
						imgResult.setRGB(x + 1, y - 1, Color.BLACK.getRGB());
					}
					if (x > 0) {
						imgResult.setRGB(x - 1, y, Color.BLACK.getRGB());
					}
					if (x > 0 && y > 0) {
						imgResult.setRGB(x - 1, y - 1, Color.BLACK.getRGB());
					}
					if (x > 0 && y < img.getHeight() - 1) {
						imgResult.setRGB(x - 1, y + 1, Color.BLACK.getRGB());
					}
					if (y < img.getHeight() - 1) {
						imgResult.setRGB(x, y + 1, Color.BLACK.getRGB());
					}
					if (y > 0) {
						imgResult.setRGB(x, y - 1, Color.BLACK.getRGB());
					}
				} else {
					imgResult.setRGB(x, y, Color.BLACK.getRGB());
				}

			}
		}
		return imgResult;
	}

	private void erodePixel(BufferedImage img, BufferedImage imgResult, int x,
			int y) {
		if (new Color(img.getRGB(x, y)).equals(Color.RED)) {
			imgResult.setRGB(x, y, Color.WHITE.getRGB());
			if (x < img.getWidth() - 1) {
				imgResult.setRGB(x + 1, y, Color.WHITE.getRGB());
			}
			if (x < img.getWidth() - 1 && y < img.getHeight() - 1) {
				imgResult.setRGB(x + 1, y + 1, Color.WHITE.getRGB());
			}
			if (x < img.getWidth() - 1 && y > 0) {
				imgResult.setRGB(x + 1, y - 1, Color.WHITE.getRGB());
			}
			if (x > 0) {
				imgResult.setRGB(x - 1, y, Color.WHITE.getRGB());
			}
			if (x > 0 && y > 0) {
				imgResult.setRGB(x - 1, y - 1, Color.WHITE.getRGB());
			}
			if (x > 0 && y < img.getHeight() - 1) {
				imgResult.setRGB(x - 1, y + 1, Color.WHITE.getRGB());
			}
			if (y < img.getHeight() - 1) {
				imgResult.setRGB(x, y + 1, Color.WHITE.getRGB());
			}
			if (y > 0) {
				imgResult.setRGB(x, y - 1, Color.WHITE.getRGB());
			}
		} else {
			imgResult.setRGB(x, y, Color.BLACK.getRGB());
		}
	}

}
