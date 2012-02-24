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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class Rotula {
	Integer rotulosCount;
	List<Point> pontos;
	int contador = 0;
	HashMap<Integer, Rotulo> rotulos = new HashMap<Integer, Rotulo>();

	public void coloreContorno(BufferedImage img) {
		rotulosCount = 0;

		boolean canRotula = false;
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				if (new Color(img.getRGB(x, y)).equals(Color.WHITE)) {
					canRotula = true;
				}
				if (canRotula && img.getRGB(x, y) == Color.BLACK.getRGB()
						&& hasNextWhite(x, y, img)) {

					pontos = new ArrayList<Point>();
					contador = 0;
					segmentation(img, x, y);

					rotulos.put(rotulosCount++, new Rotulo(pontos, img));

					canRotula = false;
				}
			}
		}
		verify(img);

	}

	private boolean hasNextWhite(int x, int y, BufferedImage img) {
		for (int h = y; h < img.getHeight(); h++) {
			if (img.getRGB(x, h) == Color.WHITE.getRGB())
				return true;
		}
		return false;
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

		// if((aux.x>0) && (aux.y >0) && !(new Color(img.getRGB(x -1,
		// y-1)).equals(Color.RED)))
		// locations.push(new Point(x-1,y-1));

		if (aux.y > 0 && !new Color(img.getRGB(x, y - 1)).equals(Color.RED)) {
			locations.push(new Point(x, y - 1));
		}

		// if((aux.x<maxW) && (aux.y>0) && !(new Color(img.getRGB(x+ 1,
		// y-1)).equals(Color.RED)))

		// locations.push(new Point(x+1, y-1));

		if (aux.x < maxW && !new Color(img.getRGB(x + 1, y)).equals(Color.RED)) {
			locations.push(new Point(x + 1, y));
		}

		// if((aux.x<maxW) && (aux.y<maxH) && !(new Color(img.getRGB(x +1,
		// y+1)).equals( Color.RED)))
		// locations.push(new Point(x+1,y+1));

		if (aux.y < maxH && !new Color(img.getRGB(x, y + 1)).equals(Color.RED)) {
			locations.push(new Point(x, y + 1));
		}

		// if((aux.y<maxH) && (x > 0) && !(new Color(img.getRGB(x -1,
		// y+1)).equals(Color.RED)))
		// locations.push(new Point(x -1,y+1));
	}

	/**
	 *M�todo que faz o crescimento de regi�es
	 */
	public void segmentation(BufferedImage img, int x, int y) {
		final Stack<Point> locations = new Stack<Point>();

		Point aux;
		locations.push(new Point(x, y));
		// img.setRGB(x, y, Color.RED.getRGB());
		while (!locations.empty()) {
			aux = locations.pop();
			// img.setRGB(aux.x, aux.y, Color.RED.getRGB());

			if (new Color(img.getRGB(aux.x, aux.y)).equals(Color.BLACK)) {
				contador++;
				img.setRGB(aux.x, aux.y, Color.RED.getRGB());
				pontos.add(aux);
				nextStep(aux.x, aux.y, img, locations, aux);
			}
		}

		// window.finishSegmentation(result);

	}

	private void verify(BufferedImage img) {
		for (int x = 0; x < rotulos.values().size(); x++) {
			final Rotulo a = rotulos.get(x);
			for (int y = x + 1; y < rotulos.values().size(); y++) {
				final Rotulo b = rotulos.get(y);
				final double coefA = (double) (b.center.y - a.center.y)
						/ (double) (b.center.x - a.center.x);
				final Point pMedio = new Point((a.center.x + b.center.x) / 2,
						(a.center.y + b.center.y) / 2);
				for (int z = 0; z < rotulos.values().size(); z++) {
					if (!rotulos.get(z).equals(a) && !rotulos.get(z).equals(b)) {
						final Rotulo c = rotulos.get(z);
						final double coefB = (double) (pMedio.y - c.center.y)
								/ (double) (pMedio.x - c.center.x);
						if (coefB * coefA > -1.2 && coefB * coefA < -0.8) {

							/*
							 * for(Point p : c.pontos) { img.setRGB(p.x, p.y,
							 * Color.GREEN.getRGB()); }
							 */
							img.setRGB(a.center.x, a.center.y, Color.GREEN
									.getRGB());
							img.setRGB(b.center.x, b.center.y, Color.GREEN
									.getRGB());
							img.setRGB(c.center.x, c.center.y, Color.GREEN
									.getRGB());

						}
					}
				}
			}
		}
	}

}

class Rotulo {
	public Point center;

	// public List<Point> pontos;
	public Rotulo(List<Point> pontos, BufferedImage img) {
		// this.pontos = pontos;
		int sumX = 0;
		int sumY = 0;
		for (final Point ponto : pontos) {
			if (ponto.x == 0 || ponto.y == 0 || ponto.x == img.getWidth() - 1
					|| ponto.y == img.getHeight() - 1) {
				if (img.getRGB(ponto.x, ponto.y) == Color.RED.getRGB()) {
					center = new Point(0, 0);

					for (final Point p2 : pontos) {
						img.setRGB(p2.x, p2.y, Color.BLACK.getRGB());
					}
					// pontos = new ArrayList<Point>();
					return;
				}
			} else {
				// this.pontos = pontos;
			}
			sumX += ponto.x;
			sumY += ponto.y;
		}
		sumX = sumX / pontos.size();
		sumY = sumY / pontos.size();
		center = new Point(sumX, sumY);
	}
}
