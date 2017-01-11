package com.server.extensions.plane;

/**
 * Created by wuyingtan on 2016/12/21.
 */
public class Plane {
    private long userId;
    private int airFrame;
    private int wing;
    private int ejector;

    public Plane() {

    }

    public Plane(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getAirFrame() {
        return airFrame;
    }

    public void setAirFrame(int airFrame) {
        this.airFrame = airFrame;
    }

    public int getWing() {
        return wing;
    }

    public void setWing(int wing) {
        this.wing = wing;
    }

    public int getEjector() {
        return ejector;
    }

    public void setEjector(int ejector) {
        this.ejector = ejector;
    }
}
