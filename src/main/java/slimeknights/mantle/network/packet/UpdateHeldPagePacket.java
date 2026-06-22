package slimeknights.mantle.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookHelper;

/** Packet to update the page in a book in the players hand */
public record UpdateHeldPagePacket(InteractionHand hand, String page) implements CustomPacketPayload {
  public static final Type<UpdateHeldPagePacket> TYPE =
    new Type<>(ResourceLocation.fromNamespaceAndPath(Mantle.modId, "update_held_page"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateHeldPagePacket> STREAM_CODEC =
    StreamCodec.composite(
      ByteBufCodecs.idMapper(i -> InteractionHand.values()[i], InteractionHand::ordinal), UpdateHeldPagePacket::hand,
      ByteBufCodecs.stringUtf8(100), UpdateHeldPagePacket::page,
      UpdateHeldPagePacket::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(UpdateHeldPagePacket packet, IPayloadContext context) {
    context.enqueueWork(() -> {
      ServerPlayer player = (ServerPlayer) context.player();
      if (player != null && packet.page() != null) {
        ItemStack stack = player.getItemInHand(packet.hand());
        if (!stack.isEmpty()) {
          BookHelper.writeSavedPageToBook(stack, packet.page());
        }
      }
    });
  }
}
