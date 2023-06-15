package committee.nova.tprequest;

import committee.nova.tprequest.callback.TeleportationCallback;
import committee.nova.tprequest.cfg.TprConfig;
import committee.nova.tprequest.command.argument.TeleportRequestArgument;
import committee.nova.tprequest.command.init.CommandInit;
import committee.nova.tprequest.storage.ServerStorage;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.YamlConfigSerializer;
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
        ArgumentTypes.register("teleport_request", TeleportRequestArgument.class, new ConstantArgumentSerializer<>(TeleportRequestArgument::instance));
        ServerTickEvents.END_SERVER_TICK.register(ServerStorage::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> ServerStorage.requests.clear());
        CommandRegistrationCallback.EVENT.register(CommandInit::init);
        TeleportationCallback.EVENT.register(((sender, receiver, tpType) -> TeleportationRequest.getNotificationSound()
                .ifPresent(r -> sender.playSound(r, SoundCategory.PLAYERS, 1.0F, 1.0F))));
    }

    public static int getTpCd() {
        return cfg.tpCd;
    }

    public static int getExpirationTime() {
        return cfg.expirationTime;
    }

    public static List<String> getAlternativesFor(String cmd) {
        return switch (cmd) {
            case "trtpa" -> cfg.saTpa;
            case "trtpahere" -> cfg.saTpahere;
            case "trtpaccept" -> cfg.saTpaccept;
            case "trtpcancel" -> cfg.saTpcancel;
            case "trtpdeny" -> cfg.saTpdeny;
            case "trtpignore" -> cfg.saTpignore;
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
}
