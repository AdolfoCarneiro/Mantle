# CLAUDE.md — Mantle-neo (NeoForge 1.21.1 port)

This is a fork of SlimeKnights/Mantle porting from Forge 1.20.1 → NeoForge 1.21.1.
Branch: `neoforge-1.21.1`. Phase 0 (toolchain) and Tasks 1–4 of Phase 1 are complete.

## Port progress
Ledger: `C:/Users/Adolfo/source/repos/tinkers-new/.superpowers/sdd/progress.md`
Plan:   `C:/Users/Adolfo/source/repos/tinkers-new/docs/superpowers/plans/2026-06-21-phase1-mantle-core.md`

## File location model
- Forge originals awaiting port: `src/_quarantine/java/slimeknights/mantle/`
- NeoForge target (compiled):    `src/main/java/slimeknights/mantle/`
- To port a package: copy from quarantine → apply transforms → delete quarantine copy → commit

## CRITICAL: Porting approach — use bulk bash, NOT subagents
Subagents read/write files one-by-one: 100+ tool calls per task, hit rate limits, take 60+ min.
**Correct approach (takes ~2 min per task):**
```bash
# 1. Copy files
cp -r src/_quarantine/java/slimeknights/mantle/PACKAGE src/main/java/slimeknights/mantle/

# 2. Apply import replacements with sed -i
find src/main/java/slimeknights/mantle/PACKAGE -name "*.java" | xargs sed -i \
  -e 's|net\.minecraftforge\.fluids\.FluidStack|net.neoforged.neoforge.fluids.FluidStack|g' \
  ... (see full table below)

# 3. Delete quarantine copies
rm -rf src/_quarantine/java/slimeknights/mantle/PACKAGE

# 4. Commit
git add -A && git commit -m "Phase 1 Task N: port PACKAGE layer"
```

## Import mapping table (Forge → NeoForge)
```
net.minecraftforge.eventbus.api.IEventBus           → net.neoforged.bus.api.IEventBus
net.minecraftforge.eventbus.api.EventPriority        → net.neoforged.bus.api.EventPriority
net.minecraftforge.fml.common.Mod                   → net.neoforged.fml.common.Mod
net.minecraftforge.fml.ModContainer                  → net.neoforged.fml.ModContainer
net.minecraftforge.fml.ModList                       → net.neoforged.fml.ModList
net.minecraftforge.fml.loading.FMLEnvironment        → net.neoforged.fml.loading.FMLEnvironment
net.minecraftforge.api.distmarker.Dist               → net.neoforged.api.distmarker.Dist
net.minecraftforge.api.distmarker.OnlyIn             → net.neoforged.api.distmarker.OnlyIn
net.minecraftforge.common.MinecraftForge             → net.neoforged.neoforge.common.NeoForge
MinecraftForge.EVENT_BUS                             → NeoForge.EVENT_BUS
net.minecraftforge.registries.DeferredRegister       → net.neoforged.neoforge.registries.DeferredRegister
net.minecraftforge.registries.RegistryObject         → net.neoforged.neoforge.registries.DeferredHolder
net.minecraftforge.registries.ForgeRegistries        → net.neoforged.neoforge.registries.NeoForgeRegistries (Neo-specific) or net.minecraft.core.registries.BuiltInRegistries (vanilla)
net.minecraftforge.registries.ForgeRegistries.Keys.FLUID_TYPES → net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.FLUID_TYPES
net.minecraftforge.registries.IForgeRegistry         → net.neoforged.neoforge.registries.IForgeRegistry
net.minecraftforge.fluids.FluidStack                 → net.neoforged.neoforge.fluids.FluidStack
net.minecraftforge.fluids.FluidType                  → net.neoforged.neoforge.fluids.FluidType
net.minecraftforge.fluids.ForgeFlowingFluid          → net.neoforged.neoforge.fluids.BaseFlowingFluid  (CLASS NAME CHANGES)
net.minecraftforge.fluids.ForgeFlowingFluid.Properties → net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties
net.minecraftforge.fluids.capability.IFluidHandler   → net.neoforged.neoforge.fluids.capability.IFluidHandler
net.minecraftforge.fluids.capability.IFluidHandlerItem → net.neoforged.neoforge.fluids.capability.IFluidHandlerItem
net.minecraftforge.fluids.capability.ForgeCapabilities → net.neoforged.neoforge.capabilities.Capabilities
net.minecraftforge.common.util.LazyOptional          → (REMOVE — Neo caps return T directly, null if absent)
net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions → net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions
net.minecraftforge.client.event.*                    → net.neoforged.neoforge.client.event.*
net.minecraftforge.event.*                           → net.neoforged.neoforge.event.*
net.minecraftforge.common.crafting.conditions.ICondition → net.neoforged.neoforge.common.conditions.ICondition
net.minecraftforge.common.crafting.CraftingHelper    → net.neoforged.neoforge.common.crafting.CraftingHelper
net.minecraftforge.common.data.ExistingFileHelper    → net.neoforged.neoforge.common.data.ExistingFileHelper
net.minecraftforge.common.SoundActions               → net.neoforged.neoforge.common.SoundActions
net.minecraftforge.common.ForgeConfigSpec            → net.neoforged.neoforge.common.ModConfigSpec
net.minecraftforge.common.ForgeMod                   → net.neoforged.neoforge.common.NeoForgeMod
net.minecraftforge.common.ToolAction / ToolActions    → net.neoforged.neoforge.common.ItemAbility / ItemAbilities
net.minecraftforge.common.capabilities.ForgeCapabilities → net.neoforged.neoforge.capabilities.Capabilities (e.g. Capabilities.FluidHandler.BLOCK/ITEM, Capabilities.ItemHandler.BLOCK/ITEM)
net.minecraftforge.network.NetworkHooks.openScreen(player, provider, pos) → player.openMenu(provider)  (vanilla; NetworkHooks removed)
net.minecraftforge.client.model.data.ModelData       → net.neoforged.neoforge.client.model.data.ModelData
@Mod.EventBusSubscriber                              → @EventBusSubscriber (net.neoforged.fml.common.EventBusSubscriber)
Mod.EventBusSubscriber.Bus.MOD                       → EventBusSubscriber.Bus.MOD
FriendlyByteBuf (in Streamable decode/encode)        → RegistryFriendlyByteBuf
```

