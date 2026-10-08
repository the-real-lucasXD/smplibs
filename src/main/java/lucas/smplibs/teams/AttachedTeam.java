package lucas.smplibs.teams;

/**
 * Contains the base class for custom teams data of an SMP. To define teams data, use the following format:
 * <blockquote><pre>
 *   public class CustomTeam extends AttachedTeam {
 *     // define custom methods and fields here. Examples:
 *     private int bank = 200; // 200 represents the fallback value.
 *
 *     public void withdraw(int amount) {
 *       bank -= amount;
 *     }
 *
 *     // runtime fields are marked as transient
 *     public transient double depositedThisSession = 0;
 *   }
 * </pre></blockquote>
 * Afterwards, put {@code CustomTeam.class} as the field for
 * {@code SMPInfo#teamsClass} and all non-static, non-transient methods will be saved.
 *
 * @since 1.2.0
 */
public class AttachedTeam {
  transient Team team;
  public final Team player() { return team; }
}