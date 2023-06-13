package committee.nova.tprequest.api;

public interface ITeleportable {
    void setTeleportCd(int cd);

    int getTeleportCd();

    default boolean isCoolingDown() {
        return getTeleportCd() > 0;
    }
}
