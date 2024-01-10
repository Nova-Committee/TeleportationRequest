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
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
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
            src.sendSuccess(() -> Component.literal("找到以下请求："), false);
            shouldShow.forEach(t -> src.sendSuccess(() -> t.getSummary(server).append(t.isIgnored() ?
                    Component.literal("（已忽略）") : Component.empty()).setStyle(Style.EMPTY
                    .withColor(t.isIgnored() ? ChatFormatting.GRAY : ChatFormatting.YELLOW)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpaccept " + t.getId().toString()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("接受传送请求%s",
                            t.getSummary(server))))), false));
            return 1;
        }
        if (request != null) {
            if (!request.execute(server)) {
                src.sendFailure(Component.literal("未找到传送请求的发送者或接收者……"));
                return 0;
            }
            TeleportRequest finalRequest = request;
            Utilities.getPlayer(server, request.getSender()).ifPresent(p -> {
                p.displayClientMessage(Component.translatable("传送请求%s已接受，传送中……",
                        finalRequest.getSummary(server)).withStyle(ChatFormatting.GREEN), false);
                ((ITeleportable) p).tprequest$setTeleportCd(TeleportationRequest.getTpCd());
            });
            src.sendSuccess(() -> Component.translatable("传送请求%s已接受，传送中……", finalRequest.getSummary(server)).withStyle(ChatFormatting.GREEN), false);
            ServerStorage.requests.remove(request);
            return 1;
        }
        src.sendFailure(Component.literal("未找到匹配的传送请求……"));
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
            src.sendSuccess(() -> Component.literal("找到以下请求："), false);
            shouldShow.forEach(t -> src.sendSuccess(() -> t.getSummary(server).append(t.isIgnored() ?
                    Component.literal("（已忽略）") : Component.empty()).setStyle(Style.EMPTY
                    .withColor(t.isIgnored() ? ChatFormatting.GRAY : ChatFormatting.YELLOW)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpdeny " + t.getId().toString()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("拒绝传送请求%s",
                            t.getSummary(server))))), false));
            return 1;
        }
        if (request != null) {
            if (!ServerStorage.requests.remove(request)) {
                src.sendFailure(Component.translatable("传送请求%s已被移除……", request.getSummary(server)));
                return 0;
            }
            TeleportRequest finalRequest = request;
            Utilities.getPlayer(server, request.getSender()).ifPresent(s -> s.displayClientMessage(Component.translatable("传送请求%s已拒绝。",
                    finalRequest.getSummary(server)).withStyle(ChatFormatting.RED), false));
            src.sendSuccess(() -> Component.translatable("传送请求%s已拒绝。", finalRequest.getSummary(server)).withStyle(ChatFormatting.YELLOW), false);
            return 1;
        }
        src.sendFailure(Component.literal("未找到匹配的传送请求……"));
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
            src.sendSuccess(() -> Component.literal("找到以下请求："), false);
            shouldShow.forEach(t -> src.sendSuccess(() -> t.getSummary(server).setStyle(Style.EMPTY
                    .withColor(ChatFormatting.YELLOW)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpignore " + t.getId().toString()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("忽略传送请求%s",
                            t.getSummary(server))))), false));
            return 1;
        }
        if (request == null) {
            src.sendFailure(Component.literal("未找到匹配的传送请求……"));
            return 0;
        }
        request.setIgnored(true);
        TeleportRequest finalRequest = request;
        src.sendSuccess(() -> Component.translatable("传送请求%s已忽略。", finalRequest.getSummary(server)).withStyle(ChatFormatting.YELLOW), false);
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
            src.sendSuccess(() -> Component.literal("找到以下请求："), false);
            shouldShow.forEach(t -> src.sendSuccess(() -> t.getSummary(server).setStyle(Style.EMPTY
                    .withColor(ChatFormatting.YELLOW)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/trtpcancel " + t.getId().toString()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("取消传送请求%s",
                            t.getSummary(server))))), false));
            return 1;
        }
        if (request != null) {
            if (!ServerStorage.requests.remove(request)) {
                src.sendFailure(Component.translatable("传送请求%s已被移除……", request.getSummary(server)));
                return 0;
            }
            TeleportRequest finalRequest = request;
            Utilities.getPlayer(server, request.getReceiver()).ifPresent(s -> s.displayClientMessage(Component.translatable("传送请求%s已取消。",
                    finalRequest.getSummary(server)).withStyle(ChatFormatting.GRAY), false));
            src.sendSuccess(() -> Component.translatable("传送请求%s已取消。", finalRequest.getSummary(server)).withStyle(ChatFormatting.YELLOW), false);
            return 1;
        }
        src.sendFailure(Component.literal("未找到匹配的传送请求……"));
        return 0;
    }
}
