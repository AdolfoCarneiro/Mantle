package slimeknights.mantle.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.IngredientType;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Stream;

/** Ingredient that shows all potion variants on the displayed item list */
public class PotionDisplayIngredient extends ItemIngredient {
  public static final RecordLoadable<PotionDisplayIngredient> LOADABLE = RecordLoadable.create(ItemsField.INSTANCE, TAG_FIELD, PotionDisplayIngredient::new);
  public static final MapCodec<PotionDisplayIngredient> CODEC = LOADABLE.mapCodec(TypedMap.EMPTY);
  public static final StreamCodec<RegistryFriendlyByteBuf, PotionDisplayIngredient> STREAM_CODEC = LOADABLE;

  protected PotionDisplayIngredient(List<Item> items, @Nullable TagKey<Item> tag) {
    super(items, tag);
  }

  public static PotionDisplayIngredient of(List<ItemLike> items) {
    return new PotionDisplayIngredient(toItem(items), null);
  }

  public static PotionDisplayIngredient of(ItemLike... items) {
    return of(List.of(items));
  }

  public static PotionDisplayIngredient of(TagKey<Item> tag) {
    return new PotionDisplayIngredient(List.of(), tag);
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public Stream<ItemStack> getItems() {
    ItemStack[] baseStacks = super.getItems().toArray(ItemStack[]::new);
    return BuiltInRegistries.POTION.stream()
      .filter(pot -> !pot.getEffects().isEmpty())
      .flatMap(pot -> {
        Holder<Potion> holder = BuiltInRegistries.POTION.wrapAsHolder(pot);
        return java.util.Arrays.stream(baseStacks).map(base -> PotionContents.createItemStack(base.getItem(), holder));
      });
  }

  @Override
  public IngredientType<?> getType() {
    return MantleRecipes.POTION_DISPLAY_INGREDIENT.get();
  }
}
