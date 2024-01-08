package committee.nova.tprequest.event.handler;

import committee.nova.tprequest.TeleportationRequest;
import committee.nova.tprequest.command.init.CommandInit;
import committee.nova.tprequest.event.TeleportationEvent;
import committee.nova.tprequest.permnode.PermNode;
import committee.nova.tprequest.storage.ServerStorage;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;

import java.util.Arrays;

@Mod.EventBusSubscriber
public class ForgeEventHandler {
    @SubscribeEvent
    public static void onAddNode(PermissionGatherEvent.Nodes event) {
        Arrays.stream(PermNode.values()).map(PermNode::getNode).forEach(event::addNodes);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ServerStorage.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        ServerStorage.requests.clear();
    }

    @SubscribeEvent
    public static void registerCmd(RegisterCommandsEvent event) {
        CommandInit.init(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onTp(TeleportationEvent event) {
        TeleportationRequest.getNotificationSound().ifPresent(r -> event.getSender().playNotifySound(r, SoundSource.PLAYERS, 1.0F, 1.0F));
    }
}
