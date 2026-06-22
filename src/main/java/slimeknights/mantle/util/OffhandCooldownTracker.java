package slimeknights.mantle.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.MantleCapabilities;
import slimeknights.mantle.network.MantleNetwork;
import slimeknights.mantle.network.packet.SwingArmPacket;

import javax.annotation.Nullable;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * Logic to handle offhand having its own cooldown
 */
public class OffhandCooldownTracker {
  public static final ResourceLocation KEY = Mantle.getResource("offhand_cooldown");

  /** @deprecated use {@link #get(Player)} */
  @Deprecated(forRemoval = true)
  public static final Function<OffhandCooldownTracker, Float> COOLDOWN_TRACKER = OffhandCooldownTracker::getCooldown;

  private static final WeakHashMap<Player, OffhandCooldownTracker> INSTANCES = new WeakHashMap<>();

  @Nullable
  private final Player player;
  private int lastCooldown = 0;
  private int attackReady = 0;
  private int enabled = 0;

  public OffhandCooldownTracker(@Nullable Player player) {
    this.player = player;
  }

  public static OffhandCooldownTracker getOrCreate(Player player) {
    return INSTANCES.computeIfAbsent(player, OffhandCooldownTracker::new);
  }

  @Nullable
  public static OffhandCooldownTracker get(Player player) {
    return player.getCapability(MantleCapabilities.OFFHAND_COOLDOWN);
  }

  private int getTicksExisted() {
    if (player == null) return 0;
    return player.tickCount;
  }

  @Deprecated(forRemoval = true)
  public boolean isEnabled() {
    return enabled > 0;
  }

  @Deprecated(forRemoval = true)
  public void setEnabled(boolean enable) {
    if (enable) {
      enabled++;
    } else {
      enabled--;
    }
  }

  public void applyCooldown(int cooldown) {
    this.lastCooldown = cooldown;
    this.attackReady = getTicksExisted() + cooldown;
  }

  public float getCooldown() {
    int ticksExisted = getTicksExisted();
    if (ticksExisted > this.attackReady || this.lastCooldown == 0) {
      return 1.0f;
    }
    return Mth.clamp((this.lastCooldown + ticksExisted - this.attackReady) / (float) this.lastCooldown, 0f, 1f);
  }

  public boolean isAttackReady() {
    return getTicksExisted() + this.lastCooldown > this.attackReady;
  }


  /* Helpers */

  public static float getCooldown(Player player) {
    OffhandCooldownTracker tracker = get(player);
    return tracker != null ? tracker.getCooldown() : 1.0f;
  }

  public static void applyCooldown(Player player, int cooldown) {
    OffhandCooldownTracker tracker = get(player);
    if (tracker != null) {
      tracker.applyCooldown(cooldown);
    }
  }

  public static boolean isAttackReady(Player player) {
    OffhandCooldownTracker tracker = get(player);
    return tracker == null || tracker.isAttackReady();
  }

  public static void applyCooldown(Player player, float attackSpeed, int cooldownTime) {
    applyCooldown(player, Math.round(cooldownTime / attackSpeed));
  }

  public static void swingHand(LivingEntity entity, InteractionHand hand, boolean updateSelf) {
    if (!entity.swinging || entity.swingTime >= entity.getCurrentSwingDuration() / 2 || entity.swingTime < 0) {
      entity.swingTime = -1;
      entity.swinging = true;
      entity.swingingArm = hand;
      if (!entity.level().isClientSide) {
        SwingArmPacket packet = new SwingArmPacket(entity, hand);
        if (updateSelf) {
          MantleNetwork.sendToTrackingAndSelf(entity, packet);
        } else {
          MantleNetwork.sendToTracking(entity, packet);
        }
      }
    }
  }
}
