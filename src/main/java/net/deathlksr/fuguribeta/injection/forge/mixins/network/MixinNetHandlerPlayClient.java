package net.deathlksr.fuguribeta.injection.forge.mixins.network;

import io.netty.buffer.Unpooled;
import net.deathlksr.fuguribeta.event.EntityMovementEvent;
import net.deathlksr.fuguribeta.event.EventManager;
import net.deathlksr.fuguribeta.features.module.modules.exploit.AntiExploit;
import net.deathlksr.fuguribeta.features.module.modules.movement.NoRotateSet;
import net.deathlksr.fuguribeta.features.module.modules.player.Blink;
import net.deathlksr.fuguribeta.handler.payload.ClientFixes;
import net.deathlksr.fuguribeta.script.api.global.Chat;
import net.deathlksr.fuguribeta.utils.ClientUtils;
import net.deathlksr.fuguribeta.utils.PacketUtils;
import net.deathlksr.fuguribeta.utils.Rotation;
import net.deathlksr.fuguribeta.utils.RotationUtils;
import net.deathlksr.fuguribeta.utils.extensions.PlayerExtensionKt;
import net.deathlksr.fuguribeta.utils.misc.RandomUtils;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.client.C19PacketResourcePackStatus;
import net.minecraft.network.play.server.*;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;
import java.net.URISyntaxException;

