package com.wehop.priest.business;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import com.slfuture.carrie.base.etc.Serial;
import com.slfuture.carrie.base.json.JSONNumber;
import com.slfuture.carrie.base.json.JSONObject;
import com.slfuture.carrie.base.json.JSONString;
import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.carrie.base.model.core.IEventable;
import com.slfuture.carrie.base.type.List;
import com.slfuture.carrie.base.type.safe.Table;
import com.slfuture.pluto.communication.Networking;
import com.slfuture.pluto.communication.response.CommonResponse;
import com.slfuture.pluto.communication.response.JSONResponse;
import com.slfuture.pluto.etc.Version;
import com.slfuture.pluto.framework.Broadcaster;
import com.slfuture.pluto.sensor.Reminder;
import com.slfuture.pretty.im.Module;
import com.slfuture.pretty.im.core.IReactor;
import com.slfuture.pretty.im.view.form.SingleChatActivity;
import com.wehop.priest.Program;
import com.wehop.priest.business.core.IMeListener;
import com.wehop.priest.business.structure.notify.AddAcceptNotify;
import com.wehop.priest.business.structure.notify.BeRemovedNotify;
import com.wehop.priest.business.structure.notify.BeSelectedNotify;
import com.wehop.priest.business.user.Doctor;
import com.wehop.priest.business.user.Patient;
import com.wehop.priest.business.user.User;
import com.wehop.priest.framework.Storage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.Toast;

/**
 * 当前登录用户类
 */
public class Me extends Doctor implements Serializable, IReactor {
	private static final long serialVersionUID = 1L;

	/**
	 * 手机号码
	 */
	public String phone;
	/**
	 * 密码
	 */
	public String password;
	/**
	 * 口令
	 */
	public String token = null;
	/**
	 * 医生列表
	 */
	public List<Doctor> doctors = new List<Doctor>();
	/**
	 * 病人列表
	 */
	public List<Patient> patients = new List<Patient>();
	/**
	 * 最近联系人
	 */
	public Table<String, Bitmap> contacts = new Table<String, Bitmap>();

	/**
	 * 实例
	 */
	public static Me instance = null;


	/**
	 * 登录
	 * 
	 * @param context 上下文
	 * @param phone 手机号码
	 * @param code 验证码
	 * @param callback 回调函数
	 */
	public static void login(Context context, String phone, String code, IEventable<Boolean> callback) {
		Networking.doCommand("login", new JSONResponse(context, callback) {
			@SuppressWarnings("unchecked")
			@Override
			public void onFinished(JSONVisitor content) {
				final IEventable<Boolean> callback = (IEventable<Boolean>) tag;
				if(null == content) {
					if(null != callback) {
						callback.on(false);
					}
					return;
				}
				if(content.getInteger("code", 0) <= 0) {
					if(null != callback) {
						callback.on(false);
					}
					return;
				}
				content = content.getVisitor("data");
				if(null == content) {
					if(null != callback) {
						callback.on(false);
					}
					return;
				}
				Me me = new Me();
				me.parse(content);
				instance = me;
				try {
					instance.save();
				}
				catch (IOException e) {
					throw new RuntimeException("存储用户信息失败", e);
				}
				Module.reactor = instance;
				Module.login(new IEventable<Boolean>() {
					@Override
					public void on(Boolean arg0) {
						if(null == callback) {
							return;
						}
						if(arg0) {
							callback.on(true);
						}
						else {
							callback.on(false);
						}
					}
				});
			}
		}, phone, code);
	}

	/**
	 * 自动登录
	 * 
	 * @param context 上下文
	 * @param callback 回调函数
	 */
	public static void autoLogin(Context context, IEventable<Boolean> callback) {
		try {
			Me me = read();
			if(null == me) {
				if(null != callback) {
					callback.on(false);
				}
			}
			else {
				instance = me;
				Networking.doCommand("check", new JSONResponse(context, callback) {
					@SuppressWarnings("unchecked")
					@Override
					public void onFinished(JSONVisitor content) {
						final IEventable<Boolean> callback = (IEventable<Boolean>) tag;
						if(null == content) {
							instance = null;
							callback.on(false);
						}
						else {
							if(0 < content.getInteger("code", 0)) {
								if(instance.parse(content.getVisitor("data"))) {
									try {
										instance.save();
									}
									catch (IOException e) {
										throw new RuntimeException("存储用户信息失败", e);
									}
								}
								Module.reactor = instance;
								Module.login(new IEventable<Boolean>() {
									@Override
									public void on(Boolean arg0) {
										if(arg0) {
											callback.on(true);
										}
										else {
											callback.on(false);
										}
									}
								});
							}
							else {
								instance = null;
								callback.on(false);
							}
						}
					}
				}, me.phone, me.token);
			}
		}
		catch (IOException e) {
			throw new RuntimeException("读取用户信息失败", e);
		}
	}

