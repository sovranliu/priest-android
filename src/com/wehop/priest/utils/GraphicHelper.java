package com.wehop.priest.utils;

import com.wehop.priest.Program;
import com.wehop.priest.R;
import com.wehop.priest.business.Logic;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;

/**
 * 图像处理帮助类
 */
public class GraphicHelper {
	/**
	 * 生成圆形的图片
	 * 
	 * @param bitmap 图片对象
	 * @param width 目标宽度
	 * @param height 目标高度
	 * @return 打圆的图片
	 */
	public static Bitmap makeCycleImage(Bitmap bitmap, int width, int height) {
		float radius = 0;
		if(width > height) {
			radius = height;
		}
		else {
			radius = width;
		}
		Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		RectF rect = new RectF(0, 0, radius, radius);
		canvas.drawRoundRect(rect, radius/2, radius/2, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, null, rect, paint);
		return result;
	}

	/**
	 * 给位图增加环
	 * 
	 * @param bitmap 位图对象
	 * @param stroke 位图对象
	 */
	public static Bitmap makeImageRing(Bitmap bitmap, int strokeWidth) {
		float radius = bitmap.getWidth();
		if(radius > bitmap.getHeight()) {
			radius = bitmap.getHeight();
		}
		radius = radius / 2 - strokeWidth + 1;
		Bitmap result = bitmap;
		Canvas canvas = new Canvas(result);
		Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        paint.setColor(Program.application.getResources().getColor(R.color.blue));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, radius, paint);
		return result;
	}
}
