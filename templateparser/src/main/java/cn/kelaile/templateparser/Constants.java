package cn.kelaile.templateparser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class Constants {
	private static Logger log = Logger.getLogger(Constants.class);

	private static Properties prop = new Properties();
	public static  String ACCOUNTINFOPATH = null; //灵云配置信息
	public static  String LOGFILEPATH = null; //灵云的日志路径
	public static  String OCRLIBPATH = null; //灵云类库放置的路径
	public static  String TESTIMGPATH = null; //灵云测试图片路径
	public static JedisHelper FACTORY = null;
	public static  String TEMPLATEKEY = null;// 在redis中取模板的key
	public static  String ALIYUNADDRESS = null;//"http://oss.aliyuncs.com"; // 阿里云地址
	static{
		try {
			prop.load(Constants.class.getClassLoader().getResourceAsStream("config.properties"));
			ACCOUNTINFOPATH = prop.getProperty("accountinfopath");
			LOGFILEPATH = prop.getProperty("logfilepath");
			OCRLIBPATH = prop.getProperty("ocrlibpath");
			TESTIMGPATH = prop.getProperty("testimgpath");
			TEMPLATEKEY = prop.getProperty("templatekey");
			ALIYUNADDRESS =prop.getProperty("aliyunAddress");
			String path = Constants.class.getClassLoader()
					.getResource("JedisPool.xml").getPath();
			log.info("configfile path = " + path);

			File f = new File(path);
			SAXReader reader = new SAXReader();
			Document document;
			document = reader.read(f);
			Element ele = document.getRootElement().element("JedisConfig");
			Element element = document.getRootElement().element("JedisServer");
			FACTORY = new JedisHelper(JedisHelper.generateConfig(ele), element);
			log.info("Jedis配置:" + FACTORY.getJedisConfigString());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}catch (DocumentException e) {
			log.error(e.getMessage(), e);
		}
	}

}
