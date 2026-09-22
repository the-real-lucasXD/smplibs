package lucas.smplibs.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lucas.smplibs.SMPLibs;
import lucas.smplibs.SMPInfo;
import lucas.smplibs.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the utility class for the initialization of all player data classes.
 *
 * @since 1.0.0
 */
public class PlayerManager {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final ConcurrentHashMap<String, ConcurrentHashMap<String, AttachedPlayer>> players = new ConcurrentHashMap<>();

  /**
   * Attaches player data for all queued SMPs into the registry. This method should not be called, as it is automatically
   * called at the end of server startup.
   */
  @ApiStatus.Internal
  public static void loadAndAttach() {
    try {
      var path = FabricLoader.getInstance().getConfigDir().resolve("smp/playerdata.json");
      if (Files.exists(path)) {
        try {
          Type type = TypeToken.getParameterized(ConcurrentHashMap.class, String.class, Player.class).getType();
          Player.values = GSON.fromJson(Files.readString(path), type);
          if (Player.values == null) Player.values = new ConcurrentHashMap<>();
        } catch (Exception e) {
          SMPLibs.LOGGER.error("An unexpected error occurred while attempting to parse default player data. " +
            "This could be due to a corrupted or malformed playerdata.json. Deleting that file may fix the problem. " +
            "The full error log will be included in the exception thrown below."
          );
          throw new RuntimeException(e);
        }
      }
      Player.values.forEach((uuid, player) -> player.uuid = uuid);
      saveDefault();

      for (SMPInfo info : ConfigManager.attachments().values()) {
        ConcurrentHashMap<String, AttachedPlayer> values;
        path = FabricLoader.getInstance().getConfigDir().resolve("smp/" + info.id() + "/playerdata.json");
        if (Files.exists(path)) {
          try {
            Type type = TypeToken.getParameterized(ConcurrentHashMap.class, String.class, info.playerClass()).getType();
            values = GSON.fromJson(Files.readString(path), type);
            if (values == null) values = new ConcurrentHashMap<>();
            values.forEach((uuid, value) -> value.setPlayer(Player.get(uuid)));
          } catch (Exception e) {
            SMPLibs.LOGGER.error("An unexpected error occurred while attempting to parse player data for the mod " +
                "with id '{}'. This could be due to a corrupted or malformed {}/playerdata.json. Deleting that file " +
                "may fix the problem. The full error log will be included in the exception thrown below.",
              info.id(), info.id()
            );
            throw new RuntimeException(e);
          }
        } else values = new ConcurrentHashMap<>();
        players.put(info.id(), values);
        save(info.id(), values);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void save(String id, ConcurrentHashMap<String, AttachedPlayer> values) throws IOException {
    var path = FabricLoader.getInstance().getConfigDir().resolve("smp/"+id+"/playerdata.json");
    Files.createDirectories(path.getParent());
    if (Files.notExists(path)) Files.createFile(path);
    Files.writeString(path, GSON.toJson(values));
  }

  /**
   * Saves all default player data. This method should not be called, as it is automatically called each time
   * the server is saved.
   *
   * @throws IOException if an error occurrs while saving player data.
   */
  public static void saveDefault() throws IOException {
    var path = FabricLoader.getInstance().getConfigDir().resolve("smp/playerdata.json");
    Files.createDirectories(path.getParent());
    if (Files.notExists(path)) Files.createFile(path);
    Files.writeString(path, GSON.toJson(Player.values));
  }

  /**
   * Retrieve the data of a specific player of a mod. Note that this method returns the data with {@link AttachedPlayer}
   * type. To get the player data in the correct class, cast it with:
   *  <blockquote><pre>
   *   public static CustomPlayer get(ServerPlayer player) {
   *     return (CustomPlayer) PlayerManager.get("custom", player);
   *   }
   * </pre></blockquote>
   *
   * @param id the unique id of the mod
   * @param player the target player's {@link ServerPlayer} object
   * @return the custom player data for the target player, with type {@link Object}
   */
  public static AttachedPlayer get(String id, ServerPlayer player) {
    try {
      ConcurrentHashMap<String, AttachedPlayer> values = players.get(id);
      if (values == null) return null;

      return getHelper(id, player, values);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Retrieve the data of a specific player of a mod. Note that this method returns the data with {@link AttachedPlayer}
   * type. To get the player data in the correct class, cast it with:
   *  <blockquote><pre>
   *   public static CustomPlayer get(String uuid) {
   *     return (CustomPlayer) PlayerManager.get("custom", uuid);
   *   }
   * </pre></blockquote>
   *
   * @param id the unique id of the mod
   * @param uuid the target player's string uuid
   * @return the custom player data for the target player, with type {@link Object}
   */
  public static AttachedPlayer get(String id, String uuid) {
    ConcurrentHashMap<String, ? extends AttachedPlayer> values = players.get(id);
    if (values == null) return null;

    return values.get(uuid);
  }

  private static AttachedPlayer getHelper(
    String id, ServerPlayer player, ConcurrentHashMap<String, AttachedPlayer> values
  ) throws Exception {
    String uuid = player.getStringUUID();

    if (!values.containsKey(uuid)) {
      Class<? extends AttachedPlayer> playerClass = ConfigManager.attachments().get(id).playerClass();

      AttachedPlayer newInstance = playerClass.getConstructor(Player.class).newInstance(Player.get(player));
      values.put(uuid, newInstance);
    }

    return values.get(uuid);
  }

  /**
   * Loads all required player data of a player. This method should not be called, as it is automatically called
   * each time a player joins.
   */
  @ApiStatus.Internal
  public static void loadAllPlayerAttachments(ServerPlayer player) {
    for (String attachment : ConfigManager.attachments().keySet()) {
      get(attachment, player);
    }
  }

  /**
   * Saves the player data of all custom SMPs. This method should not be called, as it is automatically called each time
   * the server is saved.
   */
  @ApiStatus.Internal
  public static void saveAll() {
    players.forEach((id, values) -> {
      try {
        save(id, values);
      } catch (IOException e) {
        SMPLibs.LOGGER.error("An error occurred while trying to save player data for mod {}: ", id, e);
      }
    });
  }
}