package slimeknights.mantle.fluid.transfer;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import slimeknights.mantle.Mantle;

import java.util.HashSet;
import java.util.Set;

/** Packet to sync fluid container transfer */
public record FluidContainerTransferPacket(Set<Item> items) implements CustomPacketPayload {
  public static final Type<FluidContainerTransferPacket> TYPE =
    new Type<>(ResourceLocation.fromNamespaceAndPath(Mantle.modId, "fluid_container_transfer"));
  public static final StreamCodec<RegistryFriendlyByteBuf, FluidContainerTransferPacket> STREAM_CODEC =
    StreamCodec.composite(
      ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.registry(Registries.ITEM)),
      FluidContainerTransferPacket::items,
      FluidContainerTransferPacket::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }

  public static void handle(FluidContainerTransferPacket packet, IPayloadContext context) {
    context.enqueueWork(() -> FluidContainerTransferManager.INSTANCE.setContainerItems(packet.items()));
  }
}
