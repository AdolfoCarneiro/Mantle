# Mantle NeoForge 1.21.1 Port — Mechanical Transformation Ruleset

> **Authoritative contract.** Every subsequent Phase 1 task references rules by name.
> Do not alter rule semantics after Task 1 ships; add an amendment section instead.

---

## RULE-RL — ResourceLocation Factory

**Why:** MC 1.21 made the `ResourceLocation(String, String)` and `ResourceLocation(String)` constructors private.

| Old (Forge 1.20.1) | New (NeoForge 1.21.1) |
|---|---|
| `new ResourceLocation(modid, path)` | `ResourceLocation.fromNamespaceAndPath(modid, path)` |
| `new ResourceLocation(combined)` | `ResourceLocation.parse(combined)` |

**Scope:** 40+ files in `src/_quarantine/`, 0 files in `src/main/java/` (already fixed in Phase 0).

**Worked examples:**
```java
// Two-arg form
return new ResourceLocation(modID, name);
// becomes:
return ResourceLocation.fromNamespaceAndPath(modID, name);

// Single-arg form (namespace defaults to "minecraft" when no colon present)
new ResourceLocation("undead")
// becomes:
ResourceLocation.parse("undead")
```

**Exclusions:**
- `ResourceLocation::new` — method reference, keep as-is. Replace with a lambda `s -> ResourceLocation.parse(s)` or appropriate factory when the owning layer is restored. (Task 1 leaves it; each owning task fixes it.)
- `new ResourceLocation.Serializer()` — inner class instantiation, unrelated.

---

## RULE-IMPORT — Package Relocations

**Why:** NeoForge moved/renamed Forge's top-level packages.

Applied **per-file as each layer is touched** by its owning task (not repo-wide in Task 1).

| Old import | New import |
|---|---|
| `net.minecraftforge.eventbus.api.IEventBus` | `net.neoforged.bus.api.IEventBus` |
| `net.minecraftforge.eventbus.api.EventPriority` | `net.neoforged.bus.api.EventPriority` |
| `net.minecraftforge.fml.common.Mod` | `net.neoforged.fml.common.Mod` |
| `net.minecraftforge.api.distmarker.Dist` | `net.neoforged.api.distmarker.Dist` |
| `net.minecraftforge.fml.loading.FMLEnvironment` | `net.neoforged.fml.loading.FMLEnvironment` |
| `net.minecraftforge.common.MinecraftForge` (`.EVENT_BUS`) | `net.neoforged.neoforge.common.NeoForge` (`.EVENT_BUS`) |
| `net.minecraftforge.fluids.FluidStack` | `net.neoforged.neoforge.fluids.FluidStack` |
| `net.minecraftforge.fluids.FluidType` | `net.neoforged.neoforge.fluids.FluidType` |
| `net.minecraftforge.event.*` | `net.neoforged.neoforge.event.*` |

**Scope:** ~170 files reference `net.minecraftforge`. Split across owning layers; each task fixes its own files.

**NOTE on FluidStack:** Neo `FluidStack` is data-component based — behavioral change is handled in Task 4/7, not here. The import rename is safe to do at any time.

---

## RULE-REGISTRIES — ForgeRegistries → BuiltInRegistries / NeoForgeRegistries

**Why:** `net.minecraftforge.registries.ForgeRegistries` is gone. Vanilla registries moved to `net.minecraft.core.registries.BuiltInRegistries`.

| Old | New |
|---|---|
| `ForgeRegistries.ITEMS` | `BuiltInRegistries.ITEM` |
| `ForgeRegistries.BLOCKS` | `BuiltInRegistries.BLOCK` |
| `ForgeRegistries.FLUID_TYPES` | `NeoForgeRegistries.FLUID_TYPES` |
| `buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS)` | Removed entirely in Task 6 — replaced by `ByteBufCodecs.registry(Registries.ITEM)` |

**Scope:** 11 files.

**Worked example:**
```java
// Old
ForgeRegistries.ITEMS.getValue(new ResourceLocation(id))
// New
BuiltInRegistries.ITEM.get(ResourceLocation.parse(id))
```

---

## RULE-EVENTBUS — Subscriber Annotation

**Why:** The `@Mod.EventBusSubscriber` nested annotation was promoted to a top-level class in NeoForge.

| Old | New |
|---|---|
| `@Mod.EventBusSubscriber` | `@EventBusSubscriber` |
| `import net.minecraftforge.fml.common.Mod` (used for annotation) | `import net.neoforged.fml.common.EventBusSubscriber` |
| `bus = Mod.EventBusSubscriber.Bus.MOD` | `bus = EventBusSubscriber.Bus.MOD` |

**Scope:** Files with `@Mod.EventBusSubscriber`. Applied per-layer by owning task.

---

## Application Log

| Task | Rule applied | Files changed | Date |
|---|---|---|---|
| Phase 1 Task 1 | RULE-RL (two-arg + single-arg) repo-wide | 40 quarantine files | 2026-06-21 |
