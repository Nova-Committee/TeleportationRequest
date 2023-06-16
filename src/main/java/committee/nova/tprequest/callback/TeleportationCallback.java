package committee.nova.tprequest.callback;

import committee.nova.tprequest.request.TeleportRequest;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public interface TeleportationCallback {
    Event<TeleportationCallback> EVENT = EventFactory.createArrayBacked(TeleportationCallback.class, listeners -> ((sender, receiver, tpType, formerWorld, formerPos) -> {
        for (final var l : listeners) l.postTeleport(sender, receiver, tpType, formerWorld, formerPos);
    }));

    void postTeleport(
            ServerPlayerEntity sender, ServerPlayerEntity receiver, TeleportRequest.TeleportationType tpType,
            RegistryKey<World> formerWorld, Vec3d formerPos
    );

}
