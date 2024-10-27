/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura;
import net.ccbluex.liquidbounce.features.module.modules.movement.NoSlow;
import net.ccbluex.liquidbounce.features.module.modules.visual.PlayerEdit;
import net.ccbluex.liquidbounce.utils.APIConnecter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.ccbluex.liquidbounce.FuguriBeta;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer {


    @Shadow
    public abstract ModelPlayer getMainModel();

    /**
     * @author CCBlueX
     */
    @Overwrite
    private void setModelVisibilities(AbstractClientPlayer p_setModelVisibilities_1_) {
        ModelPlayer modelplayer = this.getMainModel();
        if (p_setModelVisibilities_1_.isSpectator()) {
            modelplayer.setInvisible(false);
            modelplayer.bipedHead.showModel = true;
            modelplayer.bipedHeadwear.showModel = true;
        } else {
            ItemStack itemstack = p_setModelVisibilities_1_.inventory.getCurrentItem();
            modelplayer.setInvisible(true);
            modelplayer.bipedHeadwear.showModel = p_setModelVisibilities_1_.isWearing(EnumPlayerModelParts.HAT);
            modelplayer.bipedBodyWear.showModel = p_setModelVisibilities_1_.isWearing(EnumPlayerModelParts.JACKET);
            modelplayer.bipedLeftLegwear.showModel = p_setModelVisibilities_1_.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
            modelplayer.bipedRightLegwear.showModel = p_setModelVisibilities_1_.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
            modelplayer.bipedLeftArmwear.showModel = p_setModelVisibilities_1_.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
            modelplayer.bipedRightArmwear.showModel = p_setModelVisibilities_1_.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
            modelplayer.heldItemLeft = 0;
            modelplayer.aimedBow = false;
            modelplayer.isSneak = p_setModelVisibilities_1_.isSneaking();
            if (itemstack == null) {
                modelplayer.heldItemRight = 0;
            } else {
                modelplayer.heldItemRight = 1;
                boolean isForceBlocking = p_setModelVisibilities_1_ instanceof EntityPlayerSP && ((itemstack.getItem() instanceof ItemSword && KillAura.INSTANCE.getRenderBlocking()) || NoSlow.INSTANCE.isUNCPBlocking());
                if (p_setModelVisibilities_1_.getItemInUseCount() > 0 || isForceBlocking) {
                    EnumAction enumaction = isForceBlocking? EnumAction.BLOCK : itemstack.getItemUseAction();
                    if (enumaction == EnumAction.BLOCK) {
                        modelplayer.heldItemRight = 3;
                    } else if (enumaction == EnumAction.BOW) {
                        modelplayer.aimedBow = true;
                    }
                }
            }
        }
    }

    @Inject(method = "renderLivingAt", at = @At("HEAD"))
    protected void renderLivingAt(AbstractClientPlayer entityLivingBaseIn, double x, double y, double z, CallbackInfo callbackInfo) {
        if(FuguriBeta.INSTANCE.getModuleManager().get(PlayerEdit.class).getState() & entityLivingBaseIn.equals(Minecraft.getMinecraft().thePlayer) && PlayerEdit.INSTANCE.getEditPlayerSizeValue().get()) {
            GlStateManager.scale(PlayerEdit.INSTANCE.getPlayerSizeValue().get(), PlayerEdit.INSTANCE.getPlayerSizeValue().get(), PlayerEdit.INSTANCE.getPlayerSizeValue().get());
        }
    }

    @Inject(method = {"getEntityTexture"}, at = {@At("HEAD")}, cancellable = true)
    public void getEntityTexture(AbstractClientPlayer entity, CallbackInfoReturnable<ResourceLocation> ci) {
        final PlayerEdit playerEdit = PlayerEdit.INSTANCE;
        final ResourceLocation rabbit = APIConnecter.INSTANCE.callImage("rabbit", "models");
        final ResourceLocation fred = APIConnecter.INSTANCE.callImage("freddy", "models");
        final ResourceLocation imposter = APIConnecter.INSTANCE.callImage("imposter", "models");

        if (playerEdit.getState()) {
            if (playerEdit.getMode().contains("Rabbit")) {
                ci.setReturnValue(rabbit);
            }
            if (playerEdit.getMode().contains("Freddy")) {
                ci.setReturnValue(fred);
            }
            if (playerEdit.getMode().contains("Imposter")) {
                ci.setReturnValue(imposter);
            }
        }
    }
}
