package committee.nova.tprequest.mixin;

import com.mojang.authlib.GameProfile;
import committee.nova.tprequest.api.ITeleportable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements ITeleportable {
    @Unique
    private int tprequest$cd;

    public MixinServerPlayer(Level world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Override
    public void tprequest$setTeleportCd(int cd) {
        this.tprequest$cd = cd;
    }

    @Override
    public int tprequest$getTeleportCd() {
        return tprequest$cd;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void inject$write(CompoundTag nbt, CallbackInfo ci) {
        nbt.putInt("tprequest_cd", tprequest$cd);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void inject$read(CompoundTag nbt, CallbackInfo ci) {
        tprequest$cd = nbt.getInt("tprequest_cd");
    }

    @Inject(method = "doTick", at = @At("HEAD"))
    private void inject$tick(CallbackInfo ci) {
        if (tprequest$cd > 0) tprequest$cd--;
    }
}
