package com.financetracker.ui.anim;

/**
 * 60 fps clock for the application
 * 
 * All components don't need to spin up a Swing Timer component,
 * they can use a callback here. The timer only runs while there
 * is at least one subscriber, so an app that is idle costs nothing.
 * 
 * All callbacks run on the EDT, so it's safe to mutate Swing state
 * and call repaint() from a Frame.
 */

public final class Animator {

    public interface Frame {
        boolean tick(long nowNanos, double deltaSeconds);
    }

    

}
