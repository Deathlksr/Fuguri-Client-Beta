package net.ccbluex.liquidbounce.utils.render;

import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

public class renderutilnahui {
    public void RenderUtils() {
    }

    public static void otherDrawOutlinedBoundingBoxGL11(Entity entity, float x, float y, float z, double width, double height) {
        width *= 1.5;
        float yaw1 = 135.0F;
        float newYaw1;
        newYaw1 = yaw1;

        newYaw1 *= -1.0F;
        newYaw1 = (float) ((double) newYaw1 * 0.017453292519943295);
        float yaw2 = 225.0F;
        float newYaw2;
        newYaw2 = yaw2;

        newYaw2 *= -1.0F;
        newYaw2 = (float) ((double) newYaw2 * 0.017453292519943295);
        float yaw3 = 315.0F;
        float newYaw3;
        newYaw3 = yaw3;

        newYaw3 *= -1.0F;
        newYaw3 = (float) ((double) newYaw3 * 0.017453292519943295);
        float yaw4 = 405.0F;
        float newYaw4;
        newYaw4 = yaw4;

        newYaw4 *= -1.0F;
        newYaw4 = (float) ((double) newYaw4 * 0.017453292519943295);
        double x1 = Math.sin(newYaw1) * width + (double) x;
        double z1 = Math.cos(newYaw1) * width + (double) z;
        double x2 = Math.sin(newYaw2) * width + (double) x;
        double z2 = Math.cos(newYaw2) * width + (double) z;
        double x3 = Math.sin(newYaw3) * width + (double) x;
        double z3 = Math.cos(newYaw3) * width + (double) z;
        double x4 = Math.sin(newYaw4) * width + (double) x;
        double z4 = Math.cos(newYaw4) * width + (double) z;
        double y2 = (double) y + height;
        GL11.glBegin(1);
        GL11.glVertex3d(x1, y, z1);
        GL11.glVertex3d(x2, y, z2);
        GL11.glVertex3d(x2, y, z2);
        GL11.glVertex3d(x3, y, z3);
        GL11.glVertex3d(x3, y, z3);
        GL11.glVertex3d(x4, y, z4);
        GL11.glVertex3d(x4, y, z4);
        GL11.glVertex3d(x1, y, z1);
        GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x3, y2, z3);
        GL11.glVertex3d(x3, y2, z3);
        GL11.glVertex3d(x4, y2, z4);
        GL11.glVertex3d(x4, y2, z4);
        GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x1, y, z1);
        GL11.glVertex3d(x1, y2, z1);
        GL11.glVertex3d(x2, y, z2);
        GL11.glVertex3d(x2, y2, z2);
        GL11.glVertex3d(x3, y, z3);
        GL11.glVertex3d(x3, y2, z3);
        GL11.glVertex3d(x4, y, z4);
        GL11.glVertex3d(x4, y2, z4);
        GL11.glEnd();
    }
}