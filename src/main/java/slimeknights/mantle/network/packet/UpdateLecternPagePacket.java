package slimeknights.mantle.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.book.BookHelper;
import slimeknights.mantle.util.BlockEntityHelper;

/** Packet to update the book page in a lectern */
public record UpdateLecternPagePacket(BlockPos pos, String page) implements CustomPacketPayload {
  public static final Type<UpdateLecternPagePacket> TYPE =
    new Type<>(ResourceLocation.fromNamespaceAndPath(Mantle.modId, "update_lectern_page"));
  public static final StreamCodec<RegistryFriendlyByteBuf, UpdateLecternPagePacket> STREAM_CODEC =
    StreamCodec.composite(
      BlockPos.STREAM_CODEC, UpdateLecternPagePacket::pos,
      ByteBufCodecs.stringUtf8(100), UpdateLecternPagePacket::page,
      UpdateLecternPagePacket::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(UpdateLecternPagePacket packet, IPayloadContext context) {
    context.enqueueWork(() -> {
      ServerPlayer player = (ServerPlayer) context.player();
      if (player != null && packet.page() != null) {
        Level world = player.getCommandSenderWorld();
        BlockEntityHelper.get(LecternBlockEntity.class, world, packet.pos()).ifPresent(te -> {
          ItemStack stack = te.getBook();
          if (!stack.isEmpty()) {
            BookHelper.writeSavedPageToBook(stack, packet.page());
          }
        });
      }
    });
  }
}
