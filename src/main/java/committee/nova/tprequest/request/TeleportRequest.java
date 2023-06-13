package committee.nova.tprequest.request;

import committee.nova.tprequest.TeleportationRequest;
import committee.nova.tprequest.event.TeleportationEvent;
import committee.nova.tprequest.util.Utilities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

import java.util.UUID;

public interface TeleportRequest {
    UUID getId();

    boolean tick();

    boolean execute(MinecraftServer server);

    void setIgnored(boolean ignored);

    boolean isIgnored();

    int getExpiration();

    UUID getSender();

    UUID getReceiver();

    TeleportationType getType();

    MutableComponent getSummary(MinecraftServer server);

    default MutableComponent getCmdSuggestion(MinecraftServer server) {
        return getSummary(server).append(",").append(new TextComponent(getId().toString()));
    }

    default boolean isRelevantTo(ServerPlayer player) {
        final UUID uuid = player.getUUID();
        return uuid.equals(getSender()) || uuid.equals(getReceiver());
    }

    class To implements TeleportRequest {
        private final UUID sender;
        private final UUID receiver;
        private final UUID uuid = UUID.randomUUID();
        private int timeout = TeleportationRequest.getExpirationTime();
        private boolean ignored = false;

        public To(UUID sender, UUID receiver) {
            this.sender = sender;
            this.receiver = receiver;
        }

        @Override
        public UUID getId() {
            return uuid;
        }

        @Override
        public boolean tick() {
            timeout--;
            return timeout <= 0;
        }

        @Override
        public boolean execute(MinecraftServer server) {
            final ServerPlayer oS = server.getPlayerList().getPlayer(sender);
            final ServerPlayer oR = server.getPlayerList().getPlayer(receiver);
            if (oS == null || oR == null) return false;
            oS.teleportTo(oR.getLevel(), oR.getX(), oR.getY(), oR.getZ(), oR.getYRot(), oR.getXRot());
            MinecraftForge.EVENT_BUS.post(new TeleportationEvent(oS, oR, getType()));
            return true;
        }

        @Override
        public void setIgnored(boolean ignored) {
            this.ignored = ignored;
        }

        @Override
        public boolean isIgnored() {
            return ignored;
        }

        @Override
        public int getExpiration() {
            return timeout;
        }

        @Override
        public UUID getSender() {
            return sender;
        }

        @Override
        public UUID getReceiver() {
            return receiver;
        }

        @Override
        public TeleportationType getType() {
            return TeleportationType.TO;
        }

        @Override
        public MutableComponent getSummary(MinecraftServer server) {
            return new TranslatableComponent("format.tprequest.summary.to",
                    Utilities.getPlayerName(server, sender), Utilities.getPlayerName(server, receiver)).withStyle(ChatFormatting.WHITE);
        }
    }

    class Here implements TeleportRequest {
        private final UUID sender;
        private final UUID receiver;
        private final UUID uuid = UUID.randomUUID();
        private int timeout = TeleportationRequest.getExpirationTime();
        private boolean ignored = false;

        public Here(UUID sender, UUID receiver) {
            this.sender = sender;
            this.receiver = receiver;
        }

        @Override
        public UUID getId() {
            return uuid;
        }

        @Override
        public boolean tick() {
            timeout--;
            return timeout <= 0;
        }

        @Override
        public boolean execute(MinecraftServer server) {
            final ServerPlayer oS = server.getPlayerList().getPlayer(sender);
            final ServerPlayer oR = server.getPlayerList().getPlayer(receiver);
            if (oS == null || oR == null) return false;
            oR.teleportTo(oS.getLevel(), oS.getX(), oS.getY(), oS.getZ(), oS.getYRot(), oS.getXRot());
            MinecraftForge.EVENT_BUS.post(new TeleportationEvent(oS, oR, getType()));
            return true;
        }

        @Override
        public void setIgnored(boolean ignored) {
            this.ignored = ignored;
        }

        @Override
        public boolean isIgnored() {
            return ignored;
        }

        @Override
        public int getExpiration() {
            return timeout;
        }

        @Override
        public UUID getSender() {
            return sender;
        }

        @Override
        public UUID getReceiver() {
            return receiver;
        }

        @Override
        public TeleportationType getType() {
            return TeleportationType.HERE;
        }

        @Override
        public MutableComponent getSummary(MinecraftServer server) {
            return new TranslatableComponent("format.tprequest.summary.here",
                    Utilities.getPlayerName(server, sender), Utilities.getPlayerName(server, receiver));
        }
    }

    enum TeleportationType {
        TO,
        HERE;
    }
}
