package slimeknights.mantle;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mantle
 *
 * Central mod object for Mantle.
 * Phase 0 stub: minimal NeoForge entrypoint. All subsystems are quarantined
 * under src/_quarantine and restored incrementally in Phase 1 (Mantle core).
 *
 * @author Sunstrike <sun@sunstrike.io>
 */
@Mod(Mantle.modId)
public class Mantle {
  public static final String modId = "mantle";
  public static final Logger logger = LogManager.getLogger("Mantle");
  /** Namespace for common tags; "c" standard on 1.21.1 (was "forge" on 1.20.1). */
  public static final String COMMON = "c";

  /* Instance of this mod, used for grabbing prototype fields */
  public static Mantle instance;

  public Mantle(IEventBus modEventBus, ModContainer modContainer) {
    instance = this;
    logger.info("Mantle Phase 0 stub loaded (NeoForge 1.21.1). Subsystems quarantined.");
    // Phase 1 restores: config registration, MantleTags, network, recipes,
    // capabilities, datagen, predicates, client events. Intentionally empty here.
  }

  /**
   * Gets a resource location for Mantle.
   * @param name  Name
   * @return  Resource location instance
   */
  public static ResourceLocation getResource(String name) {
    return ResourceLocation.fromNamespaceAndPath(modId, name);
  }

  /**
   * Gets a resource location for the common namespace ("c" on 1.21.1).
   * @param name  Name
   * @return  Resource location instance
   */
  public static ResourceLocation commonResource(String name) {
    return ResourceLocation.fromNamespaceAndPath(COMMON, name);
  }

  /**
   * Makes a translation key for the given name.
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @return  Translation key
   */
  public static String makeDescriptionId(String base, String name) {
    return Util.makeDescriptionId(base, getResource(name));
  }

  /**
   * Makes a translation text component for the given name.
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @return  Translation component
   */
  public static MutableComponent makeComponent(String base, String name) {
    return Component.translatable(makeDescriptionId(base, name));
  }

  /**
   * Makes a translation text component for the given name with format args.
   * @param base  Base name, such as "block" or "gui"
   * @param name  Object name
   * @param args  Additional arguments to format strings
   * @return  Translation component
   */
  public static MutableComponent makeComponent(String base, String name, Object... args) {
    return Component.translatable(makeDescriptionId(base, name), args);
  }
}
