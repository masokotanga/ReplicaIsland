package com.replica.replicaisland;

import javax.microedition.khronos.opengles.GL10;

public class BufferLibrary extends BaseObject {
    private static final int GRID_LIST_SIZE = 256;
    private FixedSizeArray<Grid> mGridList;
   
    public BufferLibrary() {
        super();
        
        mGridList = new FixedSizeArray<Grid>(GRID_LIST_SIZE);
    }
    
    @Override
    public void reset() {
        removeAll();
    }
    
    public void add(Grid grid) { 
         mGridList.add(grid);
    }
    
    public void removeAll() {
        mGridList.clear();
    }
    
    public void generateHardwareBuffers(GL10 gl) {
    	if (sSystemRegistry.contextParameters.supportsVBOs) {
	        final int count = mGridList.getCount();
	        for (int x = 0; x < count; x++) {
	            Grid grid = mGridList.get(x);
	            grid.generateHardwareBuffers(gl);
	        }
    	}
    }
    
    public void releaseHardwareBuffers(GL10 gl) {
    	if (sSystemRegistry.contextParameters.supportsVBOs) {
	        final int count = mGridList.getCount();
	        for (int x = 0; x < count; x++) {
	            Grid grid = mGridList.get(x);
	            grid.releaseHardwareBuffers(gl);
	        }
    	}
    }
    
    public void invalidateHardwareBuffers() {
    	if (sSystemRegistry.contextParameters.supportsVBOs) {
	        final int count = mGridList.getCount();
	        for (int x = 0; x < count; x++) {
	            Grid grid = mGridList.get(x);
	            grid.invalidateHardwareBuffers();
	        }
    	}
    }

}
