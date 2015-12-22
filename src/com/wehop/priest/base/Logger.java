package com.wehop.priest.base;

import com.wehop.priest.Program;

import android.util.Log;

/**
 * 日志类
 */
public class Logger {
	/**
	 * 调试日志
	 * 
	 * @param message 日志内容
	 */
	public static void d(String message) {
		Log.d(Program.ID, message);
	}

	/**
	 * 信息日志
	 * 
	 * @param message 日志内容
	 */
	public static void i(String message) {
		Log.i(Program.ID, message);
	}

	/**
	 * 错误日志
	 * 
	 * @param message 日志内容
	 */
	public static void e(String message) {
		Log.e(Program.ID, message);
	}

	/**
	 * 错误日志
	 * 
	 * @param message 日志内容
	 * @param t 可抛出对象
	 */
	public static void e(String message, Throwable t) {
		Log.e(Program.ID, message, t);
	}
}
