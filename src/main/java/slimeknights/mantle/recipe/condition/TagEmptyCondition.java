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

/** Condition that checks when a tag is empty. Works for any registry */
public class TagEmptyCondition<T> extends TagCondition<T> implements LootItemCondition {
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static final MapCodec<TagEmptyCondition<?>> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
    ResourceLocation.CODEC.optionalFieldOf("registry", Registries.ITEM.location())
      .forGetter(c -> c.getTag().registry().location()),
    ResourceLocation.CODEC.fieldOf("tag").forGetter(c -> c.getTag().location())
  ).apply(inst, (regLoc, tagLoc) ->
    new TagEmptyCondition(TagKey.create(ResourceKey.createRegistryKey(regLoc), tagLoc))));

  public TagEmptyCondition(TagKey<T> tag) {
    super(tag);
  }

  public TagEmptyCondition(ResourceKey<? extends Registry<T>> registry, ResourceLocation name) {
    this(TagKey.create(registry, name));
  }

  @Override
  public MapCodec<? extends ICondition> codec() {
    return CODEC;
  }

  @Override
  public LootItemConditionType getType() {
    return MantleLoot.TAG_EMPTY;
  }

  @Override
  public boolean test(IContext context) {
    return context.getTag(tag).isEmpty();
  }

  @Override
  public boolean test(LootContext context) {
    Registry<T> registry = registry(context);
    return registry != null && !registry.getTagOrEmpty(tag).iterator().hasNext();
  }
}
