package net.deathlksr.fuguribeta.utils.render.animation;

import net.deathlksr.fuguribeta.utils.timing.MathUtils;

public class Animation2DUtils {
    private float x, y;
    private float endX, endY;

    public Animation2DUtils(float x, float y, float endX, float endY) {
        this.x = x;
        this.y = y;
        this.endX = endX;
        this.endY = endY;
    }

    public void update(float smooth) {
        x = MathUtils.lerp(x, endX, smooth);
        y = MathUtils.lerp(y, endY, smooth);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getEndX() {
        return endX;
    }

    public void setEndX(float endX) {
        this.endX = endX;
    }

    public float getEndY() {
        return endY;
    }

    public void setEndY(float endY) {
        this.endY = endY;
    }
}