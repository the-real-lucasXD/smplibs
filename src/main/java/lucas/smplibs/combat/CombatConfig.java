package lucas.smplibs.combat;

/**
 * The config object representing the settings for combat.
 */
public final class CombatConfig {
  public int duration = 1200;
  public boolean allowElytraFlight = true;
  public CombatLimitConfig limits = new CombatLimitConfig();
}
