package slimeknights.mantle.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

/** Send-helper façade over NeoForge PacketDistributor */
public class MantleNetwork {
  private MantleNetwork() {}

  public static <T extends CustomPacketPayload> void sendToServer(T payload) {
    PacketDistributor.sendToServer(payload);
  }

  public static <T extends CustomPacketPayload> void sendTo(ServerPlayer player, T payload) {
    PacketDistributor.sendToPlayer(player, payload);
  }

  public static <T extends CustomPacketPayload> void sendToTrackingAndSelf(Entity entity, T payload) {
    PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
  }

  public static <T extends CustomPacketPayload> void sendToTracking(Entity entity, T payload) {
    PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
  }
}
