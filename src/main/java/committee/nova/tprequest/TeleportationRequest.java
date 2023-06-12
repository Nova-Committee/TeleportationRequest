package committee.nova.tprequest;

import committee.nova.tprequest.callback.TeleportationCallback;
import committee.nova.tprequest.cfg.Config;
import committee.nova.tprequest.command.argument.TeleportRequestArgument;
import committee.nova.tprequest.command.init.CommandInit;
import committee.nova.tprequest.storage.ServerStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

public class TeleportationRequest implements ModInitializer {
    private static Config cfg;
    private static int tpCd;
    private static int expirationTime;
    private static boolean shortAlternatives;
    private static String notificationSound;

    @Override
    public void onInitialize() {
        syncCfg();
        ArgumentTypes.register("teleport_request", TeleportRequestArgument.class, new ConstantArgumentSerializer<>(TeleportRequestArgument::instance));
        ServerTickEvents.END_SERVER_TICK.register(ServerStorage::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> ServerStorage.requests.clear());
        CommandRegistrationCallback.EVENT.register(CommandInit::init);
        TeleportationCallback.EVENT.register(((sender, receiver, tpType) -> TeleportationRequest.getNotificationSound()
                .ifPresent(r -> sender.playSound(r, SoundCategory.PLAYERS, 1.0F, 1.0F))));
    }

    public static void syncCfg() {
        if (cfg == null) cfg = Config.of("TeleportationRequest-Config").provider(path -> """
                # TeleportationRequest-Config
                # Cool-down time (tick) after a successful teleportation request
                tpCd=600
                # Expiration time (tick) of a teleportation request
                expirationTime=1200
                # Set to true to register short alternatives of teleportation request commands, e.g. /trtpa -> /tpa
                shortAlternatives=true
                # Notification sound to be played after a teleportation. Leave a blank to disable.
                notificationSound=minecraft:entity.enderman.teleport
                """
        ).request();
        tpCd = cfg.getOrDefault("tpCd", 600);
        expirationTime = cfg.getOrDefault("expirationTime", 1200);
        shortAlternatives = cfg.getOrDefault("shortAlternatives", true);
        notificationSound = cfg.getOrDefault("notificationSound", "minecraft:entity.enderman.teleport");
    }

    public static int getTpCd() {
        return tpCd;
    }

    public static int getExpirationTime() {
        return expirationTime;
    }

    public static boolean shouldRegisterShortAlternatives() {
        return shortAlternatives;
    }

    public static Optional<SoundEvent> getNotificationSound() {
        try {
            return Optional.ofNullable(Registry.SOUND_EVENT.get(new Identifier(notificationSound)));
        } catch (InvalidIdentifierException ignored) {
            return Optional.empty();
        }
    }

    public static boolean reload() {
        final boolean r = cfg.reload();
        syncCfg();
        return r;
    }
}
