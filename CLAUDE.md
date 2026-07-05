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

## Porting approach — histórico do bulk-copy do Mantle (NÃO é mais a regra geral)

> **Regra de execução canônica (atual):** o processo de trabalho é o orquestrador+subagent definido
> em `../tinkers-new/.superpowers/sdd/orchestrator-protocol.md` (decidido 2026-07-02, fases F1–F6),
> incluindo a regra de git safety. A regra antiga "use bulk bash, NOT subagents" abaixo valia SÓ
> para o bulk-copy mecânico quarantine→main da Phase 1 do Mantle (concluído) — nunca foi proibição
> geral de subagents. Para trabalho de fase, siga o protocolo canônico, não esta seção.

Referência histórica do bulk-copy (útil se sobrar algum pacote em `src/_quarantine/`): copiar em
massa via bash é ~2 min/task vs. 60+ min de subagent file-by-file —
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
- `ItemStack` legacy NBT API fully removed: `getTag()`/`getOrCreateTag()`/`hasTag()`/`setTag()`/`ItemStack.of(CompoundTag)`/
  `stack.save(CompoundTag)` are ALL gone. Use `DataComponents.CUSTOM_DATA` + `CustomData.of(tag)` for arbitrary mod NBT
  (`stack.has(DataComponents.CUSTOM_DATA) ? stack.get(DataComponents.CUSTOM_DATA).copyTag() : new CompoundTag()` to read,
  `stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag))` to write, `stack.remove(DataComponents.CUSTOM_DATA)` to clear).
  For serializing whole stacks: `ItemStack.parseOptional(HolderLookup.Provider, CompoundTag)` replaces `ItemStack.of(tag)`;
  `stack.save(HolderLookup.Provider, tag)` (cast result to CompoundTag) replaces `stack.save(tag)`.
