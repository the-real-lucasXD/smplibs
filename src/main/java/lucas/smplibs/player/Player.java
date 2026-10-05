package lucas.smplibs.player;

import lucas.smplibs.SMPLibs;
import lucas.smplibs.combat.Combat;
import net.minecraft.server.level.ServerPlayer;
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
  private Player(String uuid) { this.uuid = uuid; }
  public UUID uuid() { return UUID.fromString(uuid); }
  public ServerPlayer serverPlayer() {
    if (!this.online) return null;
    return SMPLibs.server == null ? null : SMPLibs.server.getPlayerList().getPlayer(uuid());
  }

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

  static ConcurrentHashMap<String, Player> values = new ConcurrentHashMap<>();
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
  public static Player get(ServerPlayer player) {
    if (!values.containsKey(player.getStringUUID()))
      values.put(player.getStringUUID(), new Player(player.getStringUUID()));
    return get(player.getStringUUID());
  }

  /**
   * Retrieve the default player data of a player.
   *
   * @param uuid the target player's string UUID
   * @return the {@link Player} object in the registry representing the player's data. If the {@code player}
   * is not found in the registry, {@code null} is returned.
   */
  public static Player get(String uuid) { return values.get(uuid); }
}