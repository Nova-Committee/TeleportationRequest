package committee.nova.tprequest.cfg;

import committee.nova.tprequest.TeleportationRequest;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.List;

@Config(name = TeleportationRequest.MODID)
public class TprConfig implements ConfigData {
    @Comment("Cool-down time (tick) after a successful teleportation request")
    public double tpCd = 60.0;
    @Comment("Expiration time (tick) of a teleportation request")
    public double expirationTime = 60.0;
    @Comment("Alternatives of /trtpa")
    public List<String> saTpa = List.of("tpa");
    @Comment("Alternatives of /trtpahere")
    public List<String> saTpahere = List.of("tpahere");
    @Comment("Alternatives of /trtpcancel")
    public List<String> saTpcancel = List.of("tpcancel");
    @Comment("Alternatives of /trtpaccept")
    public List<String> saTpaccept = List.of("tpaccept", "tpyes");
    @Comment("Alternatives of /trtpdeny")
    public List<String> saTpdeny = List.of("tpdeny", "tpno");
    @Comment("Alternatives of /trtpignore")
    public List<String> saTpignore = List.of("tpignore");
    @Comment("Alternatives of /trtplist")
    public List<String> saTplist = List.of("tplist");
    @Comment("Notification sound to be played after a teleportation. Leave a blank to disable.")
    public String notificationSound = "minecraft:entity.enderman.teleport";
}
