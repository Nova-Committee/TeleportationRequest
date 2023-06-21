package committee.nova.tprequest.command.init;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import committee.nova.tprequest.TeleportationRequest;
import committee.nova.tprequest.api.ITeleportable;
import committee.nova.tprequest.command.impl.CommandImpl;
import committee.nova.tprequest.permnode.PermNode;
import committee.nova.tprequest.request.TeleportRequest;
import committee.nova.tprequest.storage.ServerStorage;
import committee.nova.tprequest.util.Utilities;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Map;

public class CommandInit {
    private static final Map<String, LiteralCommandNode<ServerCommandSource>> cmds = Maps.newHashMap();

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        cmds.put("trtpa", dispatcher.register(CommandManager.literal("trtpa").then(
                CommandManager.argument("player", EntityArgumentType.player())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPA, 0)).executes(ctx -> {
                            final ServerCommandSource src = ctx.getSource();
                            final ServerPlayerEntity sender = src.getPlayer();
                            final ServerPlayerEntity receiver = EntityArgumentType.getPlayer(ctx, "player");
                            if (receiver.equals(sender) && Utilities.isProduction()) {
                                src.sendError(new TranslatableText("msg.tprequest.self"));
                                return 0;
                            }
                            final ITeleportable t = (ITeleportable) sender;
                            if (t.isCoolingDown()) {
                                src.sendError(new TranslatableText("msg.tprequest.cd", Utilities.getActualSecondStr(t.getTeleportCd())));
                                return 0;
                            }
                            final var request = new TeleportRequest.To(sender.getUuid(), receiver.getUuid());
                            final int timeout = request.getExpirationTime();
                            final boolean sent = ServerStorage.addRequest(request);
                            if (!sent) {
                                src.sendError(new TranslatableText("msg.tprequest.existed"));
                                return 0;
                            }
                            final String id = request.getId().toString();
                            src.sendFeedback(new TranslatableText("msg.tprequest.sent", Utilities.getActualSecondStr(timeout)).formatted(Formatting.GREEN), false);
                            final Text summary = request.getSummary(src.getServer());
                            src.sendFeedback(new TranslatableText("selection.tprequest.cancel").setStyle(Style.EMPTY
                                    .withColor(Formatting.GRAY)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpcancel " + id))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new TranslatableText("selection.tprequest.cancel.info", summary)))), false);
                            receiver.sendMessage(new TranslatableText("msg.tprequest.info.to", sender.getName())
                                    .formatted(Formatting.YELLOW), false);
                            receiver.sendMessage(new TranslatableText("msg.tprequest.respond.format",
                                    new TranslatableText("selection.tprequest.accept").setStyle(Style.EMPTY
                                            .withColor(Formatting.GREEN)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpaccept " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    new TranslatableText("selection.tprequest.accept.info", summary)))),
                                    new TranslatableText("selection.tprequest.deny").setStyle(Style.EMPTY
                                            .withColor(Formatting.RED)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpdeny " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    new TranslatableText("selection.tprequest.deny.info", summary)))),
                                    new TranslatableText("selection.tprequest.ignore").setStyle(Style.EMPTY
                                            .withColor(Formatting.GRAY)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpignore " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    new TranslatableText("selection.tprequest.ignore.info", summary))))
                            ), false);
                            return 1;
                        })).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPA, 0))
        ));
        cmds.put("trtpahere", dispatcher.register(CommandManager.literal("trtpahere").then(
                CommandManager.argument("player", EntityArgumentType.player())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPAHERE, 0)).executes(ctx -> {
                            final ServerCommandSource src = ctx.getSource();
                            final ServerPlayerEntity sender = src.getPlayer();
                            final ServerPlayerEntity receiver = EntityArgumentType.getPlayer(ctx, "player");
                            if (receiver.equals(sender) && Utilities.isProduction()) {
                                src.sendError(new TranslatableText("msg.tprequest.self"));
                                return 0;
                            }
                            final ITeleportable t = (ITeleportable) sender;
                            if (t.isCoolingDown()) {
                                src.sendError(new TranslatableText("msg.tprequest.cd", Utilities.getActualSecondStr(t.getTeleportCd())));
                                return 0;
                            }
                            final var request = new TeleportRequest.Here(sender.getUuid(), receiver.getUuid());
                            final int timeout = request.getExpirationTime();
                            final boolean sent = ServerStorage.addRequest(request);
                            if (!sent) {
                                src.sendError(new TranslatableText("msg.tprequest.existed"));
                                return 0;
                            }
                            final String id = request.getId().toString();
                            src.sendFeedback(new TranslatableText("msg.tprequest.sent", Utilities.getActualSecondStr(timeout)).formatted(Formatting.GREEN), false);
                            final Text summary = request.getSummary(src.getServer());
                            src.sendFeedback(new TranslatableText("selection.tprequest.cancel").setStyle(Style.EMPTY
                                    .withColor(Formatting.GRAY)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpcancel " + id))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new TranslatableText("selection.tprequest.cancel.info", summary)))), false);
                            receiver.sendMessage(new TranslatableText("msg.tprequest.info.here", sender.getName())
                                    .formatted(Formatting.YELLOW), false);
                            receiver.sendMessage(new TranslatableText("msg.tprequest.respond.format",
                                    new TranslatableText("selection.tprequest.accept").setStyle(Style.EMPTY
                                            .withColor(Formatting.GREEN)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpaccept " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    new TranslatableText("selection.tprequest.accept.info", summary)))),
                                    new TranslatableText("selection.tprequest.deny").setStyle(Style.EMPTY
                                            .withColor(Formatting.RED)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpdeny " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    new TranslatableText("selection.tprequest.deny.info", summary)))),
                                    new TranslatableText("selection.tprequest.ignore").setStyle(Style.EMPTY
                                            .withColor(Formatting.GRAY)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpignore " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    new TranslatableText("selection.tprequest.ignore.info", summary))))
                            ), false);
                            return 1;
                        })).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPAHERE, 0))
        ));
        cmds.put("trtpcancel", dispatcher.register(CommandManager.literal("trtpcancel").then(
                CommandManager.argument("id", UuidArgumentType.uuid())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPCANCEL, 0)).executes(ctx -> {
                            final ServerCommandSource src = ctx.getSource();
                            final TeleportRequest request = Utilities.parseRequest(UuidArgumentType.getUuid(ctx, "id"));
                            final ServerPlayerEntity srcPlayer = src.getPlayer();
                            if (!request.getSender().equals(srcPlayer.getUuid())) {
                                src.sendError(new TranslatableText("msg.tprequest.notfound", ""));
                                return 0;
                            }
                            if (!ServerStorage.requests.remove(request)) {
                                src.sendError(new TranslatableText("msg.tprequest.already_removed"));
                                return 0;
                            }
                            final MinecraftServer server = src.getServer();
                            if (!request.isIgnored())
                                Utilities.getPlayer(server, request.getReceiver()).ifPresent(s -> s.sendMessage(
                                        new TranslatableText("msg.tprequest.cancelled", request.getSummary(server)).formatted(Formatting.GRAY), false));
                            src.sendFeedback(new TranslatableText("msg.tprequest.cancelled", request.getSummary(server))
                                    .formatted(Formatting.YELLOW), false);
                            return 1;
                        })
        ).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPCANCEL, 0)).executes(CommandImpl::cancel)));
        cmds.put("trtpaccept", dispatcher.register(CommandManager.literal("trtpaccept").then(
                CommandManager.argument("id", UuidArgumentType.uuid())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPACCEPT, 0)).executes(ctx -> {
                            final ServerCommandSource src = ctx.getSource();
                            final TeleportRequest request = Utilities.parseRequest(UuidArgumentType.getUuid(ctx, "id"));
                            final ServerPlayerEntity srcPlayer = src.getPlayer();
                            if (!request.getReceiver().equals(srcPlayer.getUuid())) {
                                src.sendError(new TranslatableText("msg.tprequest.notfound", ""));
                                return 0;
                            }
                            final MinecraftServer server = src.getServer();
                            if (!request.execute(server)) {
                                src.sendError(new TranslatableText("msg.tprequest.not_present"));
                                return 0;
                            }
                            Utilities.getPlayer(server, request.getSender()).ifPresent(p -> {
                                p.sendMessage(new TranslatableText("msg.tprequest.accepted",
                                        request.getSummary(server)).formatted(Formatting.GREEN), false);
                                ((ITeleportable) p).setTeleportCd(TeleportationRequest.getTpCd());
                            });
                            src.sendFeedback(new TranslatableText("msg.tprequest.accepted", request.getSummary(server)).formatted(Formatting.GREEN), false);
                            ServerStorage.requests.remove(request);
                            return 1;
                        })
        ).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPACCEPT, 0)).executes(CommandImpl::accept)));
        cmds.put("trtpdeny", dispatcher.register(CommandManager.literal("trtpdeny").then(
                CommandManager.argument("id", UuidArgumentType.uuid())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPDENY, 0)).executes(ctx -> {
                            final ServerCommandSource src = ctx.getSource();
                            final TeleportRequest request = Utilities.parseRequest(UuidArgumentType.getUuid(ctx, "id"));
                            final ServerPlayerEntity srcPlayer = src.getPlayer();
                            if (!request.getReceiver().equals(srcPlayer.getUuid())) {
                                src.sendError(new TranslatableText("msg.tprequest.notfound", ""));
                                return 0;
                            }
                            if (!ServerStorage.requests.remove(request)) {
                                src.sendError(new TranslatableText("msg.tprequest.already_removed"));
                                return 0;
                            }
                            final MinecraftServer server = src.getServer();
                            Utilities.getPlayer(server, request.getSender()).ifPresent(s -> s.sendMessage(new TranslatableText("msg.tprequest.denied",
                                    request.getSummary(server)).formatted(Formatting.RED), false));
                            src.sendFeedback(new TranslatableText("msg.tprequest.denied", request.getSummary(server)).formatted(Formatting.YELLOW), false);
                            return 1;
                        })
        ).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPDENY, 0)).executes(CommandImpl::deny)));
        cmds.put("trtpignore", dispatcher.register(CommandManager.literal("trtpignore").then(
                CommandManager.argument("id", UuidArgumentType.uuid())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPIGNORE, 0)).executes(ctx -> {
                            final ServerCommandSource src = ctx.getSource();
                            final TeleportRequest request = Utilities.parseRequest(UuidArgumentType.getUuid(ctx, "id"));
                            final ServerPlayerEntity srcPlayer = src.getPlayer();
                            if (!request.getReceiver().equals(srcPlayer.getUuid())) {
                                src.sendError(new TranslatableText("msg.tprequest.notfound", ""));
                                return 0;
                            }
                            request.setIgnored(true);
                            final MinecraftServer server = src.getServer();
                            src.sendFeedback(new TranslatableText("msg.tprequest.ignored", request.getSummary(server)).formatted(Formatting.YELLOW), false);
                            return 1;
                        })
        ).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPIGNORE, 0)).executes(CommandImpl::ignore)));
        cmds.put("trtplist", dispatcher.register(CommandManager.literal("trtplist")
                .executes(ctx -> {
                    final ServerCommandSource src = ctx.getSource();
                    final MinecraftServer server = src.getServer();
                    final ServerPlayerEntity player = src.getPlayer();
                    final List<TeleportRequest> requests = ServerStorage.getRequestCopied();
                    final List<TeleportRequest> received = requests.stream().filter(r -> r.isReceiver(player)).toList();
                    final List<TeleportRequest> sent = requests.stream().filter(r -> r.isSender(player)).toList();
                    boolean empty = true;
                    if (!sent.isEmpty()) {
                        empty = false;
                        src.sendFeedback(new TranslatableText("category.tprequest.sent").formatted(Formatting.AQUA), false);
                        sent.forEach(r -> {
                            final Text summary = r.getSummary(src.getServer());
                            src.sendFeedback(summary, false);
                            src.sendFeedback(new TranslatableText("selection.tprequest.cancel").setStyle(Style.EMPTY
                                    .withColor(Formatting.GRAY)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpcancel " + r.getId().toString()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            new TranslatableText("selection.tprequest.cancel.info", summary)))), false);
                        });
                    }
                    if (!received.isEmpty()) {
                        empty = false;
                        src.sendFeedback(new TranslatableText("category.tprequest.received").formatted(Formatting.LIGHT_PURPLE), false);
                        received.forEach(t -> {
                            final MutableText summary = t.getSummary(server);
                            final String id = t.getId().toString();
                            final boolean ignored = t.isIgnored();
                            src.sendFeedback(summary.styled(s -> {
                                if (ignored) return s.withColor(Formatting.GRAY);
                                return s;
                            }).append(t.isIgnored() ?
                                    new TranslatableText("status.tprequest.ignored") : LiteralText.EMPTY), false);
                            src.sendFeedback(new TranslatableText("msg.tprequest.respond.format",
                                    new TranslatableText("selection.tprequest.accept").setStyle(Style.EMPTY
                                            .withColor(Formatting.GREEN)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpaccept " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    new TranslatableText("selection.tprequest.accept.info", summary)))),
                                    new TranslatableText("selection.tprequest.deny").setStyle(Style.EMPTY
                                            .withColor(Formatting.RED)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpdeny " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    new TranslatableText("selection.tprequest.deny.info", summary)))),
                                    ignored ? LiteralText.EMPTY : new TranslatableText("selection.tprequest.ignore").setStyle(Style.EMPTY
                                            .withColor(Formatting.GRAY)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpignore " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    new TranslatableText("selection.tprequest.ignore.info", summary))))), false);
                        });
                    }
                    if (empty) src.sendError(new TranslatableText("msg.tprequest.no_pending"));
                    return 1;
                })
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPLIST, 0))));
        dispatcher.register(CommandManager.literal("tprequest")
                .then(CommandManager.literal("help").executes(ctx -> {
                    final ServerCommandSource src = ctx.getSource();
                    for (final var e : cmds.keySet()) {
                        src.sendFeedback(new TranslatableText("desc.tprequest." + e).formatted(Formatting.GREEN), false);
                        final List<String> alias = TeleportationRequest.getAlternativesFor(e);
                        if (!alias.isEmpty()) continue;
                        src.sendFeedback(new TranslatableText("desc.tprequest.alias").formatted(Formatting.YELLOW), false);
                        alias.forEach(a -> src.sendFeedback(new LiteralText("/" + a).formatted(Formatting.YELLOW), false));
                    }
                    return 1;
                }).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_HELP, 0)))
                .then(CommandManager.literal("reload").executes(ctx -> {
                    final ServerCommandSource src = ctx.getSource();
                    final var success = TeleportationRequest.reload(src.getServer());
                    src.sendFeedback(new TranslatableText("msg.tprequest.reload." + (success ? "success" : "failure")
                            .formatted(success ? Formatting.GREEN : Formatting.RED)), false);
                    return success ? 1 : 0;
                }).requires(p -> Utilities.checkPerm(p, PermNode.ADMIN_RELOAD, p.getServer().getOpPermissionLevel())))
                .requires(p -> true));
        for (final var e : cmds.entrySet()) {
            final var p = e.getValue();
            TeleportationRequest.getAlternativesFor(e.getKey()).forEach(a -> dispatcher.register(CommandManager.literal(a)
                    .redirect(p)
                    .executes(p.getCommand())
                    .requires(p.getRequirement()))
            );
        }
    }
}