- `BlockEntity.load(CompoundTag)` / `saveAdditional(CompoundTag)` / `getUpdateTag()` ALL gained a `HolderLookup.Provider`
  param and `load` was renamed `loadAdditional` (protected): `loadAdditional(CompoundTag, HolderLookup.Provider)`,
  `saveAdditional(CompoundTag, HolderLookup.Provider)`, `getUpdateTag(HolderLookup.Provider)`. Thread the Provider through
  any custom helper methods (e.g. Mantle's `saveSynced`) that get called from inside these overrides.
- `PressurePlateBlock`/`ButtonBlock` constructors changed: `PressurePlateBlock(BlockSetType, Properties)` — the old
  `Sensitivity` enum is GONE, plain `PressurePlateBlock` is now always "every entity" sensitivity.
  `ButtonBlock(BlockSetType, int ticksToStayPressed, Properties)` — no more trailing boolean "arrowsCanPress" param.
- `EventBusSubscriber.Bus.FORGE` renamed `Bus.GAME` (`Bus.MOD` unchanged).
- `ForgeEventFactory` class removed → `net.neoforged.neoforge.event.EventHooks` (e.g. `canCreateFluidSource(level,pos,state)`,
  3 args now, the old boolean "canConvert" 4th arg is computed internally).
- `IForgeRegistry<T>` interface removed entirely. NeoForge registries (`NeoForgeRegistries.FLUID_TYPES` etc.) are now plain
  vanilla `Registry<T>` instances directly (not wrapped in a `Supplier`/`.get()`). Use vanilla `Registry` methods:
  `entrySet()` not `getEntries()`, `get(ResourceLocation)` not `getValue(id)`.
- `PotionUtils` class removed → potion data lives in the `DataComponents.POTION_CONTENTS` component (`PotionContents`).
  `PotionContents.is(Holder<Potion>)` replaces `PotionUtils.getPotion(stack) == somePotion`.
  `PotionContents.createItemStack(Item, Holder<Potion>)` replaces `PotionUtils.setPotion(new ItemStack(item), potion)`.
- `FluidStack` constructors now require `Holder<Fluid>` (not raw `Fluid`) when passing a `DataComponentPatch`:
  `new FluidStack(Holder<Fluid>, int, DataComponentPatch)`. Get a holder from a raw `Fluid` via
  `BuiltInRegistries.FLUID.wrapAsHolder(fluid)`. Build a patch via `DataComponentPatch.builder().set(type, value).build()`.
- **Trust no cached jar's API shape without checking it's the version actually resolved.** Multiple versions of the same
  artifact (e.g. fancymodloader 4.0.42 vs 11.0.4/11.0.5) can sit in `~/.gradle/caches/modules-2` simultaneously from other
  projects; only one is on THIS project's classpath. Run
  `gradlew dependencies --configuration compileClasspath --console=plain` to find the real resolved version before
  trusting a decompiled/extracted class from the wrong jar (e.g. `EventBusSubscriber.Bus` has `FORGE`/no-`bus()`-field in
  one fancymodloader version and `GAME`/`MOD` in another — checked the wrong one first and got a false answer).

## CRITICAL: don't trust low error counts — raise -Xmaxerrs
javac's default error cap is 100, and it was **silently truncating** the real error count in every prior session's
compile checks. `build.gradle`'s `tasks.withType(JavaCompile)` now sets `options.compilerArgs += ['-Xmaxerrs', '5000']`
— always rely on the count AFTER that change, never assume an old "N errors, all known/expected" note from before this
was added is still accurate. Discovered at the end of Task 9e: the true error count was 471, not the 22 visible before.
This happens because "package does not exist" errors from not-yet-ported layers structurally short-circuit deeper
type-checking of files that import them — once the missing package exists, MANY MORE independent, previously-invisible
errors in the same file surface for the first time. Treat every prior "Task N: complete, 0 new errors" note in the
ledger as **provisional** until re-verified with the raised cap.
- `net.minecraft.commands.CommandRuntimeException` removed → use Brigadier `SimpleCommandExceptionType`.
- `net.minecraft.world.level.storage.loot.Serializer` REMOVED entirely. `LootItemConditionType` /
  `LootItemFunctionType<T>` / `LootPoolEntryType` are now records wrapping `MapCodec<? extends X>` — no more
  Serializer inner classes with Gson `serialize()`/`deserialize()`. `LootPoolSingletonContainer` /
  `LootItemConditionalFunction` constructors take `List<...>` not arrays; use their protected
  `singletonFields(inst)` / `commonFields(inst)` helpers inside `RecordCodecBuilder.mapCodec(...)`.
- `StatePropertiesPredicate.ANY` REMOVED → use `new StatePropertiesPredicate(List.of())` (same semantics).
- `TagKey.codec(ResourceKey<? extends Registry<T>>)` → `Codec<TagKey<T>>`, use for tag fields in record codecs.
- `IGlobalLootModifier.codec()` return type changed `Codec` → `MapCodec` (NeoForge-side, not vanilla) —
  GLM `CODEC` fields must use `RecordCodecBuilder.mapCodec(...)` not `.create(...)`.
- `RegisterEvent.getForgeRegistry()` REMOVED → `event.getRegistry(ResourceKey<Registry<T>>)` (typed, nullable).
- `ItemStack`/`Item` capability instance hooks removed (`ICapabilityProvider`, `Item.initCapabilities()`) —
  same shift as the BlockEntity capability model above: expose a plain getter method, wire it via
  `RegisterCapabilitiesEvent.registerItem(...)` in the owning mod.
- **Decompiled vanilla 1.21.1 source available locally** for checking real signatures instead of guessing:
  `C:\Users\Adolfo\.gradle\caches\neoformruntime\intermediate_results\decompile_ddcc63f9ea61da77e44ccaa505f486b70f81cc75_output.jar`
  contains `net/minecraft/**/*.java`. Inspect with `unzip -p <jar> <path/To/Class.java>`.
  NeoForge's own source: `net.neoforged:neoforge:<version>-sources.jar` in `.gradle/caches/modules-2/files-2.1/`.

## Commands
Run from `C:/Users/Adolfo/source/repos/Mantle-neo`:
```
.\gradlew.bat compileJava    # verify compile (errors expected from unported quarantine layers)
.\gradlew.bat build          # full build (only after all tasks complete)
.\gradlew.bat runClient      # launch Minecraft (Phase 0 verified: Mantle stub loads)
.\gradlew.bat runData        # datagen
```
JDK: `C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot` (set in gradle.properties)
