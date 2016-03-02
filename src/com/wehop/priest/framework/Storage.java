package com.wehop.priest.framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.slfuture.carrie.base.character.Encoding;
import com.slfuture.carrie.base.etc.Serial;
import com.slfuture.carrie.base.json.JSONNumber;
import com.slfuture.carrie.base.json.JSONObject;
import com.slfuture.carrie.base.json.JSONString;
import com.slfuture.carrie.base.json.core.IJSON;
import com.slfuture.carrie.base.text.Text;
import com.slfuture.carrie.base.time.Date;
import com.slfuture.carrie.base.type.core.ILink;
import com.slfuture.pluto.storage.SDCard;
import com.wehop.priest.base.Logger;
import com.wehop.priest.Program;

/**
 * 存储器
 */
public class Storage {
	/**
	 * 路径常量
	 */
	public final static String ROOT_NAME = Program.ID;
	public final static String STORAGE_ROOT = SDCard.root() + ROOT_NAME;
	public final static String IMAGE_ROOT = STORAGE_ROOT + "/image/";
	public final static String DATA_ROOT = STORAGE_ROOT + "/data/";
	public final static String CONFIG_ROOT = STORAGE_ROOT + "/config/";
	public final static String VOICE_ROOT = STORAGE_ROOT + "voice/";

	/**
	 * 用户相关信息
	 */
	private static ConcurrentHashMap<String, Object> data = null;


	/**
	 * 构造函数
	 */
	private Storage() { }

	/**
	 * 获取指定图片码的路径
	 * 
	 * @param code 图片码
	 * @return 图片路径
	 */
	public static String imagePath(String code) {
		File dir = new File(IMAGE_ROOT);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		return IMAGE_ROOT + code;
	}

	/**
	 * 获取图片目录
	 * 
	 * @return 图片目录
	 */
	public static String imageFolder() {
		File file = new File(IMAGE_ROOT);
		if(!file.exists()) {
			file.mkdirs();
		}
		return IMAGE_ROOT;
	}

	/**
	 * 获取数据目录
	 * 
	 * @return 数据目录
	 */
	public static String dataFolder() {
		File file = new File(DATA_ROOT);
		if(!file.exists()) {
			file.mkdirs();
		}
		return DATA_ROOT;
	}
	/**
	 * 获取语音目录
	 * 
	 * @return 语音目录
	 */
	public static String voiceFolder() {
		File file = new File(VOICE_ROOT);
		if(!file.exists()) {
			file.mkdirs();
		}
		return VOICE_ROOT;
	}

	/**
	 * 获取指定名称的图片
	 * 
	 * @param imageName 图片名称
	 * @return 位图
	 */
	public static Bitmap getImage(File file) {
		if(null == file) {
			return null;
		}
		return BitmapFactory.decodeFile(file.getAbsolutePath());
	}

	/**
	 * 获取指定名称的图片
	 * 
	 * @param imageName 图片名称
	 * @return 位图
	 */
	public static Bitmap getImage(String imageName) {
		if(null == imageName) {
			return null;
		}
		return BitmapFactory.decodeFile(IMAGE_ROOT + imageName);
	}

	/**
	 * 获取指定URL中的图片名称
	 * 
	 * @param url 图片URL
	 * @return 图片名称
	 */
	public static String getImageName(String url) {
		if(Text.isBlank(url)) {
			return null;
		}
		int i = url.lastIndexOf("/");
		if(-1 == i) {
			return url;
		}
		return url.substring(i + 1);
	}

