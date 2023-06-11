package committee.nova.tprequest.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.Optional;
import java.util.UUID;

public class Utilities {
    public static Text getPlayerName(MinecraftServer server, UUID uuid) {
        final ServerPlayerEntity p = server.getPlayerManager().getPlayer(uuid);
        return p == null ? new TranslatableText("msg.tprequest.unknown_player") : p.getName();
    }

    public static Optional<ServerPlayerEntity> getPlayer(MinecraftServer server, UUID player) {
        return Optional.ofNullable(server.getPlayerManager().getPlayer(player));
    }
}
