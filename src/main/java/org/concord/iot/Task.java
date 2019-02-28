package org.concord.iot;

/**
 * @author Charles Xie
 */

public class Task {

    private String name;
    private volatile boolean stopped;
    private RainbowHat hat;
    private Runnable runnable;

    public Task(String name, RainbowHat hat) {
        this.name = name;
        this.hat = hat;
    }

    public String getName() {
        return name;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public void run() {
        hat.submitTask(runnable);
    }

}
