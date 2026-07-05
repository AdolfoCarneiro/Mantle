package slimeknights.mantle.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.recipe.MantleRecipes;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Ingredient matching an item with a specific set of NBT, comparing NBT as a partial subset.
 * <p>
 * Replaces the Forge 1.20.1 {@code forge:nbt} ingredient (the {@code NBTIngredient} type), which
 * NeoForge 1.21.1 dropped in favor of {@code neoforge:components}. Keeps the original
 * {@code {"item": <id>, "nbt": {...}}} schema. On 1.21.1, mod NBT lives in the
 * {@link DataComponents#CUSTOM_DATA} component, so matching/building targets that component's tag.
 */
public class NBTIngredient implements ICustomIngredient {
  /** Codec for JSON serialization (inner content, excluding the type field) */
  public static final MapCodec<NBTIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(i -> i.item),
    CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(i -> Optional.ofNullable(i.nbt))
  ).apply(inst, (item, nbt) -> new NBTIngredient(item, nbt.orElse(null))));

  /** StreamCodec for network serialization */
  public static final StreamCodec<RegistryFriendlyByteBuf, NBTIngredient> STREAM_CODEC = StreamCodec.of(
    (buf, ing) -> {
      buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(ing.item));
      buf.writeBoolean(ing.nbt != null);
      if (ing.nbt != null) {
        buf.writeNbt(ing.nbt);
      }
    },
    buf -> {
      Item item = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
      CompoundTag nbt = buf.readBoolean() ? buf.readNbt() : null;
      return new NBTIngredient(item, nbt);
    }
  );

  private final Item item;
  @Nullable
  private final CompoundTag nbt;

  protected NBTIngredient(Item item, @Nullable CompoundTag nbt) {
    this.item = item;
    this.nbt = nbt;
  }

  /** Creates an ingredient matching the given item with the given NBT */
  public static NBTIngredient of(ItemLike item, @Nullable CompoundTag nbt) {
    return new NBTIngredient(item.asItem(), nbt);
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    if (stack == null || stack.getItem() != item) {
      return false;
    }
    if (nbt == null) {
      return true;
    }
    // 1.21.1 stores mod NBT under the CUSTOM_DATA component; compare against that tag.
    // partial subset match: every key in the expected NBT must be present and equal in the stack.
    CompoundTag stackNbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    return NbtUtils.compareNbt(nbt, stackNbt, true);
  }

  @Override
  public Stream<ItemStack> getItems() {
    ItemStack stack = new ItemStack(item);
    if (nbt != null) {
      stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }
    return Stream.of(stack);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return MantleRecipes.NBT_INGREDIENT.get();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof NBTIngredient that && item == that.item && Objects.equals(nbt, that.nbt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(item, nbt);
  }
}