## Buffer type rule (Streamable implementations)
`Streamable<T>` now extends `StreamCodec<RegistryFriendlyByteBuf, T>`.
All `decode(FriendlyByteBuf, ...)` and `encode(FriendlyByteBuf, ...)` in Streamable implementors → `RegistryFriendlyByteBuf`.

## MC 1.21.1 API removals found during port
- `MobEffect.builtInRegistryHolder()` → `BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect.get())`
- `Registry.DEFAULT` constant → `-1`
- `Holder.containsTag(TagKey)` → `Holder.is(TagKey)`
- `Holder.Reference<T>` no longer implements `Supplier<T>` → use `holder::value` lambda
- `RecipeSerializer.fromJson/fromNetwork` → `MapCodec<T> codec()` + `StreamCodec<RegistryFriendlyByteBuf,T> streamCodec()`
- `Recipe.getId()` removed — id lives in registry holder
- `new ResourceLocation(a,b)` → `ResourceLocation.fromNamespaceAndPath(a,b)` (already applied in Task 1)
- `new ResourceLocation(s)` → `ResourceLocation.parse(s)` (already applied in Task 1)
- `BlockEntity.getCapability(Capability, Direction)` / `invalidateCaps()` REMOVED — capabilities are no longer instance methods on
  BlockEntity/Entity/ItemStack in NeoForge. They're queried via `level.getCapability(BlockCapability<T,C> cap, BlockPos pos, C context)`
  (nullable return, no LazyOptional) and registered per concrete type via `RegisterCapabilitiesEvent.registerBlockEntity(cap, TYPE, (be,ctx)->handler)`
  in the type's owning mod — NOT inside a shared abstract base class. Mantle's `InventoryBlockEntity`/`MantleBlockEntity` just expose a
  plain getter (e.g. `getItemHandler()`); subclass registration call sites (in Mantle or consuming mods) wire that into
  `Capabilities.ItemHandler.BLOCK` registration.
- `LevelReader` has no `getCapability` (only `Level` does) — methods like `Block.canSurvive(state, LevelReader, pos)` that need a
  capability check must `instanceof Level` guard first.
- `TierSortingRegistry` (net.neoforged.neoforge.common) — not found in NeoForge 1.21.1; for vanilla-only tier ordering, hardcode the
  vanilla `List.of(Tiers.WOOD, Tiers.STONE, Tiers.IRON, Tiers.DIAMOND, Tiers.NETHERITE)` order instead.
- `net.minecraft.commands.CommandRuntimeException` removed → use Brigadier `SimpleCommandExceptionType`.

## Commands
Run from `C:/Users/Adolfo/source/repos/Mantle-neo`:
```
.\gradlew.bat compileJava    # verify compile (errors expected from unported quarantine layers)
.\gradlew.bat build          # full build (only after all tasks complete)
.\gradlew.bat runClient      # launch Minecraft (Phase 0 verified: Mantle stub loads)
.\gradlew.bat runData        # datagen
```
JDK: `C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot` (set in gradle.properties)