import static net.deathlksr.fuguribeta.utils.MinecraftInstance.mc;
import static net.minecraft.network.play.client.C19PacketResourcePackStatus.Action.ACCEPTED;
import static net.minecraft.network.play.client.C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @Shadow
    public int currentServerMaxPlayers;
    @Shadow
    @Final
    private NetworkManager netManager;
    @Shadow
    private Minecraft gameController;
    @Shadow
    private WorldClient clientWorldController;

    @Redirect(method = "handleExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/S27PacketExplosion;getStrength()F"))
    private float onExplosionVelocity(S27PacketExplosion packetExplosion) {
        if (AntiExploit.INSTANCE.getState() && AntiExploit.INSTANCE.getLimitExplosionStrength()) {
            float strength = packetExplosion.getStrength();
            float fixedStrength = MathHelper.clamp_float(strength, -1000.0f, 1000.0f);

            if (fixedStrength != strength) {
                Chat.print("Limited too strong explosion");
                return fixedStrength;
            }
        }
        return packetExplosion.getStrength();
    }

    @Redirect(method = "handleExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/S27PacketExplosion;func_149149_c()F"))
    private float onExplosionWorld(S27PacketExplosion packetExplosion) {
        if (AntiExploit.INSTANCE.getState() && AntiExploit.INSTANCE.getLimitExplosionRange()) {
            float originalRadius = packetExplosion.func_149149_c();
            float radius = MathHelper.clamp_float(originalRadius, -1000.0f, 1000.0f);

            if (radius != originalRadius) {
                Chat.print("Limited too big TNT explosion radius");
                return radius;
            }
        }
        return packetExplosion.func_149149_c();
    }

    @Redirect(method = "handleParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/S2APacketParticles;getParticleCount()I", ordinal = 1))
    private int onParticleAmount(S2APacketParticles packetParticles) {
        if (AntiExploit.INSTANCE.getState() && AntiExploit.INSTANCE.getLimitParticlesAmount() && packetParticles.getParticleCount() >= 500) {
            Chat.print("Limited too many particles");
            return 100;
        }
        return packetParticles.getParticleCount();
    }

    @Redirect(method = "handleParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/S2APacketParticles;getParticleSpeed()F"))
    private float onParticleSpeed(S2APacketParticles packetParticles) {
        if (AntiExploit.INSTANCE.getState() && AntiExploit.INSTANCE.getLimitParticlesSpeed() && packetParticles.getParticleSpeed() >= 10f) {
            Chat.print("Limited too fast particles speed");
            return 5f;
        }
        return packetParticles.getParticleSpeed();
    }

    @Redirect(method = "handleSpawnObject", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/S0EPacketSpawnObject;getType()I"))
    private int onSpawnObjectType(S0EPacketSpawnObject packet) {
        if (AntiExploit.INSTANCE.getState() && AntiExploit.INSTANCE.getLimitedArrowsSpawned() && packet.getType() == 60) {
            int arrows = AntiExploit.INSTANCE.getArrowMax();

            if (++arrows >= AntiExploit.INSTANCE.getMaxArrowsSpawned()) {
                return -1; // Cancel arrows spawn
            }
        }
        return packet.getType();
    }

    @Redirect(method = "handleChangeGameState", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/play/server/S2BPacketChangeGameState;getGameState()I"))
    private int onChangeGameState(S2BPacketChangeGameState packet) {
        if (AntiExploit.INSTANCE.getState() && AntiExploit.INSTANCE.getCancelDemo() && mc.isDemo()) {
            return -1; // Cancel demo
        }

        return packet.getGameState();
    }

    @Inject(method = "handleResourcePack", at = @At("HEAD"), cancellable = true)
    private void handleResourcePack(final S48PacketResourcePackSend p_handleResourcePack_1_, final CallbackInfo callbackInfo) {
        final String url = p_handleResourcePack_1_.getURL();
        final String hash = p_handleResourcePack_1_.getHash();

        if (ClientFixes.INSTANCE.getBlockResourcePackExploit()) {
            try {
                final String scheme = new URI(url).getScheme();
                final boolean isLevelProtocol = "level".equals(scheme);

                if (!"http".equals(scheme) && !"https".equals(scheme) && !isLevelProtocol)
                    throw new URISyntaxException(url, "Wrong protocol");

                if (isLevelProtocol && (url.contains("..") || !url.endsWith("/resources.zip")))
                    throw new URISyntaxException(url, "Invalid levelstorage resourcepack path");
            } catch (final URISyntaxException e) {
                ClientUtils.INSTANCE.getLOGGER().error("Failed to handle resource pack", e);

                // Accepted is always sent.
                netManager.sendPacket(new C19PacketResourcePackStatus(hash, ACCEPTED));
                // But we fail of course.
                netManager.sendPacket(new C19PacketResourcePackStatus(hash, FAILED_DOWNLOAD));

                callbackInfo.cancel();
            }
        }
    }

    @Inject(method = "handleJoinGame", at = @At("HEAD"), cancellable = true)
    private void handleJoinGameWithAntiForge(S01PacketJoinGame packetIn, final CallbackInfo callbackInfo) {
        if (!ClientFixes.INSTANCE.getFmlFixesEnabled() || !ClientFixes.INSTANCE.getBlockFML() || mc.isIntegratedServerRunning())
            return;

        PacketThreadUtil.checkThreadAndEnqueue(packetIn, (NetHandlerPlayClient) (Object) this, gameController);
        gameController.playerController = new PlayerControllerMP(gameController, (NetHandlerPlayClient) (Object) this);
        clientWorldController = new WorldClient((NetHandlerPlayClient) (Object) this, new WorldSettings(0L, packetIn.getGameType(), false, packetIn.isHardcoreMode(), packetIn.getWorldType()), packetIn.getDimension(), packetIn.getDifficulty(), gameController.mcProfiler);
        gameController.gameSettings.difficulty = packetIn.getDifficulty();
        gameController.loadWorld(clientWorldController);
        gameController.thePlayer.dimension = packetIn.getDimension();
        gameController.displayGuiScreen(new GuiDownloadTerrain((NetHandlerPlayClient) (Object) this));
        gameController.thePlayer.setEntityId(packetIn.getEntityId());
        currentServerMaxPlayers = packetIn.getMaxPlayers();
        gameController.thePlayer.setReducedDebug(packetIn.isReducedDebugInfo());
        gameController.playerController.setGameType(packetIn.getGameType());
        gameController.gameSettings.sendSettingsToServer();
        netManager.sendPacket(new C17PacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(ClientBrandRetriever.getClientModName())));
        callbackInfo.cancel();
    }

    @Inject(method = "handleEntityMovement", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;onGround:Z"))
    private void handleEntityMovementEvent(S14PacketEntity packetIn, final CallbackInfo callbackInfo) {
        final Entity entity = packetIn.getEntity(clientWorldController);

        if (entity != null)
            EventManager.INSTANCE.callEvent(new EntityMovementEvent(entity));
    }

    @Inject(method = "handlePlayerPosLook", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;setPositionAndRotation(DDDFF)V", shift = At.Shift.BEFORE))
    private void injectNoRotateSetPositionOnly(S08PacketPlayerPosLook p_handlePlayerPosLook_1_, CallbackInfo ci) {
        NoRotateSet module = NoRotateSet.INSTANCE;

        // Save the server's requested rotation before it resets the rotations
        module.setSavedRotation(PlayerExtensionKt.getRotation(Minecraft.getMinecraft().thePlayer));
    }

    @Redirect(method = "handlePlayerPosLook", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkManager;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private void injectNoRotateSetAndAntiServerRotationOverride(NetworkManager instance, Packet p_sendPacket_1_) {
        Blink module2 = Blink.INSTANCE;
        boolean shouldTrigger = module2.blinkingSend();
        PacketUtils.sendPacket(p_sendPacket_1_, shouldTrigger);

        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        NoRotateSet module = NoRotateSet.INSTANCE;

        if (player == null || !module.shouldModify(player)) {
            return;
        }

        int sign = RandomUtils.INSTANCE.nextBoolean() ? 1 : -1;

        Rotation rotation = player.ticksExisted == 0 ? RotationUtils.INSTANCE.getServerRotation() : module.getSavedRotation();

        if (module.getAffectRotation()) {
            NoRotateSet.INSTANCE.rotateBackToPlayerRotation();
        }

        // Slightly modify the client-side rotations, so they pass the rotation difference check in onUpdateWalkingPlayer, EntityPlayerSP.
        player.rotationYaw = (rotation.getYaw() + 0.000001f * sign) % 360.0F;
        player.rotationPitch = (rotation.getPitch() + 0.000001f * sign) % 360.0F;
        RotationUtils.INSTANCE.syncRotations();
    }
}
