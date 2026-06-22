package slimeknights.mantle.recipe.helper;

import com.mojang.serialization.MapCodec;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.primitive.StringLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.typed.TypedMap;
import slimeknights.mantle.util.typed.TypedMapBuilder;

import java.util.function.Supplier;

/** Recipe serializer using loadables for codec/streamCodec. */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LoadableRecipeSerializer<T extends Recipe<?>> implements LoggingRecipeSerializer<T> {
  public static final ContextKey<RecipeSerializer<?>> SERIALIZER = new ContextKey<>("serializer");
  public static final ContextKey<TypeAwareRecipeSerializer<?>> TYPED_SERIALIZER = new ContextKey<>("typed_serializer");
  public static final ContextKey<RecipeType<?>> TYPE = new ContextKey<>("type");
  public static final LoadableField<String,Recipe<?>> RECIPE_GROUP = StringLoadable.DEFAULT.defaultField("group", "", Recipe::getGroup);

  protected final RecordLoadable<T> loadable;

  public static <T extends Recipe<?>> RecipeSerializer<T> of(RecordLoadable<T> loadable) {
    return new LoadableRecipeSerializer<>(loadable);
  }

  public static <T extends R, R extends Recipe<?>> TypeAwareRecipeSerializer<T> of(RecordLoadable<T> loadable, Supplier<? extends RecipeType<R>> type) {
    return new TypeAware<>(loadable, type);
  }

  public static <T extends Recipe<?>> RecipeSerializer<T> deprecated(RecordLoadable<T> loadable, String replacement) {
    return new Deprecated<>(loadable, replacement);
  }

  protected TypedMapBuilder contextBuilder() {
    return TypedMapBuilder.builder().put(ContextKey.DEBUG, "Recipe").put(SERIALIZER, this);
  }

  @Override
  public MapCodec<T> codec() {
    return loadable.mapCodec(contextBuilder().build());
  }

  @Override
  public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
    TypedMap context = contextBuilder().build();
    return StreamCodec.of(
      (buf, recipe) -> {
        try {
          loadable.encode(buf, recipe);
        } catch (RuntimeException e) {
          Mantle.logger.error("{}: Error writing recipe to packet using loadable {}", getClass().getSimpleName(), loadable, e);
          throw e;
        }
      },
      buf -> {
        try {
          return loadable.decode(buf, context);
        } catch (RuntimeException e) {
          Mantle.logger.error("{}: Error reading recipe from packet using loadable {}", getClass().getSimpleName(), loadable, e);
          throw e;
        }
      }
    );
  }

  public static class TypeAware<T extends Recipe<?>> extends LoadableRecipeSerializer<T> implements TypeAwareRecipeSerializer<T> {
    private final Supplier<? extends RecipeType<?>> type;
    protected TypeAware(RecordLoadable<T> loadable, Supplier<? extends RecipeType<?>> type) {
      super(loadable);
      this.type = type;
    }

    @Override
    protected TypedMapBuilder contextBuilder() {
      return super.contextBuilder().put(TYPE, getType()).put(TYPED_SERIALIZER, this);
    }

    @Override
    public RecipeType<?> getType() {
      return type.get();
    }
  }

  private static class Deprecated<T extends Recipe<?>> extends LoadableRecipeSerializer<T> {
    private final String replacement;
    protected Deprecated(RecordLoadable<T> loadable, String replacement) {
      super(loadable);
      this.replacement = replacement;
    }

    @Override
    public MapCodec<T> codec() {
      return super.codec().xmap(t -> {
        Mantle.logger.warn("Using deprecated recipe serializer {}, {}", BuiltInRegistries.RECIPE_SERIALIZER.getKey(this), replacement);
        return t;
      }, t -> t);
    }
  }
}
