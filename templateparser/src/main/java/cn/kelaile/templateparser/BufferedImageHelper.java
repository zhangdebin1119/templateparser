package cn.kelaile.templateparser;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.BitSet;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;


public class BufferedImageHelper {
	private final static Logger log = Logger.getLogger(BufferedImageHelper.class);

	/**
	 * 接收到的二进制转成BufferedImage
	 * 
	 * @param bytes
	 * @return
	 */
	public static BufferedImage byteToBufferedImage(byte[] bytes) {
		BufferedImage image = null;
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
			image = ImageIO.read(bin);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return image;
	}

	/**
	 * 把图片（BufferedImage）平铺scale次
	 * 
	 * @param src
	 * @return
	 */
	public static BufferedImage drawBufferedImageFive(BufferedImage src,
			int scale) {
		int width = src.getWidth() * scale;
		int height = src.getHeight();
		BufferedImage newImage = new BufferedImage(width, height, src.getType());
		Graphics2D g = newImage.createGraphics();
		for (int i = 0; i < scale; i++) {
			g.drawImage(src, i * src.getWidth(), 0, src.getWidth(),
					src.getHeight(), null);
		}
		g.dispose();
		return newImage;
	}

	/**
	 * 二维码字节流变成ImageData(BitSet bitSet, int width, int height)
	 * 
	 * @param buffer
	 * @return
	 */
	public static BitSet bufferedImageToBitSet(BufferedImage buffer,
			int threshold) {
		if (buffer == null) {
			return null;
		}
		int width = buffer.getWidth();
		int height = buffer.getHeight();
		BitSet bitSet = new BitSet(width * height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				bitSet.set(y * width + x, buffer.getRGB(x, y) <= threshold);
			}
		}
		return bitSet;
	}
	/**
	 * 二维码字节流变成ImageData(BitSet bitSet, int width, int height)
	 * 
	 * @param buffer
	 * @return
	 */
	public static ImageData bufferedImageToImageData(BufferedImage buffer,
			int threshold) {
		if (buffer == null) {
			return null;
		}
		int width = buffer.getWidth();
		int height = buffer.getHeight();
		BitSet bitSet = new BitSet(width * height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				bitSet.set(y * width + x, buffer.getRGB(x, y) <= threshold);
			}
		}
		return new ImageData(bitSet, width, height);
	}

	/**
	 * 二维码流变成ImageData
	 * 
	 * @param buffer
	 * @return
	 */
	public static ImageData bufferedImageToImageData(BufferedImage buffer) {
		return bufferedImageToImageData(buffer, new Color(0, 0, 0).getRGB());
	}

	/**
	 * 
	 * @param src
	 * @return
	 */
	public static byte[] bufferedImageToByteArray(BufferedImage src) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ImageIO.write(src, "png", os);
			return os.toByteArray();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 图片保存
	 * 
	 * @param src
	 * @param filepath
	 */
	public static void bufferedImageToFile(BufferedImage src, String filepath) {
		log.debug("存储文件 --- " + filepath);
		File imgfile = new File(filepath);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(imgfile);
			ImageIO.write(src, "png", fos);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 对图片进行放大
	 * 
	 * @param originalImage
	 *            原始图片
	 * @param times
	 *            放大倍数
	 * @return
	 */
	public static BufferedImage zoomInImage(BufferedImage originalImage,
			Integer times) {
		int width = originalImage.getWidth() * times;
		int height = originalImage.getHeight() * times;
		BufferedImage newImage = new BufferedImage(width, height,
				originalImage.getType());
		Graphics2D g = newImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();
		return newImage;
	}
}
