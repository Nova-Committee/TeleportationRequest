package committee.nova.tprequest.permnode;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

import java.util.Locale;

public enum PermNode {
    COMMON_HELP((p, u, c) -> true),
    COMMON_TPA((p, u, c) -> true),
    COMMON_TPAHERE((p, u, c) -> true),
    COMMON_TPCANCEL((p, u, c) -> true),
    COMMON_TPACCEPT((p, u, c) -> true),
    COMMON_TPDENY((p, u, c) -> true),
    COMMON_TPIGNORE((p, u, c) -> true),
    ADMIN_RELOAD((p, u, c) -> p != null && p.hasPermissions(2));

    private final PermissionNode<Boolean> node;

    PermNode(PermissionNode.PermissionResolver<Boolean> resolver) {
        this.node = new PermissionNode<>(getId(), PermissionTypes.BOOLEAN, resolver);
    }

    public ResourceLocation getId() {
        return new ResourceLocation("tprequest", this.name().toLowerCase(Locale.ENGLISH).replace('_', '.'));
    }

    public PermissionNode<Boolean> getNode() {
        return node;
    }
}
