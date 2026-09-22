package lucas.smplibs;

import lucas.smplibs.player.AttachedPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.NonNull;


/**
 * Describes the metadata and associated classes for a custom SMP.
 * <p>
 * Refer to {@link AttachedPlayer} on guidelines to properly set up the class for {@code playerClass} without causing
 * conflicts with the mod's built-in config saving system.
 * <p>
 * To properly set up the class for {@code configClass} without causing conflicts with the mod's built-in config saving
 * system, follow the same system as for {@code playerClass}, where all fields that are intended to be saved are non-static.
 * Avoid runtime-only fields, but if needed mark them as transient. Below is an example:
 * <blockquote><pre>{@code
 *   public class CustomConfigs {
 *     // define custom methods and fields here. Examples:
 *     public int maxArrows = 128; // 128 represents the fallback value.
 *
 *     public boolean canUseMoreArrows(int current) {
 *       if (current >= maxArrows) playersHittingArrowLimit ++;
 *       return current < maxArrows;
 *     }
 *
 *     // runtime only fields
 *     public transient int playersHittingArrowLimit = 0;
 *   }
 * }</pre></blockquote>
 *
 * @param id the unique identifier for the SMP. this id can only be made of lowercase and uppercase characters, digits,
 *           underscores or hyphens
 * @param name the display name of the SMP, shown in the config dialogs.
 * @param description the description of the SMP, shown in the config dialogs.
 * @param icon the {@link ResourceKey} item which points to an {@link Item} object, used as the icon shown in the
 *             config dialogs for the SMP.
 * @param playerClass the class where player-specific data will be stored.
 * @param configClass the class where global configs will be stored.
 */
public record SMPInfo(
  String id,
  String name,
  String description,
  @NonNull ResourceKey<Item> icon,
  @NonNull Class<? extends AttachedPlayer> playerClass,
  @NonNull Class<?> configClass
) {
  /**
   * Creates a {@code SMPInfo} object with the specified metadata and configuration.
   *
   * @throws IllegalArgumentException if {@code id} contains any characters outside of lowercase and uppercase
   *                                  characters, digits, underscores and hyphens
   */
  public SMPInfo {
    if (id == null || !id.matches("^[a-zA-Z0-9_-]+$"))
      throw new IllegalArgumentException("Invalid id: " + id);
  }
}