	/**
	 * 退出登录
	 */
	public void logout() {
		Module.logout(null);
		instance = null;
		delete();
	}

	/**
	 * 刷新私人成员
	 * 
	 * @param context 上下文 
	 * @param callback 结果
	 */
	public void refreshDoctor(Context context, IEventable<Boolean> callback) {
		Networking.doCommand("doctorList", new JSONResponse(context, callback) {
			@SuppressWarnings("unchecked")
			@Override
			public void onFinished(JSONVisitor content) {
				 IEventable<Boolean> callback = (IEventable<Boolean>) tag;
				 if(null == content || content.getInteger("code", 1) < 0) {
					 callback.on(false);
					 return;
				 }
				 doctors = new List<Doctor>();
				 if(null == content.getVisitors("data")) {
					 return;
				 }
				 for(JSONVisitor item : content.getVisitors("data")) {
					 Doctor doctor = new Doctor();
					 if(doctor.parse(item)) {
						 doctors.add(doctor);
					 }
				 }
				 try {
					save();
				 }
				 catch (IOException e) {}
				 callback.on(true);
			}
		}, token);
	}

	/**
	 * 刷新成员
	 * 
	 * @param context 上下文 
	 * @param callback 结果
	 */
	public void refreshPatient(Context context, IEventable<Boolean> callback) {
		Networking.doCommand("patientList", new JSONResponse(context, callback) {
			@SuppressWarnings("unchecked")
			@Override
			public void onFinished(JSONVisitor content) {
				 IEventable<Boolean> callback = (IEventable<Boolean>) tag;
				 if(null == content || content.getInteger("code", 1) < 0) {
					 callback.on(false);
					 return;
				 }
				 patients = new List<Patient>();
				 if(null == content.getVisitors("data")) {
					 return;
				 }
				 for(JSONVisitor item : content.getVisitors("data")) {
					 Patient patient = new Patient();
					 if(patient.parse(item)) {
						 patients.add(patient);
					 }
				 }
				 try {
					save();
				 }
				 catch (IOException e) {}
				 callback.on(true);
			}
		}, token);
	}

	/**
	 * 打开聊天对话框
	 * 
	 * @param context 上下文
	 * @param groupId 聊天群组ID
	 * @param remoteId 聊天对方ID
	 */
	public void doChat(Context context, String groupId, String remoteId) {
		Intent intent = new Intent(context, SingleChatActivity.class);
		intent.putExtra("selfId", imId);
		intent.putExtra("groupId", groupId);
		intent.putExtra("remoteId", remoteId);
		context.startActivity(intent);
	}

	/**
	 * 保存
	 */
	public void save() throws IOException {
		contacts = new Table<String, Bitmap>();
		Serial.restore(this, file());
	}

	/**
	 * 通过通信ID获取好友
	 * 
	 * @param imId 通信ID
	 * @return 好友对象
	 */
	public User fetchUserByIM(String imId) {
		for(Doctor doctor : doctors) {
			if(imId.equals(doctor.imId)) {
				return doctor;
			}
		}
		for(Patient patient : patients) {
			if(imId.equals(patient.imId)) {
				return patient;
			}
		}
		return null;
	}

	/**
	 * 通过用户ID获取好友
	 * 
	 * @param userId 用户ID
	 * @return 好友对象
	 */
	public User fetchUserById(String userId) {
		for(Doctor doctor : doctors) {
			if(userId.equals(doctor.id)) {
				return doctor;
			}
		}
		for(Patient patient : patients) {
			if(userId.equals(patient.id)) {
				return patient;
			}
		}
		return null;
	}

	/**
	 * 解析数据生成用户对象
	 * 
	 * @param visitor 数据
	 * @return 解析结果
	 */
	public boolean parse(JSONVisitor visitor) {
		if(!super.parse(visitor)) {
			return false;
		}
		phone = visitor.getString("username");
		token = visitor.getString("token");
		doctors.clear();
		for(JSONVisitor item : visitor.getVisitors("doctorList")) {
			// 医生
			Doctor doctor = new Doctor();
			if(doctor.parse(item)) {
				doctors.add(doctor);
			}
		}
		patients.clear();
		for(JSONVisitor item : visitor.getVisitors("patientList")) {
			// 病人
			Patient patient = new Patient();
			if(patient.parse(item)) {
				patients.add(patient);
			}
		}
		return true;
	}

