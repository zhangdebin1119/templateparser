package cn.kelaile.templateparser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StringHelper {
	protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6',
		'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	
	/**
	 * 实现join方法
	 * 
	 * @param join
	 * @param strAry
	 * @return
	 */
	public static String join(String join, String[] strAry) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < strAry.length; i++) {
			if (i == (strAry.length - 1)) {
				sb.append(strAry[i]);
			} else {
				sb.append(strAry[i]).append(join);
			}
		}
		return sb.toString();
	}
	
	/**
	 * 转换成16进制
	 * 
	 * @param b
	 */
	public static String convertToHexString(byte... b) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			char c0 = hexDigits[(b[i] & 0xf0) >> 4];
			char c1 = hexDigits[b[i] & 0xf];
			sb.append(c0);
			sb.append(c1);
		}
		return sb.toString();
	}
	
	/**
	 * 转换成16进制
	 * 
	 * @param b
	 */
	public static String convertToHexString(byte[]... b) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			for (int j = 0; j < b[i].length; j++) {
				char c0 = hexDigits[(b[i][j] & 0xf0) >> 4];
				char c1 = hexDigits[b[i][j] & 0xf];
				sb.append(c0);
				sb.append(c1);
			}
		}
		return sb.toString();
	}
	
	/**
	 * 转换成16进制
	 * 
	 * @param b
	 */
	public static String convertToTempHexString(byte... b) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			char c0 = hexDigits[(b[i] & 0xf0) >> 4];
			char c1 = hexDigits[b[i] & 0xf];
			sb.append(c0);
			sb.append(c1);
			sb.append(' ');
		}
		return sb.toString().toLowerCase();
	}
	
	/**
	 * 低位在前
	 * 
	 * @param x
	 * @param length
	 * @return
	 */
	public static byte[] intToLowBytes(int x, int length) {
		length = Math.min(4, length);
		if (length < 1)
			return null;
		byte[] result = new byte[length];
		for (int i = 0; i < length; i++)
			result[i] = (byte) ((x >> (i * 8)) & 0xff);
		return result;
	}
	
	/**
	 * 低位在前
	 * 
	 * @param x
	 * @param length
	 * @return
	 */
	public static byte[] longToLowBytes(long x, int length) {
		length = Math.min(8, length);
		if (length < 1)
			return null;
		byte[] result = new byte[length];
		for (int i = 0; i < length; i++)
			result[i] = (byte) ((x >> (i * 8)) & 0xff);
		return result;
	}
	
	/**
	 * 低位在前
	 * 
	 * @param bytes
	 * @return
	 */
	public static int lowBytesToInt(byte... bytes) {
		if (bytes == null)
			return 0;
		int result = 0;
		int length = bytes.length;
		for (int i = 0; i < length; i++)
			result += ((bytes[i] & 0xff) << (i * 8));
		return result;
	}
	
	/**
	 * 低位在前
	 * 
	 * @param bytes
	 * @return
	 */
	public static long lowBytesToLong(byte... bytes) {
		if (bytes == null)
			return 0;
		long result = 0;
		int length = bytes.length;
		for (int i = 0; i < length; i++)
			result += ((bytes[i] & 0xff) << (i * 8));
		return result;
	}
	
	/**
	 * 高位在前
	 * 
	 * @param x
	 * @param length
	 * @return
	 */
	public static byte[] intToHighBytes(int x, int length) {
		length = Math.min(4, length);
		if (length < 1)
			return null;
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(x);
		byte[] result = new byte[length];
		System.arraycopy(buffer.array(), 4 - length, result, 0, length);
		return result;
	}
	
	/**
	 * 高位在前
	 * 
	 * @param x
	 * @param length
	 * @return
	 */
	public static byte[] longToHighBytes(long x, int length) {
		length = Math.min(8, length);
		if (length < 1)
			return null;
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(x);
		byte[] result = new byte[length];
		System.arraycopy(buffer.array(), 8 - length, result, 0, length);
		return result;
	}
	
	/**
	 * 高位在前
	 * 
	 * @param bytes
	 * @return
	 */
	public static long highBytesToInt(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.put(bytes);
		buffer.flip();// need flip
		return buffer.getInt();
	}
	
	/**
	 * 高位在前
	 * 
	 * @param bytes
	 * @return
	 */
	public static long highBytesToLong(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		for (int i = 0; i < 8 - bytes.length; i++) {
			buffer.put((byte) 0);
		}
		buffer.put(bytes);
		buffer.flip();// need flip
		return buffer.getLong();
	}
	
	/**
	 * 补齐0x80 0x00 规则：补齐到8的整数倍 如果补一个0x80刚好到整数倍，则接着补8个0x00
	 * 
	 * @param bytes
	 * @return
	 */
	public static byte[] bytesPaddedToEightMultiple(byte[] bytes) {
		if (bytes == null || bytes.length == 0)
			return null;
		int paddedLength = 8 - bytes.length % 8;
		if (paddedLength == 1)
			paddedLength = 9;
		byte[] result = new byte[bytes.length + paddedLength];
		System.arraycopy(bytes, 0, result, 0, bytes.length);
		result[bytes.length] = (byte) 0x80;
		for (int i = 1; i < paddedLength; i++) {
			result[bytes.length + i] = 0x00;
		}
		return result;
	}
	
	/**
	 * 从补齐后bytes获得真实bytes
	 * 
	 * @param bytes
	 * @return
	 */
	public static byte[] getRealBytesFromPaddedBytes(byte[] bytes) {
		if (bytes == null || bytes.length == 0)
			return null;
		int length = bytes.length;
		int realLength = 0;
		for (int i = length - 1; i > 0; i--) {
			if (bytes[i] == (byte) 0x80) {
				realLength = i;
				break;
			}
		}
		if (realLength == 0)
			return null;
		byte[] result = new byte[realLength];
		System.arraycopy(bytes, 0, result, 0, realLength);
		return result;
	}
	
	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}
	
	/**
	 * Convert char to byte
	 * 
	 * @param c
	 *            char
	 * @return byte
	 */
	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	
	/**
	 * gzip解压缩
	 * 
	 * @param zipString
	 * @return
	 * @throws IOException
	 */
	public static String uncompress(String str) throws IOException {
		if (str == null || str.equals("")) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(
				hexStringToBytes(str));
		GZIPInputStream gunzip = new GZIPInputStream(in);
		byte[] buffer = new byte[256];
		int n;
		while ((n = gunzip.read(buffer)) >= 0) {
			out.write(buffer, 0, n);
		}
		return out.toString();
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static String compress(String str) throws IOException {
		if (str == null || str.length() == 0) {
			return str;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(str.getBytes());
		gzip.close();
		return out.toString("utf-8");
	}
	
	/**
	 * 通过接口compactString()的压缩方式进行解压
	 * 
	 * @param tempString
	 * @return
	 */
	public static String decompressionString(String tempString) {
		char[] tempBytes = tempString.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < tempBytes.length; i++) {
			char c = tempBytes[i];
			char firstCharacter = (char) (c >>> 8);
			char secondCharacter = (char) ((byte) c);
			sb.append(firstCharacter);
			if (secondCharacter != 0)
				sb.append(secondCharacter);
		}
		return sb.toString();
	}
	
	/**
	 * 对需要进行压缩的字符串进行压缩，返回一个相对较小的字符串
	 * 
	 * @param tempString
	 * @return
	 */
	public static String compactString(String tempString) {
		StringBuffer sb = new StringBuffer();
		byte[] tempBytes = tempString.getBytes();
		for (int i = 0; i < tempBytes.length; i += 2) {
			char firstCharacter = (char) tempBytes[i];
			char secondCharacter = 0;
			if (i + 1 < tempBytes.length)
				secondCharacter = (char) tempBytes[i + 1];
			firstCharacter <<= 8;
			sb.append((char) (firstCharacter + secondCharacter));
		}
		return sb.toString();
	}
	
	/**
	 * 字符串转化成分为单位的整数
	 * 
	 * @param fee
	 * @return
	 */
	public static Integer stringToIntegerFee(String fee) {
		if (fee == null || fee.isEmpty())
			return 0;
		boolean isNegative = false; //是否负数
		if(fee.startsWith("-")){
			fee = fee.substring(1);
			isNegative = true;
		}
		if (fee.split(".").length > 2) {
			return 0;
		}
		if (fee.indexOf(".") > -1) {
			String tail = fee.substring(fee.indexOf(".") + 1);
			if (tail.length() > 1) {
				tail = tail.substring(0, 2);
			} else if (tail.length() == 1) {
				tail = tail + "0";
			} else {
				tail = "00";
			}
			fee = fee.substring(0, fee.indexOf(".")) + tail;
		} else {
			fee = fee + "00";
		}
		try {
			Integer result = Integer.valueOf(fee);
			return isNegative?(-result) : result;
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
