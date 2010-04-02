/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.replica.replicaisland;

/** 
 * Manages input from a roller wheel and touch screen.  Reduces frequent UI messages to
 * an average direction over a short period of time.
 */
public class InputSystem extends BaseObject {
	
	private class InputButton {
		private static final float BUTTON_LIFETIME = -1.0f; //60.0f * 10.0f;
		
		private float mTimeout = BUTTON_LIFETIME;
		private boolean mDown;
		private boolean mReleased;
		private int mDownFrames;
		private float mLastPressedTime;
		private float mDownTime;
		private float mMagnitude;
		private boolean mActive = true;	// if false, does all necessary tracking but returns false to queries.
		
		public void press(float currentTime, float magnitude) {
			if (!mDown) {
				mDown = true;
				mDownFrames = 0;
				mDownTime = currentTime;
			}
			mMagnitude = magnitude;
			mLastPressedTime = currentTime;
			mReleased = false;
		}
		
		public void release() {
			mReleased = true;
		}
		
		public void update(float currentTime) {
			if (mDown) {
				mDownFrames++;
				if (mDownFrames > 1) {
					if (mReleased || (mTimeout > 0.0f && (currentTime - mLastPressedTime) > mTimeout)) {
						mDown = false;
					}
				}
			}
		}
		
		public final boolean getPressed() {
			return mActive ? mDown : false;
		}
		
		public final boolean getTriggered() {
			return mActive ? (mDown && mDownFrames <= 1) : false;
		}
		
		public final float getPressedDuration(float currentTime) {
			return mActive ? (currentTime - mDownTime) : 0.0f;
		}
		
		public final float getLastPressedTime() {
			return mLastPressedTime;
		}
		
		public final float getMagnitude() {
			float magnitude = 0.0f;
			if (mActive && mDown) {
				magnitude = mMagnitude;
			}
			return magnitude;
		}
		
		public final void setTimeout(float timeout) {
			mTimeout = timeout;
		}

		public void setActive(boolean active) {
			mActive = active;
		}
	}
	
	private class InputXY {
		private InputButton mXAxis = new InputButton();
		private InputButton mYAxis = new InputButton();
		
		public final void press(float currentTime, float x, float y) {
			mXAxis.press(currentTime, x);
			mYAxis.press(currentTime, y);
		}
		
		public final void release() {
			mXAxis.release();
			mYAxis.release();
		}
		
		public final void update(float currentTime) {
			mXAxis.update(currentTime);
			mYAxis.update(currentTime);
		}
		
		public final boolean getTriggered() {
			return mXAxis.getTriggered() || mYAxis.getTriggered();
		}
		
		public final boolean getPressed() {
			return mXAxis.getPressed() || mYAxis.getPressed();
		}
		
		public final void setVector(Vector2 vector) {
			vector.x = mXAxis.getMagnitude();
			vector.y = mYAxis.getMagnitude();
		}
		
		public final float getX() {
			return mXAxis.getMagnitude();
		}
		
		public final float getY() {
			return mYAxis.getMagnitude();
		}
		
		public final float getLastPressedTime() {
			return Math.max(mXAxis.getLastPressedTime(), mYAxis.getLastPressedTime());
		}
		
		public final void releaseX() {
			mXAxis.release();
		}
		
		public final void releaseY() {
			mYAxis.release();
		}
		
		public final void setTimeout(float timeout) {
			mXAxis.setTimeout(timeout);
			mYAxis.setTimeout(timeout);
		}
	}
	
	private final static float KEY_ROLL_SPEED = 0.25f;
	private final static float TILT_ROLL_SPEED = 1.0f;
	private final static float TILT_ROLL_MODIFIER = 8.0f;
    private final static float DPAD_TIMEOUT = -1.0f; //2.0f;
    private final static float ROLL_TIMEOUT = 0.1f;
    private final static int ROLL_HISTORY_SIZE = 10;
    // Raw trackball input is filtered by this value. Increasing it will 
    // make the control more twitchy, while decreasing it will make the control more precise.
    private final static float ROLL_FILTER = 0.4f;

	private InputXY mTouchScreen = new InputXY();
	private InputXY mOrientationSensor = new InputXY();
	private InputXY mDirectionalPad = new InputXY();
    private InputButton mClick = new InputButton();
        
    private Vector2 mTempDirection = new Vector2();
    
