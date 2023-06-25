package committee.nova.tprequest.mixin;

import committee.nova.tprequest.TeleportationRequest;
import committee.nova.tprequest.api.ITeleportable;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class MixinPlayerManager {
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void inject$onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        final ITeleportable t = (ITeleportable) player;
        final int cd = TeleportationRequest.getTpCd();
        if (t.getTeleportCd() > cd) t.setTeleportCd(cd);
    }
}
