package org.dstadler.commons.gpx;

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang3.time.FastDateFormat;

public class TrackPoint {
    private static final FastDateFormat TIME_FORMAT_OUT =
            FastDateFormat.getInstance("HH:mm:ss", TimeZone.getTimeZone("Europe/Vienna"));

    private double latitude;
    private double longitude;
    private double elevation;
    public Date time;
    private int hr;
    private int cadence;
    private double speed;
    private double temp;

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public void setCadence(int cadence) {
        this.cadence = cadence;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getElevation() {
        return elevation;
    }

    public long getTime() {
        return time.getTime();
    }

    public String getTimeString() {
        return TIME_FORMAT_OUT.format(getTime());
    }

    public String formatTime(long start) {
        int minutes = (int) ((getTime() - start) / 60 / 1000);
        int seconds = (int) ((getTime() - start) / 1000) % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    public int getHr() {
        return hr;
    }

    public int getCadence() {
        return cadence;
    }

    public double getSpeed() {
        return speed;
    }

    public double getTemp() {
        return temp;
    }

    @Override
    public String toString() {
        return "TrackPoint{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", elevation=" + elevation +
                ", time=" + time +
                ", hr=" + hr +
                ", cadence=" + cadence +
                ", speed=" + speed +
                ", temp=" + temp +
                '}';
    }
}
