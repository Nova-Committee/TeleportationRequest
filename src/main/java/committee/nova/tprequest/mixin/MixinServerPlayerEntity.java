package committee.nova.tprequest.mixin;

import com.mojang.authlib.GameProfile;
import committee.nova.tprequest.api.ITeleportable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity implements ITeleportable {
    private int cd;

    public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Override
    public void setTeleportCd(int cd) {
        this.cd = cd;
    }

    @Override
    public int getTeleportCd() {
        return cd;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    private void inject$write(NbtCompound nbt, CallbackInfo ci) {
        final var tag = new NbtCompound();
        tag.putInt("cd", cd);
        nbt.put("tprequest", tag);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    private void inject$read(NbtCompound nbt, CallbackInfo ci) {
        if (!nbt.contains("tprequest")) return;
        cd = nbt.getCompound("tprequest").getInt("cd");
    }

    @Inject(method = "playerTick", at = @At("HEAD"))
    private void inject$tick(CallbackInfo ci) {
        if (cd > 0) cd--;
    }
}
