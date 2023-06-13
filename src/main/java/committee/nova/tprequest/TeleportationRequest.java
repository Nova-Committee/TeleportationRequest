package committee.nova.tprequest;

import committee.nova.tprequest.command.argument.TeleportRequestArgument;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

@Mod("tprequest")
public class TeleportationRequest {
    private static final ForgeConfigSpec CFG;
    private static final ForgeConfigSpec.IntValue tpCd;
    private static final ForgeConfigSpec.IntValue expirationTime;
    private static final ForgeConfigSpec.BooleanValue shortAlternatives;
    private static final ForgeConfigSpec.ConfigValue<String> notificationSound;


    static {
        final var builder = new ForgeConfigSpec.Builder();
        builder.comment("Teleportation Request Configuration").push("general");
        tpCd = builder.comment("Cool-down time (tick) after a successful teleportation request")
                .defineInRange("tpCd", 600, 0, Integer.MAX_VALUE);
        expirationTime = builder.comment("Expiration time (tick) of a teleportation request")
                .defineInRange("expirationTime", 1200, 0, Integer.MAX_VALUE);
        shortAlternatives = builder.comment("Set to true to register short alternatives of teleportation request commands, e.g. /trtpa -> /tpa")
                .define("shortAlternatives", true);
        notificationSound = builder.comment("Notification sound to be played after a teleportation. Leave a blank to disable")
                .define("notificationSound", "minecraft:entity.enderman.teleport");
        builder.pop();
        CFG = builder.build();
    }

    public TeleportationRequest() {
        ArgumentTypes.register("teleport_request", TeleportRequestArgument.class, new EmptyArgumentSerializer<>(TeleportRequestArgument::instance));
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CFG);
    }

    public static int getTpCd() {
        return tpCd.get();
    }

    public static int getExpirationTime() {
        return expirationTime.get();
    }

    public static boolean shouldRegisterShortAlternatives() {
        return shortAlternatives.get();
    }

    public static Optional<SoundEvent> getNotificationSound() {
        try {
            return Optional.ofNullable(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(notificationSound.get())));
        } catch (ResourceLocationException ignored) {
            return Optional.empty();
        }
    }

    public static boolean reload() {
        CFG.afterReload();
        return true;
    }
}
