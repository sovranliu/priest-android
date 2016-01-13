package com.wehop.priest.view.form;

import java.io.File;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.slfuture.pluto.communication.Host;
import com.slfuture.pluto.communication.response.ImageResponse;
import com.slfuture.pluto.etc.GraphicsHelper;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.ActivityEx;
import com.wehop.priest.R;
import com.wehop.priest.view.control.ImageViewEx;

@ResourceView(id = R.layout.activity_image)
public class ImageActivity extends ActivityEx {
	@ResourceView(id = R.id.image_image_main)
	public ImageViewEx image;

	/**
	 * 界面创建
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        String path = this.getIntent().getStringExtra("path");
        String url = this.getIntent().getStringExtra("url");
        if(null != path && (new File(path)).exists()) {
        	image.setImage(GraphicsHelper.decodeFile(new File(path), 500, 500));
        }
        else {
            Host.doImage("image", new ImageResponse(url, null) {
    			@Override
    			public void onFinished(Bitmap content) {
    		        image.setImage(content);
    			}
            }, url);
        }
    }
}
