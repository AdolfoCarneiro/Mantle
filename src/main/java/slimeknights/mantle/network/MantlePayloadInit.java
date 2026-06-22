package slimeknights.mantle.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferPacket;
import slimeknights.mantle.network.packet.DropLecternBookPacket;
import slimeknights.mantle.network.packet.OpenLecternBookPacket;
import slimeknights.mantle.network.packet.OpenNamedBookPacket;
import slimeknights.mantle.network.packet.SwingArmPacket;
import slimeknights.mantle.network.packet.UpdateHeldPagePacket;
import slimeknights.mantle.network.packet.UpdateInventoryPagePacket;
import slimeknights.mantle.network.packet.UpdateLecternPagePacket;

public final class MantlePayloadInit {
  private MantlePayloadInit() {}

  public static void register(RegisterPayloadHandlersEvent event) {
    PayloadRegistrar registrar = event.registrar("2");
    registrar.playToClient(OpenLecternBookPacket.TYPE, OpenLecternBookPacket.STREAM_CODEC, OpenLecternBookPacket::handle);
    registrar.playToClient(SwingArmPacket.TYPE, SwingArmPacket.STREAM_CODEC, SwingArmPacket::handleClient);
    registrar.playToClient(OpenNamedBookPacket.TYPE, OpenNamedBookPacket.STREAM_CODEC, OpenNamedBookPacket::handle);
    registrar.playToClient(FluidContainerTransferPacket.TYPE, FluidContainerTransferPacket.STREAM_CODEC, FluidContainerTransferPacket::handle);
    registrar.playToServer(UpdateHeldPagePacket.TYPE, UpdateHeldPagePacket.STREAM_CODEC, UpdateHeldPagePacket::handle);
    registrar.playToServer(UpdateInventoryPagePacket.TYPE, UpdateInventoryPagePacket.STREAM_CODEC, UpdateInventoryPagePacket::handle);
    registrar.playToServer(UpdateLecternPagePacket.TYPE, UpdateLecternPagePacket.STREAM_CODEC, UpdateLecternPagePacket::handle);
    registrar.playToServer(DropLecternBookPacket.TYPE, DropLecternBookPacket.STREAM_CODEC, DropLecternBookPacket::handle);
  }
}
