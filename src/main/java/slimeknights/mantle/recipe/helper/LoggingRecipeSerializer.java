package slimeknights.mantle.recipe.helper;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Marker interface — network handled via streamCodec() */
public interface LoggingRecipeSerializer<T extends Recipe<?>> extends RecipeSerializer<T> {
}
