package cn.kelaile.templateparser;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;


public class Template {
	private final static Logger log = Logger.getLogger(Template.class);

	private final String Name_KEY = "TemplateName";
	private final String Type_KEY = "TemplateType"; 
	private final String Detector_KEY = "Detector";
	private final String Data_KEY = "Data";
	private final String Width_KEY = "Width";

	private int width;

	private String name;
	private int type;

	private List<Detector> detectors;
	private List<ResolverTemplate> resolverDatas;

	public Template(JSONObject object, boolean isImg) {
		if (object.has(Width_KEY))
			this.width = object.getInt(Width_KEY);
		if (object.has(Name_KEY))
			this.name = object.getString(Name_KEY);
		if (object.has(Type_KEY))
			this.type = object.getInt(Type_KEY);
		if (object.has(Detector_KEY)) {
			detectors = new ArrayList<Detector>();
			JSONArray jsonArray = object.getJSONArray(Detector_KEY);
			Iterator iteratorArray = jsonArray.iterator();
			while (iteratorArray.hasNext()) {
				detectors.add(new Detector((JSONObject) iteratorArray.next(),
						isImg));
			}
		}
		if (object.has(Data_KEY)) {
			resolverDatas = new ArrayList<ResolverTemplate>();
			JSONArray jsonArray = object.getJSONArray(Data_KEY);
			Iterator iteratorArray = jsonArray.iterator();
			while (iteratorArray.hasNext()) {
				resolverDatas.add(new ResolverTemplate(
						(JSONObject) iteratorArray.next(), isImg));
			}
		}
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Detector> getDetectors() {
		return detectors;
	}

	public void setDetectors(List<Detector> detectors) {
		this.detectors = detectors;
	}

	public List<ResolverTemplate> getDatas() {
		return resolverDatas;
	}

	public void setDatas(List<ResolverTemplate> datas) {
		this.resolverDatas = datas;
	}

	public boolean processImageDetectors(ImageData image, int[] finds) {
		int whiteheight = BitSetHelper.getWhiteHeight(image);
		boolean isValid = false;
		// 包含的逻辑值
		boolean isInclude = false;
		// 排除的逻辑值
		boolean isExclude = true;
		// 是否配置了包含
		boolean isHaveInclude = false;
		for (Detector detector : getDetectors()) {
			if (detector.getFeature() == null) {
				continue;
			}
			int exclusionType = detector.getExclusionType();
			if (exclusionType == 2) {
				isHaveInclude = true;
			}
			BitSet bitSet = new BitSet(3);
			for (int i = 0; i < 3; i++) {
				bitSet.set(i,
						detector.judgeImgDatas(image, whiteheight, finds[i]));
				if (exclusionType == 2 && bitSet.get(i)) {
					isInclude = true;
					break;
				}
			}
			if (exclusionType == 1 && bitSet.cardinality() != 3) { // 排除需要在三个位置上排除
				isExclude = false;
				break;
			}
		}
		if (isHaveInclude) {
			isValid = isInclude && isExclude;
		} else {
			isValid = isExclude;
		}
		return isValid;
	}

	public boolean processNoImageDetectors(String stringData) {
		if (detectors == null || detectors.size() == 0) {
			log.info("detectors is"+detectors+"-detectors.size:"+detectors.size());
			return false;
		}
		boolean isValid = true;
		// 包含的逻辑值
		boolean isInclude = false;
		// 排除的逻辑值
		boolean isExclude = true;
		// 是否配置了包含
		boolean isHaveInclude = false;
		for (Detector detector : detectors) {
			if (detector.getFeature() == null) {				
				continue;
			}
			int exclusionType = detector.getExclusionType();
			if (exclusionType == 2) {
				isHaveInclude = true;
				if(isHaveInclude && isInclude) //如果 包含已经成功，就不用再判断
					continue;
			}
			// 包含 只要判断包含
			if(exclusionType == 2  && detector.judgeString(stringData)){
				isInclude = true;
			}
			if (exclusionType == 1 && !detector.judgeString(stringData)){ // 排除只要一个不成功，就是不成功
				isExclude = false;
				break;
			}
			log.debug(detector.getExclusionType() + "---" + isValid);
		}
		if (isHaveInclude) {
			isValid = isInclude && isExclude;
		} else {
			isValid = isExclude;
		}
		return isValid;
	}


	/**
	 *  判断字符串模版
	 * @param template
	 * @param data
	 * @return
	 */
	 
	public static JSONObject isTemplateDetectMatch(Template template,
			String data) {
		if (template.getDetectors() == null
				|| template.getDetectors().size() == 0)
			return null;
		if (!template.processNoImageDetectors(data))
			return null;
		JSONObject element = new JSONObject();
		if (template.getDatas() != null)
			for (ResolverTemplate resolver : template.getDatas()) {
				String v = resolver.getFieldValue(data);
				if (v != null && !v.trim().isEmpty()) {
					element.element(resolver.getFieldName(), v);
				} else {
					if (!element.has(resolver.getFieldName())) {
						element.element(resolver.getFieldName(), "");
					}
				}
			}
		return element;
	}


	/**
	 * 判断图片模版
	 * 
	 */
	public static JSONObject isTemplateDetectMatch(Template template,
			ImageData data, int whiteheight) {
		if (template.getDetectors() == null
				|| template.getDetectors().size() == 0)
			return null;
		int[] finds = new int[] { 0, 1, -1 };
		boolean isValid = false;
		// 包含的逻辑值
		boolean isInclude = false;
		// 排除的逻辑值
		boolean isExclude = true;
		// 是否配置了包含
		boolean isHaveInclude = false;
		for (Detector detector : template.getDetectors()) {
			if (detector.getFeature() == null) {
				continue;
			}
			int exclusionType = detector.getExclusionType();
			if (exclusionType == 2) {
				isHaveInclude = true;
			}
			BitSet bitSet = new BitSet(3);
			for (int i = 0; i < 3; i++) {
				bitSet.set(i,
						detector.judgeImgDatas(data, whiteheight, finds[i]));
				if (exclusionType == 2 && bitSet.get(i)) {
					isInclude = true;
					break;
				}
			}
			if (exclusionType == 1 && bitSet.cardinality() != 3) { // 排除需要在三个位置上排除
				isExclude = false;
				break;
			}
		}
		if (isHaveInclude) {
			isValid = isInclude && isExclude;
		} else {
			isValid = isExclude;
		}
		if (!isValid) {
			return null;
		}
		JSONObject element = new JSONObject();
		if (template.getDatas() != null)
			for (ResolverTemplate resolver : template.getDatas()) {
				String v = null;
				for (int i = 0; i < 3; i++) {
					v = resolver.getFieldValueFromImgDatas(data, whiteheight,
							finds[i]);
					if (v != null && !v.trim().isEmpty()) {
						break;
					}
				}
				if (v != null && !v.trim().isEmpty()) {
					element.element(resolver.getFieldName(), v);
				} else {
					if (!element.has(resolver.getFieldName())) {
						element.element(resolver.getFieldName(), "");
					}
				}
			}
		return element;
	}

}
