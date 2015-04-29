package cn.kelaile.templateparser;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.vietocr.ImageIOHelper;

import org.apache.log4j.Logger;

public class OCRHelper {
	public static Tesseract1 TESS1 = null;
	public static Tesseract1 TESSENG = null;
	static {
		System.setProperty("jsse.enableSNIExtension", "false");
		TESS1 = new Tesseract1();
		TESS1.setDatapath("/usr/share/tesseract-ocr/tessdata");
		TESS1.setLanguage("eng");
		TESS1.setTessVariable("tessedit_char_whitelist", "0123456789.");
		TESS1.setPageSegMode(TessAPI.TessPageSegMode.PSM_SINGLE_WORD);
		TESS1.initTesseract();
		TESSENG = new Tesseract1();
		TESSENG.setDatapath("/usr/share/tesseract-ocr/tessdata");
		TESSENG.setLanguage("eng");
		TESSENG.setPageSegMode(TessAPI.TessPageSegMode.PSM_SINGLE_WORD);
		TESSENG.initTesseract();
		
	}
	private final static Logger log = Logger.getLogger(OCRHelper.class);
	private static final int MinHeight = 24;
	private static final int MinWidth = 24;

	private static String OCRImage(BufferedImage image, Tesseract1 tess1) {
		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage ocrImage = image;
		if (width < MinWidth || height < MinHeight) {
			ocrImage = BufferedImageHelper.zoomInImage(image, 2);
			log.info("zoom In 2");
		}
		String s = null;
		try {
			s = tess1.doOCR(ocrImage.getWidth(), ocrImage.getHeight(),
					ImageIOHelper.convertImageData(ocrImage), new Rectangle(0,
							0, ocrImage.getWidth(), ocrImage.getHeight()), 1);

		} catch (TesseractException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (s != null) {
				s = s.replaceAll("[^\\d.]", "");
			}
			try {
				if (s == null || s.isEmpty()) {
					int scale = 10;
					BufferedImage fiveImage = BufferedImageHelper
							.drawBufferedImageFive(ocrImage, scale);
					s = tess1.doOCR(
							fiveImage.getWidth(),
							fiveImage.getHeight(),
							ImageIOHelper.convertImageData(fiveImage),
							new Rectangle(0, 0, fiveImage.getWidth(), fiveImage
									.getHeight()), 1);
					if (s != null && s.length() > 0) {
						s = s.replaceAll("[^\\d.]", "");
						if (s.length() > 1)
							s = s.substring(0, Math.max(s.length() / scale, 1));
					}
				}
			} catch (TesseractException e) {
				log.error(e.getMessage(), e);
			}
		}
		return s;
	}
	public static String OCREngImage(BufferedImage image){
		String result=OCRImage2(image, TESSENG);
		return result;
	}
	private static String OCRImage2(BufferedImage image, Tesseract1 tess1) {
		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage ocrImage = image;
		if (width < MinWidth || height < MinHeight) {
			ocrImage = BufferedImageHelper.zoomInImage(image, 2);
			log.info("zoom In 2");
		}
		String s = null;
		try {
//			s= tess1.doOCR(ocrImage);
			s = tess1.doOCR(ocrImage.getWidth(), ocrImage.getHeight(),
					ImageIOHelper.convertImageData(ocrImage), new Rectangle(0,
							0, ocrImage.getWidth(), ocrImage.getHeight()), 1);

		} catch (TesseractException e) {
			log.error(e.getMessage(), e);
		} finally {
			
			try {
				if (s == null || s.isEmpty()) {
					int scale = 10;
					BufferedImage fiveImage = BufferedImageHelper
							.drawBufferedImageFive(ocrImage, scale);
					s = tess1.doOCR(
							fiveImage.getWidth(),
							fiveImage.getHeight(),
							ImageIOHelper.convertImageData(fiveImage),
							new Rectangle(0, 0, fiveImage.getWidth(), fiveImage
									.getHeight()), 1);
					
				}
			} catch (TesseractException e) {
				log.error(e.getMessage(), e);
			}
		}
		return s;
	}

	/**
	 * 
	 * @param image
	 * @return
	 */
	public static String OCRImage(BufferedImage image) {
		return OCRImage(image, TESS1);
	}
	

	private static String OCRImage(ByteBuffer buffer,int width,int height, Tesseract1 tess1) {
		String s = null;
		try {
			s = tess1.doOCR(width, height, buffer, new Rectangle(0,
							0, width, height), 1);

		} catch (TesseractException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (s != null) {
				s = s.replaceAll("[^\\d.]", "");
			}
		}
		return s;
	}

	/**
	 * 
	 * @param image
	 * @return
	 */
	public static String OCRImage(ByteBuffer buffer,int width,int height) {
		return OCRImage(buffer, width, height, TESS1);
	}
	
	public static void main(String [] args) throws IOException{

	    System.out.print(OCREngImage(ImageIO.read(new File("/Users/zhangdebin/Documents/tablenotest.png"))));
//		byte[] bytes = BufferedImageHelper.bufferedImageToByteArray( ImageIO.read(new File("/Users/sixwww/Downloads/3081-rtableNo.png")));
//	    System.out.print(OCRImage(ByteBuffer.wrap(bytes), 480, 1238));
		
	}
}
