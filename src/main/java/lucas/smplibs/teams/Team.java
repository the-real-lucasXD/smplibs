package lucas.smplibs.teams;

import net.minecraft.world.scores.PlayerTeam;

/**
 * Contains the base class for team-specific data of an SMP. To define teams data, use the following format:
 * <blockquote><pre>
 *   public class CustomTeam extends Team {
 *     // define custom methods and fields here. Examples:
 *     private int bank = 1000; // 1000 represents the default value
 *
 *     public void withdraw(int x) {
 *       bank -= x;
 *       withdrawnThisSession += x;
 *     }
 *
 *     // runtime fields are marked as transient
 *     public transient int withdrawnThisSession = 0;
 *   }
 * </pre></blockquote>
 * Afterwards, simply put {@code CustomTeam.class} as the field for
 * {@code SMPInfo#teamsClass} and all non-static, non-transient methods will be saved.
 *
 * @since 1.1.1
 */
public class Team {
  transient PlayerTeam playerTeam;
  public final PlayerTeam playerTeam() { return playerTeam; }
}
