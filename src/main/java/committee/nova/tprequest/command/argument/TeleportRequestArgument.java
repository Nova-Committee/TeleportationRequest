package committee.nova.tprequest.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import committee.nova.tprequest.request.TeleportRequest;
import committee.nova.tprequest.storage.ServerStorage;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.UUID;

public class TeleportRequestArgument implements ArgumentType<TeleportRequest> {
    public static final DynamicCommandExceptionType INVALID_UUID =
            new DynamicCommandExceptionType(uuid -> Component.translatable("msg.tprequest.invalid_uuid", uuid));
    public static final DynamicCommandExceptionType REQUEST_NOT_FOUND =
            new DynamicCommandExceptionType(uuid -> Component.translatable("msg.tprequest.notfound", uuid));

    public static TeleportRequestArgument instance() {
        return new TeleportRequestArgument();
    }

    public static TeleportRequest getRequest(final CommandContext<?> context, final String name) {
        return context.getArgument(name, TeleportRequest.class);
    }

    private TeleportRequestArgument() {
    }

    @Override
    public TeleportRequest parse(StringReader reader) throws CommandSyntaxException {
        final String str = reader.readString();
        final UUID id;
        try {
            id = UUID.fromString(str);
        } catch (IllegalArgumentException ignored) {
            throw INVALID_UUID.create(str);
        }
        final List<TeleportRequest> l = ServerStorage.getRequestCopied();
        for (final TeleportRequest r : l) if (r.getId().equals(id)) return r;
        throw REQUEST_NOT_FOUND.create(id.toString());
    }

    @MethodsReturnNonnullByDefault
    @ParametersAreNonnullByDefault
    public static class Serializer implements ArgumentTypeInfo<TeleportRequestArgument, Serializer.Template> {
        @Override
        public void serializeToNetwork(Template p_235375_, FriendlyByteBuf p_235376_) {

        }

        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf p_235377_) {
            return new Template();
        }

        @Override
        public void serializeToJson(Template p_235373_, JsonObject p_235374_) {

        }

        @Override
        public Template unpack(TeleportRequestArgument p_235372_) {
            return new Template();
        }

        public class Template implements ArgumentTypeInfo.Template<TeleportRequestArgument> {
            @Override
            public TeleportRequestArgument instantiate(CommandBuildContext ctx) {
                return TeleportRequestArgument.instance();
            }

            @Override
            public ArgumentTypeInfo<TeleportRequestArgument, ?> type() {
                return Serializer.this;
            }
        }
    }
}
