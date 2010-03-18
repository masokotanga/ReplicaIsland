package com.replica.replicaisland;

public class HitPointPool extends TObjectPool<HitPoint> {

    @Override
    protected void fill() {
        final int size = getSize();
        for (int x = 0; x < size; x++) {
            getAvailable().add(new HitPoint());
        }
    }
    
    @Override
    public void release(Object entry) {
        ((HitPoint)entry).reset();
        super.release(entry);
    }

}
