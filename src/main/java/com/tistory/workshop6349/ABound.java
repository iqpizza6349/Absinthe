package com.tistory.workshop6349;

public class ABound {

    public int frame = -1;
    public boolean exact = false;

    public ABound(int frame, boolean exact) {
        this.frame = frame;
        this.exact = exact;
    }

    public ABound update(int frame, boolean exact) {
        if (frame < this.frame) {
            return new ABound(frame, exact);
        }
        return this;
    }
}
