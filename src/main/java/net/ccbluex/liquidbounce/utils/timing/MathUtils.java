package net.ccbluex.liquidbounce.utils.timing;

public class MathUtils {
    public static float lerp(final float start, final float end, final float smooth) {
        return start + (end - start) * smooth;
    }
}
