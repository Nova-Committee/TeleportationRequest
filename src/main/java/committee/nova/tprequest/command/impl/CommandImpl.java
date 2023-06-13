package committee.nova.tprequest.command.impl;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import committee.nova.tprequest.TeleportationRequest;
import committee.nova.tprequest.api.ITeleportable;
import committee.nova.tprequest.request.TeleportRequest;
import committee.nova.tprequest.storage.ServerStorage;
import committee.nova.tprequest.util.Utilities;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class CommandImpl {
    public static int accept(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final CommandSourceStack src = ctx.getSource();
        final ServerPlayer srcPlayer = src.getPlayerOrException();
        final var l = ServerStorage.getRequestCopied();
        TeleportRequest request = null;
        final List<TeleportRequest> shouldShow = new ArrayList<>();
        final MinecraftServer server = src.getServer();
        for (final var r : l)
            if (r.getReceiver().equals(srcPlayer.getUUID())) {
                if (request == null) {
                    request = r;
                    if (r.isIgnored()) shouldShow.add(r);
                } else {
                    if (shouldShow.isEmpty()) shouldShow.add(request);
                    shouldShow.add(r);
                }
            }
        if (!shouldShow.isEmpty()) {
            src.sendSuccess(new TranslatableComponent("msg.tprequest.following"), false);
            shouldShow.forEach(t -> src.sendSuccess(t.getSummary(server).append(t.isIgnored() ?
                    new TranslatableComponent("status.tprequest.ignored") : TextComponent.EMPTY).setStyle(Style.EMPTY
                    .withColor(t.isIgnored() ? ChatFormatting.GRAY : ChatFormatting.YELLOW)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpaccept " + t.getId().toString()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("selection.tprequest.accept.info",
                            t.getSummary(server))))), false));
            return 1;
        }
        if (request != null) {
            if (!request.execute(server)) {
                src.sendFailure(new TranslatableComponent("msg.tprequest.not_present"));
                return 0;
            }
            TeleportRequest finalRequest = request;
            Utilities.getPlayer(server, request.getSender()).ifPresent(p -> {
                p.displayClientMessage(new TranslatableComponent("msg.tprequest.accepted",
                        finalRequest.getSummary(server)).withStyle(ChatFormatting.GREEN), false);
                ((ITeleportable) p).setTeleportCd(TeleportationRequest.getTpCd());
            });
            src.sendSuccess(new TranslatableComponent("msg.tprequest.accepted", request.getSummary(server)).withStyle(ChatFormatting.GREEN), false);
            ServerStorage.requests.remove(request);
            return 1;
        }
        src.sendFailure(new TranslatableComponent("msg.tprequest.notfound", ""));
        return 0;
    }

    public static int deny(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final CommandSourceStack src = ctx.getSource();
        final ServerPlayer srcPlayer = src.getPlayerOrException();
        final ImmutableList<TeleportRequest> l = ServerStorage.getRequestCopied();
        TeleportRequest request = null;
        final List<TeleportRequest> shouldShow = new ArrayList<>();
        final MinecraftServer server = src.getServer();
        for (final var r : l)
            if (r.getReceiver().equals(srcPlayer.getUUID())) {
                if (request == null) {
                    request = r;
                    if (r.isIgnored()) shouldShow.add(r);
                } else {
                    if (shouldShow.isEmpty()) shouldShow.add(request);
                    shouldShow.add(r);
                }
            }
        if (!shouldShow.isEmpty()) {
            src.sendSuccess(new TranslatableComponent("msg.tprequest.following"), false);
            shouldShow.forEach(t -> src.sendSuccess(t.getSummary(server).append(t.isIgnored() ?
                    new TranslatableComponent("status.tprequest.ignored") : TextComponent.EMPTY).setStyle(Style.EMPTY
                    .withColor(t.isIgnored() ? ChatFormatting.GRAY : ChatFormatting.YELLOW)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpdeny " + t.getId().toString()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("selection.tprequest.deny.info",
                            t.getSummary(server))))), false));
            return 1;
        }
        if (request != null) {
            if (!ServerStorage.requests.remove(request)) {
                src.sendFailure(new TranslatableComponent("msg.tprequest.already_removed", request.getSummary(server)));
                return 0;
            }
            TeleportRequest finalRequest = request;
            Utilities.getPlayer(server, request.getSender()).ifPresent(s -> s.displayClientMessage(new TranslatableComponent("msg.tprequest.denied",
                    finalRequest.getSummary(server)).withStyle(ChatFormatting.RED), false));
            src.sendSuccess(new TranslatableComponent("msg.tprequest.denied", request.getSummary(server)).withStyle(ChatFormatting.YELLOW), false);
            return 1;
        }
        src.sendFailure(new TranslatableComponent("msg.tprequest.notfound", ""));
        return 0;
    }

    public static int ignore(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final CommandSourceStack src = ctx.getSource();
        final ServerPlayer srcPlayer = src.getPlayerOrException();
        final ImmutableList<TeleportRequest> l = ServerStorage.getRequestCopied();
        TeleportRequest request = null;
        final List<TeleportRequest> shouldShow = new ArrayList<>();
        for (final var r : l)
            if (r.getReceiver().equals(srcPlayer.getUUID()) && !r.isIgnored()) {
                if (request == null) request = r;
                else {
                    if (shouldShow.isEmpty()) shouldShow.add(request);
                    shouldShow.add(r);
                }
            }
        final MinecraftServer server = src.getServer();
        if (!shouldShow.isEmpty()) {
            src.sendSuccess(new TranslatableComponent("msg.tprequest.following"), false);
            shouldShow.forEach(t -> src.sendSuccess(t.getSummary(server).setStyle(Style.EMPTY
                    .withColor(ChatFormatting.YELLOW)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpignore " + t.getId().toString()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("selection.tprequest.ignore.info",
                            t.getSummary(server))))), false));
            return 1;
        }
        if (request == null) {
            src.sendFailure(new TranslatableComponent("msg.tprequest.notfound", ""));
            return 0;
        }
        request.setIgnored(true);
        src.sendSuccess(new TranslatableComponent("msg.tprequest.ignored", request.getSummary(server)).withStyle(ChatFormatting.YELLOW), false);
        return 1;
    }

    public static int cancel(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final CommandSourceStack src = ctx.getSource();
        final ServerPlayer srcPlayer = src.getPlayerOrException();
        final var l = ServerStorage.getRequestCopied();
        TeleportRequest request = null;
        final List<TeleportRequest> shouldShow = new ArrayList<>();
        final MinecraftServer server = src.getServer();
        for (final var r : l)
            if (r.getReceiver().equals(srcPlayer.getUUID())) {
                if (request == null) request = r;
                else {
                    if (shouldShow.isEmpty()) shouldShow.add(request);
                    shouldShow.add(r);
                }
            }
        if (!shouldShow.isEmpty()) {
            src.sendSuccess(new TranslatableComponent("msg.tprequest.following"), false);
            shouldShow.forEach(t -> src.sendSuccess(t.getSummary(server).setStyle(Style.EMPTY
                    .withColor(ChatFormatting.YELLOW)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpcancel " + t.getId().toString()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("selection.tprequest.cancel.info",
                            t.getSummary(server))))), false));
            return 1;
        }
        if (request != null) {
            if (!ServerStorage.requests.remove(request)) {
                src.sendFailure(new TranslatableComponent("msg.tprequest.already_removed", request.getSummary(server)));
                return 0;
            }
            TeleportRequest finalRequest = request;
            Utilities.getPlayer(server, request.getReceiver()).ifPresent(s -> s.displayClientMessage(new TranslatableComponent("msg.tprequest.cancelled",
                    finalRequest.getSummary(server)).withStyle(ChatFormatting.GRAY), false));
            src.sendSuccess(new TranslatableComponent("msg.tprequest.cancelled", request.getSummary(server)).withStyle(ChatFormatting.YELLOW), false);
            return 1;
        }
        src.sendFailure(new TranslatableComponent("msg.tprequest.notfound", ""));
        return 0;
    }
}
