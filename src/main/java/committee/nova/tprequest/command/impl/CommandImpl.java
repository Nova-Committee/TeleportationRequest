package committee.nova.tprequest.command.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import committee.nova.tprequest.TeleportationRequest;
import committee.nova.tprequest.api.ITeleportable;
import committee.nova.tprequest.request.TeleportRequest;
import committee.nova.tprequest.storage.ServerStorage;
import committee.nova.tprequest.util.Utilities;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class CommandImpl {
    public static int accept(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final ServerCommandSource src = ctx.getSource();
        final ServerPlayerEntity srcPlayer = src.getPlayer();
        final var l = ServerStorage.getRequestCopied();
        TeleportRequest request = null;
        final List<TeleportRequest> shouldShow = new ArrayList<>();
        final MinecraftServer server = src.getServer();
        for (final var r : l)
            if (r.getReceiver().equals(srcPlayer.getUuid())) {
                if (request == null) {
                    request = r;
                    if (r.isIgnored()) shouldShow.add(r);
                } else {
                    if (shouldShow.isEmpty()) shouldShow.add(request);
                    shouldShow.add(r);
                }
            }
        if (!shouldShow.isEmpty()) {
            src.sendFeedback(new TranslatableText("msg.tprequest.following"), false);
            shouldShow.forEach(t -> src.sendFeedback(t.getSummary(server).append(t.isIgnored() ?
                    new TranslatableText("status.tprequest.ignored") : LiteralText.EMPTY).setStyle(Style.EMPTY
                    .withColor(t.isIgnored() ? Formatting.GRAY : Formatting.YELLOW)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpaccept " + t.getId().toString()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("selection.tprequest.accept.info",
                            t.getSummary(server))))), false));
            return 1;
        }
        if (request != null) {
            if (!request.execute(server)) {
                src.sendError(new TranslatableText("msg.tprequest.not_present"));
                return 0;
            }
            TeleportRequest finalRequest = request;
            Utilities.getPlayer(server, request.getSender()).ifPresent(p -> {
                p.sendMessage(new TranslatableText("msg.tprequest.accepted",
                        finalRequest.getSummary(server)).formatted(Formatting.GREEN), false);
                ((ITeleportable) p).setTeleportCd(TeleportationRequest.getTpCd());
            });
            src.sendFeedback(new TranslatableText("msg.tprequest.accepted", request.getSummary(server)).formatted(Formatting.GREEN), false);
            ServerStorage.requests.remove(request);
            return 1;
        }
        src.sendError(new TranslatableText("msg.tprequest.notfound"));
        return 0;
    }

    public static int deny(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final ServerCommandSource src = ctx.getSource();
        final ServerPlayerEntity srcPlayer = src.getPlayer();
        final ImmutableList<TeleportRequest> l = ServerStorage.getRequestCopied();
        TeleportRequest request = null;
        final List<TeleportRequest> shouldShow = new ArrayList<>();
        final MinecraftServer server = src.getServer();
        for (final var r : l)
            if (r.getReceiver().equals(srcPlayer.getUuid())) {
                if (request == null) {
                    request = r;
                    if (r.isIgnored()) shouldShow.add(r);
                } else {
                    if (shouldShow.isEmpty()) shouldShow.add(request);
                    shouldShow.add(r);
                }
            }
        if (!shouldShow.isEmpty()) {
            src.sendFeedback(new TranslatableText("msg.tprequest.following"), false);
            shouldShow.forEach(t -> src.sendFeedback(t.getSummary(server).append(t.isIgnored() ?
                    new TranslatableText("status.tprequest.ignored") : LiteralText.EMPTY).setStyle(Style.EMPTY
                    .withColor(t.isIgnored() ? Formatting.GRAY : Formatting.YELLOW)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpdeny " + t.getId().toString()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("selection.tprequest.deny.info",
                            t.getSummary(server))))), false));
            return 1;
        }
        if (request != null) {
            if (!ServerStorage.requests.remove(request)) {
                src.sendError(new TranslatableText("msg.tprequest.already_removed", request.getSummary(server)));
                return 0;
            }
            TeleportRequest finalRequest = request;
            Utilities.getPlayer(server, request.getSender()).ifPresent(s -> s.sendMessage(new TranslatableText("msg.tprequest.denied",
                    finalRequest.getSummary(server)).formatted(Formatting.RED), false));
            src.sendFeedback(new TranslatableText("msg.tprequest.denied", request.getSummary(server)).formatted(Formatting.YELLOW), false);
            return 1;
        }
        src.sendError(new TranslatableText("msg.tprequest.notfound"));
        return 0;
    }

    public static int ignore(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final ServerCommandSource src = ctx.getSource();
        final ServerPlayerEntity srcPlayer = src.getPlayer();
        final ImmutableList<TeleportRequest> l = ServerStorage.getRequestCopied();
        TeleportRequest request = null;
        final List<TeleportRequest> shouldShow = new ArrayList<>();
        for (final var r : l)
            if (r.getReceiver().equals(srcPlayer.getUuid()) && !r.isIgnored()) {
                if (request == null) request = r;
                else {
                    if (shouldShow.isEmpty()) shouldShow.add(request);
                    shouldShow.add(r);
                }
            }
        final MinecraftServer server = src.getServer();
        if (!shouldShow.isEmpty()) {
            src.sendFeedback(new TranslatableText("msg.tprequest.following"), false);
            shouldShow.forEach(t -> src.sendFeedback(t.getSummary(server).setStyle(Style.EMPTY
                    .withColor(Formatting.YELLOW)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpignore " + t.getId().toString()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("selection.tprequest.ignore.info",
                            t.getSummary(server))))), false));
            return 1;
        }
        if (request == null) {
            src.sendError(new TranslatableText("msg.tprequest.notfound"));
            return 0;
        }
        request.setIgnored(true);
        src.sendFeedback(new TranslatableText("msg.tprequest.ignored", request.getSummary(server)).formatted(Formatting.YELLOW), false);
        return 1;
    }

    public static int cancel(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final ServerCommandSource src = ctx.getSource();
        final ServerPlayerEntity srcPlayer = src.getPlayer();
        final var l = ServerStorage.getRequestCopied();
        TeleportRequest request = null;
        final List<TeleportRequest> shouldShow = new ArrayList<>();
        final MinecraftServer server = src.getServer();
        for (final var r : l)
            if (r.getReceiver().equals(srcPlayer.getUuid())) {
                if (request == null) request = r;
                else {
                    if (shouldShow.isEmpty()) shouldShow.add(request);
                    shouldShow.add(r);
                }
            }
        if (!shouldShow.isEmpty()) {
            src.sendFeedback(new TranslatableText("msg.tprequest.following"), false);
            shouldShow.forEach(t -> src.sendFeedback(t.getSummary(server).setStyle(Style.EMPTY
                    .withColor(Formatting.YELLOW)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpcancel " + t.getId().toString()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("selection.tprequest.cancel.info",
                            t.getSummary(server))))), false));
            return 1;
        }
        if (request != null) {
            if (!ServerStorage.requests.remove(request)) {
                src.sendError(new TranslatableText("msg.tprequest.already_removed", request.getSummary(server)));
                return 0;
            }
            TeleportRequest finalRequest = request;
            Utilities.getPlayer(server, request.getReceiver()).ifPresent(s -> s.sendMessage(new TranslatableText("msg.tprequest.cancelled",
                    finalRequest.getSummary(server)).formatted(Formatting.GRAY), false));
            src.sendFeedback(new TranslatableText("msg.tprequest.cancelled", request.getSummary(server)).formatted(Formatting.YELLOW), false);
            return 1;
        }
        src.sendError(new TranslatableText("msg.tprequest.notfound"));
        return 0;
    }
}
