package committee.nova.tprequest.permnode;

import java.util.Locale;

public enum PermNode {
    COMMON_HELP,
    COMMON_TPA,
    COMMON_TPAHERE,
    COMMON_TPCANCEL,
    COMMON_TPACCEPT,
    COMMON_TPDENY,
    COMMON_TPIGNORE,
    ADMIN_RELOAD;

    public String getNode() {
        return "tprequest." + this.name().toLowerCase(Locale.ENGLISH).replace('_', '.');
    }
}
