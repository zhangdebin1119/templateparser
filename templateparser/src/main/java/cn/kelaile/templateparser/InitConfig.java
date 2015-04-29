package cn.kelaile.templateparser;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import net.sourceforge.tess4j.TessAPI;
import net.sourceforge.tess4j.Tesseract1;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


/**
 * 获取项目配置信息
 * 
 * 
 */
final class InitConfig {
	private static final Logger log = Logger.getLogger(InitConfig.class);
	static int nosslport = 7777; // 监听设备的socket端口号（业务通道）
	static int withsslport = 7000; // 监听设备的ssl socket端口号（业务通道）
	static int certport = 8888; // 收银单socket端口号（业务通道）
	static int withssldebuggingport = 7700; // 监听设备的ssl socket端口号（网络状态、调试通道）
	static boolean jiami = false; // 是否加密
	static String dimensionalAddress = "";// 二维码生成地址
	static String aliyunAddress = "http://oss.aliyuncs.com"; // 阿里云地址
	static String rawBucketName = "raw-data"; // 原始数据保存地址
	static String eparseBucketName = "eparse-data"; // 解析数据保存地址
	static String sslkeystorepath = ""; // SSL keystore路径
	static String ssltruststorepath = ""; // SSL truststore路径
	static String sslkeystorepassword = ""; // SSL keystore密码
	static String ssltruststorepassword = ""; // SSL truststore密码
	static int idleTime = 60 * 6; // socket读超时时间	
	static boolean isOldVersionEnable = false; // 是否启用旧版本v0

	static JedisHelper factory = null;
	//static List<ESCPOSCommand> escPOSCommandList = null;

	public static String mqhost = null;// prop.getProperty("mqhost");
	public static String mqqueue = null;// prop.getProperty("mqqueue");
	static {
		Locale locale = Locale.getDefault(); 
		ResourceBundle resource = ResourceBundle.getBundle("config",locale);
		dimensionalAddress = resource.getString("dimensionalAddress");
		
		

		System.setProperty("jsse.enableSNIExtension", "false");

		String path = InitConfig.class.getClassLoader()
				.getResource("JedisPool.xml").getPath();
		log.info("configfile path = " + path);

		File f = new File(path);
		SAXReader reader = new SAXReader();
		Document document;
		try {
			document = reader.read(f);
			Element ele = document.getRootElement().element("JedisConfig");
			Element element = document.getRootElement().element("JedisServer");
			factory = new JedisHelper(JedisHelper.generateConfig(ele), element);
		} catch (DocumentException e) {
			log.error(e.getMessage(), e);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		log.info("Jedis配置:" + factory.getJedisConfigString());
	//	escPOSCommandList = ESCPOSCommands.getStaticCommands();
		
	}
	public static void main(String[] args){
		System.out.println(dimensionalAddress);
	}
}
