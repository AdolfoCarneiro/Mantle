package slimeknights.mantle.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.item.ILecternBookItem;

/** Packet to open a book on a lectern */
public record OpenLecternBookPacket(BlockPos pos, ItemStack book) implements CustomPacketPayload {
  public static final Type<OpenLecternBookPacket> TYPE =
    new Type<>(ResourceLocation.fromNamespaceAndPath(Mantle.modId, "open_lectern_book"));
  public static final StreamCodec<RegistryFriendlyByteBuf, OpenLecternBookPacket> STREAM_CODEC =
    StreamCodec.composite(
      BlockPos.STREAM_CODEC, OpenLecternBookPacket::pos,
      ItemStack.STREAM_CODEC, OpenLecternBookPacket::book,
      OpenLecternBookPacket::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(OpenLecternBookPacket packet, IPayloadContext context) {
    context.enqueueWork(() -> HandleClient.handle(packet));
  }

  private static class HandleClient {
    private static void handle(OpenLecternBookPacket packet) {
      if (packet.book().getItem() instanceof ILecternBookItem item) {
        item.openLecternScreenClient(packet.pos(), packet.book());
      }
    }
  }
}
