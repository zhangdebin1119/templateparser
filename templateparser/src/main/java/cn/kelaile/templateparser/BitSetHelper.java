package cn.kelaile.templateparser;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.log4j.Logger;



public class BitSetHelper {
	
	private static Logger log = Logger.getLogger(BitSetHelper.class);
	/**
	 * 计算图片下面的空白高度
	 * 
	 * @param bitSet
	 * @param width
	 * @param height
	 * @return
	 */
	public static int getWhiteHeight(BitSet bitSet, int width, int height) {
		if (bitSet == null) {
			return 0;
		}
		BitSet empty = new BitSet(width);
		int y = height - 1;
		while (y > 0) {
			BitSet b = getSubBitSet(bitSet, width, 1, new Point(0, y), width);
			if (!b.equals(empty)) {
				break;
			}
			y--;
		}
		return height - y;
	}
	/**
	 * 切掉支付宝二维码的区域
	 * https://tfsimg.alipay.com/images/mobilecodec/T1y9dcXmBJXXXXXXXX
	 * @param imageData
	 * @return
	 */
	public static int getAliQrCodeBitSet(ImageData imageData){
		if (imageData == null || imageData.getBitSet() == null ||imageData.getHeight() == 0|| imageData.getWidth() == 0) {
			return 0;
		}
		int y = 0;
		int threshold = imageData.getHeight() / 10;
		System.out.println("当前:" + threshold);
		int width =imageData.getWidth();
		BitSet bitSet = imageData.getBitSet();
		while(y < imageData.getHeight()){
			BitSet b = getSubBitSet(bitSet, width, 1, new Point(0, y), width);
			System.out.println(b.cardinality() + "当前:" + y);
			if(b.cardinality() > threshold){
				break;
			}
			y++;
		}
		return Math.min(y - 10, width);
	}
	/**
	 * 
	 * @param data
	 * @return
	 */
	public static BufferedImage bitSetToBufferedImage(ImageData data) {
		if (data == null || data.getBitSet() == null)
			return null;
		return bitSetToBufferedImage(data.getBitSet(), data.getWidth(),
				data.getHeight());
	}