	/**
	 * 获取存储文件
	 * 
	 * @return 存储文件
	 */
	public static File file() {
		return new File(Storage.dataFolder() + "me." + Version.fetchVersion(Program.application).toString() + ".dat");
	}

	/**
	 * 删除
	 */
	private void delete() {
		file().delete();
	}

	/**
	 * 读取
	 * 
	 * @return 返回存储的对象
	 */
	private static Me read() throws IOException {
		File file = file();
		if(!file.exists()) {
			return null;
		}
		try {
			return Serial.extract(file, Me.class);
		}
		catch (ClassNotFoundException e1) {
			return null;
		}
	}

	@Override
	public Bitmap getPhoto(String userId) {
		if(null == contacts) {
			contacts = new Table<String, Bitmap>();
		}
		Bitmap cache = contacts.get(userId);
		if(null != cache) {
			return cache;
		}
		if(this.imId.equals(userId)) {
			cache = photo();
		}
		User user = fetchUserByIM(userId);
		if(null != user) {
			cache = user.photo();
		}
		if(null != cache) {
			contacts.put(userId, cache);
		}
		return cache;
	}

	@Override
	public String getName(String userId) {
		if(imId.equals(userId)) {
			return nickname;
		}
		User user = fetchUserByIM(userId);
		if(null == user) {
			return null;
		}
		return user.nickname();
	}

	@Override
	public String getUserId() {
		return imId;
	}

	@Override
	public String getPassword() {
		return phone;
	}

	@Override
	public void onConflict() {
		logout();
		Broadcaster.<IMeListener>broadcast(Program.application, IMeListener.class).onConflict();
	}

	@Override
	public void onCommand(final String from, final String action, final com.slfuture.carrie.base.type.Table<String, Object> data) {
		Integer type = (Integer) data.get("type");
		String source = (String) data.get("source");
		if("systemMessage".equals(action)) {
			Runtime.hasUnreadMessage = true;
		}
		else if("message".equals(action)) {
			boolean sentry = false;
			for(Patient patient : patients) {
				if(from.equals(patient.imId)) {
					sentry = true;
				}
			}
			if(!sentry) {
				refreshPatient(Program.application, new IEventable<Boolean>() {
					@Override
					public void on(Boolean event) { }
				});
			}
		}
		else if("send".equals(action)) {
			JSONObject object = new JSONObject();
			object.put("action", new JSONString("send"));
			object.put("from", new JSONString(from));
			object.put("to", new JSONString((String) data.get("to")));
			object.put("type", new JSONNumber((Integer) data.get("type")));
			Networking.doCommand("Hit", new CommonResponse<String>() {
				@Override
				public void onFinished(String content) { }
			}, "doctor-platform-onlineDiag", object.toString());
			return;
		}
		if(null != type && BeSelectedNotify.TYPE_BESELECTED == type) {
			Me.instance.refreshPatient(Program.application, new IEventable<Boolean>() {
				@Override
				public void on(Boolean result) {
					if(!result) {
						return;
					}
					Broadcaster.<IMeListener>broadcast(Program.application, IMeListener.class).onCommand(from, action, data);
				}
			});
			Reminder.vibrate(Program.application);
			return;
		}
		if(null != type && (AddAcceptNotify.TYPE_ADDACCEPT == type || BeRemovedNotify.TYPE_BEREMOVE == type)) {
			if(User.CATEGORY_DOCTOR.equals(source)) {
				Me.instance.refreshDoctor(Program.application, new IEventable<Boolean>() {
					@Override
					public void on(Boolean result) {
						if(!result) {
							return;
						}
						Broadcaster.<IMeListener>broadcast(Program.application, IMeListener.class).onCommand(from, action, data);
					}
				});
			}
			else if(User.CATEGORY_PATIENT.equals(source)) {
				Me.instance.refreshPatient(Program.application, new IEventable<Boolean>() {
					@Override
					public void on(Boolean result) {
						if(!result) {
							return;
						}
						Broadcaster.<IMeListener>broadcast(Program.application, IMeListener.class).onCommand(from, action, data);
					}
				});
			}
			Reminder.vibrate(Program.application);
			return;
		}
		Reminder.ringtone(Program.application);
		Broadcaster.<IMeListener>broadcast(Program.application, IMeListener.class).onCommand(from, action, data);
	}
}
