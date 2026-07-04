package slimeknights.mantle.recipe.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
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

  /**
   * Serializes the given ingredient to JSON, for use in datagen.
   * <p>
   * {@link ItemNameIngredient} cannot round-trip through {@link Ingredient#CODEC} as its {@link #getType()} is
   * datagen only (see class javadoc), so it must be special cased here rather than using the generic ingredient
   * codec, which would otherwise crash trying to look up its (non-existent) {@link IngredientType}. This also
   * recurses into {@link CompoundIngredient} (e.g. from {@code CompoundIngredient.of(...)}), as the generic codec
   * serializes its children the same way it would serialize a top level ingredient, so a compound containing an
   * {@link ItemNameIngredient} child hits the same crash.
   * @param ingredient  Ingredient to serialize, may or may not wrap an {@link ItemNameIngredient}
   * @return  JSON representation of the ingredient
   */
  public static JsonElement serialize(Ingredient ingredient) {
    if (ingredient.isCustom()) {
      ICustomIngredient custom = ingredient.getCustomIngredient();
      if (custom instanceof ItemNameIngredient itemName) {
        return itemName.toJson();
      }
      if (custom instanceof CompoundIngredient compound) {
        JsonArray array = new JsonArray();
        for (Ingredient child : compound.children()) {
          JsonElement childJson = serialize(child);
          if (childJson.isJsonArray()) {
            array.addAll(childJson.getAsJsonArray());
          } else {
            array.add(childJson);
          }
        }
        return array;
      }
    }
    return Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient).getOrThrow();
  }
}
