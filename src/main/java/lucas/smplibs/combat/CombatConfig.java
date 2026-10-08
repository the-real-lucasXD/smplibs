package lucas.smplibs.combat;

/**
 * Configs regarding the combat system.
 *
 * @since 1.1.0
 */
public final class CombatConfig {
  public int duration = 1200;
  public boolean allowElytraFlight = true;
  public CombatLimitConfig limits = new CombatLimitConfig();
}
