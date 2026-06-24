package slimeknights.mantle.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
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
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/** Simple ingredient checking for an item with a specific potion */
public class PotionIngredient extends ItemIngredient {
  public static final RecordLoadable<PotionIngredient> LOADABLE = RecordLoadable.create(
    ItemsField.INSTANCE, TAG_FIELD,
    Loadables.POTION.requiredField("potion", i -> i.potion),
    PotionIngredient::new);
  public static final MapCodec<PotionIngredient> CODEC = LOADABLE.mapCodec(TypedMap.EMPTY);
  public static final StreamCodec<RegistryFriendlyByteBuf, PotionIngredient> STREAM_CODEC = LOADABLE;

  private final Potion potion;

  protected PotionIngredient(List<Item> items, @Nullable TagKey<Item> itemTag, Potion potion) {
    super(items, itemTag);
    this.potion = potion;
  }

  public static PotionIngredient of(Potion potion, List<ItemLike> items) {
    return new PotionIngredient(toItem(items), null, potion);
  }

  public static PotionIngredient of(Potion potion, ItemLike... items) {
    return of(potion, Arrays.asList(items));
  }

  public static PotionIngredient of(Potion potion, TagKey<Item> tag) {
    return new PotionIngredient(List.of(), tag, potion);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    if (stack == null || !super.test(stack)) return false;
    PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
    if (contents == null) return false;
    return contents.potion().isPresent() && contents.potion().get().value() == potion;
  }

  @Override
  public Stream<ItemStack> getItems() {
    Holder<Potion> holder = BuiltInRegistries.POTION.wrapAsHolder(potion);
    Stream<ItemStack> fromItems = items.stream().map(item -> PotionContents.createItemStack(item, holder));
    Stream<ItemStack> fromTag = tag == null ? Stream.empty() :
      RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, tag).map(item -> PotionContents.createItemStack(item, holder));
    return Stream.concat(fromItems, fromTag);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return MantleRecipes.POTION_INGREDIENT.get();
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o)) return false;
    return o instanceof PotionIngredient that && potion == that.potion;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), potion);
  }
}
