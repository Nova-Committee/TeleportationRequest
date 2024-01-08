package committee.nova.tprequest.event;

import committee.nova.tprequest.request.TeleportRequest;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public class TeleportationEvent extends Event {
    private final ServerPlayer sender;
    private final ServerPlayer receiver;
    private final TeleportRequest.TeleportationType tpType;

    public TeleportationEvent(ServerPlayer sender, ServerPlayer receiver, TeleportRequest.TeleportationType tpType) {
        this.sender = sender;
        this.receiver = receiver;
        this.tpType = tpType;
    }

    public ServerPlayer getSender() {
        return sender;
    }

    public ServerPlayer getReceiver() {
        return receiver;
    }

    public TeleportRequest.TeleportationType getTpType() {
        return tpType;
    }
}
