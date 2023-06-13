package committee.nova.tprequest.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import committee.nova.tprequest.permnode.PermNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.Optional;
import java.util.UUID;

public class Utilities {
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
            e.printStackTrace();
            return true;
        }
    }
}
