package committee.nova.tprequest.storage;

import com.google.common.collect.ImmutableList;
import committee.nova.tprequest.request.TeleportRequest;
import committee.nova.tprequest.util.Utilities;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.CopyOnWriteArrayList;

public class ServerStorage {
    public static final CopyOnWriteArrayList<TeleportRequest> requests = new CopyOnWriteArrayList<>();

    public static void tick(MinecraftServer server) {
        final var it = requests.iterator();
        while (it.hasNext()) {
            final var r = it.next();
            if (!r.tick()) continue;
            final Component timeout = Component.translatable("msg.tprequest.timeout", r.getSummary(server)).withStyle(ChatFormatting.AQUA);
            if (!r.isIgnored())
                Utilities.getPlayer(server, r.getReceiver()).ifPresent(p -> p.displayClientMessage(timeout, false));
            it.remove();
        }
    }

    public static boolean addRequest(TeleportRequest request) {
        for (final var r : requests)
            if (r.getSender().equals(request.getSender())
                    || (r.getReceiver().equals(request.getSender()) && !r.isIgnored())) return false;
        return requests.add(request);
    }

    public static ImmutableList<TeleportRequest> getRequestCopied() {
        return ImmutableList.copyOf(requests);
    }
}
