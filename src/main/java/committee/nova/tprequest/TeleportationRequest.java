package committee.nova.tprequest;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.StringArgumentType;
import committee.nova.tprequest.api.ITeleportable;
import committee.nova.tprequest.cfg.Config;
import committee.nova.tprequest.request.TeleportRequest;
import committee.nova.tprequest.storage.ServerStorage;
import committee.nova.tprequest.util.Utilities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.UUID;

public class TeleportationRequest implements ModInitializer {
    private static Config cfg;
    private static int tpCd;
    private static int expirationTime;

    @Override
    public void onInitialize() {
        syncCfg();
        ServerTickEvents.END_SERVER_TICK.register(ServerStorage::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> ServerStorage.requests.clear());
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            final var tpa = dispatcher.register(CommandManager.literal("tpa").then(
                    CommandManager.argument("player", EntityArgumentType.player()).requires(p -> true).executes(ctx -> {
                        final ServerCommandSource src = ctx.getSource();
                        final ServerPlayerEntity sender = src.getPlayer();
                        final ServerPlayerEntity receiver = EntityArgumentType.getPlayer(ctx, "player");
                        if (receiver.equals(sender)) {
                            src.sendError(new TranslatableText("msg.tprequest.self"));
                            return 0;
                        }
                        final ITeleportable t = (ITeleportable) sender;
                        if (t.isCoolingDown()) {
                            src.sendError(new TranslatableText("msg.tprequest.cd", t.getTeleportCd()));
                            return 0;
                        }
                        final var request = new TeleportRequest.To(sender.getUuid(), receiver.getUuid());
                        final int timeout = request.getExpiration();
                        final boolean sent = ServerStorage.addRequest(request);
                        if (!sent) {
                            src.sendError(new TranslatableText("msg.tprequest.existed"));
                            return 0;
                        }
                        final String id = request.getId().toString();
                        src.sendFeedback(new TranslatableText("msg.tprequest.sent", timeout).formatted(Formatting.GREEN), false);
                        src.sendFeedback(new TranslatableText("selection.tprequest.cancel").setStyle(Style.EMPTY
                                .withColor(Formatting.GRAY)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpcancel " + id))), false);
                        receiver.sendMessage(new TranslatableText("msg.tprequest.info.to", sender.getName())
                                .formatted(Formatting.YELLOW), false);
                        receiver.sendMessage(new TranslatableText("msg.tprequest.respond.format",
                                new TranslatableText("selection.tprequest.accept").setStyle(Style.EMPTY
                                        .withColor(Formatting.GREEN)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpaccept " + id))),
                                new TranslatableText("selection.tprequest.deny").setStyle(Style.EMPTY
                                        .withColor(Formatting.RED)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpdeny " + id))),
                                new TranslatableText("selection.tprequest.ignore").setStyle(Style.EMPTY
                                        .withColor(Formatting.GRAY)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpignore " + id)))
                        ), false);
                        return 1;
                    })).requires(p -> true)
            );
            final var tpahere = dispatcher.register(CommandManager.literal("tpahere").then(
                    CommandManager.argument("player", EntityArgumentType.player()).requires(p -> true).executes(ctx -> {
                        final ServerCommandSource src = ctx.getSource();
                        final ServerPlayerEntity sender = src.getPlayer();
                        final ServerPlayerEntity receiver = EntityArgumentType.getPlayer(ctx, "player");
                        if (receiver.equals(sender)) {
                            src.sendError(new TranslatableText("msg.tprequest.self"));
                            return 0;
                        }
                        final ITeleportable t = (ITeleportable) sender;
                        if (t.isCoolingDown()) {
                            src.sendError(new TranslatableText("msg.tprequest.cd", t.getTeleportCd()));
                            return 0;
                        }
                        final var request = new TeleportRequest.Here(sender.getUuid(), receiver.getUuid());
                        final int timeout = request.getExpiration();
                        final boolean sent = ServerStorage.addRequest(request);
                        if (!sent) {
                            src.sendError(new TranslatableText("msg.tprequest.existed"));
                            return 0;
                        }
                        final String id = request.getId().toString();
                        src.sendFeedback(new TranslatableText("msg.tprequest.sent", timeout).formatted(Formatting.GREEN), false);
                        src.sendFeedback(new TranslatableText("msg.tprequest.cancel").setStyle(Style.EMPTY
                                .withColor(Formatting.GRAY)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpcancel " + id))), false);
                        receiver.sendMessage(new TranslatableText("msg.tprequest.info.here", sender.getName())
                                .formatted(Formatting.YELLOW), false);
                        receiver.sendMessage(new TranslatableText("msg.tprequest.respond.format",
                                new TranslatableText("selection.tprequest.accept").setStyle(Style.EMPTY
                                        .withColor(Formatting.GREEN)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpaccept " + id))),
                                new TranslatableText("selection.tprequest.deny").setStyle(Style.EMPTY
                                        .withColor(Formatting.RED)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpdeny " + id))),
                                new TranslatableText("selection.tprequest.ignore").setStyle(Style.EMPTY
                                        .withColor(Formatting.GRAY)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpignore " + id)))
                        ), false);
                        return 1;
                    })).requires(p -> true)
            );
            final var tpcancel = dispatcher.register(CommandManager.literal("tpcancel").then(
                    CommandManager.argument("id", StringArgumentType.string()).requires(p -> true).executes(ctx -> {
                        final ServerCommandSource src = ctx.getSource();
                        UUID id;
                        try {
                            id = UUID.fromString(StringArgumentType.getString(ctx, "id"));
                        } catch (IllegalArgumentException ignored) {
                            src.sendError(new TranslatableText("msg.tprequest.wrong_uuid"));
                            return 0;
                        }
                        final ServerPlayerEntity srcPlayer = src.getPlayer();
                        for (final var r : ServerStorage.getRequestCopied())
                            if (r.getSender().equals(srcPlayer.getUuid()) && r.getId().equals(id)) {
                                final MinecraftServer server = src.getServer();
                                if (!ServerStorage.requests.remove(r)) {
                                    src.sendError(new TranslatableText("msg.tprequest.already_removed"));
                                    return 0;
                                }
                                if (!r.isIgnored())
                                    Utilities.getPlayer(server, r.getReceiver()).ifPresent(s -> s.sendMessage(new TranslatableText("msg.tprequest.cancelled",
                                            r.getSummary(server)).formatted(Formatting.GRAY), false));
                                src.sendFeedback(new TranslatableText("msg.tprequest.cancelled", r.getSummary(server)).formatted(Formatting.YELLOW), false);
                                return 1;
                            }
                        src.sendError(new TranslatableText("msg.tprequest.notfound"));
                        return 0;
                    })
            ).requires(p -> true).executes(ctx -> {
                final ServerCommandSource src = ctx.getSource();
                final ServerPlayerEntity srcPlayer = src.getPlayer();
                final ImmutableList<TeleportRequest> reversed = ServerStorage.getRequestCopied().reverse();
                for (final var r : reversed)
                    if (r.getSender().equals(srcPlayer.getUuid())) {
                        final MinecraftServer server = src.getServer();
                        if (!ServerStorage.requests.remove(r)) {
                            src.sendError(new TranslatableText("msg.tprequest.already_removed", r.getSummary(server)));
                            return 0;
                        }
                        Utilities.getPlayer(server, r.getReceiver()).ifPresent(s -> s.sendMessage(new TranslatableText("msg.tprequest.cancelled",
                                r.getSummary(server)).formatted(Formatting.GRAY), false));
                        src.sendFeedback(new TranslatableText("msg.tprequest.cancelled", r.getSummary(server)).formatted(Formatting.YELLOW), false);
                        return 1;
                    }
                src.sendError(new TranslatableText("msg.tprequest.notfound"));
                return 0;
            }));
            final var tpaccept = dispatcher.register(CommandManager.literal("tpaccept").then(
                    CommandManager.argument("id", StringArgumentType.string()).requires(p -> true).executes(ctx -> {
                        final ServerCommandSource src = ctx.getSource();
                        UUID id;
                        try {
                            id = UUID.fromString(StringArgumentType.getString(ctx, "id"));
                        } catch (IllegalArgumentException ignored) {
                            src.sendError(new TranslatableText("msg.tprequest.wrong_uuid"));
                            return 0;
                        }
                        final ServerPlayerEntity srcPlayer = src.getPlayer();
                        for (final var r : ServerStorage.getRequestCopied())
                            if (r.getReceiver().equals(srcPlayer.getUuid()) && r.getId().equals(id)) {
                                final MinecraftServer server = src.getServer();
                                if (!r.execute(server)) {
                                    src.sendError(new TranslatableText("msg.tprequest.not_present"));
                                    return 0;
                                }
                                Utilities.getPlayer(server, r.getSender()).ifPresent(p -> {
                                    p.sendMessage(new TranslatableText("msg.tprequest.accepted",
                                            r.getSummary(server)).formatted(Formatting.GREEN), false);
                                    ((ITeleportable) p).setTeleportCd(TeleportationRequest.getTpCd());
                                });
                                src.sendFeedback(new TranslatableText("msg.tprequest.accepted", r.getSummary(server)).formatted(Formatting.GREEN), false);
                                ServerStorage.requests.remove(r);
                                return 1;
                            }
                        src.sendError(new TranslatableText("msg.tprequest.notfound"));
                        return 0;
                    })
            ).requires(p -> true).executes(ctx -> {
                final ServerCommandSource src = ctx.getSource();
                final ServerPlayerEntity srcPlayer = src.getPlayer();
                final var reversed = ServerStorage.getRequestCopied().reverse();
                for (final var r : reversed)
                    if (r.getReceiver().equals(srcPlayer.getUuid())) {
                        final MinecraftServer server = src.getServer();
                        if (!r.execute(server)) {
                            src.sendError(new TranslatableText("msg.tprequest.not_present"));
                            return 0;
                        }
                        Utilities.getPlayer(server, r.getSender()).ifPresent(p -> {
                            p.sendMessage(new TranslatableText("msg.tprequest.accepted",
                                    r.getSummary(server)).formatted(Formatting.GREEN), false);
                            ((ITeleportable) p).setTeleportCd(TeleportationRequest.getTpCd());
                        });
                        src.sendFeedback(new TranslatableText("msg.tprequest.accepted", r.getSummary(server)).formatted(Formatting.GREEN), false);
                        ServerStorage.requests.remove(r);
                        return 1;
                    }
                src.sendError(new TranslatableText("msg.tprequest.notfound"));
                return 0;
            }));
            final var tpdeny = dispatcher.register(CommandManager.literal("tpdeny").then(
                    CommandManager.argument("id", StringArgumentType.string()).requires(p -> true).executes(ctx -> {
                        final ServerCommandSource src = ctx.getSource();
                        UUID id;
                        try {
                            id = UUID.fromString(StringArgumentType.getString(ctx, "id"));
                        } catch (IllegalArgumentException ignored) {
                            src.sendError(new TranslatableText("msg.tprequest.wrong_uuid"));
                            return 0;
                        }
                        final ServerPlayerEntity srcPlayer = src.getPlayer();
                        for (final var r : ServerStorage.getRequestCopied())
                            if (r.getReceiver().equals(srcPlayer.getUuid()) && r.getId().equals(id)) {
                                final MinecraftServer server = src.getServer();
                                if (!ServerStorage.requests.remove(r)) {
                                    src.sendError(new TranslatableText("msg.tprequest.already_removed"));
                                    return 0;
                                }
                                Utilities.getPlayer(server, r.getSender()).ifPresent(s -> s.sendMessage(new TranslatableText("msg.tprequest.denied",
                                        r.getSummary(server)).formatted(Formatting.RED), false));
                                src.sendFeedback(new TranslatableText("msg.tprequest.denied", r.getSummary(server)).formatted(Formatting.YELLOW), false);
                                return 1;
                            }
                        src.sendError(new TranslatableText("msg.tprequest.notfound"));
                        return 0;
                    })
            ).requires(p -> true).executes(ctx -> {
                final ServerCommandSource src = ctx.getSource();
                final ServerPlayerEntity srcPlayer = src.getPlayer();
                final ImmutableList<TeleportRequest> reversed = ServerStorage.getRequestCopied().reverse();
                for (final var r : reversed)
                    if (r.getReceiver().equals(srcPlayer.getUuid())) {
                        final MinecraftServer server = src.getServer();
                        if (!ServerStorage.requests.remove(r)) {
                            src.sendError(new TranslatableText("msg.tprequest.already_removed", r.getSummary(server)));
                            return 0;
                        }
                        Utilities.getPlayer(server, r.getSender()).ifPresent(s -> s.sendMessage(new TranslatableText("msg.tprequest.denied",
                                r.getSummary(server)).formatted(Formatting.RED), false));
                        src.sendFeedback(new TranslatableText("msg.tprequest.denied", r.getSummary(server)).formatted(Formatting.YELLOW), false);
                        return 1;
                    }
                src.sendError(new TranslatableText("msg.tprequest.notfound"));
                return 0;
            }));
            final var tpignore = dispatcher.register(CommandManager.literal("tpignore").then(
                    CommandManager.argument("id", StringArgumentType.string()).requires(p -> true).executes(ctx -> {
                        final ServerCommandSource src = ctx.getSource();
                        UUID id;
                        try {
                            id = UUID.fromString(StringArgumentType.getString(ctx, "id"));
                        } catch (IllegalArgumentException ignored) {
                            src.sendError(new TranslatableText("msg.tprequest.wrong_uuid"));
                            return 0;
                        }
                        final ServerPlayerEntity srcPlayer = src.getPlayer();
                        for (final var r : ServerStorage.getRequestCopied())
                            if (r.getReceiver().equals(srcPlayer.getUuid())
                                    && r.getId().equals(id) && !r.isIgnored()) {
                                final MinecraftServer server = src.getServer();
                                r.setIgnored(true);
                                src.sendFeedback(new TranslatableText("msg.tprequest.ignored", r.getSummary(server)).formatted(Formatting.YELLOW), false);
                                return 1;
                            }
                        src.sendError(new TranslatableText("msg.tprequest.notfound"));
                        return 0;
                    })
            ).requires(p -> true).executes(ctx -> {
                final ServerCommandSource src = ctx.getSource();
                final ServerPlayerEntity srcPlayer = src.getPlayer();
                final ImmutableList<TeleportRequest> reversed = ServerStorage.getRequestCopied().reverse();
                for (final var r : reversed)
                    if (r.getReceiver().equals(srcPlayer.getUuid()) && !r.isIgnored()) {
                        final MinecraftServer server = src.getServer();
                        r.setIgnored(true);
                        src.sendFeedback(new TranslatableText("msg.tprequest.ignored", r.getSummary(server)).formatted(Formatting.YELLOW), false);
                        return 1;
                    }
                src.sendError(new TranslatableText("msg.tprequest.notfound"));
                return 0;
            }));
            dispatcher.register(CommandManager.literal("trtpa").redirect(tpa).requires(p -> true));
            dispatcher.register(CommandManager.literal("trtpahere").redirect(tpahere).requires(p -> true));
            dispatcher.register(CommandManager.literal("trtpcancel").redirect(tpcancel).requires(p -> true));
            dispatcher.register(CommandManager.literal("trtpaccept").redirect(tpaccept).requires(p -> true));
            dispatcher.register(CommandManager.literal("trtpdeny").redirect(tpdeny).requires(p -> true));
            dispatcher.register(CommandManager.literal("trtpignore").redirect(tpignore).requires(p -> true));
        });
    }

    public static void syncCfg() {
        if (cfg == null) cfg = Config.of("TeleportationRequest-Config").provider(path -> """
                # TeleportationRequest-Config
                # Cool-down time (tick) after a successful teleportation request
                tpCd=600
                # Expiration time (tick) of a teleportation request
                expirationTime=1200
                """
        ).request();
        tpCd = cfg.getOrDefault("tpCd", 600);
        expirationTime = cfg.getOrDefault("expirationTime", 1200);
    }

    public static int getTpCd() {
        return tpCd;
    }

    public static int getExpirationTime() {
        return expirationTime;
    }
}