    // Roll averaging
    private FixedSizeArray<Vector2> mRecentRollInput = new FixedSizeArray<Vector2>(ROLL_HISTORY_SIZE);
    private int mCurrentInputSlot;
    private Vector2 mCurrentRollDirection = new Vector2();
   
    private boolean mUseOrientationForRoll = false;	// If true, pipes tilt into the directional pad.
    private float mOrientationSensitivityModifier = 0.5f;
       
    public InputSystem() {
        super();
        for (int x = 0; x < ROLL_HISTORY_SIZE; x++) {
        	mRecentRollInput.add(new Vector2());
        }
        reset();
    }
    
    @Override
    public void reset() {
    	mCurrentInputSlot = 0;
    	mCurrentRollDirection.zero();
    }

    public void roll(float x, float y) {
    	if (!mDirectionalPad.getPressed()) {
    		for (int index = 0; index < ROLL_HISTORY_SIZE; index++) {
    			mRecentRollInput.get(index).zero();
            }  
            mCurrentRollDirection.set(x * ROLL_FILTER, y * ROLL_FILTER);
            mRecentRollInput.get(0).set(x * ROLL_FILTER, y * ROLL_FILTER);
            mCurrentInputSlot = 1;
    	} else {
	    	// recalculate accumulated direction
	        mCurrentRollDirection.subtract(mRecentRollInput.get(mCurrentInputSlot));
	        mRecentRollInput.get(mCurrentInputSlot).set(x * ROLL_FILTER, y * ROLL_FILTER);
	        mCurrentRollDirection.add(mRecentRollInput.get(mCurrentInputSlot));
	        
	        mCurrentInputSlot = (mCurrentInputSlot + 1) % ROLL_HISTORY_SIZE;
	        
	        // Clamp to 1.0
	        if (Math.abs(mCurrentRollDirection.x) > 1.0f) {
	            if (mCurrentRollDirection.x < 0.0f) {
	                mCurrentRollDirection.x = (-1.0f);
	            } else {
	                mCurrentRollDirection.x = (1.0f);
	            }
	        }
	
	        if (Math.abs(mCurrentRollDirection.y) > 1.0f) {
	            if (mCurrentRollDirection.y < 0.0f) {
	                mCurrentRollDirection.y = (-1.0f);
	            } else {
	                mCurrentRollDirection.y = (1.0f);
	            }
	        }
    	}
        TimeSystem time = sSystemRegistry.timeSystem;
        mDirectionalPad.setTimeout(ROLL_TIMEOUT);
        mDirectionalPad.press(time.getGameTime(), mCurrentRollDirection.x, mCurrentRollDirection.y);
    }
    
    public void touch(int x, int y, boolean released) {
        if (released) {
        	mTouchScreen.release();
        } else {
        	ContextParameters params = sSystemRegistry.contextParameters;
        	TimeSystem time = sSystemRegistry.timeSystem;
        	// Change the origin of the touch location from the top-left to the bottom-left to match
            // OpenGL space.
            // TODO: UNIFY THIS SHIT
        	mTouchScreen.press(time.getGameTime(), x, params.gameHeight - y);
        }
    }
    
    public void clickDown() {
        TimeSystem time = sSystemRegistry.timeSystem;
        mClick.press(time.getGameTime(), 1.0f);
    }
    
    public void clickUp() {        
    	mClick.release();
    }
    
    public void setOrientation(float azimuth, float pitch, float roll) {   
        //DebugLog.d("Orientation", "Pitch: " + pitch + "  Roll: " + roll);
        final float correctedPitch = -pitch / 180.0f;
        final float correctedRoll = -roll / 90.0f;
        //DebugLog.d("Orientation", "Pitch: " + correctedPitch + "  Roll: " + correctedRoll);

        TimeSystem time = sSystemRegistry.timeSystem;
        mOrientationSensor.press(time.getGameTime(), correctedPitch, correctedRoll);
        if (mUseOrientationForRoll) {
        	float smoothedPitch = correctedPitch;
        	float smoothedRoll = correctedRoll;
        	if (Math.abs(correctedPitch) < 0.03f) {
        		smoothedPitch = 0.0f;	// dead zone
        	} else if (Math.abs(correctedPitch) < 0.1f) {
        		smoothedPitch *= 0.75f;
        	}
        	if (Math.abs(smoothedRoll) < 0.03f) {
        		smoothedRoll = 0.0f;	// dead zone
        	} else if (Math.abs(smoothedRoll) < 0.1f) {
        		smoothedRoll *= 0.75f;
        	}
        	
        	//roll(smoothedPitch * TILT_ROLL_SPEED, smoothedRoll * TILT_ROLL_SPEED);
        	mDirectionalPad.press(time.getGameTime(), 
        			smoothedPitch * (TILT_ROLL_SPEED + (TILT_ROLL_MODIFIER * mOrientationSensitivityModifier)), 
        			smoothedRoll * (TILT_ROLL_SPEED + (TILT_ROLL_MODIFIER * mOrientationSensitivityModifier)));
        }
    }
    
