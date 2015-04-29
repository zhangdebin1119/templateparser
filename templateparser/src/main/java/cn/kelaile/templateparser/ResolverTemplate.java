package cn.kelaile.templateparser;

import java.awt.Point;
import java.io.IOException;
import java.util.BitSet;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

/**
 * 
 * @author zhangdebin
 *
 */
public class ResolverTemplate {
	public enum TemplateAlign {
		Left(1), Middle(3), Right(2);
		private final int value;

		private TemplateAlign(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}

		public static TemplateAlign fromInt(int key) {
			for (TemplateAlign type : TemplateAlign.values()) {
				if (type.value == key) {
					return type;
				}
			}
			return null;
		}
	}

	private final static Logger log = Logger
			.getLogger(ResolverTemplate.class);

	private final String FieldName_KEY = "FieldName";
	private final String LeftFeature_KEY = "LeftFeature";
	private final String LeftWidth_KEY = "LeftWidth";
	private final String RightFeature_KEY = "RightFeature";
	private final String RightWidth_KEY = "RightWidth";
	private final String Line_KEY = "Line";
	private final String Height_KEY = "Height";
	private final String Align_KEY = "Align";

	public ResolverTemplate() {
	}

	public ResolverTemplate(JSONObject object, boolean isImg) {
		if (object.has(FieldName_KEY))
			this.fieldName = object.getString(FieldName_KEY);
		if (object.has(Line_KEY))
			this.lineNum = object.getInt(Line_KEY);
		if (object.has(LeftWidth_KEY))
			this.leftWidth = object.getInt(LeftWidth_KEY);
		try {
			if (object.has(LeftFeature_KEY)) {
				String left = object.getString(LeftFeature_KEY);
				if (left != null && !left.trim().isEmpty()) {
					this.leftString = isImg ? StringHelper.uncompress(left)
							: left;
				}else if (!isImg) {
					this.leftString = "";
				}
			}
			if (object.has(RightFeature_KEY)) {
				String right = object.getString(RightFeature_KEY);
				if (right != null && !right.trim().isEmpty()) {
					this.rightString = isImg ? StringHelper.uncompress(right)
							: right;
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		if (object.has(RightWidth_KEY))
			this.rightWidth = object.getInt(RightWidth_KEY);
		if (object.has(Height_KEY))
			this.height = object.getInt(Height_KEY);
		if (object.has(Align_KEY)) {
			int a = object.getInt(Align_KEY);
			this.align = TemplateAlign.fromInt(a);
		}
	}

	private String fieldName;
	private String leftString;
	private String rightString;
	private int lineNum;
	private int leftWidth;
	private int rightWidth;
	private int height;
	/**
	 * 1 左对齐 2 右对齐 3 居中
	 */
	private TemplateAlign align;

	public TemplateAlign getAlign() {
		return align;
	}

	public void setAlign(TemplateAlign align) {
		this.align = align;
	}

	public int getLeftWidth() {
		return leftWidth;
	}

	public void setLeftWidth(int leftWidth) {
		this.leftWidth = leftWidth;
	}

	public int getRightWidth() {
		return rightWidth;
	}

	public void setRightWidth(int rightWidth) {
		this.rightWidth = rightWidth;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getLeftString() {
		return leftString;
	}

	public void setLeftString(String leftString) {
		this.leftString = leftString;
	}

	public String getRightString() {
		return rightString;
	}

	public void setRightString(String rightString) {
		this.rightString = rightString;
	}

	public int getLineNum() {
		return lineNum;
	}

	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

	/**
	 * 从文本获得信息
	 * 
	 * @param receipt
	 * @return
	 */
	public String getFieldValue(String receipt) {
		if (receipt == null || receipt.isEmpty())
			return null;
		String[] lines = receipt.split("\n");
		if (lines.length < Math.abs(lineNum))
			return null;
		int realNum = (lineNum > 0 ? (lineNum - 1) : (lines.length + lineNum));
		if (realNum > lines.length - 1 || realNum < 0)
			return null;
		String findString = lines[realNum];
		if (findString == null || findString.isEmpty())
			return null;
		int leftIndex = 0;
		if (leftString != null && leftString.length() > 0)
			leftIndex = findString.indexOf(leftString);
		int rightIndex = findString.length();
		if (rightString != null && rightString.length() > 0) {
			if (leftIndex > 0) {
				rightIndex = findString.substring(leftIndex).indexOf(
						rightString);
				if (rightIndex > 0) {
					rightIndex += leftIndex;
				}
			} else {
				rightIndex = findString.indexOf(rightString);
			}
		}
		if (leftIndex == -1 || rightIndex == -1)
			return null;
		try {
			return findString.substring(leftIndex + leftString.length(),
					rightIndex);
		} catch (StringIndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * 取左开始索引
	 * 
	 * @param width
	 * @return
	 */
	private int getLeftStart(BitSet bitSet, int width) {
		if (bitSet == null || width == 0) {
			return -1;
		}
		BitSet lBitSet = BitSetHelper.createFromString(leftString);
		int startIndex = 0;
		int borderIndex = width;
		int findIndex = -1;
		ImageData originImageData = new ImageData(bitSet, width, height);
		switch (align) {
		case Left:
			startIndex = 0;
			borderIndex = width - leftWidth - rightWidth;
			findIndex = BitSetHelper.searchBitSet(originImageData, lBitSet,
					leftWidth, startIndex, borderIndex, true);
			break;
		case Middle:
			startIndex = (width - rightWidth + leftWidth) / 2;
			borderIndex = 0;
			findIndex = BitSetHelper.searchBitSet(originImageData, lBitSet,
					leftWidth, startIndex, borderIndex, false);
			break;
		case Right:
			startIndex = width - leftWidth - rightWidth;
			borderIndex = 0;
			findIndex = BitSetHelper.searchBitSet(originImageData, lBitSet,
					leftWidth, startIndex, borderIndex, false);
			break;
		default:
			log.warn("no this align:" + align);
			break;
		}
		return findIndex > -1 ? (findIndex + leftWidth) : -1;
	}

	/**
	 * 取右开始索引
	 * 
	 * @param width
	 * @return
	 */
	private int getRightStart(BitSet bitSet, int width, int leftIndex) {
		if (bitSet == null || width == 0)
			return 0;
		BitSet rBitSet = BitSetHelper.createFromString(rightString);
		int startIndex = leftIndex;
		ImageData originImageData = new ImageData(bitSet, width, height);
		int borderIndex = width - rightWidth;
		return BitSetHelper.searchBitSet(originImageData, rBitSet, rightWidth,
				startIndex, borderIndex, false);
	}

	/**
	 * 从图片获得信息
	 * 
	 * @param data
	 * @return
	 */
	public String getFieldValueFromImgDatas(ImageData data, int whiteHeight,
			int correctY) {
		if (data == null)
			return null;
		if (data.getHeight() < Math.abs(lineNum))
			return null;
		if (height < 1)
			return null;
		int realY = lineNum > 0 ? (lineNum - 1)
				: (data.getHeight() + lineNum - whiteHeight);
		realY = realY + correctY;
		if(realY<0){
			log.info("realY="+realY+"<0");
			return null;
		}
		log.debug("高度 " + height + " Y坐标" + realY + " lineNum " + lineNum);
		int leftIndex = data.getWidth();
		int rightIndex = 0;
		BitSet bitSet = BitSetHelper.getSubBitSet(data.getBitSet(),
				data.getWidth(), height, new Point(leftIndex, realY),
				data.getWidth());
		if (leftString != null && leftString.length() > 0) {
			leftIndex = getLeftStart(bitSet, data.getWidth());
		} else {
			leftIndex = 0;
		}
		if (rightString != null && rightString.length() > 0) {
			rightIndex = getRightStart(bitSet, data.getWidth(), leftIndex);
		} else {
			rightIndex = data.getWidth();
		}
		log.debug("RightIndex ---" + rightIndex + "LeftIndex ---" + leftIndex);
		if (rightIndex <= leftIndex || rightIndex == -1 || leftIndex == -1)
			return null;
		if (leftIndex == 0 && rightIndex == data.getWidth())
			return OCRHelper.OCRImage(BitSetHelper.bitSetToBufferedImage(
					bitSet, rightIndex - leftIndex, height));
		BitSet findBitSet = BitSetHelper.getSubBitSet(bitSet, rightIndex
				- leftIndex + 1, height, new Point(leftIndex, 0),
				data.getWidth());
		return OCRHelper.OCRImage(BitSetHelper.bitSetToBufferedImage(
				findBitSet, rightIndex - leftIndex + 1, height));
	}
}
