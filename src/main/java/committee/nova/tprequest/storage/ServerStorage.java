package committee.nova.tprequest.storage;

import com.google.common.collect.ImmutableList;
import committee.nova.tprequest.request.TeleportRequest;
import committee.nova.tprequest.util.Utilities;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.concurrent.CopyOnWriteArrayList;

public class ServerStorage {
    public static final CopyOnWriteArrayList<TeleportRequest> requests = new CopyOnWriteArrayList<>();

    public static void tick(MinecraftServer server) {
        requests.forEach(r -> {
            if (!r.tick()) return;
            final Text timeout = new TranslatableText("msg.tprequest.timeout", r.getSummary(server)).formatted(Formatting.AQUA);
            if (!r.isIgnored())
                Utilities.getPlayer(server, r.getReceiver()).ifPresent(p -> p.sendMessage(timeout, false));
            requests.remove(r);
        });
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
