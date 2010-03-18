package com.replica.replicaisland;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class YesNoDialogPreference extends DialogPreference {
	private YesNoDialogListener mListener;
	
	public abstract interface YesNoDialogListener {
		public abstract void onDialogClosed(boolean positiveResult);
	}
	
	public YesNoDialogPreference(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.yesNoPreferenceStyle);
		// TODO Auto-generated constructor stub
	}

	public YesNoDialogPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public YesNoDialogPreference(Context context) {
        this(context, null);
    }

	public void setListener(YesNoDialogListener listener) {
		mListener = listener;
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (mListener != null) {
			mListener.onDialogClosed(positiveResult);
		}
    }
}
