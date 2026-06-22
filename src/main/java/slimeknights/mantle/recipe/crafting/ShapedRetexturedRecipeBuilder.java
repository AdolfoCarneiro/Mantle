package slimeknights.mantle.recipe.crafting;

import lombok.RequiredArgsConstructor;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "fromShaped")
public class ShapedRetexturedRecipeBuilder {
  private final ShapedRecipeBuilder parent;
  private Ingredient texture = null;
  private boolean matchAll = false;

  /**
   * Sets the texture source to the given ingredient
   * @param texture Ingredient to use for texture
   * @return Builder instance
   */
  public ShapedRetexturedRecipeBuilder setSource(Ingredient texture) {
    this.texture = texture;
    return this;
  }

  /**
   * Sets the texture source to the given tag
   * @param tag Tag to use for texture
   * @return Builder instance
   */
  public ShapedRetexturedRecipeBuilder setSource(TagKey<Item> tag) {
    return setSource(Ingredient.of(tag));
  }

  /**
   * Sets the match first property on the recipe.
   * If set, uses the first ingredient match for the texture. If unset, all items must be the same.
   * @return Builder instance
   */
  public ShapedRetexturedRecipeBuilder setMatchAll() {
    this.matchAll = true;
    return this;
  }

  /** Builds the recipe with the default name */
  public void build(RecipeOutput output) {
    validate();
    Ingredient tex = texture;
    boolean all = matchAll;
    parent.save(wrap(output, tex, all));
  }

  /**
   * Builds the recipe using the given location
   * @param output   Recipe output
   * @param location Recipe location
   */
  public void build(RecipeOutput output, ResourceLocation location) {
    validate();
    Ingredient tex = texture;
    boolean all = matchAll;
    parent.save(wrap(output, tex, all), location);
  }

  private void validate() {
    if (texture == null) {
      throw new IllegalStateException("No texture defined for texture recipe");
    }
  }

  private static RecipeOutput wrap(RecipeOutput output, Ingredient texture, boolean matchAll) {
    return new RecipeOutput() {
      @Override
      public void accept(ResourceLocation id, Recipe<?> recipe, AdvancementHolder advancement) {
        ShapedRetexturedRecipe retextured;
        if (recipe instanceof ShapedRecipe shaped) {
          retextured = new ShapedRetexturedRecipe(shaped, texture, matchAll);
        } else {
          throw new IllegalStateException("Expected ShapedRecipe, got " + recipe.getClass());
        }
        output.accept(id, retextured, advancement);
      }

      @Override
      public Advancement.Builder advancement() {
        return output.advancement();
      }
    };
  }
}
