package slimeknights.mantle.recipe.crafting;

import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Builder for a shaped recipe with fallbacks */
@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "fallback")
public class ShapedFallbackRecipeBuilder {
  private final ShapedRecipeBuilder base;
  private final List<ResourceLocation> alternatives = new ArrayList<>();

  /**
   * Adds a single alternative to this recipe
   * @param location  Alternative
   * @return  Builder instance
   */
  public ShapedFallbackRecipeBuilder addAlternative(ResourceLocation location) {
    this.alternatives.add(location);
    return this;
  }

  /**
   * Adds a list of alternatives to this recipe
   * @param locations  Alternative list
   * @return  Builder instance
   */
  public ShapedFallbackRecipeBuilder addAlternatives(Collection<ResourceLocation> locations) {
    this.alternatives.addAll(locations);
    return this;
  }

  /** Builds the recipe using the output as the name */
  public void build(RecipeOutput output) {
    List<ResourceLocation> alts = List.copyOf(alternatives);
    base.save(wrap(output, alts));
  }

  /**
   * Builds the recipe using the given ID
   * @param output  Recipe output
   * @param id      Recipe ID
   */
  public void build(RecipeOutput output, ResourceLocation id) {
    List<ResourceLocation> alts = List.copyOf(alternatives);
    base.save(wrap(output, alts), id);
  }

  private static RecipeOutput wrap(RecipeOutput output, List<ResourceLocation> alts) {
    return new RecipeOutput() {
      @Override
      public void accept(ResourceLocation id, net.minecraft.world.item.crafting.Recipe<?> recipe, AdvancementHolder advancement) {
        ShapedFallbackRecipe fallback;
        if (recipe instanceof ShapedRecipe shaped) {
          fallback = new ShapedFallbackRecipe(shaped, alts);
        } else {
          throw new IllegalStateException("Expected ShapedRecipe, got " + recipe.getClass());
        }
        output.accept(id, fallback, advancement);
      }

      @Override
      public void accept(ResourceLocation id, net.minecraft.world.item.crafting.Recipe<?> recipe, AdvancementHolder advancement, ICondition... conditions) {
        ShapedFallbackRecipe fallback;
        if (recipe instanceof ShapedRecipe shaped) {
          fallback = new ShapedFallbackRecipe(shaped, alts);
        } else {
          throw new IllegalStateException("Expected ShapedRecipe, got " + recipe.getClass());
        }
        output.accept(id, fallback, advancement, conditions);
      }

      @Override
      public net.minecraft.advancements.Advancement.Builder advancement() {
        return output.advancement();
      }
    };
  }
}
