package committee.nova.tprequest.util;

import committee.nova.tprequest.permnode.PermNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.UUID;

public class Utilities {
    public static Text getPlayerName(MinecraftServer server, UUID uuid) {
        final ServerPlayerEntity p = server.getPlayerManager().getPlayer(uuid);
        return p == null ? Text.translatable("msg.tprequest.unknown_player") : p.getName();
    }

    public static Optional<ServerPlayerEntity> getPlayer(MinecraftServer server, UUID player) {
        return Optional.ofNullable(server.getPlayerManager().getPlayer(player));
    }

    public static boolean isProduction() {
        return !FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static boolean checkPerm(ServerCommandSource player, PermNode permNode, int defaultRequiredLevel) {
        return Permissions.check(player, permNode.getNode(), defaultRequiredLevel);
    }
}
