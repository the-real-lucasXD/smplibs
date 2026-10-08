package lucas.smplibs;

import lucas.smplibs.teams.AttachedTeam;
import lucas.smplibs.player.AttachedPlayer;
import lucas.smplibs.teams.Team;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.NonNull;

/**
 * Describes the metadata and associated classes for a custom SMP. To load the SMP, put the following code somewhere that will
 * be called during the server startup (for example, during mod initialization):
 * <blockquote><pre>{@code
 *   ConfigManager.attach(smpInfo);
 * }</pre></blockquote>
 * where {@code smpInfo} is the {@link SMPInfo} object representing the SMP.
 *
 * @param id the unique identifier for the SMP. this id can only be made of lowercase and uppercase characters, digits,
 *           underscores or hyphens.<p>
 * @param name the display name of the SMP, shown in the config dialogs.<p>
 * @param description the description of the SMP, shown in the config dialogs.<p>
 * @param icon the {@link ResourceKey} item which points to an {@link Item} object, used as the icon shown in the
 *             config dialogs for the SMP.<p>
 * @param playerClass the class where team-specific data will be stored. To properly set up the class without
 *                    causing conflicts with the mod's built-in config saving system, refer to {@link Team}
 *                    for guidelines.<p>
 * @param teamsClass the class where player-specific data will be stored. To properly set up the class without
 *                   causing conflicts with the mod's built-in config saving system, refer to {@link AttachedPlayer}
 *                   for guidelines.<p>
 * @param configClass the class where global configs will be stored. To properly set up the class without causing
 *                    conflicts with the mod's built-in config saving system, follow the same system for
 *                    {@code playerClass}, where all fields that are intended to be saved are non-static. Avoid
 *                    runtime-only fields, but if needed mark them as {@code transient}. Below is an example:
 *                    <blockquote><pre>{@code
 *                      public class CustomConfigs {
 *                        // define custom methods and fields
 *                        public int maxArrows = 128; // 128 represents the default value
 *
 *                        public boolean canUseMoreArrows(int current) {
 *                          if (current >= maxArrows) playersHittingArrowLimit ++;
 *                          return current < maxArrows;
 *                        }
 *
 *                        // runtime only fields are marked as transient
 *                        public transient int playersHittingArrowLimit = 0;
 *                      }
 *                    }</pre></blockquote>
 *                    Afterwards, put {@code CustomConfigs.class} as the field for {@code configClass} and all
 *                    non-static, non-transient methods will be saved.<p>
 *
 * @since 1.0.0
 */
public record SMPInfo(
  @NonNull String id,
  @NonNull String name,
  @NonNull String description,
  @NonNull ResourceKey<Item> icon,
  @NonNull Class<? extends AttachedPlayer> playerClass,
  @NonNull Class<?> configClass,
  @NonNull Class<? extends AttachedTeam> teamsClass
) {
  /**
   * Creates a {@code SMPInfo} object with the specified metadata and configuration.
   *
   * @throws IllegalArgumentException if {@code id} contains any characters outside of lowercase and uppercase
   *                                  characters, digits, underscores and hyphens
   */
  public SMPInfo {
    if (!id.matches("^[a-zA-Z0-9_-]+$")) throw new IllegalArgumentException("Invalid id: " + id);
    if (name.isEmpty()) throw new IllegalArgumentException("Invalid name: " + name);
  }
}