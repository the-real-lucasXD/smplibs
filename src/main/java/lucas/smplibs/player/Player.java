package lucas.smplibs.player;

import lucas.smplibs.SMPLibs;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the default player data instance.
 *
 * @since 1.0.0
 */
public final class Player {
  public transient String uuid;
  public transient boolean online = false;

  public Player(String uuid) { this.uuid = uuid; }

  public UUID uuid() { return UUID.fromString(uuid); }
  public ServerPlayer serverPlayer() {
    return SMPLibs.server == null ? null : SMPLibs.server.getPlayerList().getPlayer(uuid());
  }

  static ConcurrentHashMap<String, Player> values = new ConcurrentHashMap<>();

  /**
   * Retrieve the default player data of player.
   *
   * @param player the target player's {@link ServerPlayer} object
   * @return the player's data
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
   * @return the player's data
   */
  public static Player get(String uuid) { return values.get(uuid); }
}
