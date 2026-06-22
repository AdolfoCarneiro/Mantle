package slimeknights.mantle.fluid;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferManager;
import slimeknights.mantle.fluid.transfer.IFluidContainerTransfer;
import slimeknights.mantle.fluid.transfer.IFluidContainerTransfer.TransferDirection;
import slimeknights.mantle.fluid.transfer.IFluidContainerTransfer.TransferResult;

import javax.annotation.Nullable;

import static slimeknights.mantle.util.TranslationHelper.COMMA_FORMAT;

/** Alternative to FluidUtil since the forge util is a buggy mess */
@SuppressWarnings("unused")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FluidTransferHelper {
  private static final String KEY_FILLED = Mantle.makeDescriptionId("block", "tank.filled");
  private static final String KEY_DRAINED = Mantle.makeDescriptionId("block", "tank.drained");

  public static SoundEvent getSound(FluidStack fluid, SoundAction action, SoundEvent fallback) {
    SoundEvent event = fluid.getFluid().getFluidType().getSound(fluid, action);
    return event == null ? fallback : event;
  }

  public static SoundEvent getEmptySound(FluidStack fluid) {
    return getSound(fluid, SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY);
  }

  public static SoundEvent getFillSound(FluidStack fluid) {
    return getSound(fluid, SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL);
  }

  public static FluidStack tryTransfer(IFluidHandler input, IFluidHandler output, int maxFill) {
    return tryTransfer(input, output, input.drain(maxFill, FluidAction.SIMULATE));
  }

  public static FluidStack tryTransfer(IFluidHandler input, IFluidHandler output, FluidStack fluid) {
    if (!fluid.isEmpty()) {
      int simulatedFill = output.fill(fluid.copy(), FluidAction.SIMULATE);
      if (simulatedFill > 0) {
        FluidStack drainedFluid = input.drain(new FluidStack(fluid, simulatedFill), FluidAction.EXECUTE);
        if (!drainedFluid.isEmpty()) {
          int actualFill = output.fill(drainedFluid.copy(), FluidAction.EXECUTE);
          if (actualFill < drainedFluid.getAmount()) {
            int toReturn = drainedFluid.getAmount() - actualFill;
            drainedFluid.setAmount(actualFill);
            int returned = input.fill(new FluidStack(drainedFluid, toReturn), FluidAction.EXECUTE);
            if (returned < toReturn) {
              Mantle.logger.error("Lost {} fluid during transfer", toReturn - returned);
            }
          }
        }
        return drainedFluid;
      }
    }
    return FluidStack.EMPTY;
  }

  public enum FluidInteractionResult {
    FILLED_STACK, DRAINED_STACK, CONTAINER, MISSING;
    public boolean didTransfer() { return this == FILLED_STACK || this == DRAINED_STACK; }
    public boolean hasContainer() { return this != MISSING; }
  }

  @Deprecated(forRemoval = true)
  public static boolean interactWithBucket(Level world, BlockPos pos, Player player, InteractionHand hand, Direction hit, Direction offset) {
    if (player.getItemInHand(hand).getItem() instanceof BucketItem) {
      IFluidHandler handler = world.getCapability(Capabilities.FluidHandler.BLOCK, pos, hit);
      if (handler != null) {
        return interactWithFilledBucket(world, pos, handler, player, hand, offset).hasContainer();
      }
    }
    return false;
  }

  public static FluidInteractionResult interactWithFilledBucket(Level world, BlockPos pos, IFluidHandler handler, Player player, InteractionHand hand, Direction offset) {
    ItemStack held = player.getItemInHand(hand);
    if (held.getItem() instanceof BucketItem bucket) {
      Fluid fluid = bucket.getFluid();
      if (fluid != Fluids.EMPTY) {
        if (!world.isClientSide) {
          FluidStack fluidStack = new FluidStack(bucket.getFluid(), FluidType.BUCKET_VOLUME);
          if (handler.fill(fluidStack, FluidAction.SIMULATE) == FluidType.BUCKET_VOLUME) {
            SoundEvent sound = getEmptySound(fluidStack);
            handler.fill(fluidStack, FluidAction.EXECUTE);
            bucket.checkExtraContent(player, world, held, pos.relative(offset));
            world.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            player.displayClientMessage(Component.translatable(KEY_FILLED, COMMA_FORMAT.format(FluidType.BUCKET_VOLUME), fluidStack.getDisplayName()), true);
            if (!player.isCreative()) {
              player.setItemInHand(hand, held.getCraftingRemainingItem());
            }
            return FluidInteractionResult.DRAINED_STACK;
          }
        }
        return FluidInteractionResult.CONTAINER;
      }
    }
    return FluidInteractionResult.MISSING;
  }

  public static void playEmptySound(Level world, BlockPos pos, Player player, FluidStack transferred) {
    world.playSound(null, pos, getEmptySound(transferred), SoundSource.BLOCKS, 1.0F, 1.0F);
    player.displayClientMessage(Component.translatable(KEY_FILLED, COMMA_FORMAT.format(transferred.getAmount()), transferred.getDisplayName()), true);
  }

  public static void playFillSound(Level world, BlockPos pos, Player player, FluidStack transferred) {
    world.playSound(null, pos, getFillSound(transferred), SoundSource.BLOCKS, 1.0F, 1.0F);
    player.displayClientMessage(Component.translatable(KEY_DRAINED, COMMA_FORMAT.format(transferred.getAmount()), transferred.getDisplayName()), true);
  }

  @Deprecated(forRemoval = true)
  public static boolean interactWithFluidItem(Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    return interactWithContainer(world, pos, player, hand, hit).hasContainer();
  }

  public static FluidInteractionResult interactWithContainer(Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    if (!player.getItemInHand(hand).isEmpty()) {
      IFluidHandler handler = world.getCapability(Capabilities.FluidHandler.BLOCK, pos, hit.getDirection());
      if (handler != null) {
        return interactWithContainer(world, pos, handler, player, hand);
      }
    }
    return FluidInteractionResult.MISSING;
  }

  public static FluidInteractionResult interactWithContainer(Level world, BlockPos pos, IFluidHandler teHandler, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (FluidContainerTransferManager.INSTANCE.mayHaveTransfer(stack)) {
      if (!world.isClientSide) {
        FluidStack currentFluid = teHandler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
        IFluidContainerTransfer transfer = FluidContainerTransferManager.INSTANCE.getTransfer(stack, currentFluid);
        if (transfer != null) {
          TransferResult result = transfer.transfer(stack, currentFluid, teHandler, TransferDirection.AUTO);
          if (result != null) {
            if (result.didFill()) {
              playFillSound(world, pos, player, result.fluid());
            } else {
              playEmptySound(world, pos, player, result.fluid());
            }
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, result.stack()));
            return result.didFill() ? FluidInteractionResult.FILLED_STACK : FluidInteractionResult.DRAINED_STACK;
          }
        }
      }
      return FluidInteractionResult.CONTAINER;
    }
    ItemStack copy = stack.copyWithCount(1);
    IFluidHandlerItem itemCapability = copy.getCapability(Capabilities.FluidHandler.ITEM);
    if (itemCapability != null) {
      FluidInteractionResult result = FluidInteractionResult.CONTAINER;
      if (!world.isClientSide) {
        FluidStack transferred = tryTransfer(itemCapability, teHandler, Integer.MAX_VALUE);
        if (!transferred.isEmpty()) {
          playEmptySound(world, pos, player, transferred);
          result = FluidInteractionResult.DRAINED_STACK;
        } else {
          transferred = tryTransfer(teHandler, itemCapability, Integer.MAX_VALUE);
          if (!transferred.isEmpty()) {
            playFillSound(world, pos, player, transferred);
            result = FluidInteractionResult.FILLED_STACK;
          }
        }
        if (!transferred.isEmpty()) {
          player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, itemCapability.getContainer()));
        }
      }
      return result;
    }
    return FluidInteractionResult.MISSING;
  }

  public static boolean interactWithTank(Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
    Direction direction = hit.getDirection();
    return interactWithTank(world, pos, player, hand, direction, direction);
  }

  public static boolean interactWithTank(Level world, BlockPos pos, Player player, InteractionHand hand, Direction hit, Direction offset) {
    if (!player.getItemInHand(hand).isEmpty()) {
      IFluidHandler handler = world.getCapability(Capabilities.FluidHandler.BLOCK, pos, hit);
      if (handler != null) {
        return interactWithContainer(world, pos, handler, player, hand).hasContainer()
          || interactWithFilledBucket(world, pos, handler, player, hand, offset).hasContainer();
      }
    }
    return false;
  }

  public static ItemStack interactWithTankSlot(IFluidHandler teHandler, ItemStack stack, TransferDirection direction) {
    TransferResult result = interactWithStack(teHandler, stack, direction);
    return result != null ? result.stack() : ItemStack.EMPTY;
  }

  @Nullable
  public static TransferResult interactWithStack(IFluidHandler teHandler, ItemStack stack, TransferDirection direction) {
    if (!stack.isEmpty()) {
      if (FluidContainerTransferManager.INSTANCE.mayHaveTransfer(stack)) {
        FluidStack currentFluid = teHandler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
        IFluidContainerTransfer transfer = FluidContainerTransferManager.INSTANCE.getTransfer(stack, currentFluid);
        if (transfer != null) {
          TransferResult result = transfer.transfer(stack, currentFluid, teHandler, direction);
          if (result != null) {
            stack.shrink(1);
            return result;
          }
        }
      }
      ItemStack copy = stack.copyWithCount(1);
      IFluidHandlerItem itemCapability = copy.getCapability(Capabilities.FluidHandler.ITEM);
      if (itemCapability != null) {
        FluidStack transferred = FluidStack.EMPTY;
        boolean didFill = true;
        if (direction == TransferDirection.REVERSE) {
          transferred = tryTransfer(teHandler, itemCapability, Integer.MAX_VALUE);
        }
        if (direction.canEmpty() && transferred.isEmpty()) {
          transferred = tryTransfer(itemCapability, teHandler, Integer.MAX_VALUE);
          if (!transferred.isEmpty()) {
            didFill = false;
          }
        }
        if (direction != TransferDirection.REVERSE && direction.canFill() && transferred.isEmpty()) {
          transferred = tryTransfer(teHandler, itemCapability, Integer.MAX_VALUE);
        }
        if (!transferred.isEmpty()) {
          stack.shrink(1);
          return new TransferResult(itemCapability.getContainer(), transferred, didFill);
        }
      }
    }
    return null;
  }

  public static ItemStack fillFromTankSlot(IFluidHandler teHandler, ItemStack stack, FluidStack fluid) {
    TransferResult result = fillStack(teHandler, stack, fluid);
    return result != null ? result.stack() : ItemStack.EMPTY;
  }

  @Nullable
  public static TransferResult fillStack(IFluidHandler teHandler, ItemStack stack, FluidStack fluid) {
    if (!stack.isEmpty()) {
      if (FluidContainerTransferManager.INSTANCE.mayHaveTransfer(stack)) {
        IFluidContainerTransfer transfer = FluidContainerTransferManager.INSTANCE.getTransfer(stack, fluid);
        if (transfer != null) {
          TransferResult result = transfer.transfer(stack, fluid, teHandler, TransferDirection.FILL_ITEM);
          if (result != null) {
            stack.shrink(1);
            return result;
          }
        }
      }
      ItemStack copy = stack.copyWithCount(1);
      IFluidHandlerItem itemCapability = copy.getCapability(Capabilities.FluidHandler.ITEM);
      if (itemCapability != null) {
        FluidStack transferred = tryTransfer(teHandler, itemCapability, fluid.copy());
        if (!transferred.isEmpty()) {
          stack.shrink(1);
          return new TransferResult(itemCapability.getContainer(), transferred, true);
        }
      }
    }
    return null;
  }

  public static ItemStack getOrTransferFilled(Player player, ItemStack emptyStack, ItemStack filledStack) {
    if (emptyStack.isEmpty()) {
      return filledStack;
    }
    if (!player.getInventory().add(filledStack)) {
      player.drop(filledStack, false);
    }
    return emptyStack;
  }

  @SuppressWarnings("deprecation")
  public static void playUISound(Player player, SoundEvent sound) {
    if (player.level().isClientSide) {
      player.playSound(sound);
    } else if (player instanceof ServerPlayer serverPlayer) {
      serverPlayer.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), player.getSoundSource(), player.getX(), player.getY(), player.getZ(), 1, 1, player.getRandom().nextLong()));
    }
  }

  public static ItemStack handleUIResult(Player player, ItemStack emptyStack, @Nullable TransferResult result) {
    if (result == null) {
      return emptyStack;
    }
    playUISound(player, result.getSound());
    return getOrTransferFilled(player, emptyStack, result.stack());
  }
}
