package committee.nova.tprequest.command.init;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.CommandDispatcher;
import committee.nova.tprequest.TeleportationRequest;
import committee.nova.tprequest.api.ITeleportable;
import committee.nova.tprequest.command.impl.CommandImpl;
import committee.nova.tprequest.permnode.PermNode;
import committee.nova.tprequest.request.TeleportRequest;
import committee.nova.tprequest.request.TeleportRequest.Here;
import committee.nova.tprequest.request.TeleportRequest.To;
import committee.nova.tprequest.storage.ServerStorage;
import committee.nova.tprequest.util.Utilities;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

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

    public static void init(CommandDispatcher<CommandSourceStack> dispatcher) {
        final var tpa = dispatcher.register(Commands.literal("trtpa").then(
                Commands.argument("player", EntityArgument.player())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPA)).executes(ctx -> {
                            final CommandSourceStack src = ctx.getSource();
                            final ServerPlayer sender = src.getPlayerOrException();
                            final ServerPlayer receiver = EntityArgument.getPlayer(ctx, "player");
                            if (receiver.equals(sender) && Utilities.isProduction()) {
                                src.sendFailure(Component.translatable("msg.tprequest.self"));
                                return 0;
                            }
                            final ITeleportable t = (ITeleportable) sender;
                            if (t.isCoolingDown()) {
                                src.sendFailure(Component.translatable("msg.tprequest.cd", t.tprequest$getTeleportCd()));
                                return 0;
                            }
                            final var request = new To(sender.getUUID(), receiver.getUUID());
                            final int timeout = request.getExpiration();
                            final boolean sent = ServerStorage.addRequest(request);
                            if (!sent) {
                                src.sendFailure(Component.translatable("msg.tprequest.existed"));
                                return 0;
                            }
                            final String id = request.getId().toString();
                            src.sendSuccess(() -> Component.translatable("msg.tprequest.sent", timeout)
                                    .withStyle(ChatFormatting.GREEN), false);
                            final Component summary = request.getSummary(src.getServer());
                            src.sendSuccess(() -> Component.translatable("selection.tprequest.cancel").setStyle(Style.EMPTY
                                    .withColor(ChatFormatting.GRAY)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpcancel " + id))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.translatable("selection.tprequest.cancel.info", summary)))), false);
                            receiver.displayClientMessage(Component.translatable("msg.tprequest.info.to", sender.getName())
                                    .withStyle(ChatFormatting.YELLOW), false);
                            receiver.displayClientMessage(Component.translatable("msg.tprequest.respond.format",
                                    Component.translatable("selection.tprequest.accept").setStyle(Style.EMPTY
                                            .withColor(ChatFormatting.GREEN)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpaccept " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    Component.translatable("selection.tprequest.accept.info", summary)))),
                                    Component.translatable("selection.tprequest.deny").setStyle(Style.EMPTY
                                            .withColor(ChatFormatting.RED)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpdeny " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    Component.translatable("selection.tprequest.deny.info", summary)))),
                                    Component.translatable("selection.tprequest.ignore").setStyle(Style.EMPTY
                                            .withColor(ChatFormatting.GRAY)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpignore " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    Component.translatable("selection.tprequest.ignore.info", summary))))
                            ), false);
                            return 1;
                        })).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPA))
        );
        final var tpahere = dispatcher.register(Commands.literal("trtpahere").then(
                Commands.argument("player", EntityArgument.player())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPAHERE)).executes(ctx -> {
                            final CommandSourceStack src = ctx.getSource();
                            final ServerPlayer sender = src.getPlayerOrException();
                            final ServerPlayer receiver = EntityArgument.getPlayer(ctx, "player");
                            if (receiver.equals(sender) && Utilities.isProduction()) {
                                src.sendFailure(Component.translatable("msg.tprequest.self"));
                                return 0;
                            }
                            final ITeleportable t = (ITeleportable) sender;
                            if (t.isCoolingDown()) {
                                src.sendFailure(Component.translatable("msg.tprequest.cd", t.tprequest$getTeleportCd()));
                                return 0;
                            }
                            final var request = new Here(sender.getUUID(), receiver.getUUID());
                            final int timeout = request.getExpiration();
                            final boolean sent = ServerStorage.addRequest(request);
                            if (!sent) {
                                src.sendFailure(Component.translatable("msg.tprequest.existed"));
                                return 0;
                            }
                            final String id = request.getId().toString();
                            src.sendSuccess(() -> Component.translatable("msg.tprequest.sent", timeout).withStyle(ChatFormatting.GREEN), false);
                            final Component summary = request.getSummary(src.getServer());
                            src.sendSuccess(() -> Component.translatable("selection.tprequest.cancel").setStyle(Style.EMPTY
                                    .withColor(ChatFormatting.GRAY)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpcancel " + id))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.translatable("selection.tprequest.cancel.info", summary)))), false);
                            receiver.displayClientMessage(Component.translatable("msg.tprequest.info.here", sender.getName())
                                    .withStyle(ChatFormatting.YELLOW), false);
                            receiver.displayClientMessage(Component.translatable("msg.tprequest.respond.format",
                                    Component.translatable("selection.tprequest.accept").setStyle(Style.EMPTY
                                            .withColor(ChatFormatting.GREEN)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpaccept " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    Component.translatable("selection.tprequest.accept.info", summary)))),
                                    Component.translatable("selection.tprequest.deny").setStyle(Style.EMPTY
                                            .withColor(ChatFormatting.RED)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpdeny " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    Component.translatable("selection.tprequest.deny.info", summary)))),
                                    Component.translatable("selection.tprequest.ignore").setStyle(Style.EMPTY
                                            .withColor(ChatFormatting.GRAY)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpignore " + id))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                    Component.translatable("selection.tprequest.ignore.info", summary))))
                            ), false);
                            return 1;
                        })).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPAHERE))
        );
        final var tpcancel = dispatcher.register(Commands.literal("trtpcancel").then(
                Commands.argument("id", UuidArgument.uuid())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPCANCEL)).executes(ctx -> {
                            final CommandSourceStack src = ctx.getSource();
                            final TeleportRequest request = Utilities.parseRequest(UuidArgument.getUuid(ctx, "id"));
                            final ServerPlayer srcPlayer = src.getPlayerOrException();
                            if (!request.getSender().equals(srcPlayer.getUUID())) {
                                src.sendFailure(Component.translatable("msg.tprequest.notfound", ""));
                                return 0;
                            }
                            if (!ServerStorage.requests.remove(request)) {
                                src.sendFailure(Component.translatable("msg.tprequest.already_removed"));
                                return 0;
                            }
                            final MinecraftServer server = src.getServer();
                            if (!request.isIgnored())
                                Utilities.getPlayer(server, request.getReceiver()).ifPresent(s -> s.displayClientMessage(
                                        Component.translatable("msg.tprequest.cancelled", request.getSummary(server)).withStyle(ChatFormatting.GRAY), false));
                            src.sendSuccess(() -> Component.translatable("msg.tprequest.cancelled", request.getSummary(server))
                                    .withStyle(ChatFormatting.YELLOW), false);
                            return 1;
                        })
        ).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPCANCEL)).executes(CommandImpl::cancel));
        final var tpaccept = dispatcher.register(Commands.literal("trtpaccept").then(
                Commands.argument("id", UuidArgument.uuid())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPACCEPT)).executes(ctx -> {
                            final CommandSourceStack src = ctx.getSource();
                            final TeleportRequest request = Utilities.parseRequest(UuidArgument.getUuid(ctx, "id"));
                            final ServerPlayer srcPlayer = src.getPlayerOrException();
                            if (!request.getReceiver().equals(srcPlayer.getUUID())) {
                                src.sendFailure(Component.translatable("msg.tprequest.notfound", ""));
                                return 0;
                            }
                            final MinecraftServer server = src.getServer();
                            if (!request.execute(server)) {
                                src.sendFailure(Component.translatable("msg.tprequest.not_present"));
                                return 0;
                            }
                            Utilities.getPlayer(server, request.getSender()).ifPresent(p -> {
                                p.displayClientMessage(Component.translatable("msg.tprequest.accepted",
                                        request.getSummary(server)).withStyle(ChatFormatting.GREEN), false);
                                ((ITeleportable) p).tprequest$setTeleportCd(TeleportationRequest.getTpCd());
                            });
                            src.sendSuccess(() -> Component.translatable("msg.tprequest.accepted", request.getSummary(server)).withStyle(ChatFormatting.GREEN), false);
                            ServerStorage.requests.remove(request);
                            return 1;
                        })
        ).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPACCEPT)).executes(CommandImpl::accept));
        final var tpdeny = dispatcher.register(Commands.literal("trtpdeny").then(
                Commands.argument("id", UuidArgument.uuid())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPDENY)).executes(ctx -> {
                            final CommandSourceStack src = ctx.getSource();
                            final TeleportRequest request = Utilities.parseRequest(UuidArgument.getUuid(ctx, "id"));
                            final ServerPlayer srcPlayer = src.getPlayerOrException();
                            if (!request.getReceiver().equals(srcPlayer.getUUID())) {
                                src.sendFailure(Component.translatable("msg.tprequest.notfound", ""));
                                return 0;
                            }
                            if (!ServerStorage.requests.remove(request)) {
                                src.sendFailure(Component.translatable("msg.tprequest.already_removed"));
                                return 0;
                            }
                            final MinecraftServer server = src.getServer();
                            Utilities.getPlayer(server, request.getSender()).ifPresent(s -> s.displayClientMessage(Component.translatable("msg.tprequest.denied",
                                    request.getSummary(server)).withStyle(ChatFormatting.RED), false));
                            src.sendSuccess(() -> Component.translatable("msg.tprequest.denied", request.getSummary(server)).withStyle(ChatFormatting.YELLOW), false);
                            return 1;
                        })
        ).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPDENY)).executes(CommandImpl::deny));
        final var tpignore = dispatcher.register(Commands.literal("trtpignore").then(
                Commands.argument("id", UuidArgument.uuid())
                        .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPIGNORE)).executes(ctx -> {
                            final CommandSourceStack src = ctx.getSource();
                            final TeleportRequest request = Utilities.parseRequest(UuidArgument.getUuid(ctx, "id"));
                            final ServerPlayer srcPlayer = src.getPlayerOrException();
                            if (!request.getReceiver().equals(srcPlayer.getUUID())) {
                                src.sendFailure(Component.translatable("msg.tprequest.notfound", ""));
                                return 0;
                            }
                            request.setIgnored(true);
                            final MinecraftServer server = src.getServer();
                            src.sendSuccess(() -> Component.translatable("msg.tprequest.ignored", request.getSummary(server)).withStyle(ChatFormatting.YELLOW), false);
                            return 1;
                        })
        ).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPIGNORE)).executes(CommandImpl::ignore));
        final boolean shortAlternatives = TeleportationRequest.shouldRegisterShortAlternatives();
        dispatcher.register(Commands.literal("tprequest")
                .then(Commands.literal("help").executes(ctx -> {
                    final CommandSourceStack src = ctx.getSource();
                    for (final var e : cmds.entrySet()) {
                        final String key = "desc.tprequest." + e.getKey();
                        src.sendSuccess(() -> Component.translatable(key, e.getKey()), false);
                        if (shortAlternatives) src.sendSuccess(() -> Component.translatable(key, e.getValue()), false);
                    }
                    return 1;
                }).requires(p -> Utilities.checkPerm(p, PermNode.COMMON_HELP)))
                .then(Commands.literal("reload").executes(ctx -> {
                    final boolean success = TeleportationRequest.reload();
                    ctx.getSource().sendSuccess(() -> Component.translatable("msg.tprequest.reload." + (success ? "success" : "failure")
                            .formatted(success ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
                    return success ? 1 : 0;
                }).requires(p -> Utilities.checkPerm(p, PermNode.ADMIN_RELOAD)))
                .requires(p -> true));
        if (!shortAlternatives) return;
        dispatcher.register(Commands.literal("tpa").redirect(tpa)
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPA)));
        dispatcher.register(Commands.literal("tpahere").redirect(tpahere)
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPAHERE)));
        dispatcher.register(Commands.literal("tpcancel").redirect(tpcancel).executes(CommandImpl::cancel)
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPCANCEL)));
        dispatcher.register(Commands.literal("tpaccept").redirect(tpaccept).executes(CommandImpl::accept)
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPACCEPT)));
        dispatcher.register(Commands.literal("tpdeny").redirect(tpdeny).executes(CommandImpl::deny)
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPDENY)));
        dispatcher.register(Commands.literal("tpignore").redirect(tpignore).executes(CommandImpl::ignore)
                .requires(p -> Utilities.checkPerm(p, PermNode.COMMON_TPIGNORE)));
    }
}
