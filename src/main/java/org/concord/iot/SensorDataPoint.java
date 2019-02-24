package org.concord.iot;

/**
 * @author Charles Xie
 *
 */
public final class SensorDataPoint {

    private final double time;
    private final double value;

    public SensorDataPoint(double time, double value) {
        this.time = time;
        this.value = value;
    }

    public double getTime() {
        return time;
    }

    public double getValue() {
        return value;
    }

}