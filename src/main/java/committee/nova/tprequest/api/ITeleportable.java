package committee.nova.tprequest.api;

public interface ITeleportable {
    void tprequest$setTeleportCd(int cd);

    int tprequest$getTeleportCd();

    default boolean isCoolingDown() {
        return tprequest$getTeleportCd() > 0;
    }
}
