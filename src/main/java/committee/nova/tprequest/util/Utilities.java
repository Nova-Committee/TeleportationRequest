package committee.nova.tprequest.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import committee.nova.tprequest.permnode.PermNode;
import committee.nova.tprequest.request.TeleportRequest;
import committee.nova.tprequest.storage.ServerStorage;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Utilities {
    public static final DynamicCommandExceptionType REQUEST_NOT_FOUND =
            new DynamicCommandExceptionType(uuid -> new TranslatableText("msg.tprequest.notfound", uuid));

    public static Text getPlayerName(MinecraftServer server, UUID uuid) {
        final ServerPlayerEntity p = server.getPlayerManager().getPlayer(uuid);
        return p == null ? new TranslatableText("msg.tprequest.unknown_player") : p.getName();
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

    public static double getActualSecond(int tick) {
        return tick / 20.0;
    }

    public static String getActualSecondStr(int tick) {
        return String.format("%.1f", tick / 20.0);
    }

    public static TeleportRequest parseRequest(UUID id) throws CommandSyntaxException {
        final List<TeleportRequest> l = ServerStorage.getRequestCopied();
        for (final TeleportRequest r : l) if (r.getId().equals(id)) return r;
        throw REQUEST_NOT_FOUND.create(id.toString());
    }
}
