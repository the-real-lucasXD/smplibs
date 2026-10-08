package lucas.smplibs.player;

import lucas.smplibs.SMPLibs;
import lucas.smplibs.combat.Combat;
import lucas.smplibs.teams.Team;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static lucas.smplibs.config.Config.configs;

/**
 * Represents the default player data instance.
 *
 * @since 1.0.0
 * @see AttachedPlayer
 */
public final class Player {
  transient String uuid;
  public transient boolean online = false;
  public UUID uuid() { return UUID.fromString(uuid); }

  /**
   * Gets the {@link ServerPlayer} object of the player, if they are online.
   * @return the player's {@link ServerPlayer} object, or {@code null} if the player is offline, or the
   *         server is {@code null}.
   */
  public ServerPlayer serverPlayer() {
    if (!this.online) return null;
    return SMPLibs.server() == null ? null : SMPLibs.server().getPlayerList().getPlayer(uuid());
  }

  private Player(String uuid) { this.uuid = uuid; }

  private transient Combat combat = null;
  public Combat combat() { return combat; }
  public boolean inCombat() { return combat != null; }
  public void clearCombat() {
    combat.bossBar.removeAllPlayers();
    if (combat.attacker != null) combat.attacker.combat.attacker = null;
    combat = null;
  }
  public void enterCombat(@Nullable Player attacker) {
    if (configs().combat.duration <= 0) return;
    if (!inCombat()) combat = new Combat(this, attacker);
    else combat.reset(attacker);
  }

  public void inventoryRefresh() { Objects.requireNonNull(serverPlayer()).containerMenu.sendAllDataToRemote(); }

  public boolean leaveTeam() {
    Team team = getTeam();
    if (team != null) team.removeFromTeam(this);
    return team != null;
  }
  public Team getTeam() {
    for (Team team : Team.teams().values()) {
      if (team.isInTeam(this)) return team;
    } return null;
  }


  static Map<String, Player> values = new ConcurrentHashMap<>();
  /**
   * Retrieve all default player data.
   * @return an unmodifiable {@link Map} containing the player object, where the key is a {@link String}
   * representing the UUID, and the value is the corresponding {@link Player} representing the player's data.
   */
  public static Map<String, Player> players() { return Collections.unmodifiableMap(values); }

  /**
   * Retrieve the default player data of player.
   *
   * @param player the target player's {@link ServerPlayer} object
   * @return the {@link Player} object in the registry representing the player's data. If the {@code player} is not found
   * in the registry, a new object is automatically created and that object is returned.
   */
  public static @NonNull Player get(ServerPlayer player) { return get(player.getStringUUID(), true); }
  public static @NonNull Player get(String uuid) { return get(uuid, true); }
  public static @NonNull Player get(String uuid, boolean check) {
    if (!values.containsKey(uuid)) {
      Player player = new Player(uuid);
      values.put(uuid, player);
      if (check) PlayerManager.updateAcrossAllAttachments(player);
    } return values.get(uuid);
  }
}