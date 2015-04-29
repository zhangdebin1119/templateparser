package cn.kelaile.templateparser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.log4j.Logger;

import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.model.ObjectMetadata;
import com.aliyun.openservices.oss.model.PutObjectResult;

/**
 * Access Key ID: mQH1ZNnkhhrbkWF9 Access Key Secret:
 * Ae7i5dgCHMHpuEQbLDgrPtUHz7CC1X
 * 
 * @author sixwww
 * 
 */
public class AliyunOssHelper {

	private static Logger log = Logger.getLogger(AliyunOssHelper.class);
	private static OSSClient client = null;
	static {
		String accessKeyId = "mQH1ZNnkhhrbkWF9";
		String accessKeySecret = "Ae7i5dgCHMHpuEQbLDgrPtUHz7CC1X";
		log.info("ALIYUNADDRESS:" + Constants.ALIYUNADDRESS);
		client = new OSSClient(Constants.ALIYUNADDRESS, accessKeyId,
				accessKeySecret);
	}

	/**
	 * 上传文件
	 * 
	 * @param bucketName
	 * @param key
	 * @param bytes
	 * @return
	 */
	public static String putData(String bucketName, String key, byte[] bytes) {
		log.debug("putData start" + bucketName + key);
		ObjectMetadata objectMeta = new ObjectMetadata();
		objectMeta.setContentLength(bytes.length);
		objectMeta.setContentType("application/octet-stream");
		InputStream input = new ByteArrayInputStream(bytes);
		PutObjectResult result = client.putObject(bucketName, key, input,
				objectMeta);
		log.debug("putData end" + result.toString());
		return result.getETag();
	}

	/**
	 * 读取文件
	 * 
	 * @param bucketName
	 * @param key
	 * @return
	 */
	public static byte[] getData(String bucketName, String key) {
		try {
			InputStream is = client.getObject(bucketName, key)
					.getObjectContent();
			ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
			int ch;
			while ((ch = is.read()) != -1) {
				bytestream.write(ch);
			}
			byte imgdata[] = bytestream.toByteArray();
			bytestream.close();
			return imgdata;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		// byte[] info = AliyunOssHelper.getData("eparse-data",
		// "DEV10272_7f0de91404f74e2c82cd6cec2652e961");
		// byte[] utf16=info.toString().getBytes("UTF-8");
		//
		// FindTailHelper.WriteToFile("/Users/zhangdebin/Documents/test4213",
		// info);
		// byte[] info1 = AliyunOssHelper.getData("raw-data",
		// "DEV10272_7f0de91404f74e2c82cd6cec2652e961");
		// byte[] info2 = AliyunOssHelper.getData("raw-data",
		// "DEV10552_6e52b1ad40d94e1183fcf87078b5b468");
		// int k=0;
		// k=info1.length+info2.length;
		// byte[] all=new byte [k];
		// List<byte[]> ossfile = new ArrayList<byte[]>();
		// ossfile.add(info1);
		// ossfile.add(info2);
		// int n=0;
		// for(int i=0;i<ossfile.size();i++){
		// for(int j=0;j<ossfile.get(i).length;j++){
		// all[n+j]=ossfile.get(i)[j];
		// }
		// n=n+ossfile.get(i).length;
		// }
		// FindTailHelper.WriteToFile("/Users/zhangdebin/Documents/testgetdata/all",
		// all);
		// System.out.println("sucess");
//		byte[] info = AliyunOssHelper.getData("eparse-data",
//				"DEV12042_7c24df8acbee4bbbbbdccb3d58dd8744");
//		if (info == null) {
//			log.info("byte is null");
//		}
//		String txt = new String(info);
//		String txt2 = StringHelper.convertToHexString(info);
//		System.out.println(txt);
//		System.out.println(txt2);
		 byte[] info2= AliyunOssHelper.getData("eparse-data",
		 "DEV10272_7f0de91404f74e2c82cd6cec2652e961");
		 System.out.println(new String(info2));
	}
}
