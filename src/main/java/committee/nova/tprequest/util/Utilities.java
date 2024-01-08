package committee.nova.tprequest.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import committee.nova.tprequest.TeleportationRequest;
import committee.nova.tprequest.permnode.PermNode;
import committee.nova.tprequest.request.TeleportRequest;
import committee.nova.tprequest.storage.ServerStorage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Utilities {
    public static final DynamicCommandExceptionType REQUEST_NOT_FOUND =
            new DynamicCommandExceptionType(uuid -> Component.translatable("msg.tprequest.notfound", uuid));

    public static Component getPlayerName(MinecraftServer server, UUID uuid) {
        final ServerPlayer p = server.getPlayerList().getPlayer(uuid);
        return p == null ? Component.translatable("msg.tprequest.unknown_player") : p.getName();
    }

    public static Optional<ServerPlayer> getPlayer(MinecraftServer server, UUID player) {
        return Optional.ofNullable(server.getPlayerList().getPlayer(player));
    }

    public static boolean isProduction() {
        return FMLEnvironment.production;
    }

    public static boolean checkPerm(CommandSourceStack stack, PermNode permNode) {
        try {
            final ServerPlayer player = stack.getPlayerOrException();
            return PermissionAPI.getPermission(player, permNode.getNode());
        } catch (CommandSyntaxException e) {
            TeleportationRequest.LOGGER.error("Exception caught on perm check.", e);
            return true;
        }
    }

    public static TeleportRequest parseRequest(UUID id) throws CommandSyntaxException {
        final List<TeleportRequest> l = ServerStorage.getRequestCopied();
        for (final TeleportRequest r : l) if (r.getId().equals(id)) return r;
        throw REQUEST_NOT_FOUND.create(id.toString());
    }
}