    public void keyDown(boolean left, boolean right, boolean up, boolean down, 
            boolean touch, boolean click) {
        float x = 0.0f;
        float y = 0.0f;
        TimeSystem time = sSystemRegistry.timeSystem;
        final float gameTime = time.getGameTime();
        if (left) {
            x = -KEY_ROLL_SPEED;
        } else if (right) {
            x = KEY_ROLL_SPEED;
        }
        if (up) {
            y = KEY_ROLL_SPEED;
        } else if (down) {
            y = -KEY_ROLL_SPEED;
        }
        if (x != 0.0f || y != 0.0f) {
            mDirectionalPad.setTimeout(DPAD_TIMEOUT);
            mDirectionalPad.press(gameTime, x, y);
        }
        
        if (touch) {
            mTouchScreen.press(gameTime, 0, 0);
        }
        
        if (click) {
        	mClick.press(gameTime, 1.0f);
        }
    }
    
    public void keyUp(boolean left, boolean right, boolean up, boolean down, 
            boolean touch, boolean click) {
    	if (left || right) {
    		mDirectionalPad.releaseX();
    	}
    	
    	if (up || down) {
    		mDirectionalPad.releaseY();
    	}
    	
    	if (touch) {
    		mTouchScreen.release();
    	}
    	
    	if (click) {
    		mClick.release();
    	}
    }
    
    public void releaseAllKeys() {
    	mDirectionalPad.releaseX();
    	mDirectionalPad.releaseY();
    	mTouchScreen.release();
    	mClick.release();
    }

    @Override
    public void update(float timeDelta, BaseObject parent) {
        TimeSystem time = sSystemRegistry.timeSystem;
        final float gameTime = time.getGameTime();
        
        mTouchScreen.update(gameTime);
        mDirectionalPad.update(gameTime);
        mOrientationSensor.update(gameTime);
        mClick.update(gameTime);
    }

    public final boolean getRollTriggered() {
        return mDirectionalPad.getPressed();
    }

    public final Vector2 getRollDirection() {
    	mDirectionalPad.setVector(mTempDirection);
        return mTempDirection;
    }
    
    public final boolean getTouchTriggered() {
        return mTouchScreen.getTriggered();
    }
    
    public final boolean getTouchPressed() {
        return mTouchScreen.getPressed();
    }

    public final Vector2 getTouchPosition() {
    	mTouchScreen.setVector(mTempDirection);
        return mTempDirection;
    }
    
    public final boolean getTouchedWithinRegion(int x, int y, int width, int height) {
    	mTouchScreen.setVector(mTempDirection);

        return (mTempDirection.x >= x &&
        		mTempDirection.y >= y &&
        		mTempDirection.x <= x + width &&
        		mTempDirection.y <= y + height);
    }
    
    public final boolean getClickTriggered() {
        return mClick.getTriggered();
    }
    
    public final void clearClickTriggered() {
    	mClick.release();
    }
    
    public final boolean getClickPressed() {
        return mClick.getPressed();
    }
    
    public final float getLastRollTime() {
        return mDirectionalPad.getLastPressedTime();
    }
    
    public final float getLastTouchTime() {
        return mTouchScreen.getLastPressedTime();
    }
    
    public final float getLastClickTime() {
        return mClick.getLastPressedTime();
    }
    
    public final float getPitch() {
        return mOrientationSensor.getX();
    }
    
    public final float getRoll() {
        return mOrientationSensor.getY();
    }

	public void setUseOrientationForRoll(boolean rollWithOrientation) {
		mUseOrientationForRoll = rollWithOrientation;
	}

	public void setOrientationSensitivityModifier(float modifier) {
		mOrientationSensitivityModifier = modifier;
	}
  
}
