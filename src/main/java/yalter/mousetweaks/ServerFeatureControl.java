package yalter.mousetweaks;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.nio.charset.StandardCharsets;

public class ServerFeatureControl {

    private static boolean mouseDisabled, wheelDisabled;

    public static boolean isMouseDisabled() {
        return mouseDisabled;
    }

    public static boolean isWheelDisabled() {
        return wheelDisabled;
    }

    public static void apply(ServerFeatureControlPayload payload) {
        for (String token : payload.value().strip().split("[\\s,]+")) {
            switch (token.toLowerCase()) {
                case "mouse" -> mouseDisabled = false;
                case "!mouse" -> mouseDisabled = true;

                case "wheel" -> wheelDisabled = false;
                case "!wheel" -> wheelDisabled = true;

                case "" -> {
                    mouseDisabled = true;
                    wheelDisabled = true;
                }

                default -> { }
            }
        }
    }

    public static void reset() {
        mouseDisabled = false;
        wheelDisabled = false;
    }

    public record ServerFeatureControlPayload(String value) implements CustomPacketPayload {

        public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "feature_control");
        public static final Type<ServerFeatureControlPayload> TYPE = new Type<>(ID);

        @Override
        public @NonNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static final StreamCodec<RegistryFriendlyByteBuf, ServerFeatureControlPayload> CODEC =
                new StreamCodec<>() {
                    @Override
                    public ServerFeatureControlPayload decode(RegistryFriendlyByteBuf input) {
                        int length = input.readableBytes();

                        byte[] bytes = new byte[length];
                        input.readBytes(bytes);

                        return new ServerFeatureControlPayload(new String(bytes, StandardCharsets.UTF_8));
                    }

                    // Codecs require both methods, and even though we don't send it, it's helpful in tests
                    @Override
                    public void encode(RegistryFriendlyByteBuf output, ServerFeatureControlPayload value) {
                        byte[] bytes = value.value().getBytes(StandardCharsets.UTF_8);
                        output.writeBytes(bytes);
                    }
                };
    }

}
