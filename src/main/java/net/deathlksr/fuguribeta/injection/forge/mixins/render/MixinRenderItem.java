/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.injection.forge.mixins.render;

import net.deathlksr.fuguribeta.features.module.modules.visual.CustomGlint;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.IBakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {

    @Shadow
    protected abstract void renderModel(IBakedModel model, int color);

    @Redirect(method = "renderEffect", at = @At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/RenderItem;renderModel(Lnet/minecraft/client/resources/model/IBakedModel;I)V"))
    private void renderModel(RenderItem renderItem, IBakedModel model, int color) {
        final CustomGlint glint = CustomGlint.INSTANCE;

        this.renderModel(model, glint.getState() ? glint.getColor().getRGB() : -8372020);
    }
}

