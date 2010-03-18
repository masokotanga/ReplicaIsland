package com.replica.replicaisland;

public class EventRecorder extends BaseObject {
	private Vector2 mLastDeathPosition = new Vector2();
	
	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}
	
	synchronized void setLastDeathPosition(Vector2 position) {
		mLastDeathPosition.set(position);
	}
	
	synchronized Vector2 getLastDeathPosition() {
		return mLastDeathPosition;
	}

}
