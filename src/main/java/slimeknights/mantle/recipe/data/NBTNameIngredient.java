package slimeknights.mantle.recipe.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * Ingredient for a NBT sensitive item from another mod, should never be used outside datagen.
 * Note: item NBT is deprecated in 1.21+; prefer DataComponent-based matching.
 */
public class NBTNameIngredient implements ICustomIngredient {
  private final ResourceLocation name;
  @Nullable
  private final CompoundTag nbt;

  protected NBTNameIngredient(ResourceLocation name, @Nullable CompoundTag nbt) {
    this.name = name;
    this.nbt = nbt;
  }

  public static NBTNameIngredient from(ResourceLocation name, CompoundTag nbt) {
    return new NBTNameIngredient(name, nbt);
  }

  public static NBTNameIngredient from(ResourceLocation name) {
    return new NBTNameIngredient(name, null);
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

  public JsonElement toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("item", name.toString());
    if (nbt != null) {
      json.addProperty("nbt", nbt.toString());
    }
    return json;
  }
}
