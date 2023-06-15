package committee.nova.tprequest.cfg;

import committee.nova.tprequest.TeleportationRequest;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.List;

@Config(name = TeleportationRequest.MODID)
public class TprConfig implements ConfigData {
    @Comment("Cool-down time (tick) after a successful teleportation request")
    public int tpCd = 600;
    @Comment("Expiration time (tick) of a teleportation request")
    public int expirationTime = 1200;
    @Comment("Short alternatives of /trtpa")
    public List<String> saTpa = List.of("tpa");
    @Comment("Short alternatives of /trtpahere")
    public List<String> saTpahere = List.of("tpahere");
    @Comment("Short alternatives of /trtpcancel")
    public List<String> saTpcancel = List.of("tpcancel");
    @Comment("Short alternatives of /trtpaccept")
    public List<String> saTpaccept = List.of("tpaccept", "tpyes");
    @Comment("Short alternatives of /trtpdeny")
    public List<String> saTpdeny = List.of("tpdeny", "tpno");
    @Comment("Short alternatives of /trtpignore")
    public List<String> saTpignore = List.of("tpignore");
    @Comment("Notification sound to be played after a teleportation. Leave a blank to disable.")
    public String notificationSound = "minecraft:entity.enderman.teleport";
}
