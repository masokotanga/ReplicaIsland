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
	
	private InputXY mTouchScreen = new InputXY();	// I guess for multitouch this could be an array.
	private InputXY mOrientationSensor = new InputXY();
	private InputXY mTrackball = new InputXY();
    private InputKeyboard mKeyboard = new InputKeyboard();
               
    public InputSystem() {
        super();
        reset();
    }
    
    @Override
    public void reset() {
    	mTrackball.reset();
    	mTouchScreen.reset();
    	mKeyboard.resetAll();
    	mOrientationSensor.reset();
    }

    public void roll(float x, float y) {
        TimeSystem time = sSystemRegistry.timeSystem;
    	mTrackball.press(time.getGameTime(), mTrackball.getX() + x, mTrackball.getY() + y);
    }
    
    public void touchDown(float x, float y) {
	   ContextParameters params = sSystemRegistry.contextParameters;
	   TimeSystem time = sSystemRegistry.timeSystem;
	   // Change the origin of the touch location from the top-left to the bottom-left to match
	   // OpenGL space.
	   // TODO: UNIFY THIS SHIT
	   mTouchScreen.press(time.getGameTime(), x, params.gameHeight - y);   
    }
    
    public void touchUp(float x, float y) {
    	// TODO: record up location?
    	mTouchScreen.release();
    }
    
    public void setOrientation(float azimuth, float pitch, float roll) {   
        //DebugLog.d("Orientation", "Pitch: " + pitch + "  Roll: " + roll);
        final float correctedPitch = -pitch / 180.0f;
        final float correctedRoll = -roll / 90.0f;
        //DebugLog.d("Orientation", "Pitch: " + correctedPitch + "  Roll: " + correctedRoll);

        TimeSystem time = sSystemRegistry.timeSystem;
        mOrientationSensor.press(time.getGameTime(), correctedPitch, correctedRoll);
        
    }
    
    public void keyDown(int keycode) {
    	TimeSystem time = sSystemRegistry.timeSystem;
        final float gameTime = time.getGameTime();
        mKeyboard.press(gameTime, keycode);
    }
    
    public void keyUp(int keycode) {
    	mKeyboard.release(keycode);
    }
    
    public void releaseAllKeys() {
    	mTrackball.releaseX();
    	mTrackball.releaseY();
    	mTouchScreen.release();
    	mKeyboard.releaseAll();
    	mOrientationSensor.release();
    }

	public InputXY getTouchScreen() {
		return mTouchScreen;
	}

	public InputXY getOrientationSensor() {
		return mOrientationSensor;
	}

	public InputXY getTrackball() {
		return mTrackball;
	}

	public InputKeyboard getKeyboard() {
		return mKeyboard;
	}
    
    

}
