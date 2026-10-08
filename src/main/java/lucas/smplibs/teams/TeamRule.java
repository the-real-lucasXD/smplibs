package lucas.smplibs.teams;

import net.minecraft.world.scores.Team;

/**
 * Represents the generic rule enum for generic {@code CollisionRules} or {@code Visibilities}.
 *
 * @since 1.2.0
 */
public enum TeamRule {
  ALWAYS(
    Team.CollisionRule.ALWAYS,
    Team.Visibility.ALWAYS
  ), NEVER(
    Team.CollisionRule.NEVER,
    Team.Visibility.NEVER
  ), APPLICABLE_TO_OTHER_TEAMS(
    Team.CollisionRule.PUSH_OTHER_TEAMS,
    Team.Visibility.HIDE_FOR_OTHER_TEAMS
  ), APPLICABLE_TO_OWN_TEAM(
    Team.CollisionRule.PUSH_OWN_TEAM,
    Team.Visibility.HIDE_FOR_OWN_TEAM
  );

  /**
   * The corresponding collision rule for this TeamRule.
   */
  public final Team.CollisionRule collisionRule;
  /**
   * The corresponding visibility rule for this TeamRule.
   */
  public final Team.Visibility visibility;

  TeamRule(
    Team.CollisionRule collisionRule,
    Team.Visibility visibility
  ) {
    this.collisionRule = collisionRule;
    this.visibility = visibility;
  }

  /**
   * Get the {@code TeamRule} object from a collision rule.
   * @param collisionRule the {@link Team.CollisionRule CollisionRule} object.
   * @return the {@code TeamRule} object.
   */
  public static TeamRule parse(Team.CollisionRule collisionRule) {
    return switch (collisionRule) {
      case Team.CollisionRule.ALWAYS -> ALWAYS;
      case Team.CollisionRule.NEVER -> NEVER;
      case Team.CollisionRule.PUSH_OTHER_TEAMS -> APPLICABLE_TO_OTHER_TEAMS;
      case Team.CollisionRule.PUSH_OWN_TEAM -> APPLICABLE_TO_OWN_TEAM;
    };
  }

  /**
   * Get the {@code TeamRule} object from a visibility rule.
   * @param visibility the {@link Team.Visibility Visibility} object.
   * @return the {@code TeamRule} object.
   */
  public static TeamRule parse(Team.Visibility visibility) {
    return switch (visibility) {
      case Team.Visibility.ALWAYS -> ALWAYS;
      case Team.Visibility.NEVER -> NEVER;
      case Team.Visibility.HIDE_FOR_OTHER_TEAMS -> APPLICABLE_TO_OTHER_TEAMS;
      case Team.Visibility.HIDE_FOR_OWN_TEAM -> APPLICABLE_TO_OWN_TEAM;
    };
  }
}