package slimeknights.mantle.recipe.ingredient;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.field.RecordField;
import slimeknights.mantle.data.loadable.field.UnsyncedField;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/** Abstract ingredient that matches a list of items or a tag, mirroring the vanilla syntax */
public abstract class ItemIngredient implements ICustomIngredient {
  /** Field for the item tag */
  protected static final LoadableField<TagKey<Item>,ItemIngredient> TAG_FIELD = new UnsyncedField<>(Loadables.ITEM_TAG.nullableField("tag", i -> i.tag));

  protected final List<Item> items;
  @Nullable
  protected final TagKey<Item> tag;

  protected ItemIngredient(List<Item> items, @Nullable TagKey<Item> tag) {
    this.items = items;
    this.tag = tag;
  }

  /** Maps the list to a list of items */
  protected static List<Item> toItem(List<ItemLike> items) {
    return items.stream().map(ItemLike::asItem).toList();
  }

  @Override
  public boolean test(ItemStack stack) {
    return stack != null && (items.contains(stack.getItem()) || tag != null && stack.is(tag));
  }

  @Override
  public Stream<ItemStack> getItems() {
    Stream<ItemStack> fromItems = items.stream().map(ItemStack::new);
    Stream<ItemStack> fromTag = tag == null ? Stream.empty() :
      RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, tag).map(ItemStack::new);
    return Stream.concat(fromItems, fromTag);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ItemIngredient that)) return false;
    return items.equals(that.items) && Objects.equals(tag, that.tag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items, tag);
  }

  /** Custom field that syncs the item tag as items to the client */
  public enum ItemsField implements RecordField<List<Item>,ItemIngredient> {
    INSTANCE;

    private static final Loadable<List<Item>> ITEM_LIST = Loadables.ITEM.list(ArrayLoadable.COMPACT_OR_EMPTY);

    @Override
    public List<Item> get(JsonObject json, TypedMap context) {
      return ITEM_LIST.getOrDefault(json, "item", List.of(), context);
    }

    @Override
    public void serialize(ItemIngredient parent, JsonObject json) {
      if (!parent.items.isEmpty()) {
        json.add("item", ITEM_LIST.serialize(parent.items));
      }
    }

    @Override
    public List<Item> decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
      return ITEM_LIST.decode(buffer, context);
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, ItemIngredient parent) {
      // expand tag on server so client receives flat item list
      Stream<Item> fromItems = parent.items.stream();
      Stream<Item> fromTag = parent.tag == null ? Stream.empty() :
        RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, parent.tag);
      ITEM_LIST.encode(buffer, Stream.concat(fromItems, fromTag).toList());
    }
  }
}
