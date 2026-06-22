package slimeknights.mantle.data.loadable.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.RegistryFriendlyByteBuf;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.field.LoadableField;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.function.Function;

/** Loadable for NBT compound tags */
public enum NBTLoadable implements RecordLoadable<CompoundTag> {
  /** Allows parsing NBT as a string or object */
  ALLOW_STRING,
  /** Only allows parsing NBT as an object */
  FORBID_STRING;

  @Override
  public CompoundTag convert(JsonElement element, String key, TypedMap context) {
    if (element.isJsonObject()) {
      return deserialize(element.getAsJsonObject(), context);
    }
    if (this == ALLOW_STRING && element.isJsonPrimitive()) {
      try {
        return TagParser.parseTag(element.getAsString());
      } catch (CommandSyntaxException ex) {
        throw new JsonSyntaxException("Invalid NBT string at " + key + ": " + ex.getMessage());
      }
    }
    throw new JsonSyntaxException("Expected " + key + " to be a JSON object" + (this == ALLOW_STRING ? " or string" : ""));
  }

  @Override
  public CompoundTag deserialize(JsonObject json, TypedMap context) {
    return (CompoundTag)JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json);
  }

  @Override
  public JsonElement serialize(CompoundTag object) {
    return NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, object).getAsJsonObject();
  }

  @Override
  public void serialize(CompoundTag object, JsonObject json) {
    json.entrySet().addAll(serialize(object).entrySet());
  }

  @Override
  public CompoundTag decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
    CompoundTag tag = buffer.readNbt();
    if (tag == null) {
      return new CompoundTag();
    }
    return tag;
  }

  @Override
  public void encode(RegistryFriendlyByteBuf buffer, CompoundTag object) {
    buffer.writeNbt(object);
  }

  @Override
  public <P> LoadableField<CompoundTag,P> nullableField(String key, Function<P,CompoundTag> getter) {
    return new NullableNBTField<>(this, key, getter);
  }


  /** Special implementation of nullable field for NBT that avoids creating a new CompoundTag when reading null */
  private record NullableNBTField<P>(NBTLoadable loadable, String key, Function<P,CompoundTag> getter) implements LoadableField<CompoundTag,P> {
    @Nullable
    @Override
    public CompoundTag get(JsonObject json, TypedMap context) {
      JsonElement element = json.get(key);
      if (element != null && !element.isJsonNull()) {
        return loadable.convert(element, key, context);
      }
      return null;
    }

    @Override
    public void serialize(P parent, JsonObject json) {
      CompoundTag nbt = getter.apply(parent);
      if (nbt != null) {
        json.add(key, loadable.serialize(nbt));
      }
    }

    @Nullable
    @Override
    public CompoundTag decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
      return buffer.readNbt();
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, P parent) {
      buffer.writeNbt(getter.apply(parent));
    }
  }
}
