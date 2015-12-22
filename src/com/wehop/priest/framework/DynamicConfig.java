package com.wehop.priest.framework;

import java.io.File;
import java.io.IOException;

import com.slfuture.carrie.base.async.Operator;
import com.slfuture.carrie.base.async.core.IOperation;
import com.slfuture.carrie.base.etc.Serial;
import com.slfuture.carrie.base.json.JSONObject;
import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.carrie.base.model.core.IEventable;
import com.slfuture.carrie.base.text.Text;
import com.slfuture.carrie.base.type.List;
import com.slfuture.carrie.base.type.safe.Table;
import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.net.HttpUtil;
import com.slfuture.pluto.net.future.FileFuture;
import com.wehop.priest.base.Logger;

/**
 * 动态配置
 */
public class DynamicConfig {
	/**
	 * 配置映射
	 */
	public static Table<String, List<IEventable<File>>> configMap = new Table<String, List<IEventable<File>>>();
	/**
	 * 当前版本号
	 */
	public static String version = null;


	/**
	 * 构造函数
	 */
	private DynamicConfig() {}

	/**
	 * 初始化
	 * 
	 * @return 执行结果
	 */
	public static boolean initialize() {
		File directory = new File(Storage.CONFIG_ROOT);
		if(!directory.exists()) {
			if(!directory.mkdirs()) {
				Logger.e("create config directory failed");
				return false;
			}
		}
		File versionFile = new File(Storage.CONFIG_ROOT + "version");
		version = "";
		if(versionFile.exists()) {
			try {
				version = Text.loadFile(Storage.CONFIG_ROOT + "version", 0);
			}
			catch (Exception e) { }
		}
		new Operator<Void>(new IOperation<Void> () {
		    /**
		     * 操作结束回调
		     *
		     * @return 操作结果
		     */
			@Override
		    public Void onExecute() {
				String confString = HttpUtil.send(Host.fetchURL("config", version), null, null, null);
				if(null == confString) {
					Logger.e("config check failed");
					return null;
				}
				JSONVisitor content = new JSONVisitor(JSONObject.convert(confString));
				JSONVisitor data = content.getVisitor("data");
				String currentVersion = data.getString("version");
				if(currentVersion.equalsIgnoreCase(version)) {
					Logger.d("check config equal");
					return null;
				}
				version = currentVersion;
				for(JSONVisitor visitor : data.getVisitors("changeList")) {
					String name = visitor.getString("name");
					String path = Storage.CONFIG_ROOT + name;
					File file = new File(path);
					String url = visitor.getString("url");
					FileFuture future = new FileFuture();
					future.file = new File(path + "." + Serial.makeSerialString());
					HttpUtil.send(url, null, null, future);
					try {
						future.await(0);
					}
					catch (InterruptedException ex) {
						return null;
					}
					future.file.renameTo(file);
					List<IEventable<File>> list = configMap.get(name);
					if(null != list) {
						for(IEventable<File> item : list) {
							try {
								item.on(file);
							}
							catch(Exception ex) {
								Logger.e("error occur in config change trigger", ex);
								break;
							}
						}
					}
				}
				// 保存配置版本号
				try {
					Text.saveFile(version, 0, new File(Storage.CONFIG_ROOT + "version"));
				}
				catch (IOException e) { }
		    	return null;
		    }
		});
		return true;
	}

	/**
	 * 终结
	 */
	public static void terminate() {
		configMap.clear();
	}

	/**
	 * 获取配置文件
	 * 
	 * @param name 文件名称，包含路径
	 * @return 配置文件
	 */
	public static File fetchFile(String name) {
		File file = new File(Storage.CONFIG_ROOT + name);
		if(!file.exists()) {
			return null;
		}
		return file;
	}

	/**
	 * 监视文件变化
	 * 
	 * @param name 文件名称，包含路径
	 * @param callback 回调
	 */
	public static void watchFile(String name, IEventable<File> callback) {
		List<IEventable<File>> list = configMap.get(name);
		if(null == list) {
			list = new List<IEventable<File>>();
			configMap.put(name, list);
		}
		list.add(callback);
	}
}
