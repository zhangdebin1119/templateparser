package cn.kelaile.templateparser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import cn.kelaile.templateparser.JedisHelper.JWork;
import redis.clients.jedis.Jedis;

public class DeviceTemplate {
	private final static Logger log = Logger.getLogger(DeviceTemplate.class);
	private final static String Template_KEY = "Templates";
	private final static String Type_Key = "Type";
	private final String CutCommand_KEY = "CutCommand";
	private final String CutOffset_KEY = "CutOffset";
	private int cutOffset;
	private String cutCommand;
	private List<Template> templates;
	private int type;

	public DeviceTemplate(JSONObject object) {
		try {
			if (object.has(CutOffset_KEY)) {
				this.cutOffset = object.getInt(CutOffset_KEY);
			}
			if (object.has(CutCommand_KEY)) {
				this.cutCommand = object.getString(CutCommand_KEY);
			}
			if (object.has(Type_Key)) {
				this.type = object.getInt(Type_Key);
			}
			templates = convertToTemplate(object);
		} catch (JSONException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 获得模板
	 * 
	 * @param deviceNo
	 * @return
	 */
	public DeviceTemplate(String deviceNo) {
		if (deviceNo == null) {
			return;
		}
		final String key = Constants.TEMPLATEKEY + deviceNo;
		String j = Constants.FACTORY.withJedisDo(new JWork<String>() {

			@Override
			public String work(Jedis jedis) {
				return jedis.get(key);
			}
		});

		if (null == j || j.length() < 1) {
			log.error("设备Id " + deviceNo + "没有模板, j = " + j);
			return;
		}
		JSONObject object = null;
		try {
			object = JSONObject.fromObject(j);
			if (object.has(CutOffset_KEY)) {
				Object offset = object.get(CutOffset_KEY);
				if (offset != null && !offset.toString().isEmpty()) {
					this.cutOffset = object.getInt(CutOffset_KEY);
				}
			}
			if (object.has(CutCommand_KEY)) {
				this.cutCommand = object.getString(CutCommand_KEY);
			}
			if(object.has(Type_Key)){
				this.type=object.getInt(Type_Key);
			}
			
			templates = convertToTemplate(object);
		} catch (JSONException e) {
			log.error("设备Id " + deviceNo + "解析模板出错" + e.getMessage(), e);
		}
	}

	public int getCutOffset() {
		return cutOffset;
	}

	public void setCutOffset(int cutOffset) {
		this.cutOffset = cutOffset;
	}

	public String getCutCommand() {
		return cutCommand;
	}

	public void setCutCommand(String cutCommand) {
		this.cutCommand = cutCommand;
	}

	public Template getTemplateByType(int type) {
		if (templates == null || templates.size() == 0)
			return null;
		Template result = null;
		for (Template t : templates) {
			if (t.getType() == type) {
				result = t;
				break;
			}
		}
		return result;
	}

	public List<Template> getTemplates() {
		return templates;
	}

	public void setTemplates(List<Template> templates) {
		this.templates = templates;
	}

	public byte[] getCutCmdPOSByte() {
		if (this.cutCommand == null || this.cutCommand.isEmpty())
			return null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			outputStream.write(StringHelper.intToLowBytes(cutOffset, 2));
			outputStream.write(StringHelper.hexStringToBytes(cutCommand));
			return outputStream.toByteArray();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 
	 * @param json
	 * @return
	 */
	public static List<Template> convertToTemplate(JSONObject json) {
		List<Template> templates = null;
		boolean isImg = false;
		if (json.has(Type_Key)) {
			isImg = json.getInt(Type_Key) == 2;
		}
		if (json.has(Template_KEY)) {
			templates = new ArrayList<Template>();
			JSONArray jsonArray = json.getJSONArray(Template_KEY);
			Iterator iteratorArray = jsonArray.iterator();
			while (iteratorArray.hasNext()) {
				templates.add(new Template((JSONObject) iteratorArray.next(),
						isImg));
			}
		}
		return templates;
	}
	public int getType(){
		return type;
	}
}
