package org.concord.iot.listeners;

/**
 * @author Charles Xie
 *
 */

public interface GraphListener {

    public void graphClosed(GraphEvent e);

    public void graphOpened(GraphEvent e);

}