package lucas.smplibs.teams;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import lucas.smplibs.SMPLibs;
import lucas.smplibs.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.TeamColor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the default team data instance.
 *
 * @since 1.2.0
 * @see AttachedTeam
 */
public class Team {
  @NonNull transient String name;
  private final List<String> players = new ArrayList<>();
  private Team(@NonNull String name, @NonNull String displayName) {
    this.name = name;
    this.displayName = displayName;
  }
  private Team(@NonNull String name) { this(name, name); }

  public @NonNull TeamRule collision = TeamRule.ALWAYS;
  public @NonNull TeamColor color = TeamColor.WHITE;
  public @NonNull TeamRule deathMessageVisibility = TeamRule.ALWAYS;
  public @NonNull TeamRule nameTagVisibility = TeamRule.ALWAYS;
  public @NonNull String displayName;
  public @NonNull String prefix = "";
  public @NonNull String suffix = "";
  public boolean seeFriendlyInvisibles = false;
  public boolean friendlyFire = true;

  public PlayerTeam asPlayerTeam() {
    Gson gson = new Gson();
    Component displayNameComponent, prefixComponent, suffixComponent;
    try {
      displayNameComponent = ComponentSerialization.CODEC
        .decode(JsonOps.INSTANCE, gson.fromJson(displayName, JsonElement.class))
        .getOrThrow().getFirst();
    } catch (Exception e) {
      displayNameComponent = Component.literal(displayName);
    }

    try {
      prefixComponent = ComponentSerialization.CODEC
        .decode(JsonOps.INSTANCE, gson.fromJson(prefix, JsonElement.class))
        .getOrThrow().getFirst();
    } catch (Exception e) {
      prefixComponent = Component.literal(prefix);
    }

    try {
      suffixComponent = ComponentSerialization.CODEC
        .decode(JsonOps.INSTANCE, gson.fromJson(suffix, JsonElement.class))
        .getOrThrow().getFirst();
    } catch (Exception e) {
      suffixComponent = Component.literal(suffix);
    }

    return createTeam(displayNameComponent, prefixComponent, suffixComponent);
  }
  private @NonNull PlayerTeam createTeam(Component displayName, Component prefix, Component suffix) {
    PlayerTeam team = new PlayerTeam(SMPLibs.server().getScoreboard(), name);
    team.setCollisionRule(collision.collisionRule);
    team.setColor(Optional.of(color));
    team.setDeathMessageVisibility(deathMessageVisibility.visibility);
    team.setNameTagVisibility(nameTagVisibility.visibility);
    team.setDisplayName(displayName);
    team.setPlayerPrefix(prefix);
    team.setPlayerSuffix(suffix);
    team.setSeeFriendlyInvisibles(seeFriendlyInvisibles);
    team.setAllowFriendlyFire(friendlyFire);
    team.getPlayers().addAll(playerNames());
    return team;
  }

  public List<String> playerNames() {
    List<String> names = new ArrayList<>();
    for (String player : this.players) {
      Optional<NameAndId> current = SMPLibs.server().services().nameToIdCache().get(UUID.fromString(player));
      current.ifPresent(nameAndId -> names.add(nameAndId.name()));
    } return names;
  }

  public boolean isInTeam(Player player) { return (players.contains(player.uuid().toString())); }
  public void removeFromTeam(Player player) { if (isInTeam(player)) players.remove(player.uuid().toString()); }
  public void addToTeam(Player player) {
    player.leaveTeam();
    players.add(player.uuid().toString());
  }
  public void addToTeam(String uuid) { addToTeam(Player.get(uuid)); }

  static ConcurrentHashMap<String, Team> teams = new ConcurrentHashMap<>();
  public static Map<String, Team> teams() { return Collections.unmodifiableMap(teams); }
  public static List<PlayerTeam> playerTeams() {
    List<PlayerTeam> playerTeams = new ArrayList<>();
    for (Team team : teams.values()) playerTeams.add(team.asPlayerTeam());
    return playerTeams;
  }

  public static @Nullable Team get(ServerPlayer player) { return Player.get(player).getTeam(); }
  public static @NonNull Team get(String name) { return get(name, true); }
  public static @NonNull Team get(String name, boolean check) {
    if (!teams.containsKey(name)) {
      Team team = new Team(name);
      teams.put(name, team);
      if (check) TeamManager.updateAcrossAllAttachments(team);
    } return teams.get(name);
  }
  public static @Nullable Team getWithoutCreating(String name) { return teams.get(name); }
  public static @Nullable String getUUID(String name) {
    Optional<NameAndId> current = SMPLibs.server().services().nameToIdCache().get(name);
    return current.map(nameAndId -> String.valueOf(nameAndId.id())).orElse(null);
  }
}