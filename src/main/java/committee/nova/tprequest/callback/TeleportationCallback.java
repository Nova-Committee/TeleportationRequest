package committee.nova.tprequest.callback;

import committee.nova.tprequest.request.TeleportRequest;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface TeleportationCallback {
    Event<TeleportationCallback> EVENT = EventFactory.createArrayBacked(TeleportationCallback.class, listeners -> ((sender, receiver, tpType) -> {
        for (final var l : listeners) l.postTeleport(sender, receiver, tpType);
    }));

    void postTeleport(ServerPlayerEntity sender, ServerPlayerEntity receiver, TeleportRequest.TeleportationType tpType);
}
