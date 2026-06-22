package slimeknights.mantle.recipe.data;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.data.recipes.RecipeOutput;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a recipe output wrapper that adds conditions to wrapped recipes
 */
@SuppressWarnings("unused")  // API
public class ConsumerWrapperBuilder {
  private final List<ICondition> conditions = new ArrayList<>();

  private ConsumerWrapperBuilder() {}

  /** Creates a wrapper builder */
  public static ConsumerWrapperBuilder wrap() {
    return new ConsumerWrapperBuilder();
  }

  /**
   * Adds a condition to the consumer
   * @param condition Condition to add
   * @return Builder
   */
  @CanIgnoreReturnValue
  public ConsumerWrapperBuilder addCondition(ICondition condition) {
    conditions.add(condition);
    return this;
  }

  /**
   * Builds the output for the wrapper builder
   * @param output Base output
   * @return Wrapped output with conditions applied
   */
  public RecipeOutput build(RecipeOutput output) {
    if (conditions.isEmpty()) {
      return output;
    }
    return output.withConditions(conditions.toArray(ICondition[]::new));
  }
}
