package lucas.smplibs.player;

import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Contains the base class for the player data of an SMP. To define player data, use the following format:
 * <blockquote><pre>
 *   public class CustomPlayer extends AttachedPlayer {
 *     // define custom methods and fields here. Examples:
 *     public int maxHealth = 20; // 20 represents the fallback value.
 *
 *     public void giveHealth(int value) {
 *       maxHealth += value;
 *     }
 *
 *     // runtime fields are marked as transient
 *     public transient double currentHealth = 20.0f
 *   }
 * </pre></blockquote>
 * Afterwards, pass this class to the {@code playerClass} field of the mod's {@link lucas.smplibs.SMPInfo SMPInfo}
 * object (example: {@code CustomPlayer.class}), register it with
 * {@link lucas.smplibs.config.ConfigManager#queueAttach ConfigManager.queueAttach}, and all non-static, non-transient
 * fields will automatically be loaded and stored.
 *
 * @since 1.0.0
 */
public class AttachedPlayer {
  private transient Player player;
  void setPlayer(Player player) { this.player = player; }

  public final UUID uuid() { return player.uuid(); }
  public final ServerPlayer serverPlayer() { return player.serverPlayer(); }
}