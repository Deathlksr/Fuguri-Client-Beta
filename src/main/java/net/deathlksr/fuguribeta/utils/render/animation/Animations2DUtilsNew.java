package net.deathlksr.fuguribeta.utils.render.animation;

public class Animations2DUtilsNew extends Animation2DUtils {

    private long lastMS;

    public Animations2DUtilsNew(float x, float y, float endX, float endY) {
        super(x, y, endX, endY);
        lastMS = System.currentTimeMillis();
    }

    @Override
    public void update(float smooth) {
        long delta = System.currentTimeMillis() - lastMS;
        for (int i = 0; i < delta; i++) {
            super.update(smooth);
        }
        lastMS = System.currentTimeMillis();
    }
}
