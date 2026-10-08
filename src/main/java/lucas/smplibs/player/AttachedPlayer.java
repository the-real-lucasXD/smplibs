package lucas.smplibs.player;

/**
 * Contains the base class for custom player data of an SMP. To define player data, use the following format:
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
 * Afterwards, put {@code CustomPlayer.class} as the field for
 * {@code SMPInfo#playerClass} and all non-static, non-transient methods will be saved.
 *
 * @since 1.0.0
 */
public class AttachedPlayer {
  transient Player player;
  public final Player player() { return player; }
}