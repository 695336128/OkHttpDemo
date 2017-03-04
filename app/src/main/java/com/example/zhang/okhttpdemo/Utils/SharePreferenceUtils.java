package com.example.zhang.okhttpdemo.Utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * SharedPreferences的一个工具类，调用setParam就能保存String, Integer, Boolean, Float,
 * Long类型的参数 同样调用getParam就能获取到保存在手机里面的数据
 */
public class SharePreferenceUtils {
	/** 保存在手机里面的文件名 */
	private static final String FILE_NAME = "share_date";

	private static SharedPreferences sp;
	private static Editor editor;
	private static SharePreferenceUtils preferenceUtil;

	private SharePreferenceUtils(Context context, String spFileName) {
		sp = context.getSharedPreferences(spFileName, Context.MODE_PRIVATE);
		editor = sp.edit();
	}

	private SharePreferenceUtils(Context context) {
		sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		editor = sp.edit();
	}

	public static SharePreferenceUtils getAppConfig(Context context, String spFileName) {
		if (preferenceUtil == null) {
			preferenceUtil = new SharePreferenceUtils(context, spFileName);
		}
		return preferenceUtil;
	}

	public static SharePreferenceUtils getAppConfig(Application context) {
		if (preferenceUtil == null) {
			preferenceUtil = new SharePreferenceUtils(context);
		}
		return preferenceUtil;
	}

	public static SharePreferenceUtils getAppConfig(Context context) {
		if (preferenceUtil == null) {
			preferenceUtil = new SharePreferenceUtils(context);
		}
		return preferenceUtil;
	}

	/**
	 * 移除某个key值已经对应的值
	 * 
	 * @param context
	 * @param key
	 */
	public static void remove(Context context, String key) {
		editor.remove(key);
		SharedPreferencesCompat.apply(editor);
	}

	/**
	 * 清除所有数据
	 * 
	 * @param context
	 */
	public static void clear(Context context) {
		editor.clear();
		SharedPreferencesCompat.apply(editor);
	}

	/**
	 * 查询某个key是否已经存在
	 * 
	 * @param key
	 */
	public static boolean contains(String key) {
		return sp.contains(key);
	}

	/**
	 * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
	 * 
	 * @param key
	 * @param object
	 */
	public static void setParam(String key, Object object) {

		String type = object.getClass().getSimpleName();

		if ("String".equals(type)) {
			editor.putString(key, (String) object);
		} else if ("Integer".equals(type)) {
			editor.putInt(key, (Integer) object);
		} else if ("Boolean".equals(type)) {
			editor.putBoolean(key, (Boolean) object);
		} else if ("Float".equals(type)) {
			editor.putFloat(key, (Float) object);
		} else if ("Long".equals(type)) {
			editor.putLong(key, (Long) object);
		}

		editor.commit();
	}

	/**
	 * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
	 * 
	 * @param key
	 * @param defaultObject
	 * @return
	 */
	public static Object getParam(String key, Object defaultObject) {
		String type = defaultObject.getClass().getSimpleName();

		if ("String".equals(type)) {
			return sp.getString(key, (String) defaultObject);
		} else if ("Integer".equals(type)) {
			return sp.getInt(key, (Integer) defaultObject);
		} else if ("Boolean".equals(type)) {
			return sp.getBoolean(key, (Boolean) defaultObject);
		} else if ("Float".equals(type)) {
			return sp.getFloat(key, (Float) defaultObject);
		} else if ("Long".equals(type)) {
			return sp.getLong(key, (Long) defaultObject);
		}

		return null;
	}

	/**
	 * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
	 */
	private static class SharedPreferencesCompat {
		private static final Method sApplyMethod = findApplyMethod();

		/**
		 * 反射查找apply的方法
		 * 
		 * @return
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private static Method findApplyMethod() {
			try {
				Class clz = Editor.class;
				return clz.getMethod("apply");
			} catch (NoSuchMethodException e) {
			}

			return null;
		}

		/**
		 * 如果找到则使用apply执行，否则使用commit
		 * 
		 * @param editor
		 */
		public static void apply(Editor editor) {
			try {
				if (sApplyMethod != null) {
					sApplyMethod.invoke(editor);
					return;
				}
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
			editor.commit();
		}
	}
	
	/**
	 * 存放实体类以及任意类型
	 * 
	 * @param context
	 *            上下文对象
	 * @param key
	 * @param obj
	 */
	public static void setBean(Context context, String key, Object obj) {
		if (obj instanceof Serializable) {// obj必须实现Serializable接口，否则会出问题
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(obj);
				String string64 = new String(Base64.encode(baos.toByteArray(), 0));
				Editor editor = sp.edit();
				editor.putString(key, string64).commit();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			throw new IllegalArgumentException("the obj must implement Serializble");
		}

	}
	/**
	 * 获取实体类以及任意类型
	 *
	 * @param context
	 *            上下文对象
	 * @param key
	 */
	public static Object getBean(Context context, String key) {
		Object obj = null;
		try {
			String base64 = sp.getString(key, "");
			if (base64.equals("")) {
				return null;
			}
			byte[] base64Bytes = Base64.decode(base64.getBytes(), 1);
			ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			obj = ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}

}