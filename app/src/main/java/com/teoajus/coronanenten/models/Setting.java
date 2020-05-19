package com.teoajus.coronanenten.models;

public class Setting {

    private boolean notificationwhennearvirus;
    private boolean trackwhomnear;

    public Setting(boolean notificationwhennearvirus, boolean trackwhomnear) {
        this.notificationwhennearvirus = notificationwhennearvirus;
        this.trackwhomnear = trackwhomnear;
    }

    public boolean isNotificationwhennearvirus() {
        return notificationwhennearvirus;
    }

    public boolean isTrackwhomnear() {
        return trackwhomnear;
    }

    public void setNotificationwhennearvirus(boolean notificationwhennearvirus) {
        this.notificationwhennearvirus = notificationwhennearvirus;
    }

    public void setTrackwhomnear(boolean trackwhomnear) {
        this.trackwhomnear = trackwhomnear;
    }
}
