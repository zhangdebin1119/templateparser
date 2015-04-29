package cn.kelaile.templateparser;

import java.awt.Point;
import java.io.IOException;
import java.util.BitSet;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;


public class Detector {
	private final static Logger log = Logger.getLogger(Detector.class);
	private final String Feature_KEY = "Feature";
	private final String ExclusionType_KEY = "ExclusionType";
	private final String Line_KEY = "Line";
	private final String Height_KEY = "Height";
	private final String Width_KEY = "Width";

	private String feature;
	private int exclusionType;
	private int line;
	private int height;
	private int width;

	public Detector() {
	}

	public Detector(JSONObject object, boolean isImg) {
		if (object.has(Feature_KEY)) {
			try {
				String f = object.getString(Feature_KEY);
				if (f != null && !f.trim().isEmpty()) {
					this.feature = isImg ? StringHelper.uncompress(f) : f;
				}
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		if (object.has(ExclusionType_KEY))
			this.exclusionType = object.getInt(ExclusionType_KEY);
		if (object.has(Line_KEY))
			this.line = object.getInt(Line_KEY);
		if (object.has(Height_KEY))
			this.height = object.getInt(Height_KEY);
		if (object.has(Width_KEY))
			this.width = object.getInt(Width_KEY);
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public String getFeature() {
		return feature;
	}

	public void setFeature(String feature) {
		this.feature = feature;
	}

	public int getExclusionType() {
		return exclusionType;
	}

	public void setExclusionType(int exclusionType) {
		this.exclusionType = exclusionType;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * 判读是否包含
	 * 
	 * @param receipt
	 * @return
	 */
	public boolean judgeString(String receipt) {
		if (receipt == null || receipt.isEmpty())
			return false;
		String[] lines = receipt.split("\n");
		if (lines.length < Math.abs(line))
			return false;
		int realNum = (line > 0 ? (line - 1) : (lines.length + line));
		if (realNum > lines.length - 1 || realNum < 0)
			return false;
		String findString = lines[realNum];
		if (findString == null || findString.isEmpty())
			return false;
		boolean finded = findString.contains(feature);
		if (exclusionType == 2)
			return finded;
		else
			return !finded;
	}

	/**
	 * 
	 * @param data
	 * @return
	 */
	public boolean judgeImgDatas(ImageData data, int whiteHeight, int correctY) {
		if (data == null) {
			return false;
		}
		if (data.getHeight() < Math.abs(line)) {
			return false;
		}
		int realY = line;
		if (line < 0) {
			realY = data.getHeight() + line - whiteHeight;
		}
		if(realY < 0){
			return exclusionType == 1;
		} 
		realY += correctY;
		int leftIndex = 0;
		BitSet bitSet = BitSetHelper.getSubBitSet(data.getBitSet(),
				data.getWidth(), height, new Point(leftIndex, realY),
				data.getWidth());
		ImageData originImageData = new ImageData(bitSet, data.getWidth(),
				height);
		int startIndex = data.getWidth() - width;
		int borderIndex = 0;
		BitSet b = BitSetHelper.createFromString(feature);
		int findIndex = BitSetHelper.searchBitSet(originImageData, b, width,
				startIndex, borderIndex, false);
		if (exclusionType == 2)
			return findIndex > -1;
		else
			return findIndex == -1;
	}
}