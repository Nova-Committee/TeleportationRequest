package committee.nova.tprequest;

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

public class TeleportationRequest implements ModInitializer {
    private static Config cfg;
    private static int tpCd;
    private static int expirationTime;

    @Override
    public void onInitialize() {
        syncCfg();
        ArgumentTypes.register("teleport_request", TeleportRequestArgument.class, new ConstantArgumentSerializer<>(TeleportRequestArgument::instance));
        ServerTickEvents.END_SERVER_TICK.register(ServerStorage::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> ServerStorage.requests.clear());
        CommandRegistrationCallback.EVENT.register(CommandInit::init);
    }

    public static void syncCfg() {
        if (cfg == null) cfg = Config.of("TeleportationRequest-Config").provider(path -> """
                # TeleportationRequest-Config
                # Cool-down time (tick) after a successful teleportation request
                tpCd=600
                # Expiration time (tick) of a teleportation request
                expirationTime=1200
                """
        ).request();
        tpCd = cfg.getOrDefault("tpCd", 600);
        expirationTime = cfg.getOrDefault("expirationTime", 1200);
    }

    public static int getTpCd() {
        return tpCd;
    }

    public static int getExpirationTime() {
        return expirationTime;
    }
}
