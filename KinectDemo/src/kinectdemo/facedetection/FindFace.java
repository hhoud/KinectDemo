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

public class FindFace {
	public static boolean isAllColor(int x, int y, BufferedImage img,
			Color color) {
		boolean isWhite = true;
		isWhite = isWhite && img.getRGB(x, y) == color.getRGB();
		// isWhite = isWhite && (img.getRGB(x-1, y) == color.getRGB());
		isWhite = isWhite && img.getRGB(x + 1, y) == color.getRGB();
		// isWhite = isWhite && (img.getRGB(x-1, y-1) == color.getRGB());
		// isWhite = isWhite && (img.getRGB(x-1, y+1) == color.getRGB());
		// isWhite = isWhite && (img.getRGB(x+1, y-1) == color.getRGB());
		isWhite = isWhite && img.getRGB(x + 1, y + 1) == color.getRGB();
		isWhite = isWhite && img.getRGB(x, y + 1) == color.getRGB();
		// isWhite = isWhite && (img.getRGB(x, y-1) == color.getRGB());
		return isWhite;
	}

	public static BufferedImage locateFaces(BufferedImage img) {
		new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
		/*
		 * for(int y = 1; y < img.getHeight()-1; y++) { for(int x = 1; x<
		 * img.getWidth()-1; x++) {
		 * 
		 * } }
		 */
		new Rotula().coloreContorno(img);

		return null;
	}

}
