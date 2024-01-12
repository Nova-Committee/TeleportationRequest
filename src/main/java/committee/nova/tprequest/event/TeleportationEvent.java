package committee.nova.tprequest.event;

import committee.nova.tprequest.request.TeleportRequest;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;

public class TeleportationEvent extends Event {
    private final ServerPlayer sender;
    private final ServerPlayer receiver;
    private final TeleportRequest.TeleportationType tpType;
    private final ResourceKey<Level> formerLevel;
    private final Vec3 formerPos;

    public TeleportationEvent(
            ServerPlayer sender, ServerPlayer receiver,
            TeleportRequest.TeleportationType tpType,
            ResourceKey<Level> formerLevel, Vec3 formerPos
    ) {
        this.sender = sender;
        this.receiver = receiver;
        this.tpType = tpType;
        this.formerLevel = formerLevel;
        this.formerPos = formerPos;
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

    public ResourceKey<Level> getFormerLevel() {
        return formerLevel;
    }

    public Vec3 getFormerPos() {
        return formerPos;
    }
}
