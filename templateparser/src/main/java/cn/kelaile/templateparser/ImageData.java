package cn.kelaile.templateparser;

import java.util.BitSet;

public class ImageData {
	private BitSet bitSet;
	private int width;
	private int height;

	public ImageData(BitSet bitSet, int width, int height) {
		this.bitSet = bitSet;
		this.width = width;
		this.height = height;
	}

	public BitSet getBitSet() {
		return bitSet;
	}

	public void setBitSet(BitSet bitSet) {
		this.bitSet = bitSet;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}
