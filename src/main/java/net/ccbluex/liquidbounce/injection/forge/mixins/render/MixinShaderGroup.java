package net.ccbluex.liquidbounce.injection.forge.mixins.render;

import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ShaderGroup.class)
public class MixinShaderGroup {

    @Unique
    void invokeListShaders() {
    }
}