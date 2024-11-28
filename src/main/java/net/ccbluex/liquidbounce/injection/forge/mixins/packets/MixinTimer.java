package net.ccbluex.liquidbounce.injection.forge.mixins.packets;

import net.ccbluex.liquidbounce.features.module.modules.combat.TimerRange;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Timer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@SideOnly(Side.CLIENT)
@Mixin(Timer.class)
public class MixinTimer {
    @Shadow
    public float elapsedPartialTicks;
    @Shadow
    public float timerSpeed = 1.0F;
    @Shadow
    private long lastSyncSysClock;
    @Shadow
    private long lastSyncHRClock;

    @Shadow
    public int elapsedTicks;

    @Shadow
    public float renderPartialTicks;

    @Shadow
    private double lastHRTime;

    @Shadow
    float ticksPerSecond;

    @Shadow
    private long counter;

    @Shadow
    private double timeSyncAdjustment = 1.0;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void updateTimer() {
        long lvt_1_1_ = Minecraft.getSystemTime();
        long lvt_3_1_ = lvt_1_1_ - this.lastSyncSysClock;
        long lvt_5_1_ = System.nanoTime() / 1000000L;
        double lvt_7_1_ = (double) lvt_5_1_ / 1000.0;
        if (lvt_3_1_ <= 1000L && lvt_3_1_ >= 0L) {
            this.counter += lvt_3_1_;
            if (this.counter > 1000L) {
                long lvt_9_1_ = lvt_5_1_ - this.lastSyncHRClock;
                double lvt_11_1_ = (double) this.counter / (double) lvt_9_1_;
                this.timeSyncAdjustment += (lvt_11_1_ - this.timeSyncAdjustment) * 0.20000000298023224;
                this.lastSyncHRClock = lvt_5_1_;
                this.counter = 0L;
            }

            if (this.counter < 0L) {
                this.lastSyncHRClock = lvt_5_1_;
            }
        } else {
            this.lastHRTime = lvt_7_1_;
        }

        this.lastSyncSysClock = lvt_1_1_;
        double lvt_9_2_ = (lvt_7_1_ - this.lastHRTime) * this.timeSyncAdjustment;
        this.lastHRTime = lvt_7_1_;
        lvt_9_2_ = MathHelper.clamp_double(lvt_9_2_, 0.0, 1.0);
        this.elapsedPartialTicks = (float) ((double) this.elapsedPartialTicks + lvt_9_2_ * (double) this.timerSpeed * (double) this.ticksPerSecond);
        this.elapsedTicks = (int) this.elapsedPartialTicks;
        this.elapsedPartialTicks -= (float) this.elapsedTicks;
        if (this.elapsedTicks > 10) {
            this.elapsedTicks = 10;
        }
        if (!TimerRange.INSTANCE.getFreezeAnim()) {
            if (!TimerRange.freezeAnimation()) this.renderPartialTicks = this.elapsedPartialTicks;
        } else {
            this.renderPartialTicks = this.elapsedPartialTicks;
        }
    }
}