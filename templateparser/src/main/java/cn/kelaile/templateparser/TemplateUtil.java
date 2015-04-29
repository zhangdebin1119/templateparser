package cn.kelaile.templateparser;

import java.awt.image.BufferedImage;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;


/**
 成功输出
{"errorCode":0,"templateType":1,"data":{"totalFee":"571.00   "}}
失败输出
{"errorCode":1,"templateType":0}
测试模版
 * 
 */

public class TemplateUtil {
	private static final Logger log = Logger.getLogger(TemplateUtil.class);
	
	/**
	 * 测试模板
	 * 
	 * @param json
	 * @return
	 */
	public static JSONObject testTemplate(JSONObject json) {
		JSONObject object = new JSONObject();
		object.element("errorCode", 1);
		object.element("templateType", 0);
		if (!json.has("bucketName") || !json.has("fileKey")
				|| !json.has("template") || !json.has("fileType")) {
			return object;
		}
		try {
			String bucketName = json.getString("bucketName");
			String fileKey = json.getString("fileKey");
			byte[] bytes = AliyunOssHelper.getData(bucketName, fileKey);
			if (bytes == null) {
				return object;
			}
			DeviceTemplate deviceTemplate = new DeviceTemplate(
					json.getJSONObject("template"));
			if (deviceTemplate.getTemplates() == null
					|| deviceTemplate.getTemplates().size() == 0) {
				return object;
			}
			String data = null;
			int fileType = json.getInt("fileType");
			if(fileType == 1)
				data = new String(bytes).trim();
			BufferedImage image = null;
			ImageData imgData = null;
			int whiteheight =0;
			if(fileType == 2){
				image =BufferedImageHelper.byteToBufferedImage(bytes);
				imgData = BufferedImageHelper.bufferedImageToImageData(image);
				whiteheight = BitSetHelper.getWhiteHeight(imgData);
			}
			JSONObject element = null;
			JSONObject result = null;
			for(Template t:deviceTemplate.getTemplates()){
				if(t.getDetectors() == null || t.getDetectors().size() == 0)
					continue;
				element = null;
				if(fileType == 1)
					element  =  Template.isTemplateDetectMatch(t,data);
				if(fileType == 2)
					element  =  Template.isTemplateDetectMatch(t,imgData,whiteheight);
				if(element != null)
				{
					result = new JSONObject();
					result.element("errorCode", 0);
					result.element("templateType", t.getType());
					result.element("data", element);
					return result;
				}
			}
			return object;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return object;
	}
	public static JSONObject onlyfortest(JSONObject json) {
		JSONObject object = new JSONObject();
		object.element("errorCode", 1);
		object.element("templateType", 0);
//		if (!json.has("bucketName") || !json.has("fileKey")
//				|| !json.has("template") || !json.has("fileType")) {
//			return object;
//		}
		try {
//			String bucketName = json.getString("bucketName");
//			String fileKey = json.getString("fileKey");
			String bucketName = "eparse-data";
			String fileKey = "DEV10099_423e45d48461488dab8deb32566c6ab8";
			byte[] bytes = AliyunOssHelper.getData(bucketName, fileKey);
			if (bytes == null) {
				return object;
			}
//			DeviceTemplate deviceTemplate = new DeviceTemplate(
//					json.getJSONObject("template"));
			DeviceTemplate deviceTemplate = new DeviceTemplate(
					json);
//			DeviceTemplate deviceTemplate = new DeviceTemplate("DEV10099");
			if (deviceTemplate.getTemplates() == null
					|| deviceTemplate.getTemplates().size() == 0) {
				return object;
			}
			String data = null;
//			int fileType = json.getInt("fileType");
			int fileType = 2;
			if(fileType == 1)
				data = new String(bytes).trim();
			BufferedImage image = null;
			ImageData imgData = null;
			int whiteheight =0;
			if(fileType == 2){
				image =BufferedImageHelper.byteToBufferedImage(bytes);
				imgData = BufferedImageHelper.bufferedImageToImageData(image);
				whiteheight = BitSetHelper.getWhiteHeight(imgData);
			}
			JSONObject element = null;
			JSONObject result = null;
			for(Template t:deviceTemplate.getTemplates()){
				if(t.getDetectors() == null || t.getDetectors().size() == 0)
					continue;
				element = null;
				if(fileType == 1)
					element  =  Template.isTemplateDetectMatch(t,data);
				if(fileType == 2)
					element  =  Template.isTemplateDetectMatch(t,imgData,whiteheight);
				if(element != null)
				{
					result = new JSONObject();
					result.element("errorCode", 0);
					result.element("templateType", t.getType());
					result.element("data", element);
					return result;
				}
			}
			return object;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return object;
	}
}
