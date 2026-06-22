package slimeknights.mantle.recipe.ingredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import slimeknights.mantle.recipe.MantleRecipes;
import slimeknights.mantle.registration.object.FluidObject;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/** Ingredient that matches a container of fluid */
@SuppressWarnings("unused")  // API
public class FluidContainerIngredient implements ICustomIngredient {
  /** Codec for JSON serialization (inner content, excluding type field) */
  public static final MapCodec<FluidContainerIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
    FluidIngredient.LOADABLE.codec().fieldOf("fluid").forGetter(i -> i.fluidIngredient),
    Ingredient.CODEC.optionalFieldOf("display").forGetter(i -> Optional.ofNullable(i.display))
  ).apply(inst, (fluid, display) -> new FluidContainerIngredient(fluid, display.orElse(null))));

  /** StreamCodec for network serialization */
  public static final StreamCodec<RegistryFriendlyByteBuf, FluidContainerIngredient> STREAM_CODEC = StreamCodec.of(
    (buf, ing) -> {
      FluidIngredient.LOADABLE.encode(buf, ing.fluidIngredient);
      buf.writeBoolean(ing.display != null);
      if (ing.display != null) {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ing.display);
      }
    },
    buf -> {
      FluidIngredient fluidIngredient = FluidIngredient.LOADABLE.decode(buf);
      @Nullable Ingredient display = buf.readBoolean() ? Ingredient.CONTENTS_STREAM_CODEC.decode(buf) : null;
      return new FluidContainerIngredient(fluidIngredient, display);
    }
  );

  private final FluidIngredient fluidIngredient;
  @Nullable
  private final Ingredient display;

  protected FluidContainerIngredient(FluidIngredient fluidIngredient, @Nullable Ingredient display) {
    this.fluidIngredient = fluidIngredient;
    this.display = display;
  }

  /** Creates an instance from a fluid ingredient with a display container */
  public static FluidContainerIngredient fromIngredient(FluidIngredient ingredient, Ingredient display) {
    return new FluidContainerIngredient(ingredient, display);
  }

  /** Creates an instance from a fluid ingredient with no display, not recommended */
  public static FluidContainerIngredient fromIngredient(FluidIngredient ingredient) {
    return new FluidContainerIngredient(ingredient, null);
  }

  /** Creates an instance from a fluid object with a bucket display */
  public static FluidContainerIngredient fromFluid(FluidObject<?> fluid) {
    return fromIngredient(fluid.ingredient(FluidType.BUCKET_VOLUME), Ingredient.of(fluid));
  }

  @Override
  public boolean test(ItemStack stack) {
    if (stack == null || stack.isEmpty()) return false;
    IFluidHandlerItem cap = stack.getCapability(Capabilities.FluidHandler.ITEM);
    if (cap == null || cap.getTanks() != 1) return false;
    FluidStack contained = cap.getFluidInTank(0);
    if (contained.isEmpty() || fluidIngredient.getAmount(contained.getFluid()) != contained.getAmount() || !fluidIngredient.test(contained.getFluid())) {
      return false;
    }
    // test on a copy since drain mutates the handler
    ItemStack copy = stack.copyWithCount(1);
    IFluidHandlerItem copyHandler = copy.getCapability(Capabilities.FluidHandler.ITEM);
    if (copyHandler == null) return false;
    Fluid fluid = cap.getFluidInTank(0).getFluid();
    int amount = fluidIngredient.getAmount(fluid);
    FluidStack drained = copyHandler.drain(amount, FluidAction.EXECUTE);
    return drained.getFluid() == fluid && drained.getAmount() == amount && ItemStack.matches(stack.getCraftingRemainingItem(), copyHandler.getContainer());
  }

  @Override
  public Stream<ItemStack> getItems() {
    if (display == null) return Stream.empty();
    return Arrays.stream(display.getItems());
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public IngredientType<?> getType() {
    return MantleRecipes.FLUID_CONTAINER_INGREDIENT.get();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FluidContainerIngredient that)) return false;
    return fluidIngredient.equals(that.fluidIngredient) && Objects.equals(display, that.display);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fluidIngredient, display);
  }
}
