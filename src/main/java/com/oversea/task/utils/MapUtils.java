package com.oversea.task.utils;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class MapUtils
{
	private static ReentrantLock lock = new ReentrantLock();

	/**
	 * 有更好的解决办法
	 */
	static Object walkValue = null;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void walkMap(Object data, String key)
	{
		if (data instanceof Map)
		{
			if (((Map) data).containsKey(key))
			{
				walkValue = ((Map) data).get(key);
			}
			else
			{
				for (String str : ((Map<String, ?>) data).keySet())
				{
					Object cycle = ((Map) data).get(str);
					walkMap(cycle, key);
				}
			}
		}
	}

	public static Object findValueForm(Object data, String key)
	{
		try
		{
			lock.lock();
			MapUtils.walkValue = null;
			walkMap(data, key);
			return MapUtils.walkValue;
		}
		finally
		{
			lock.unlock();
		}
	}
}
