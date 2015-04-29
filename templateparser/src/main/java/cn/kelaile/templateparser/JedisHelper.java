package cn.kelaile.templateparser;

import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisHelper {
	private final static Logger log = Logger.getLogger(JedisHelper.class);
	protected int REDIS_FAILED_RESOURCES_BEFORE_RECONNECT = 151;
	protected int REDIS_RECONNECT_RETRY_COUNT = 48;
	protected int REDIS_RECONNECT_RETRY_WAITTIME = 5000;
	/**
	 * The connection pool.
	 */
	protected JedisPool m_jedisPool;
	protected Config poolConfig;
	/**
	 * Host to connect to.
	 */
	protected String REDIS_HOST = "localhost";

	/**
	 * Port to connect to.
	 */
	protected int REDIS_PORT = 6379;

	/**
	 * 
	 */
	protected int REDIS_DB = 0;

	/**
	 * 
	 */
	protected int REDIS_TIMEOUT = 300;

	protected String REDIS_PASSWORD = "kelaile54user!@#";
	/**
	 * Need the following object to synchronize a block
	 */
	private static Object objSync = new Object();

	public static Config generateConfig(Element ele) {
		Config c = new Config();
		/**
		 * 参数whenExhaustedAction指定在池中借出对象的数目已达极限的情况下，调用它的borrowObject方法时的行为。
		 * 可以选用的值有： GenericObjectPool.WHEN_EXHAUSTED_BLOCK，表示等待；
		 * GenericObjectPool
		 * .WHEN_EXHAUSTED_GROW，表示创建新的实例（不过这就使maxActive参数失去了意义）；
		 * GenericObjectPool
		 * .WHEN_EXHAUSTED_FAIL，表示抛出一个java.util.NoSuchElementException异常。
		 * */
		for (Object o : ele.elements("property")) {
			Element e = (Element) o;
			String pName = e.attributeValue("name");
			String value = e.getText();
			if (pName.equals("whenExhaustedAction")) {
				c.whenExhaustedAction = Byte.valueOf(value);
			} else if (pName.equals("testOnBorrow")) {
				c.testOnBorrow = Boolean.valueOf(value);// 设定在借出对象时是否进行有效性检查
			} else if (pName.equals("testOnReturn")) {
				c.testOnReturn = Boolean.valueOf(value);
				;// 设定在还回对象时是否进行有效性检查
			} else if (pName.equals("maxWait")) {
				c.maxWait = Integer.valueOf(value);// 指明若在对象池空时调用borrowObject方法的行为被设定成等待，最多等待多少毫秒。如果等待时间超过了这个数值，
				// 则会抛出一个java.util.NoSuchElementException异常。如果这个值不是正数，表示无限期等待
			} else if (pName.equals("maxActive")) {
				c.maxActive = Integer.valueOf(value);
				;// 指明能从池中借出的对象的最大数目。如果这个值不是正数，表示没有限制。
			} else if (pName.equals("maxIdle")) {
				c.maxIdle = Integer.valueOf(value);
				;// 允许最大空闲对象数
			} else if (pName.equals("minIdle")) {
				c.minIdle = Integer.valueOf(value);
				;// 允许最小空闲对象数
			} else if (pName.equals("timeBetweenEvictionRunsMillis")) {
				c.timeBetweenEvictionRunsMillis = Integer.valueOf(value);// 设定间隔每过多少毫秒进行一次后台对象清理的行动。如果这个值不是正数，则实际上不会进行后台对象清理
			} else if (pName.equals("numTestsPerEvictionRun")) {
				c.numTestsPerEvictionRun = Integer.valueOf(value);// 设定在进行后台对象清理时，每次检查几个对象。如果这个值不是正数，则每次检查的对象数是检查时池内对象的总数乘以这个值的负倒数再向上取整的结果――
				// 也就是说，如果这个值是-2（-3、-4、-5……）的话，那么每次大约检查当时池内对象总数的1/2（1/3、1/4、1/5……）左右。
			} else if (pName.equals("testWhileIdle")) {
				c.testWhileIdle = Boolean.valueOf(value);// 设定在进行后台对象清理时，是否还对没有过期的池内对象进行有效性检查。不能通过有效性检查的对象也将被回收
			} else if (pName.equals("minEvictableIdleTimeMillis")) {
				c.minEvictableIdleTimeMillis = Long.valueOf(value);// #被空闲对象回收器回收前在池中保持空闲状态的最小时间毫秒数
			}
		}
		return c;
	}

	public enum JedisType {
		None(0), STRING(1), LIST(2), SET(3), ZSET(4), HASH(5);

		private final int value;

		private JedisType(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}

		public static JedisType fromInt(int key) {
			for (JedisType type : JedisType.values()) {
				if (type.value == key) {
					return type;
				}
			}
			return null;
		}
	}

	public JedisHelper(Config config, String host, int port, int db, int timeout) {
		poolConfig = config;
		REDIS_HOST = host;
		REDIS_PORT = port;
		REDIS_DB = db;
		REDIS_TIMEOUT = timeout;
	}

	public String getJedisConfigString() {
		return String.format("服务器 %s 端口号 %d 数据库 %d 密码 %s", REDIS_HOST,
				REDIS_PORT, REDIS_DB, REDIS_PASSWORD);
	}

	public JedisHelper(Config config, Element element) {
		poolConfig = config;
		REDIS_HOST = element.attributeValue("host");
		REDIS_PORT = Integer.valueOf(element.attributeValue("port"));
		REDIS_DB = Integer.valueOf(element.attributeValue("db"));
		REDIS_TIMEOUT = Integer.valueOf(element.attributeValue("timeout"));
		REDIS_PASSWORD = element.attributeValue("password");
	}

	public JedisHelper() {
		// TODO Auto-generated constructor stub
	}

	protected void createAndConnectPool() {
		if (m_jedisPool == null) {
			m_jedisPool = new JedisPool(poolConfig, REDIS_HOST, REDIS_PORT,
					REDIS_TIMEOUT,
					REDIS_PASSWORD == "" ? null : REDIS_PASSWORD, REDIS_DB);
		}
	}

	public Jedis getRes() {
		return maybeInitAndGet();
	}

	public Jedis maybeInitAndGet() {
		// in a non-thread-safe version of a singleton
		// the following line could be executed, and the
		// thread could be immediately swapped out
		if (m_jedisPool == null) {
			synchronized (objSync) {
				createAndConnectPool();
			}
		}

		// get a working resource or null otherwise
		Jedis j = getWorkingResource();

		if (j != null) {
			return j;
		}

		// at this point we could not find any resources to hand back or half
		// our pool
		// is not connected, re-establish the connections
		synchronized (objSync) {
			m_jedisPool = null;

			for (int i = 0; i < REDIS_RECONNECT_RETRY_COUNT; i++) {
				shutdownPool();
				createAndConnectPool();

				if (m_jedisPool != null) {
					Jedis jd = getWorkingResource();

					if (jd != null) {
						return jd;
					}
				}

				// wait before we trying again, except for the last attempt
				if (i < REDIS_RECONNECT_RETRY_COUNT - 1) {
					try {
						Thread.sleep(REDIS_RECONNECT_RETRY_WAITTIME);
					} catch (InterruptedException e) {
						log.error(e.getMessage());
					}
				}
			}
		}

		return null;
	}

	protected void shutdownPool() {
		if (m_jedisPool == null) {
			return;
		}
		m_jedisPool.destroy();
		m_jedisPool = null;
	}

	/**
	 * Returns you a working resource or null if none are found.
	 * 
	 * @return the working {@link Jedis} resource.
	 */
	protected Jedis getWorkingResource() {
		// try to find a working resource
		for (int i = 0; i < REDIS_FAILED_RESOURCES_BEFORE_RECONNECT; i++) {
			Jedis j = m_jedisPool.getResource();
			if (REDIS_PASSWORD != null && REDIS_PASSWORD.length() > 0) {
				j.auth(REDIS_PASSWORD);
			}

			if (j.isConnected()) {
				return j;
			} else {
				m_jedisPool.returnBrokenResource(j);
			}
		}
		return null;
	}

	/**
	 * Returns the given {@link Jedis} object back to the connection pool so it
	 * can be reused.
	 * 
	 * @param res
	 *            the object to return
	 */
	public void returnRes(Jedis res) {
		if (m_jedisPool != null) {
			m_jedisPool.returnResource(res);
		}
	}

	public <T> T withJedisDo(JWork<T> jWork) {
		// catch exception and gracefully fall back.
		try {
			Jedis j = getRes();
			T ret = (T) jWork.work(j);
			returnRes(j);
			return ret;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public void withJedisDo(JVoidWork jWork) {
		try {
			Jedis j = getRes();
			jWork.work(j);
			returnRes(j);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public interface VoidWork<Param> {
		public void work(Param jedis);
	}

	public interface Work<Return, Param> {
		public Return work(Param jedis);
	}

	public interface JWork<Return> extends Work<Return, Jedis> {

	}

	public interface JVoidWork extends VoidWork<Jedis> {

	}
}
