package com.replica.replicaisland;

public class CameraBiasComponent extends GameComponent {
	 public CameraBiasComponent() {
        super();
        setPhase(GameComponent.ComponentPhases.THINK.ordinal());
    }
    
    @Override
    public void reset() {
        
    }
    
    @Override
    public void update(float timeDelta, BaseObject parent) {   
    	GameObject parentObject = (GameObject)parent;
    	CameraSystem camera = sSystemRegistry.cameraSystem;
    	if (camera != null) {
    		camera.addCameraBias(parentObject.getPosition());
    	}
	}
}
