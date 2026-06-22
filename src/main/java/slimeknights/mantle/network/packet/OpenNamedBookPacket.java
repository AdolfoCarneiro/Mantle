package slimeknights.mantle.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.command.client.BookCommand;

public record OpenNamedBookPacket(ResourceLocation book) implements CustomPacketPayload {
  public static final Type<OpenNamedBookPacket> TYPE =
    new Type<>(ResourceLocation.fromNamespaceAndPath(Mantle.modId, "open_named_book"));
  public static final StreamCodec<RegistryFriendlyByteBuf, OpenNamedBookPacket> STREAM_CODEC =
    StreamCodec.composite(
      ResourceLocation.STREAM_CODEC, OpenNamedBookPacket::book,
      OpenNamedBookPacket::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(OpenNamedBookPacket packet, IPayloadContext context) {
    context.enqueueWork(() -> HandleClient.handle(packet));
  }

  private static class HandleClient {
    private static void handle(OpenNamedBookPacket packet) {
      BookData bookData = BookLoader.getBook(packet.book());
      if (bookData != null) {
        bookData.openGui(Component.literal("Book"), "", null, null);
      } else {
        BookCommand.bookNotFound(packet.book());
      }
    }
  }
}
