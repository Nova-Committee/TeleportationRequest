package committee.nova.tprequest.request;

import committee.nova.tprequest.TeleportationRequest;
import committee.nova.tprequest.callback.TeleportationCallback;
import committee.nova.tprequest.util.Utilities;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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

    MutableText getSummary(MinecraftServer server);

    default MutableText getCmdSuggestion(MinecraftServer server) {
        return getSummary(server).append(",").append(Text.literal(getId().toString()));
    }

    default boolean isRelevantTo(ServerPlayerEntity player) {
        final UUID uuid = player.getUuid();
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
            final ServerPlayerEntity oS = server.getPlayerManager().getPlayer(sender);
            final ServerPlayerEntity oR = server.getPlayerManager().getPlayer(receiver);
            if (oS == null || oR == null) return false;
            oS.teleport(oR.getWorld(), oR.getX(), oR.getY(), oR.getZ(), oR.getYaw(), oR.getPitch());
            TeleportationCallback.EVENT.invoker().postTeleport(oS, oR, getType());
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
        public MutableText getSummary(MinecraftServer server) {
            return Text.translatable("format.tprequest.summary.to",
                    Utilities.getPlayerName(server, sender), Utilities.getPlayerName(server, receiver)).formatted(Formatting.WHITE);
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
            final ServerPlayerEntity oS = server.getPlayerManager().getPlayer(sender);
            final ServerPlayerEntity oR = server.getPlayerManager().getPlayer(receiver);
            if (oS == null || oR == null) return false;
            oR.teleport(oS.getWorld(), oS.getX(), oS.getY(), oS.getZ(), oS.getYaw(), oS.getPitch());
            TeleportationCallback.EVENT.invoker().postTeleport(oS, oR, getType());
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
        public MutableText getSummary(MinecraftServer server) {
            return Text.translatable("format.tprequest.summary.here",
                    Utilities.getPlayerName(server, sender), Utilities.getPlayerName(server, receiver));
        }
    }

    enum TeleportationType {
        TO,
        HERE;
    }
}
