package io.github._4drian3d.signedvelocity.minestom.listener;

import io.github._4drian3d.signedvelocity.common.queue.SignedQueue;
import io.github._4drian3d.signedvelocity.common.queue.SignedResult;
import io.github._4drian3d.signedvelocity.minestom.SignedVelocity;
import net.minestom.server.event.player.PlayerPluginMessageEvent;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public final class PluginMessageListener implements Consumer<PlayerPluginMessageEvent> {
    private final SignedVelocity extension;

    public PluginMessageListener(final SignedVelocity extension) {
        this.extension = extension;
    }

    @Override
    public void accept(PlayerPluginMessageEvent event) {
        if (!Objects.equals(event.getIdentifier(), SignedVelocity.CHANNEL)) {
            return;
        }
        @SuppressWarnings("UnstableApiUsage")
        final DataInput input = new DataInput(event.getMessage());

        final UUID playerId = UUID.fromString(input.readUTF());
        final String source = input.readUTF();
        final String result = input.readUTF();

        final SignedQueue queue = switch (source) {
            case "COMMAND_RESULT" -> extension.commandQueue();
            case "CHAT_RESULT" -> extension.chatQueue();
            default -> throw new IllegalArgumentException("Invalid source " + source);
        };
        final SignedResult resulted = switch (result) {
            case "CANCEL" -> SignedResult.cancel();
            case "MODIFY" -> SignedResult.modify(input.readUTF());
            case "ALLOWED" -> SignedResult.allowed();
            default -> throw new IllegalArgumentException("Invalid result " + result);
        };
        queue.dataFrom(playerId).complete(resulted);
    }

    private record DataInput(DataInputStream dataStream) {
        DataInput(final byte[] data) {
            this(new DataInputStream(new ByteArrayInputStream(data)));
        }

        public String readUTF() {
            try {
                return dataStream.readUTF();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
