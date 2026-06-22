package slimeknights.mantle.recipe.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Ingredient for a non-NBT sensitive item from another mod, should never be used outside datagen
 */
public class ItemNameIngredient implements ICustomIngredient {
  private final List<ResourceLocation> names;

  protected ItemNameIngredient(List<ResourceLocation> names) {
    this.names = names;
  }

  public static ItemNameIngredient from(List<ResourceLocation> names) {
    return new ItemNameIngredient(names);
  }

  public static ItemNameIngredient from(ResourceLocation... names) {
    return from(Arrays.asList(names));
  }

  @Override
  public boolean test(ItemStack stack) {
    throw new UnsupportedOperationException("Datagen only");
  }

  @Override
  public Stream<ItemStack> getItems() {
    return Stream.empty();
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    throw new UnsupportedOperationException("Datagen only");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ItemNameIngredient that)) return false;
    return names.equals(that.names);
  }

  @Override
  public int hashCode() {
    return names.hashCode();
  }

  private static JsonObject forName(ResourceLocation name) {
    JsonObject json = new JsonObject();
    json.addProperty("item", name.toString());
    return json;
  }

  public JsonElement toJson() {
    if (names.size() == 1) {
      return forName(names.get(0));
    }
    JsonArray array = new JsonArray();
    for (ResourceLocation name : names) {
      array.add(forName(name));
    }
    return array;
  }
}
