package net.deathlksr.fuguribeta.utils.render;

import net.deathlksr.fuguribeta.utils.timing.MSTimer;
import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.lang3.RandomUtils;

import java.awt.*;

import javax.vecmath.Vector3f;

import static net.deathlksr.fuguribeta.utils.MinecraftInstance.mc;
import static org.lwjgl.opengl.GL11.*;

public class Particle3D {
    private final Vector3f position;
    private final Vector3f motion;

    private final long spawnTime;

    private final MSTimer timer;
    private final float scale;

    public Particle3D(Vector3f position, Vector3f motion) {
        this.position = position;
        this.motion = motion;
        this.timer = new MSTimer();
        this.spawnTime = System.currentTimeMillis();
        this.scale = RandomUtils.nextInt(2, 3);
    }

    public Particle3D(Vector3f position) {
        this.position = position;
        this.motion = new Vector3f(RandomUtils.nextFloat(0, 0.005F) - 0.0025F, RandomUtils.nextFloat(0, 0.0025F), RandomUtils.nextFloat(0, 0.005F) - 0.0025F);
        this.timer = new MSTimer();
        this.spawnTime = System.currentTimeMillis();
        this.scale = RandomUtils.nextInt(5, 7);
    }

    public static void drawPoint(final double x, final double y, final double z, final float scale, Color color) {
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_POINT_SMOOTH);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        ColorUtils.INSTANCE.setColor(color.getRGB());

        glPointSize(scale);

        glBegin(GL_POINTS);
        glVertex3d(
                x - mc.getRenderManager().viewerPosX,
                y - mc.getRenderManager().viewerPosY,
                z - mc.getRenderManager().viewerPosZ
        );
        glEnd();

        glPointSize(1);

        GlStateManager.color(1f, 1f, 1f, 1f);
        glDisable(GL_POINT_SMOOTH);
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
    }

    public void render(Color color) {
        drawPoint(position.x, position.y, position.z, scale + 5, color);
        drawPoint(position.x, position.y, position.z, scale, color);

        for (int i = 0; i < timer.elapsedTime(); i++) {
            motion.x *= 0.999f;
            motion.y *= 0.999f;
            motion.z *= 0.999f;
        }

        position.x += motion.x;
        position.y += motion.y;
        position.z += motion.z;

        timer.reset();
    }

    public long getSpawnTime() {
        return spawnTime;
    }
}