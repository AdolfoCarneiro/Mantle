package slimeknights.mantle;

import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import slimeknights.mantle.util.OffhandCooldownTracker;

public final class MantleCapabilities {
  public static final EntityCapability<OffhandCooldownTracker, Void> OFFHAND_COOLDOWN =
    EntityCapability.createVoid(
      Mantle.getResource("offhand_cooldown"),
      OffhandCooldownTracker.class);

  private MantleCapabilities() {}

  public static void register(RegisterCapabilitiesEvent event) {
    event.registerEntity(OFFHAND_COOLDOWN, EntityType.PLAYER,
      (player, ctx) -> OffhandCooldownTracker.getOrCreate(player));
  }
}
