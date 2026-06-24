package slimeknights.mantle.loot.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import slimeknights.mantle.loot.MantleLoot;

import java.util.List;
import java.util.Set;

/** Variant of {@link net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition} that allows using a tag for block type instead of a block */
@RequiredArgsConstructor
public class BlockTagLootCondition implements LootItemCondition {
  /** Empty properties predicate; matches any block state */
  private static final StatePropertiesPredicate ANY_PROPERTIES = new StatePropertiesPredicate(List.of());

  public static final MapCodec<BlockTagLootCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
    TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(c -> c.tag),
    StatePropertiesPredicate.CODEC.optionalFieldOf("properties", ANY_PROPERTIES).forGetter(c -> c.properties)
  ).apply(inst, BlockTagLootCondition::new));

  private final TagKey<Block> tag;
  private final StatePropertiesPredicate properties;

  public BlockTagLootCondition(TagKey<Block> tag) {
    this(tag, ANY_PROPERTIES);
  }

  public BlockTagLootCondition(TagKey<Block> tag, StatePropertiesPredicate.Builder builder) {
    this(tag, builder.build().orElse(ANY_PROPERTIES));
  }

  @Override
  public boolean test(LootContext context) {
    BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
    return state != null && state.is(tag) && this.properties.matches(state);
  }

  @Override
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return Set.of(LootContextParams.BLOCK_STATE);
  }

  @Override
  public LootItemConditionType getType() {
    return MantleLoot.BLOCK_TAG_CONDITION;
  }
}
