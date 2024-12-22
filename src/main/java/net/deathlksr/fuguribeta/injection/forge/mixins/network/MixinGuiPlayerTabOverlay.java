package net.deathlksr.fuguribeta.injection.forge.mixins.network;

import net.deathlksr.fuguribeta.FuguriBeta;
import net.deathlksr.fuguribeta.features.module.modules.client.IRCModule;
import net.deathlksr.fuguribeta.features.module.modules.player.MidClick;
import net.deathlksr.fuguribeta.features.module.modules.visual.NameTags;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiPlayerTabOverlay.class)
public class MixinGuiPlayerTabOverlay {

    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    private void getPlayerName(NetworkPlayerInfo networkPlayerInfoIn, CallbackInfoReturnable<String> cir) {
        String prefixFriend = !MidClick.INSTANCE.getReverse() ? FuguriBeta.INSTANCE.getFileManager().getFriendsConfig().isFriend(networkPlayerInfoIn.getGameProfile().getName()) ? "§2[Friend]§9 " : "" : !FuguriBeta.INSTANCE.getFileManager().getFriendsConfig().isFriend(networkPlayerInfoIn.getGameProfile().getName()) ? "§2[Friend]§9 " : "";
        String prefixOwner = NameTags.INSTANCE.getOwnerList().contains(networkPlayerInfoIn.getGameProfile().getName().toLowerCase()) ? "§4[Fuguri Owner]§4 " : "";
        String prefixUser = NameTags.INSTANCE.getUserList().contains(networkPlayerInfoIn.getGameProfile().getName().toLowerCase()) ? "§5[Fuguri User]§5 " : "";
        String prefixBonan = NameTags.INSTANCE.getBonanList().contains(networkPlayerInfoIn.getGameProfile().getName().toLowerCase()) ? "§6[Bonan Entwickler]§6 " : "";

        String playerName = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());

        if (!NameTags.INSTANCE.getState()) {
            cir.setReturnValue(playerName);
        } else {
            cir.setReturnValue(prefixOwner + prefixUser + prefixBonan + prefixFriend + playerName);
        }
    }
}