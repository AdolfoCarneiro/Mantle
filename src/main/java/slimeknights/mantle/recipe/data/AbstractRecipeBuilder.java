package slimeknights.mantle.recipe.data;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Common logic to create a recipe builder class
 * @param <T>
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class AbstractRecipeBuilder<T extends AbstractRecipeBuilder<T>> {
  protected final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
  @Nonnull
  protected String group = "";

  /**
   * Adds a criteria to the recipe
   * @param name      Criteria name
   * @param criterion Criteria instance
   * @return  Builder
   */
  @SuppressWarnings("unchecked")
  public T unlockedBy(String name, Criterion<?> criterion) {
    this.criteria.put(name, criterion);
    return (T) this;
  }

  /**
   * Sets the group for this recipe
   * @param group  Recipe group
   * @return  Builder
   */
  @SuppressWarnings("unchecked")
  public T group(String group) {
    this.group = group;
    return (T) this;
  }

  /**
   * Sets the group for this recipe
   * @param group  Recipe resource location group
   * @return  Builder
   */
  public T group(ResourceLocation group) {
    if ("minecraft".equals(group.getNamespace())) {
      return group(group.getPath());
    }
    return group(group.toString());
  }

  /**
   * Builds the recipe with a default recipe ID, typically based on the output
   * @param output  Recipe output
   */
  public abstract void save(RecipeOutput output);

  /**
   * Builds the recipe
   * @param output  Recipe output
   * @param id      Recipe ID
   */
  public abstract void save(RecipeOutput output, ResourceLocation id);

  private AdvancementHolder buildAdvancementInternal(RecipeOutput output, ResourceLocation id, String folder) {
    ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "recipes/" + folder + "/" + id.getPath());
    Advancement.Builder builder = Advancement.Builder.advancement()
      .parent(ResourceLocation.parse("recipes/root"))
      .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
      .rewards(AdvancementRewards.Builder.recipe(id))
      .requirements(AdvancementRequirements.Strategy.OR);
    for (Map.Entry<String, Criterion<?>> entry : criteria.entrySet()) {
      builder.addCriterion(entry.getKey(), entry.getValue());
    }
    return builder.save(output.advancement(), advancementId);
  }

  /**
   * Builds and validates the advancement
   * @param output  Recipe output
   * @param id      Recipe ID
   * @param folder  Group folder
   * @return Advancement holder
   */
  protected AdvancementHolder buildAdvancement(RecipeOutput output, ResourceLocation id, String folder) {
    if (criteria.isEmpty()) {
      throw new IllegalStateException("No way of obtaining recipe " + id);
    }
    return buildAdvancementInternal(output, id, folder);
  }

  /**
   * Builds an optional advancement
   * @param output  Recipe output
   * @param id      Recipe ID
   * @param folder  Group folder
   * @return Advancement holder, or null if no criteria defined
   */
  @SuppressWarnings("SameParameterValue")
  @Nullable
  protected AdvancementHolder buildOptionalAdvancement(RecipeOutput output, ResourceLocation id, String folder) {
    if (criteria.isEmpty()) {
      return null;
    }
    return buildAdvancementInternal(output, id, folder);
  }
}
