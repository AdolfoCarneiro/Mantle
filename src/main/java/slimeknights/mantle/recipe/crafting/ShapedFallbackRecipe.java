package slimeknights.mantle.recipe.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import slimeknights.mantle.recipe.MantleRecipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class ShapedFallbackRecipe extends ShapedRecipe {

  private final List<ResourceLocation> alternatives;
  private List<CraftingRecipe> alternativeCache;

  public ShapedFallbackRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification, List<ResourceLocation> alternatives) {
    super(group, category, pattern, result, showNotification);
    this.alternatives = alternatives;
  }

  public ShapedFallbackRecipe(ShapedRecipe base, List<ResourceLocation> alternatives) {
    super(base.group, base.category, base.pattern, base.result, base.showNotification);
    this.alternatives = alternatives;
  }

  @Override
  public boolean matches(CraftingInput input, Level world) {
    if (!super.matches(input, world)) {
      return false;
    }
    if (alternativeCache == null) {
      var manager = world.getRecipeManager();
      alternativeCache = alternatives.stream()
        .map(manager::byKey)
        .filter(Optional::isPresent)
        .map(o -> o.get().value())
        .filter(recipe -> {
          Class<?> clazz = recipe.getClass();
          return clazz == ShapedRecipe.class || clazz == ShapelessRecipe.class;
        })
        .map(recipe -> (CraftingRecipe) recipe)
        .collect(Collectors.toList());
    }
    return this.alternativeCache.stream().noneMatch(recipe -> recipe.matches(input, world));
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return MantleRecipes.CRAFTING_SHAPED_FALLBACK.get();
  }

  public static class Serializer implements RecipeSerializer<ShapedFallbackRecipe> {
    public static final MapCodec<ShapedFallbackRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
      Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
      CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.UNKNOWN).forGetter(r -> r.category),
      ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
      ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result),
      Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(r -> r.showNotification),
      ResourceLocation.CODEC.listOf().fieldOf("alternatives").forGetter(r -> r.alternatives)
    ).apply(inst, ShapedFallbackRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedFallbackRecipe> STREAM_CODEC = StreamCodec.composite(
      ShapedRecipe.Serializer.STREAM_CODEC, r -> r,
      ByteBufCodecs.collection(ArrayList::new, ResourceLocation.STREAM_CODEC), r -> r.alternatives,
      ShapedFallbackRecipe::new);

    @Override
    public MapCodec<ShapedFallbackRecipe> codec() {
      return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ShapedFallbackRecipe> streamCodec() {
      return STREAM_CODEC;
    }
  }
}
