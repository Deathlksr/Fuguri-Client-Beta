/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.injection.forge.mixins.render;

import net.deathlksr.fuguribeta.features.module.modules.client.RotationHandler;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.deathlksr.fuguribeta.utils.MinecraftInstance.mc;

@Mixin(ModelBiped.class)
@SideOnly(Side.CLIENT)
public class MixinModelBiped {

    @Shadow
    public ModelRenderer bipedRightArm;

    @Shadow
    public int heldItemRight;

    @Shadow
    public ModelRenderer bipedHead;

    @Inject(method = "setRotationAngles", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelBiped;swingProgress:F"))
    private void revertSwordAnimation(float p_setRotationAngles_1_, float p_setRotationAngles_2_, float p_setRotationAngles_3_, float p_setRotationAngles_4_, float p_setRotationAngles_5_, float p_setRotationAngles_6_, Entity p_setRotationAngles_7_, CallbackInfo callbackInfo) {
        if (heldItemRight == 3) bipedRightArm.rotateAngleY = 0F;

        if (RotationHandler.INSTANCE.shouldRotate() && p_setRotationAngles_7_ instanceof EntityPlayer && p_setRotationAngles_7_.equals(mc.thePlayer)) {
            bipedHead.rotateAngleX = (float) Math.toRadians(RotationHandler.INSTANCE.lerp(mc.timer.renderPartialTicks, RotationHandler.INSTANCE.getPrevHeadPitch(), RotationHandler.INSTANCE.getHeadPitch()));
        }
    }
}