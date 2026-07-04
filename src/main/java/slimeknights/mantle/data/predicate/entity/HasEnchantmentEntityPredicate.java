package slimeknights.mantle.data.predicate.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;

/**
 * Predicate that checks if the given entity has the given enchantment on any of their equipment
 * <p>
 * Stores a {@link ResourceKey} rather than a resolved {@link Enchantment}: enchantments are a data-driven (datapack)
 * registry in 1.21, so resolving eagerly (e.g. to build this predicate from datagen code, not from parsed JSON)
 * requires registry access that may not be a fully patched snapshot, which can throw decoding the enchantment's own
 * codec (needs item tags not always present in that snapshot). A {@link ResourceKey} needs no registry access at
 * all to construct or serialize; resolution is deferred to {@link #matches}, which already has real registry access
 * via the entity's own level.
 */
public record HasEnchantmentEntityPredicate(ResourceKey<Enchantment> enchantment) implements LivingEntityPredicate {
  public static final RecordLoadable<HasEnchantmentEntityPredicate> LOADER = RecordLoadable.create(Loadables.resourceKey(Registries.ENCHANTMENT).requiredField("enchantment", HasEnchantmentEntityPredicate::enchantment), HasEnchantmentEntityPredicate::new);

  @Override
  public boolean matches(LivingEntity entity) {
    Holder<Enchantment> holder = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
    return EnchantmentHelper.getEnchantmentLevel(holder, entity) > 0;
  }

  @Override
  public RecordLoadable<HasEnchantmentEntityPredicate> getLoader() {
    return LOADER;
  }
}
