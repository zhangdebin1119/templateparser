package cn.kelaile.templateparser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

public class RedisHelper {
	private final static Logger log = Logger.getLogger(RedisHelper.class);
	private final static String DEVICETYPE = "DeviceType";
	private final static String DEVICESTATE = "DeviceState";
	private final static String DEVICELIST = "DeviceList";
	private final static int TOTAL_COUNT = 720; // 24小时

	/**
	 * 保存设备状态
	 * 
	 * @param map
	 */
	public static void saveDeviceState(final String deviceId,
			final DeviceState state) {
		Constants.FACTORY.withJedisDo(new JedisHelper.JVoidWork() {
			@Override
			public void work(Jedis jedis) {
				try {
					JSONObject object = new JSONObject();
					object.put("State", state == DeviceState.Running ? 1 : 0);
					object.put("Time", System.currentTimeMillis());
					String key = DEVICESTATE + "_" + deviceId;
					String HKey = String.format("%s_List_%s", DEVICESTATE,
							deviceId);
					jedis.set(key, object.toString());
					Long count = jedis.rpush(HKey, object.toString());
					if (count > TOTAL_COUNT) {
						jedis.ltrim(HKey, 1, -1);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});
	}
	

	/**
	 * 保存设备状态
	 * 
	 * @param map
	 */
	public static void saveDeviceType(final String deviceId,
			final String typeString) {
		Constants.FACTORY.withJedisDo(new JedisHelper.JVoidWork() {
			@Override
			public void work(Jedis jedis) {
				try {
					String key = DEVICETYPE + "_" + deviceId;
					jedis.set(key, typeString);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		});
	}

	/**
	 * 保存当前设备列表
	 * 
	 * @param map
	 */
	public static void saveDeviceList(final Map<String, String> map) {
		Constants.FACTORY.withJedisDo(new JedisHelper.JVoidWork() {
			@Override
			public void work(Jedis jedis) {
				jedis.hmset(DEVICELIST, map);
			}
		});

	}

	/**
	 * 
	 * @param deviceId
	 * @param i
	 * @return
	 */
	public static List<byte[]> getCommandList(final String deviceId, final int i) {
		log.debug("get pay command from redis , i = " + i);
		return Constants.FACTORY
				.withJedisDo(new JedisHelper.JWork<List<byte[]>>() {

					private List<byte[]> blist;

					@Override
					public List<byte[]> work(Jedis jedis) {
						List<String> list = null;
						try {
							blist = new ArrayList<byte[]>();
							list = jedis.lrange(deviceId + 0, 0, -1);
							for (String s : list) {
								blist.add(StringHelper.hexStringToBytes(s));
							}
							log.debug("blist.size " + blist.size());
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						} finally {
							log.debug("close ");
						}
						return blist;
					}
				});
	}

	/**
	 * 删除已发送队列内容，把未发送队列内容放入已发送队列，清空未发送队列，并返回已发送队列数据(在redis中使用两个list集合实现，
	 * 通过重命名key来实现两个队列的互换)
	 * 
	 * @param deviceId
	 * @return
	 */
	public static List<byte[]> updateCommandList(final String deviceId) {
		log.debug("update command from redis");
		return Constants.FACTORY
				.withJedisDo(new JedisHelper.JWork<List<byte[]>>() {

					private List<byte[]> blist;

					@Override
					public List<byte[]> work(Jedis jedis) {
						List<String> list = null;
						try {
							if (jedis.exists(deviceId + 0)) {
								jedis.rename(deviceId + 0, deviceId + 2);// 重命名key
							}
							if (jedis.exists(deviceId + 1)) {
								jedis.rename(deviceId + 1, deviceId + 0);// 重命名key
							}
							if (jedis.exists(deviceId + 2)) {
								jedis.rename(deviceId + 2, deviceId + 1);// 重命名key
							}
							jedis.del(deviceId + 1);// 删除key
							blist = new ArrayList<byte[]>();
							list = jedis.lrange(deviceId + 0, 0, -1);
							for (String s : list) {
								blist.add(StringHelper.hexStringToBytes(s));
							}
							log.debug("blist.size = " + blist.size());
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						} finally {
							log.debug("close");
						}
						return blist;
					}
				});
	}

	/**
	 * 增量方式保存
	 * 
	 * @param deviceId
	 * @param list
	 * @param i
	 */
	public static void addCommandList(final String deviceId,
			final List<byte[]> list, final int i) {
		log.debug("add send command to redis i = " + i);
		Constants.FACTORY.withJedisDo(new JedisHelper.JVoidWork() {
			@Override
			public void work(Jedis jedis) {
				log.debug("to add command to redis");
				int size = list.size();
				String[] s = new String[size];
				for (int i = 0; i < size; i++) {
					s[i] = StringHelper.convertToHexString(list.get(i));
					log.debug(s[i]);
				}
				long l = jedis.rpush(deviceId + i, s);
				log.debug("l = " + l);
			}
		});
	}

	public static void addCommandList(final String deviceId,
			final byte[] bytes, final int i) {
		log.debug("add send command to redis , i = " + i);
		Constants.FACTORY.withJedisDo(new JedisHelper.JVoidWork() {
			@Override
			public void work(Jedis jedis) {
				log.debug("to add command to redis");
				String s = StringHelper.convertToHexString(bytes);
				log.debug(s);
				long l = jedis.rpush(deviceId + i, s);
				log.debug("l = " + l);
			}
		});
	}

	/**
	 * 从redis中获取key对应的value
	 * 
	 * @return
	 */
	public static String get(final String key) {
		return Constants.FACTORY.withJedisDo(new JedisHelper.JWork<String>() {
			@Override
			public String work(Jedis jedis) {
				return jedis.get(key);
			}
		});
	}
	public static Set<String> getKeys(final String mhkey) {
		return Constants.FACTORY.withJedisDo(new JedisHelper.JWork<Set<String>>() {
			@Override
			public Set<String> work(Jedis jedis) {
				return jedis.keys(mhkey);
			}
		});
	}

	/**
	 * 0x00 表示微信界面 (默认界面) 0x01 表示支付宝界面
	 * 
	 * @param deviceNo
	 * @return
	 */
	public static byte getPayTypeByDeviceNo(String deviceNo) {
		byte payType = 0x00; // 微信
		try {
			Map<String, String> m = getMap("DEV:" + deviceNo);
			if (m != null && m.containsKey("paytype")) {
				/**
				 * map里面保存的 payType 0 不支持 1 支持微信 2 支持支付宝 3 支持微信_支付宝
				 */
				int i = Integer.valueOf(m.get("paytype")) - 1;
				i = Math.max(0, i);
				payType = (byte) i;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return payType;
	}

	/**
	 * 从redis中获取key对应的value
	 * 
	 * @return
	 */
	public static Map<String, String> getMap(final String key) {
		return Constants.FACTORY
				.withJedisDo(new JedisHelper.JWork<Map<String, String>>() {
					@Override
					public Map<String, String> work(Jedis jedis) {
						return jedis.hgetAll(key);
					}
				});
	}

	/**
	 * 设置key的值为value
	 * 
	 * @param key
	 * @param value
	 */
	public static void set(final String key, final String value) {
		Constants.FACTORY.withJedisDo(new JedisHelper.JVoidWork() {
			@Override
			public void work(Jedis jedis) {
				jedis.set(key, value);
			}
		});
	}

	/**
	 * 设置key的值为value
	 * 
	 * @param key
	 * @param value
	 */
	public static void del(final String key) {
		Constants.FACTORY.withJedisDo(new JedisHelper.JVoidWork() {
			@Override
			public void work(Jedis jedis) {
				jedis.del(key);
			}
		});
	}
}