	/**
	 * 
	 * @param bitSet
	 * @param width
	 * @param height
	 * @return
	 */
	public static BufferedImage bitSetToBufferedImage(BitSet bitSet, int width,
			int height) {
		if(height<=0||width<=0){
			return null;
		}
		BufferedImage bufferedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D graphics2d = bufferedImage.createGraphics();
		graphics2d.setBackground(Color.WHITE);
		graphics2d.clearRect(0, 0, width, height);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (bitSet.get(j * width + i)) {
					graphics2d.setColor(Color.BLACK);
					graphics2d.fillRect(i, j, 1, 1);
				}
			}
		}
		return bufferedImage;
	}
	
	/**
	 * 计算图片下面的空白高度
	 * 
	 * @param data
	 * @return
	 */
	public static int getWhiteHeight(ImageData data) {
		if (data == null || data.getBitSet() == null) {
			return 0;
		}
		return getWhiteHeight(data.getBitSet(), data.getWidth(),
				data.getHeight());
	}
	
	/**
	 * 查找算法
	 * 
	 * @param originImageData
	 * @param findBitSet
	 * @param findWidth
	 * @param startIndex
	 * @param borderIndex
	 * @param direction
	 *            方向 true 表示从左到右 false 表示从右到左
	 * @return
	 */
	public static int searchBitSet(ImageData originImageData,
			BitSet findBitSet, int findWidth, int startIndex, int borderIndex,
			boolean direction) {
		if (originImageData == null || originImageData.getBitSet() == null
				|| originImageData.getBitSet().length() == 0
				|| originImageData.getWidth() == 0
				|| originImageData.getHeight() == 0 || findBitSet == null
				|| findWidth == 0 || findWidth > originImageData.getWidth()
				|| startIndex > originImageData.getWidth())
			return -1;
		return direction ? searchLeftBitSet(originImageData, findBitSet,
				findWidth, startIndex, borderIndex)
				: searchRightBitSet(originImageData, findBitSet, findWidth,
						startIndex, borderIndex);
	}

	/**
	 * 从左到右
	 * 
	 * @param originImageData
	 * @param findBitSet
	 * @param findWidth
	 * @param startIndex
	 * @param borderIndex
	 * @return
	 */
	private static int searchLeftBitSet(ImageData originImageData,
			BitSet findBitSet, int findWidth, int startIndex, int borderIndex) {

		int halfHeight = originImageData.getHeight() / 2;
		List<Integer> imageIndexs = new ArrayList<Integer>();
		BitSet pixels = findBitSet.get(halfHeight * findWidth, (halfHeight + 1)
				* findWidth);
		String f = bitSet2String(pixels).substring(0, findWidth);
		BitSet epixels = originImageData.getBitSet().get(
				halfHeight * originImageData.getWidth(),
				(halfHeight + 1) * originImageData.getWidth());
		if(epixels == null)
			return -1;
		String temp = bitSet2String(epixels);
		if(temp.length() < originImageData.getWidth())
			return -1;
		String f1 = temp.substring(0,
				originImageData.getWidth());
		int i = f1.indexOf(f);
		while (i > -1) {
			imageIndexs.add(i);
			if (i + 1 > f1.length())
				break;
			i = f1.indexOf(f, i + 1);
		}
		if (imageIndexs.size() == 0)
			return -1;
		if (imageIndexs.size() == 1)
			return imageIndexs.get(0);
		for (int m : imageIndexs) {
			BitSet leftBitSet = BitSetHelper.getSubBitSet(
					originImageData.getBitSet(), findWidth,
					originImageData.getHeight(), new Point(m, 0),
					originImageData.getWidth());
			if (findBitSet.equals(leftBitSet)) {
				return m;
			}
		}
		return -1;
	}

	/**
	 * 从右到左
	 * 
	 * @param originImageData
	 * @param findBitSet
	 * @param findWidth
	 * @param startIndex
	 * @param borderIndex
	 * @return
	 */
	private static int searchRightBitSet(ImageData originImageData,
			BitSet findBitSet, int findWidth, int startIndex, int borderIndex) {
		int halfHeight = originImageData.getHeight() / 2;
		List<Integer> imageIndexs = new ArrayList<Integer>();
		BitSet pixels = findBitSet.get(halfHeight * findWidth, (halfHeight + 1)
				* findWidth);
		String f = bitSet2String(pixels);
		if (f == null || f.length() < findWidth)
			return -1;
		f = f.substring(0, findWidth);
		BitSet epixels = originImageData.getBitSet().get(
				halfHeight * originImageData.getWidth(),
				(halfHeight + 1) * originImageData.getWidth());
		if(epixels == null)
			return -1;
		String temp = bitSet2String(epixels);
		if(temp.length() < originImageData.getWidth())
			return -1;
		String f1 = bitSet2String(epixels).substring(0,
				originImageData.getWidth());
		int i = f1.indexOf(f);
		while (i > -1) {
			imageIndexs.add(i);
			if (i + 1 > f1.length())
				break;
			i = f1.indexOf(f, i + 1);
		}
		if (imageIndexs.size() == 0)
			return -1;
		if (imageIndexs.size() == 1)
			return imageIndexs.get(0);
		for (int m : imageIndexs) {
			BitSet leftBitSet = BitSetHelper.getSubBitSet(
					originImageData.getBitSet(), findWidth,
					originImageData.getHeight(), new Point(m, 0),
					originImageData.getWidth());
			if (findBitSet.equals(leftBitSet)) {
				return m;
			}
		}
		return -1;
	}


	/**
	 * 
	 * @param bitSet
	 * @return
	 */
	public static byte[] bitSet2ByteArray(BitSet bitSet) {
		byte[] bytes = new byte[bitSet.size() / 8];
		for (int i = 0; i < bitSet.size(); i++) {
			int index = i / 8;
			int offset = 7 - i % 8;
			bytes[index] |= (bitSet.get(i) ? 1 : 0) << offset;
		}
		return bytes;
	}

	
	/**
	 * 从01010字符串变成BitSet
	 * 
	 * @param s
	 * @return
	 */
	public static BitSet createFromString(String s) {
		BitSet t = new BitSet(s.length());
		int lastBitIndex = s.length() - 1;
		int i = lastBitIndex;
		while (i >= 0) {
			if (s.charAt(i) == '1') {
				t.set(i);
			}
			i--;
		}
		return t;
	}

	/**
	 * BitSet变成01010字符串
	 * 
	 * @param bs
	 * @return
	 */
	public static String bitSet2String(BitSet bs) {
		StringBuffer stringbuffer = new StringBuffer(bs.length());
		for (int i = 0; i < bs.size(); i++) {
			stringbuffer.append(bs.get(i) ? '1' : '0');
		}
		return stringbuffer.toString();
	}

	/**
	 * 
	 * @param bs
	 * @param end
	 * @return
	 */
	public static String bitSet2String(BitSet bs, int end) {
		StringBuffer stringbuffer = new StringBuffer(bs.length());
		for (int i = 0; i < end; i++) {
			stringbuffer.append(bs.get(i) ? '1' : '0');
		}
		return stringbuffer.toString();
	}
	
	/**
	 * 
	 * @param bitSet
	 * @param width
	 * @param height
	 * @param x
	 * @param y
	 * @param originalWidth
	 * @return
	 */
	public static BitSet getSubBitSet(BitSet bitSet, int width, int height,
			int x,int y, int originalWidth) {
		return getSubBitSet( bitSet,  width,  height,
				new Point(x,y),  originalWidth);
	}
	/**
	 * 
	 * @param bitSet
	 *            原始bitSet
	 * @param width
	 *            需要截取的宽度
	 * @param height
	 *            需要截取的高度
	 * @param p
	 *            起始点
	 * @param originalWidth
	 *            原始宽度
	 * @return
	 */
	public static BitSet getSubBitSet(BitSet bitSet, int width, int height,
			Point p, int originalWidth) {
		if(width>originalWidth){
			log.error("getBitSet.width >originalWidth,will return null");
			return null;
		}
		BitSet b = new BitSet(width * height);
		for (int i = p.x; i < p.x + width; i++) {
			for (int j = p.y; j < p.y + height; j++) {
				b.set((j - p.y) * width + i - p.x,
						bitSet.get(j * originalWidth + i));
			}
		}
		return b;
	}
	public static BitSet byteArray2BitSet(byte[] bytes) {
		BitSet bitSet = new BitSet(bytes.length * 8);
		int index = 0;
		for (int i = 0; i < bytes.length; i++) {
			for (int j = 7; j >= 0; j--) {
				bitSet.set(index++, (bytes[i] & (1 << j)) >> j == 1 ? true
						: false);
			}
		}
		return bitSet;
	}


}
