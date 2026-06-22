package slimeknights.mantle.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookHelper;

/** Packet to update the page in a book in the players inventory */
public record UpdateInventoryPagePacket(int slot, String page) implements CustomPacketPayload {
  public static final Type<UpdateInventoryPagePacket> TYPE =
    new Type<>(ResourceLocation.fromNamespaceAndPath(Mantle.modId, "update_inventory_page"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateInventoryPagePacket> STREAM_CODEC =
    StreamCodec.composite(
      ByteBufCodecs.VAR_INT, UpdateInventoryPagePacket::slot,
      ByteBufCodecs.stringUtf8(100), UpdateInventoryPagePacket::page,
      UpdateInventoryPagePacket::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(UpdateInventoryPagePacket packet, IPayloadContext context) {
    context.enqueueWork(() -> {
      ServerPlayer player = (ServerPlayer) context.player();
      if (player != null && packet.page() != null && packet.slot() >= 0) {
        ItemStack stack = player.getInventory().getItem(packet.slot());
        if (!stack.isEmpty()) {
          BookHelper.writeSavedPageToBook(stack, packet.page());
        }
      }
    });
  }
}