	/**
	 * 获取用户相关信息
	 * 
	 * @param key 键
	 * @param clazz 返回值类型
	 * @return 返回值
	 */
	@SuppressWarnings("unchecked")
	public static <T> T data(String key, Class<T> clazz) {
		if(null == data) {
			synchronized(Storage.class) {
				if(null == data) {
					data = new ConcurrentHashMap<String, Object>();
					String path = DATA_ROOT + "data.dat";
					if(!(new File(path)).exists()) {
						return null;
					}
					String text = null;
					try {
						text = Text.loadFile(path, Encoding.ENCODING_UTF8);
					}
					catch (Exception ex) {
						Log.e(Program.ID, "load data failed", ex);
					}
					if(Text.isBlank(text)) {
						return null;
					}
					JSONObject object = JSONObject.convert(text);
					for(ILink<String, IJSON> link : object) {
						if(link.destination() instanceof JSONNumber) {
							JSONNumber number = (JSONNumber) link.destination();
							if(null == number) {
								continue;
							}
							data.put(link.origin(), number.doubleValue());
						}
						else if(link.destination() instanceof JSONString) {
							JSONString string = (JSONString) link.destination();
							if(null == string) {
								return null;
							}
							data.put(link.origin(), string.getValue());
						}
					}
				}
			}
		}
		Object result = data.get(key);
		if(null == result) {
			return null;
		}
		if(clazz.equals(Integer.class)) {
			Double d = (Double) result;
			return (T) (Integer) d.intValue();
		}
		else if(clazz.equals(Short.class)) {
			Double d = (Double) result;
			return (T) (Short) d.shortValue();
		}
		else if(clazz.equals(Byte.class)) {
			Double d = (Double) result;
			return (T) (Byte) d.byteValue();
		}
		else if(clazz.equals(Float.class)) {
			Double d = (Double) result;
			return (T) (Float) d.floatValue();
		}
		else {
			return (T) result;
		}
	}

	/**
	 * 设置用户信息
	 * 
	 * @param key 键 
	 * @param value 值
	 */
	public static void setData(String key, Object value) {
		if(null == data) {
			data = new ConcurrentHashMap<String, Object>();
		}
		if(null == value) {
			data.remove(key);
		}
		else {
			data.put(key, value);
		}
		save();
	}
	
	/**
	 * 清理
	 */
	public static void clear() {
		if(null != data) {
			data.clear();
		}
		File file = new File(DATA_ROOT + "me.dat");
		if(file.exists()) {
			file.delete();
		}
	}

	/**
	 * 保存相关信息
	 */
	public static void save() {
		if(null == data) {
			return;
		}
		File dir = new File(DATA_ROOT);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		JSONObject object = new JSONObject();
		for(Entry<String, Object> link : data.entrySet()) {
			if(null == link.getValue()) {
				continue;
			}
			if(link.getValue() instanceof Integer) {
				object.put(link.getKey(), new JSONNumber((Integer) link.getValue()));
			}
			else if(link.getValue() instanceof Double) {
				object.put(link.getKey(), new JSONNumber((Double) link.getValue()));
			}
			else if(link.getValue() instanceof String) {
				object.put(link.getKey(), new JSONString((String) link.getValue()));
			}
			else if(link.getValue() instanceof Date) {
				object.put(link.getKey(), new JSONString(link.getValue().toString()));
			}
		}
		try {
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(DATA_ROOT + "data.dat"), "UTF-8");
			fw.write(object.toString() + "\n");
			fw.flush();
			fw.close();
		}
		catch(Exception ex) { }
	}

	/**
	 * 保存摄像头图片
	 * 
	 * @param data 摄像头数据
	 * @return 保存的数据
	 */
	public static File saveCamera(Intent data) {
		String fileName = Serial.makeSerialString() + ".jpg";
		Bundle bundle = data.getExtras();
		Bitmap bitmap = (Bitmap) bundle.get("data");
		String filePath = IMAGE_ROOT + fileName;
		File file = new File(IMAGE_ROOT);
		if(!file.exists() && !file.isDirectory()) {
			file.mkdirs();
		}
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(filePath, false);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			stream.flush();
			return new File(filePath);
		}
		catch (FileNotFoundException e) {
			Logger.e("saveCamera() execute failed", e);
		}
		catch (IOException e) {
			Logger.e("saveCamera() execute failed", e);
		}
		finally {
			try {
				if(null != stream) {
					stream.close();
				}
				stream = null;
			}
			catch (IOException e) { }
		}
		return null;
	}
}
