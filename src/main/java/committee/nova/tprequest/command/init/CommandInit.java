package committee.nova.tprequest.command.init;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.CommandDispatcher;
import committee.nova.tprequest.TeleportationRequest;
import committee.nova.tprequest.api.ITeleportable;
import committee.nova.tprequest.command.argument.TeleportRequestArgument;
import committee.nova.tprequest.command.impl.CommandImpl;
import committee.nova.tprequest.permnode.PermNode;
import committee.nova.tprequest.request.TeleportRequest;
import committee.nova.tprequest.storage.ServerStorage;
import committee.nova.tprequest.util.Utilities;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;

public class CommandInit {
    private static final Map<String, String> cmds = ImmutableMap.of(
            "trtpa", "tpa",
            "trtpahere", "tpahere",
            "trtpaccept", "tpaccept",
            "trtpcancel", "tpcancel",
            "trtpdeny", "tpdeny",
            "trtpignore", "tpignore"
    );

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        final var tpa = dispatcher.register(CommandManager.literal("trtpa").then(
                CommandManager.argument("player", EntityArgumentType.player())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPA, 0)).executes(ctx -> {
                            final ServerCommandSource src = ctx.getSource();
                            final ServerPlayerEntity sender = src.getPlayer();
                            final ServerPlayerEntity receiver = EntityArgumentType.getPlayer(ctx, "player");
                            if (receiver.equals(sender) && Utilities.isProduction()) {
                                src.sendError(Text.translatable("msg.tprequest.self"));
                                return 0;
                            }
                            final ITeleportable t = (ITeleportable) sender;
                            if (t.isCoolingDown()) {
                                src.sendError(Text.translatable("msg.tprequest.cd", t.getTeleportCd()));
                                return 0;
                            }
                            final var request = new TeleportRequest.To(sender.getUuid(), receiver.getUuid());
                            final int timeout = request.getExpiration();
                            final boolean sent = ServerStorage.addRequest(request);
                            if (!sent) {
                                src.sendError(Text.translatable("msg.tprequest.existed"));
                                return 0;
                            }
                            final String id = request.getId().toString();
                            src.sendFeedback(Text.translatable("msg.tprequest.sent", timeout).formatted(Formatting.GREEN), false);
                            final Text summary = request.getSummary(src.getServer());
                            src.sendFeedback(Text.translatable("selection.tprequest.cancel").setStyle(Style.EMPTY
                                    .withColor(Formatting.GRAY)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpcancel " + id))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Text.translatable("selection.tprequest.cancel.info", summary)))), false);
                            receiver.sendMessage(Text.translatable("msg.tprequest.info.to", sender.getName())
                                    .formatted(Formatting.YELLOW), false);
                            receiver.sendMessage(Text.translatable("msg.tprequest.respond.format",
                                    Text.translatable("selection.tprequest.accept").setStyle(Style.EMPTY
                                            .withColor(Formatting.GREEN)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpaccept " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    Text.translatable("selection.tprequest.accept.info", summary)))),
                                    Text.translatable("selection.tprequest.deny").setStyle(Style.EMPTY
                                            .withColor(Formatting.RED)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpdeny " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    Text.translatable("selection.tprequest.deny.info", summary)))),
                                    Text.translatable("selection.tprequest.ignore").setStyle(Style.EMPTY
                                            .withColor(Formatting.GRAY)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpignore " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    Text.translatable("selection.tprequest.ignore.info", summary))))
                            ), false);
                            return 1;
                        })).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPA, 0))
        );
        final var tpahere = dispatcher.register(CommandManager.literal("trtpahere").then(
                CommandManager.argument("player", EntityArgumentType.player())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPAHERE, 0)).executes(ctx -> {
                            final ServerCommandSource src = ctx.getSource();
                            final ServerPlayerEntity sender = src.getPlayer();
                            final ServerPlayerEntity receiver = EntityArgumentType.getPlayer(ctx, "player");
                            if (receiver.equals(sender) && Utilities.isProduction()) {
                                src.sendError(Text.translatable("msg.tprequest.self"));
                                return 0;
                            }
                            final ITeleportable t = (ITeleportable) sender;
                            if (t.isCoolingDown()) {
                                src.sendError(Text.translatable("msg.tprequest.cd", t.getTeleportCd()));
                                return 0;
                            }
                            final var request = new TeleportRequest.Here(sender.getUuid(), receiver.getUuid());
                            final int timeout = request.getExpiration();
                            final boolean sent = ServerStorage.addRequest(request);
                            if (!sent) {
                                src.sendError(Text.translatable("msg.tprequest.existed"));
                                return 0;
                            }
                            final String id = request.getId().toString();
                            src.sendFeedback(Text.translatable("msg.tprequest.sent", timeout).formatted(Formatting.GREEN), false);
                            final Text summary = request.getSummary(src.getServer());
                            src.sendFeedback(Text.translatable("selection.tprequest.cancel").setStyle(Style.EMPTY
                                    .withColor(Formatting.GRAY)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpcancel " + id))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Text.translatable("selection.tprequest.cancel.info", summary)))), false);
                            receiver.sendMessage(Text.translatable("msg.tprequest.info.here", sender.getName())
                                    .formatted(Formatting.YELLOW), false);
                            receiver.sendMessage(Text.translatable("msg.tprequest.respond.format",
                                    Text.translatable("selection.tprequest.accept").setStyle(Style.EMPTY
                                            .withColor(Formatting.GREEN)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpaccept " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    Text.translatable("selection.tprequest.accept.info", summary)))),
                                    Text.translatable("selection.tprequest.deny").setStyle(Style.EMPTY
                                            .withColor(Formatting.RED)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpdeny " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    Text.translatable("selection.tprequest.deny.info", summary)))),
                                    Text.translatable("selection.tprequest.ignore").setStyle(Style.EMPTY
                                            .withColor(Formatting.GRAY)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpignore " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    Text.translatable("selection.tprequest.ignore.info", summary))))
                            ), false);
                            return 1;
                        })).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPAHERE, 0))
        );
        final var tpcancel = dispatcher.register(CommandManager.literal("trtpcancel").then(
                CommandManager.argument("id", TeleportRequestArgument.instance())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPCANCEL, 0)).executes(ctx -> {
                            final ServerCommandSource src = ctx.getSource();
                            final TeleportRequest request = TeleportRequestArgument.getRequest(ctx, "id");
                            final ServerPlayerEntity srcPlayer = src.getPlayer();
                            if (!request.getSender().equals(srcPlayer.getUuid())) {
                                src.sendError(Text.translatable("msg.tprequest.notfound", ""));
                                return 0;
                            }
                            if (!ServerStorage.requests.remove(request)) {
                                src.sendError(Text.translatable("msg.tprequest.already_removed"));
                                return 0;
                            }
                            final MinecraftServer server = src.getServer();
                            if (!request.isIgnored())
                                Utilities.getPlayer(server, request.getReceiver()).ifPresent(s -> s.sendMessage(
                                        Text.translatable("msg.tprequest.cancelled", request.getSummary(server)).formatted(Formatting.GRAY), false));
                            src.sendFeedback(Text.translatable("msg.tprequest.cancelled", request.getSummary(server))
                                    .formatted(Formatting.YELLOW), false);
                            return 1;
                        })
        ).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPCANCEL, 0)).executes(CommandImpl::cancel));
        final var tpaccept = dispatcher.register(CommandManager.literal("trtpaccept").then(
                CommandManager.argument("id", TeleportRequestArgument.instance())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPACCEPT, 0)).executes(ctx -> {
                            final ServerCommandSource src = ctx.getSource();
                            final TeleportRequest request = TeleportRequestArgument.getRequest(ctx, "id");
                            final ServerPlayerEntity srcPlayer = src.getPlayer();
                            if (!request.getReceiver().equals(srcPlayer.getUuid())) {
                                src.sendError(Text.translatable("msg.tprequest.notfound", ""));
                                return 0;
                            }
                            final MinecraftServer server = src.getServer();
                            if (!request.execute(server)) {
                                src.sendError(Text.translatable("msg.tprequest.not_present"));
                                return 0;
                            }
                            Utilities.getPlayer(server, request.getSender()).ifPresent(p -> {
                                p.sendMessage(Text.translatable("msg.tprequest.accepted",
                                        request.getSummary(server)).formatted(Formatting.GREEN), false);
                                ((ITeleportable) p).setTeleportCd(TeleportationRequest.getTpCd());
                            });
                            src.sendFeedback(Text.translatable("msg.tprequest.accepted", request.getSummary(server)).formatted(Formatting.GREEN), false);
                            ServerStorage.requests.remove(request);
                            return 1;
                        })
        ).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPACCEPT, 0)).executes(CommandImpl::accept));
        final var tpdeny = dispatcher.register(CommandManager.literal("trtpdeny").then(
                CommandManager.argument("id", TeleportRequestArgument.instance())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPDENY, 0)).executes(ctx -> {
                            final ServerCommandSource src = ctx.getSource();
                            final TeleportRequest request = TeleportRequestArgument.getRequest(ctx, "id");
                            final ServerPlayerEntity srcPlayer = src.getPlayer();
                            if (!request.getReceiver().equals(srcPlayer.getUuid())) {
                                src.sendError(Text.translatable("msg.tprequest.notfound", ""));
                                return 0;
                            }
                            if (!ServerStorage.requests.remove(request)) {
                                src.sendError(Text.translatable("msg.tprequest.already_removed"));
                                return 0;
                            }
                            final MinecraftServer server = src.getServer();
                            Utilities.getPlayer(server, request.getSender()).ifPresent(s -> s.sendMessage(Text.translatable("msg.tprequest.denied",
                                    request.getSummary(server)).formatted(Formatting.RED), false));
                            src.sendFeedback(Text.translatable("msg.tprequest.denied", request.getSummary(server)).formatted(Formatting.YELLOW), false);
                            return 1;
                        })
        ).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPDENY, 0)).executes(CommandImpl::deny));
        final var tpignore = dispatcher.register(CommandManager.literal("trtpignore").then(
                CommandManager.argument("id", TeleportRequestArgument.instance())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPIGNORE, 0)).executes(ctx -> {
                            final ServerCommandSource src = ctx.getSource();
                            final TeleportRequest request = TeleportRequestArgument.getRequest(ctx, "id");
                            final ServerPlayerEntity srcPlayer = src.getPlayer();
                            if (!request.getReceiver().equals(srcPlayer.getUuid())) {
                                src.sendError(Text.translatable("msg.tprequest.notfound", ""));
                                return 0;
                            }
                            request.setIgnored(true);
                            final MinecraftServer server = src.getServer();
                            src.sendFeedback(Text.translatable("msg.tprequest.ignored", request.getSummary(server)).formatted(Formatting.YELLOW), false);
                            return 1;
                        })
        ).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPIGNORE, 0)).executes(CommandImpl::ignore));
        final boolean shortAlternatives = TeleportationRequest.shouldRegisterShortAlternatives();
        dispatcher.register(CommandManager.literal("tprequest")
                .then(CommandManager.literal("help").executes(ctx -> {
                    final ServerCommandSource src = ctx.getSource();
                    for (final var e : cmds.entrySet()) {
                        final String key = "desc.tprequest." + e.getKey();
                        src.sendFeedback(Text.translatable(key, e.getKey()), false);
                        if (shortAlternatives) src.sendFeedback(Text.translatable(key, e.getValue()), false);
                    }
                    return 1;
                }).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_HELP, 0)))
                .then(CommandManager.literal("reload").executes(ctx -> {
                    final boolean success = TeleportationRequest.reload();
                    ctx.getSource().sendFeedback(Text.translatable("msg.tprequest.reload." + (success ? "success" : "failure")
                            .formatted(success ? Formatting.GREEN : Formatting.RED)), false);
                    return success ? 1 : 0;
                }).requires(p -> Utilities.checkPerm(p, PermNode.ADMIN_RELOAD, p.getServer().getOpPermissionLevel())))
                .requires(p -> true));
        if (!shortAlternatives) return;
        dispatcher.register(CommandManager.literal("tpa").redirect(tpa)
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPA, 0)));
        dispatcher.register(CommandManager.literal("tpahere").redirect(tpahere)
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPAHERE, 0)));
        dispatcher.register(CommandManager.literal("tpcancel").redirect(tpcancel).executes(CommandImpl::cancel)
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPCANCEL, 0)));
        dispatcher.register(CommandManager.literal("tpaccept").redirect(tpaccept).executes(CommandImpl::accept)
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPACCEPT, 0)));
        dispatcher.register(CommandManager.literal("tpdeny").redirect(tpdeny).executes(CommandImpl::deny)
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPDENY, 0)));
        dispatcher.register(CommandManager.literal("tpignore").redirect(tpignore).executes(CommandImpl::ignore)
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPIGNORE, 0)));
    }
}
