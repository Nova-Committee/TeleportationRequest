package committee.nova.tprequest.command.init;

import com.mojang.brigadier.CommandDispatcher;
import committee.nova.tprequest.TeleportationRequest;
import committee.nova.tprequest.api.ITeleportable;
import committee.nova.tprequest.command.argument.TeleportRequestArgument;
import committee.nova.tprequest.request.TeleportRequest;
import committee.nova.tprequest.storage.ServerStorage;
import committee.nova.tprequest.util.Utilities;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

public class CommandInit {
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        final var tpa = dispatcher.register(CommandManager.literal("trtpa").then(
                CommandManager.argument("player", EntityArgumentType.player()).requires(p -> true).executes(ctx -> {
                    final ServerCommandSource src = ctx.getSource();
                    final ServerPlayerEntity sender = src.getPlayer();
                    final ServerPlayerEntity receiver = EntityArgumentType.getPlayer(ctx, "player");
                    if (receiver.equals(sender) && Utilities.isProduction()) {
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
                })).requires(p -> true)
        );
        final var tpahere = dispatcher.register(CommandManager.literal("trtpahere").then(
                CommandManager.argument("player", EntityArgumentType.player()).requires(p -> true).executes(ctx -> {
                    final ServerCommandSource src = ctx.getSource();
                    final ServerPlayerEntity sender = src.getPlayer();
                    final ServerPlayerEntity receiver = EntityArgumentType.getPlayer(ctx, "player");
                    if (receiver.equals(sender) && Utilities.isProduction()) {
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
                })).requires(p -> true)
        );
        final var tpcancel = dispatcher.register(CommandManager.literal("trtpcancel").then(
                CommandManager.argument("id", TeleportRequestArgument.instance()).requires(p -> true).executes(ctx -> {
                    final ServerCommandSource src = ctx.getSource();
                    final TeleportRequest request = TeleportRequestArgument.getRequest(ctx, "id");
                    final ServerPlayerEntity srcPlayer = src.getPlayer();
                    if (!request.getSender().equals(srcPlayer.getUuid())) {
                        src.sendError(new TranslatableText("msg.tprequest.notfound"));
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
        ).requires(p -> true).executes(CommandImpl::cancel));
        final var tpaccept = dispatcher.register(CommandManager.literal("trtpaccept").then(
                CommandManager.argument("id", TeleportRequestArgument.instance()).requires(p -> true).executes(ctx -> {
                    final ServerCommandSource src = ctx.getSource();
                    final TeleportRequest request = TeleportRequestArgument.getRequest(ctx, "id");
                    final ServerPlayerEntity srcPlayer = src.getPlayer();
                    if (!request.getReceiver().equals(srcPlayer.getUuid())) {
                        src.sendError(new TranslatableText("msg.tprequest.notfound"));
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
        ).requires(p -> true).executes(CommandImpl::accept));
        final var tpdeny = dispatcher.register(CommandManager.literal("trtpdeny").then(
                CommandManager.argument("id", TeleportRequestArgument.instance()).requires(p -> true).executes(ctx -> {
                    final ServerCommandSource src = ctx.getSource();
                    final TeleportRequest request = TeleportRequestArgument.getRequest(ctx, "id");
                    final ServerPlayerEntity srcPlayer = src.getPlayer();
                    if (!request.getReceiver().equals(srcPlayer.getUuid())) {
                        src.sendError(new TranslatableText("msg.tprequest.notfound"));
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
        ).requires(p -> true).executes(CommandImpl::deny));
        final var tpignore = dispatcher.register(CommandManager.literal("trtpignore").then(
                CommandManager.argument("id", TeleportRequestArgument.instance()).requires(p -> true).executes(ctx -> {
                    final ServerCommandSource src = ctx.getSource();
                    final TeleportRequest request = TeleportRequestArgument.getRequest(ctx, "id");
                    final ServerPlayerEntity srcPlayer = src.getPlayer();
                    if (!request.getReceiver().equals(srcPlayer.getUuid())) {
                        src.sendError(new TranslatableText("msg.tprequest.notfound"));
                        return 0;
                    }
                    request.setIgnored(true);
                    final MinecraftServer server = src.getServer();
                    src.sendFeedback(new TranslatableText("msg.tprequest.ignored", request.getSummary(server)).formatted(Formatting.YELLOW), false);
                    return 1;
                })
        ).requires(p -> true).executes(CommandImpl::ignore));
        dispatcher.register(CommandManager.literal("tpa").redirect(tpa).requires(p -> true));
        dispatcher.register(CommandManager.literal("tpahere").redirect(tpahere).requires(p -> true));
        dispatcher.register(CommandManager.literal("tpcancel").redirect(tpcancel).executes(CommandImpl::cancel).requires(p -> true));
        dispatcher.register(CommandManager.literal("tpaccept").redirect(tpaccept).executes(CommandImpl::accept).requires(p -> true));
        dispatcher.register(CommandManager.literal("tpdeny").redirect(tpdeny).executes(CommandImpl::deny).requires(p -> true));
        dispatcher.register(CommandManager.literal("tpignore").redirect(tpignore).executes(CommandImpl::ignore).requires(p -> true));
    }
}
