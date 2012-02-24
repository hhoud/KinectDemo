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
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Area {
	public static BufferedImage convertToGrayscale(BufferedImage source) {
		final BufferedImageOp op = new ColorConvertOp(ColorSpace
				.getInstance(ColorSpace.CS_GRAY), null);
		return op.filter(source, null);
	}

	Integer contador;
	List<Point> pontos;
	public BufferedImage edge;

	private final BufferedImage original;

	public Area(BufferedImage original) {
		edge = new BufferedImage(original.getWidth(), original.getHeight(),
				original.getType());
		this.original = convertToGrayscale(original);
	}

	public BufferedImage coloreContorno(BufferedImage img) {
		final BufferedImage img2 = new BufferedImage(img.getWidth(), img
				.getHeight(), img.getType());
		int temp1, temp2;
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				if (new Color(img.getRGB(x, y)).equals(Color.WHITE)) {
					contador = 0;
					pontos = new ArrayList<Point>();
					segmentation(img, x, y, img2);

					if (contador < 300) {
						for (final Point aux : pontos) {
							img.setRGB(aux.x, aux.y, Color.BLACK.getRGB());
						}
					}
				}
				if (x + 1 < img.getWidth() && y + 1 < img.getHeight()) {
					temp1 = Math.abs(original.getRGB(x, y)
							- original.getRGB(x + 1, y + 1));
					temp2 = Math.abs(original.getRGB(x + 1, y)
							- original.getRGB(x, y + 1));
					if (temp1 + temp2 > 2900000) {
						edge.setRGB(x, y, Color.BLACK.getRGB());
					} else {
						edge.setRGB(x, y, Color.WHITE.getRGB());
					}
				}
			}
		}
		return img2;

	}

	private void erodePixel(BufferedImage imgResult, int x, int y) {
		if (new Color(imgResult.getRGB(x, y)).equals(Color.WHITE)) {
			imgResult.setRGB(x, y, Color.WHITE.getRGB());
			if (x < imgResult.getWidth() - 1) {
				imgResult.setRGB(x + 1, y, Color.WHITE.getRGB());
			}
			if (x < imgResult.getWidth() - 1 && y < imgResult.getHeight() - 1) {
				imgResult.setRGB(x + 1, y + 1, Color.WHITE.getRGB());
			}
			if (x < imgResult.getWidth() - 1 && y > 0) {
				imgResult.setRGB(x + 1, y - 1, Color.WHITE.getRGB());
			}
			if (x > 0) {
				imgResult.setRGB(x - 1, y, Color.WHITE.getRGB());
			}
			if (x > 0 && y > 0) {
				imgResult.setRGB(x - 1, y - 1, Color.WHITE.getRGB());
			}
			if (x > 0 && y < imgResult.getHeight() - 1) {
				imgResult.setRGB(x - 1, y + 1, Color.WHITE.getRGB());
			}
			if (y < imgResult.getHeight() - 1) {
				imgResult.setRGB(x, y + 1, Color.WHITE.getRGB());
			}
			if (y > 0) {
				imgResult.setRGB(x, y - 1, Color.WHITE.getRGB());
			}
		} else {
			imgResult.setRGB(x, y, Color.BLACK.getRGB());
		}
	}

	/**
	 *metodo que adiciona na pilha os vizinho validos do ponto x, y
	 */
	private void nextStep(int x, int y, BufferedImage img,
			Stack<Point> locations, Point aux) {
		final int maxW = img.getWidth() - 1;
		final int maxH = img.getHeight() - 1;
		if (aux.x > 0 && !new Color(img.getRGB(x - 1, y)).equals(Color.RED)) {
			locations.push(new Point(x - 1, y));
		}

		if (aux.x > 0 && aux.y > 0
				&& !new Color(img.getRGB(x - 1, y - 1)).equals(Color.RED)) {
			locations.push(new Point(x - 1, y - 1));
		}

		if (aux.y > 0 && !new Color(img.getRGB(x, y - 1)).equals(Color.RED)) {
			locations.push(new Point(x, y - 1));
		}

		if (aux.x < maxW && aux.y > 0
				&& !new Color(img.getRGB(x + 1, y - 1)).equals(Color.RED)) {
			locations.push(new Point(x + 1, y - 1));
		}

		if (aux.x < maxW && !new Color(img.getRGB(x + 1, y)).equals(Color.RED)) {
			locations.push(new Point(x + 1, y));
		}

		if (aux.x < maxW && aux.y < maxH
				&& !new Color(img.getRGB(x + 1, y + 1)).equals(Color.RED)) {
			locations.push(new Point(x + 1, y + 1));
		}

		if (aux.y < maxH && !new Color(img.getRGB(x, y + 1)).equals(Color.RED)) {
			locations.push(new Point(x, y + 1));
		}

		if (aux.y < maxH && x > 0
				&& !new Color(img.getRGB(x - 1, y + 1)).equals(Color.RED)) {
			locations.push(new Point(x - 1, y + 1));
		}
	}

	/**
	 *M�todo que faz o crescimento de regi�es
	 */
	public void segmentation(BufferedImage img, int x, int y, BufferedImage img2) {
		final Stack<Point> locations = new Stack<Point>();

		Point aux;
		locations.push(new Point(x, y));
		// img.setRGB(x, y, Color.RED.getRGB());
		while (!locations.empty()) {
			aux = locations.pop();
			// img.setRGB(aux.x, aux.y, Color.RED.getRGB());

			if (new Color(img.getRGB(aux.x, aux.y)).equals(Color.WHITE)) {
				img.setRGB(aux.x, aux.y, Color.RED.getRGB());
				img2.setRGB(aux.x, aux.y, Color.WHITE.getRGB());
				erodePixel(img2, aux.x, aux.y);
				contador++;
				pontos.add(aux);
				nextStep(aux.x, aux.y, img, locations, aux);
			}
		}

		// window.finishSegmentation(result);

	}

}
