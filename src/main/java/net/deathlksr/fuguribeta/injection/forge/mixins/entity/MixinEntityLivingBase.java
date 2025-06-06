/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.injection.forge.mixins.entity;

import net.deathlksr.fuguribeta.event.EventManager;
import net.deathlksr.fuguribeta.event.EventState;
import net.deathlksr.fuguribeta.event.JumpEvent;
import net.deathlksr.fuguribeta.features.module.modules.movement.AirJump;
import net.deathlksr.fuguribeta.features.module.modules.movement.Jesus;
import net.deathlksr.fuguribeta.features.module.modules.movement.NoJumpDelay;
import net.deathlksr.fuguribeta.features.module.modules.movement.Sprint;
import net.deathlksr.fuguribeta.features.module.modules.visual.Animations;
import net.deathlksr.fuguribeta.features.module.modules.client.RotationHandler;
import net.deathlksr.fuguribeta.features.module.modules.player.scaffolds.*;
import net.deathlksr.fuguribeta.utils.MovementUtils;
import net.deathlksr.fuguribeta.utils.Rotation;
import net.deathlksr.fuguribeta.utils.RotationUtils;
import net.deathlksr.fuguribeta.utils.extensions.MathExtensionsKt;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity {

    @Shadow
    public float rotationYawHead;
    @Shadow
    public boolean isJumping;
    @Shadow
    public int jumpTicks;

    @Shadow
    protected abstract float getJumpUpwardsMotion();

    @Shadow
    public abstract PotionEffect getActivePotionEffect(Potion potionIn);

    @Shadow
    public abstract boolean isPotionActive(Potion potionIn);

    @Shadow
    public void onLivingUpdate() {
    }

    @Shadow
    protected abstract void updateFallState(double y, boolean onGroundIn, Block blockIn, BlockPos pos);

    @Shadow
    public abstract float getHealth();

    @Shadow
    public abstract ItemStack getHeldItem();

    @Shadow
    protected abstract void updateAITick();

    /**
     * @author CCBlueX
     */
    @Overwrite
    protected void jump() {
        final JumpEvent prejumpEvent = new JumpEvent(getJumpUpwardsMotion(), EventState.PRE, 0.2f);
        EventManager.INSTANCE.callEvent(prejumpEvent);
        if (prejumpEvent.isCancelled()) return;

        motionY = prejumpEvent.getMotion();

        if (isPotionActive(Potion.jump))
            motionY += (float) (getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;

        if (isSprinting()) {
            float fixedYaw = this.rotationYaw;

            final RotationUtils rotationUtils = RotationUtils.INSTANCE;
            final Rotation currentRotation = rotationUtils.getCurrentRotation();
            final RotationUtils.RotationData rotationData = rotationUtils.getRotationData();
            if (currentRotation != null && rotationData != null && rotationData.getStrafe()) {
                fixedYaw = currentRotation.getYaw();
            }

            final Sprint sprint = Sprint.INSTANCE;
            if (sprint.handleEvents() && sprint.getMode().equals("Vanilla") && sprint.getAllDirections() && sprint.getJumpDirections()) {
                fixedYaw += MathExtensionsKt.toDegreesF(MovementUtils.INSTANCE.getDirection()) - this.rotationYaw;
            }

            final float f = fixedYaw * 0.017453292F;
            motionX -= MathHelper.sin(f) * 0.2F;
            motionZ += MathHelper.cos(f) * 0.2F;
        }

        isAirBorne = true;

        final JumpEvent postjumpEvent = new JumpEvent((float) motionY, EventState.POST, 0.2f);
        EventManager.INSTANCE.callEvent(postjumpEvent);
    }

    @Inject(method = "onLivingUpdate", at = @At("HEAD"))
    private void headLiving(CallbackInfo callbackInfo) {
        if (NoJumpDelay.INSTANCE.handleEvents() || Scaffold.INSTANCE.handleEvents() && Tower.INSTANCE.getTowerModeValues().equals("Pulldown")) jumpTicks = 0;
    }

    @Inject(method = "onLivingUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;isJumping:Z", ordinal = 1))
    private void onJumpSection(CallbackInfo callbackInfo) {
        if (AirJump.INSTANCE.handleEvents() && isJumping && jumpTicks == 0) {
            jump();
            jumpTicks = 10;
        }

        final Jesus liquidWalk = Jesus.INSTANCE;

        if (liquidWalk.handleEvents() && !isJumping && !isSneaking() && isInWater() && liquidWalk.getMode().equals("Swim")) {
            updateAITick();
        }
    }

    @Inject(method = "getLook", at = @At("HEAD"), cancellable = true)
    private void getLook(CallbackInfoReturnable<Vec3> callbackInfoReturnable) {
        //noinspection ConstantConditions
        if (((EntityLivingBase) (Object) this) instanceof EntityPlayerSP)
            callbackInfoReturnable.setReturnValue(getVectorForRotation(rotationPitch, rotationYaw));
    }

    /**
     * Inject head yaw rotation modification
     */
    @Inject(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;updateEntityActionState()V", shift = At.Shift.AFTER))
    private void hookHeadRotations(CallbackInfo ci) {
        Rotation rotation = RotationHandler.INSTANCE.getRotation(false);

        //noinspection ConstantValue
        this.rotationYawHead = ((EntityLivingBase) (Object) this) instanceof EntityPlayerSP && RotationHandler.INSTANCE.shouldUseRealisticMode() && rotation != null ? rotation.getYaw() : this.rotationYawHead;
    }

    /**
     * Inject body rotation modification
     */
    @Redirect(method = "onUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;rotationYaw:F", ordinal = 0))
    private float hookBodyRotationsA(EntityLivingBase instance) {
        Rotation rotation = RotationHandler.INSTANCE.getRotation(false);

        return instance instanceof EntityPlayerSP && RotationHandler.INSTANCE.shouldUseRealisticMode() && rotation != null ? rotation.getYaw() : instance.rotationYaw;
    }


    /**
     * Inject body rotation modification
     */
    @Redirect(method = "updateDistance", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;rotationYaw:F"))
    private float hookBodyRotationsB(EntityLivingBase instance) {
        Rotation rotation = RotationHandler.INSTANCE.getRotation(false);

        return instance instanceof EntityPlayerSP && RotationHandler.INSTANCE.shouldUseRealisticMode() && rotation != null ? rotation.getYaw() : instance.rotationYaw;
    }

    /**
     * @author SuperSkidder
     * @reason Animations swing speed
     */
    @ModifyConstant(method = "getArmSwingAnimationEnd", constant = @Constant(intValue = 6))
    private int injectAnimationsModule(int constant) {
        Animations module = Animations.INSTANCE;

        return module.handleEvents() ? (2 + (20 - module.getSwingSpeed())) : constant;
    }
}
