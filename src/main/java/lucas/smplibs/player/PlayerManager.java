package lucas.smplibs.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lucas.smplibs.SMPInfo;
import lucas.smplibs.SMPLibs;
import lucas.smplibs.config.ConfigManager;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the utility class for the management of all player data classes.
 *
 * @since 1.0.0
 */
public final class PlayerManager {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Map<String, Map<String, AttachedPlayer>> players = new ConcurrentHashMap<>();

  static void updateAcrossAllAttachments(@NonNull Player player) {
    try {
      for (SMPInfo info : ConfigManager.attachments().values()) {
        AttachedPlayer attachedPlayer = info.playerClass().getConstructor().newInstance();
        attachedPlayer.player = player;
        players.get(info.id()).putIfAbsent(player.uuid, attachedPlayer);
      }
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  @ApiStatus.Internal
  public static void loadAndAttach() {
    try {
      if (ConfigManager.attached()) return;
      Path path = SMPLibs.configDir();
      boolean created = false;
      Path resolved = path.resolve("playerdata.json");
      if (Files.exists(resolved)) {
        Type type = TypeToken.getParameterized(ConcurrentHashMap.class, String.class, Player.class).getType();
        String json = Files.readString(resolved);
        try {
          Player.values = GSON.fromJson(json, type);
          if (Player.values != null) created = true;
        } catch (Exception e) {
          SMPLibs.LOGGER.error("Incomplete or corrupted default player data, resetting");
          Path oldPath = path.resolve("playerdata-old.jsonc");
          Files.createDirectories(oldPath.getParent());
          Files.writeString(oldPath, """
          /*
            This file has corrupted or incomplete player data, as such a new
            default file has been created instead to allow the mod to load
            properly. To restore this set of player data,
            1) Look through this file and search for and fix problems with the
               JSON. Referring to the new player data file can help.
            2) Remove this comment.
            3) Stop the server.
            4) Replace file contents of the original player data config with
               the contents of this file.
            5) Restart the server. If the problem(s) have been fixed, the server
               should start properly.
          */
          \n
          \n
          """);
          Files.writeString(oldPath, json, StandardOpenOption.APPEND);
          SMPLibs.LOGGER.info("Successfully saved old player data to playerdata-old.jsonc");
        }
      } if (!created) Player.values = new ConcurrentHashMap<>();
      Player.values.forEach((uuid, player) -> player.uuid = uuid);

      for (SMPInfo info : ConfigManager.attachments().values()) {
        path = SMPLibs.configDir(info.id());
        created = false;
        resolved = path.resolve("playerdata.json");
        Map<String, AttachedPlayer> map = new ConcurrentHashMap<>();
        if (Files.exists(resolved)) {
          Type type = TypeToken.getParameterized(ConcurrentHashMap.class, String.class, info.playerClass()).getType();
          String json = Files.readString(resolved);
          try {
            map = GSON.fromJson(json, type);
            if (map != null) created = true;
          } catch (Exception e) {
            SMPLibs.LOGGER.error("Incomplete or corrupted player data for {}, resetting", info.id());
            Path oldPath = path.resolve("playerdata-old.jsonc");
            Files.createDirectories(oldPath.getParent());
            Files.writeString(oldPath, """
              /*
                This file has corrupted or incomplete player data, as such a new
                default file has been created instead to allow the mod to load
                properly. To restore this set of player data,
                1) Look through this file and search for and fix problems with the
                   JSON. Referring to the new player data file can help.
                2) Remove this comment.
                3) Stop the server.
                4) Replace file contents of the original player data config with
                   the contents of this file.
                5) Restart the server. If the problem(s) have been fixed, the server
                   should start properly.
              */
              \n
              \n
              """);
            Files.writeString(oldPath, json, StandardOpenOption.APPEND);
            SMPLibs.LOGGER.info("Successfully saved old player data to {}/playerdata-old.jsonc", info.id());
          }
        } if (!created) map = new ConcurrentHashMap<>();
        map.forEach((uuid, player) -> player.player = Player.get(uuid));
        players.put(info.id(), map);
      }

      Player.values.forEach((uuid, _) -> {
        for (SMPInfo info : ConfigManager.attachments().values()) get(info.id(), uuid);
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  @ApiStatus.Internal
  public static void save() {
    try {
      Path path = SMPLibs.configDir().resolve("playerdata.json");
      Files.createDirectories(path.getParent());
      Files.writeString(path, GSON.toJson(Player.values));

      for (SMPInfo info : ConfigManager.attachments().values()) {
        path = SMPLibs.configDir(info.id()).resolve("playerdata.json");
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(players.get(info.id())));
      }
    } catch (Exception e) {
      SMPLibs.LOGGER.error("Failed to save player data", e);
    }
  }

  /**
   * Retrieve the data of a specific player of an SMP. Note that this method returns the data with {@link AttachedPlayer}
   * type. To get the player data in the correct class, cast it with:
   *  <blockquote><pre>
   *   public static CustomPlayer get(String uuid) {
   *     return (CustomPlayer) PlayerManager.get("custom", uuid);
   *   }
   * </pre></blockquote>
   *
   * @param id the unique id of the mod
   * @param uuid the target player's uuid
   * @return the custom player data for the target player, with type {@link AttachedPlayer}. If no player is found, a
   *         new object is automatically created and is updated across all player data registries as well as the default
   *         {@link Player} registry.
   * @throws IllegalArgumentException if the mod of id {@code id} has not been registered
   */
  public static @NonNull AttachedPlayer get(String id, String uuid) {
    if (!players.containsKey(id)) throw new NoSuchElementException("No such attachment of name " + id + "!");
    Map<String, AttachedPlayer> map = players.get(id);
    if (!map.containsKey(uuid)) {
      Player player = Player.get(uuid, false);
      updateAcrossAllAttachments(player);
    }
    return map.get(uuid);
  }

  /**
   * Retrieve the data of a specific player of an SMP. Note that this method returns the data with {@link AttachedPlayer}
   * type. To get the player data in the correct class, cast it with:
   *  <blockquote><pre>
   *   public static CustomPlayer get(ServerPlayer player) {
   *     return (CustomPlayer) PlayerManager.get("custom", player);
   *   }
   * </pre></blockquote>
   *
   * @param id the unique id of the mod
   * @param player the target player's {@link ServerPlayer} object
   * @return the custom player data for the target player, with type {@link AttachedPlayer}. If no player is found, a
   *         player is automatically created and is updated across all player data registries as well as the default
   *         {@link Player} registry.
   * @throws IllegalArgumentException if the mod of that {@code id} has not been registered
   */
  public static @NonNull AttachedPlayer get(String id, ServerPlayer player) { return get(id, player.getStringUUID()); }
}