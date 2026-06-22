package slimeknights.mantle.recipe.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.loot.MantleLoot;

/** Inverted form of {@link TagEmptyCondition} as filled is way more common a desire than empty. */
public class TagFilledCondition<T> extends TagCondition<T> implements LootItemCondition {
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static final MapCodec<TagFilledCondition<?>> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
    ResourceLocation.CODEC.optionalFieldOf("registry", Registries.ITEM.location())
      .forGetter(c -> c.getTag().registry().location()),
    ResourceLocation.CODEC.fieldOf("tag").forGetter(c -> c.getTag().location())
  ).apply(inst, (regLoc, tagLoc) ->
    new TagFilledCondition(TagKey.create(ResourceKey.createRegistryKey(regLoc), tagLoc))));

  public TagFilledCondition(TagKey<T> tag) {
    super(tag);
  }

  public TagFilledCondition(ResourceKey<? extends Registry<T>> registry, ResourceLocation name) {
    this(TagKey.create(registry, name));
  }

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }

  @Override
  public LootItemConditionType getType() {
    return MantleLoot.TAG_FILLED;
  }

  @Override
  public boolean test(IContext context) {
    return !context.getTag(tag).isEmpty();
  }

  @Override
  public boolean test(LootContext context) {
    Registry<T> registry = registry(context);
    return registry != null && registry.getTagOrEmpty(tag).iterator().hasNext();
  }
}
