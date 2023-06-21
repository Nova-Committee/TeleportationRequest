package committee.nova.tprequest;

import committee.nova.tprequest.api.ITeleportable;
import committee.nova.tprequest.callback.TeleportationCallback;
import committee.nova.tprequest.cfg.TprConfig;
import committee.nova.tprequest.command.init.CommandInit;
import committee.nova.tprequest.storage.ServerStorage;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.YamlConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.registry.Registry;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TeleportationRequest implements ModInitializer {
    public static final String MODID = "tprequest";
    private static TprConfig cfg;

    @Override
    public void onInitialize() {
        AutoConfig.register(TprConfig.class, YamlConfigSerializer::new);
        cfg = AutoConfig.getConfigHolder(TprConfig.class).getConfig();
        ServerTickEvents.END_SERVER_TICK.register(ServerStorage::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> ServerStorage.requests.clear());
        CommandRegistrationCallback.EVENT.register(CommandInit::init);
        TeleportationCallback.EVENT.register(((sender, receiver, tpType, formerWorld, formerPos) -> TeleportationRequest.getNotificationSound()
                .ifPresent(r -> sender.playSound(r, SoundCategory.PLAYERS, 1.0F, 1.0F))));
    }

    public static int getTpCd() {
        return getActualTick(cfg.tpCd);
    }

    public static int getExpirationTime() {
        return getActualTick(cfg.expirationTime);
    }

    private static int getActualTick(double t) {
        return (int) (t * 20.0);
    }

    public static List<String> getAlternativesFor(String cmd) {
        return switch (cmd) {
            case "trtpa" -> cfg.saTpa;
            case "trtpahere" -> cfg.saTpahere;
            case "trtpaccept" -> cfg.saTpaccept;
            case "trtpcancel" -> cfg.saTpcancel;
            case "trtpdeny" -> cfg.saTpdeny;
            case "trtpignore" -> cfg.saTpignore;
            case "trtplist" -> cfg.saTplist;
            default -> Collections.emptyList();
        };
    }

    public static Optional<SoundEvent> getNotificationSound() {
        try {
            return Optional.ofNullable(Registry.SOUND_EVENT.get(new Identifier(cfg.notificationSound)));
        } catch (InvalidIdentifierException ignored) {
            return Optional.empty();
        }
    }

    public static boolean reload(MinecraftServer server) {
        final var reloaded = AutoConfig.getConfigHolder(TprConfig.class).load();
        cfg = AutoConfig.getConfigHolder(TprConfig.class).getConfig();
        postReload(server);
        return reloaded;
    }

    private static void postReload(MinecraftServer server) {
        final int newExpiration = getExpirationTime();
        ServerStorage.requests.forEach(r -> {
            if (newExpiration < r.getExpirationTime()) r.setExpirationTime(newExpiration);
        });
        final int newCd = getTpCd();
        server.getPlayerManager().getPlayerList().forEach(p -> {
            final ITeleportable t = (ITeleportable) p;
            if (t.getTeleportCd() > newCd) t.setTeleportCd(newCd);
        });
    }
}
