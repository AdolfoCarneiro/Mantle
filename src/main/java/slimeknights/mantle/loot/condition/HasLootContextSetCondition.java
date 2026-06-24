package slimeknights.mantle.loot.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import slimeknights.mantle.loot.MantleLoot;

import java.util.Objects;

/** Loot condition that only runs if all required values in the given loot context set are present. Good heuristic for using that set. */
public record HasLootContextSetCondition(LootContextParamSet set) implements LootItemCondition {
  /** Codec for serializing this condition */
  public static final MapCodec<HasLootContextSetCondition> CODEC = ResourceLocation.CODEC.xmap(
    HasLootContextSetCondition::byKey,
    condition -> Objects.requireNonNull(LootContextParamSets.getKey(condition.set()), "Unregistered loot LootContextParamSets")
  ).fieldOf("set");

  /** Looks up a {@link LootContextParamSet} by its registered key */
  private static LootContextParamSet byKey(ResourceLocation key) {
    LootContextParamSet set = LootContextParamSets.get(key);
    if (set == null) {
      throw new IllegalArgumentException("Unknown LootContextParamSet " + key);
    }
    return set;
  }

  /** Creates a new builder instance */
  public static Builder builder(LootContextParamSet set) {
    return new Builder(set);
  }

  @Override
  public LootItemConditionType getType() {
    return MantleLoot.HAS_CONTEXT_SET;
  }

  @Override
  public boolean test(LootContext context) {
    for (LootContextParam<?> param : set.getRequired()) {
      if (!context.hasParam(param)) {
        return false;
      }
    }
    return true;
  }

  /** Builder logic for this condition */
  public record Builder(LootContextParamSet set) implements LootItemCondition.Builder {
    @Override
    public LootItemCondition build() {
      return new HasLootContextSetCondition(set);
    }
  }
}
