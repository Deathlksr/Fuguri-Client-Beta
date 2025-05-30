/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.injection.forge.mixins.entity;

import net.deathlksr.fuguribeta.event.AttackEvent;
import net.deathlksr.fuguribeta.event.ClickWindowEvent;
import net.deathlksr.fuguribeta.event.EventManager;
import net.deathlksr.fuguribeta.features.module.modules.exploit.AbortBreaking;
import net.deathlksr.fuguribeta.utils.CooldownHelper;
import net.deathlksr.fuguribeta.utils.inventory.InventoryUtils;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
@SideOnly(Side.CLIENT)
public class MixinPlayerControllerMP {

    @Inject(method = "attackEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;syncCurrentPlayItem()V"))
    private void attackEntity(EntityPlayer entityPlayer, Entity targetEntity, CallbackInfo callbackInfo) {
        EventManager.INSTANCE.callEvent(new AttackEvent(targetEntity));
        CooldownHelper.INSTANCE.resetLastAttackedTicks();
    }

    @Inject(method = "getIsHittingBlock", at = @At("HEAD"), cancellable = true)
    private void getIsHittingBlock(CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (AbortBreaking.INSTANCE.handleEvents())
            callbackInfoReturnable.setReturnValue(false);
    }

    @Inject(method = "windowClick", at = @At("HEAD"), cancellable = true)
    private void windowClick(int windowId, int slotId, int mouseButtonClicked, int mode, EntityPlayer playerIn, CallbackInfoReturnable<ItemStack> callbackInfo) {
        final ClickWindowEvent event = new ClickWindowEvent(windowId, slotId, mouseButtonClicked, mode);
        EventManager.INSTANCE.callEvent(event);

        if (event.isCancelled()) {
            callbackInfo.cancel();
            return;
        }

        // Only reset click delay, if a click didn't get cancelled
        InventoryUtils.INSTANCE.getCLICK_TIMER().reset();
    }
}