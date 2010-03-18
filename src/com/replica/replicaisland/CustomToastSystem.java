package com.replica.replicaisland;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class CustomToastSystem extends BaseObject {
	private View mView;
	private TextView mText;
	private Toast mToast;
	
	public CustomToastSystem(Context context) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.custom_toast, null);
		
		mText = (TextView) mView.findViewById(R.id.text);
		mToast = new Toast(context);
		mToast.setView(mView);

	}
	
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}
	
	public void toast(String text, int length) {
		mText.setText(text);

		mToast.setGravity(Gravity.CENTER, 0, 0);
		mToast.setDuration(length);
		mToast.show();
	}

}
