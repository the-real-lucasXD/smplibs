package lucas.smplibs.teams;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lucas.smplibs.SMPInfo;
import lucas.smplibs.SMPLibs;
import lucas.smplibs.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the utility class for the management of all teams data classes.
 *
 * @since 1.1.1
 */
public final class TeamManager {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final ConcurrentHashMap<String, ConcurrentHashMap<String, Team>> teams = new ConcurrentHashMap<>();

  /**
   * Attaches teams data for all queued SMPs into the registry. This method should not be called, as it is automatically
   * called at the end of server startup. Calling it otherwise may result in unexpected bugs.
   */
  @ApiStatus.Internal
  public static void loadAndAttach() {
    try {
      for (SMPInfo info : ConfigManager.attachments().values()) {
        ConcurrentHashMap<String, Team> values;
        var path = FabricLoader.getInstance().getConfigDir().resolve("smp/" + info.id() + "/teams.json");
        if (Files.exists(path)) {
          try {
            Type type = TypeToken.getParameterized(ConcurrentHashMap.class, String.class, info.teamsClass()).getType();
            values = GSON.fromJson(Files.readString(path), type);
            if (values == null) values = new ConcurrentHashMap<>();
            values.forEach((name, value) -> value.playerTeam = SMPLibs.server.getScoreboard().getPlayerTeam(name));
          } catch (IOException e) {
            SMPLibs.LOGGER.error("An unexpected error occurred while attempting to parse teams data for the mod " +
                "with id '{}'. This could be due to a corrupted or malformed {}/teams.json. Deleting that file " +
                "may fix the problem. The full error log will be included in the exception thrown below.",
              info.id(), info.id()
            );
            throw new RuntimeException(e);
          }
        } else values = new ConcurrentHashMap<>();
        teams.put(info.id(), values);
        save(info.id(), values);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void save(String id, ConcurrentHashMap<String, Team> values) throws IOException {
    var path = FabricLoader.getInstance().getConfigDir().resolve("smp/"+id+"/teams.json");
    Files.createDirectories(path.getParent());
    if (Files.notExists(path)) Files.createFile(path);
    Files.writeString(path, GSON.toJson(values));
  }

  /**
   * Retrieve the team given the name. Note that this method returns the data with {@link Team}
   * type. To get the teams data in the correct class, cast it with:
   *  <blockquote><pre>
   *   public static CustomTeam get(String name) {
   *     return (CustomTeam) CustomTeam.get("custom", name);
   *   }
   * </pre></blockquote>
   *
   * @param id the unique id of the mod
   * @param name the target team's name
   * @return the custom team data for the target team, with type {@link Team}
   */
  public static Team get(String id, String name) {
    ConcurrentHashMap<String, Team> values = teams.get(id);
    if (values == null) return null;
    else return values.get(name);
  }

  /**
   * Retrieve the custom team of a specific player. Note that this method returns the data with {@link Team}
   * type. To get the teams data in the correct class, cast it with:
   *  <blockquote><pre>
   *   public static CustomTeam get(ServerPlayer player) {
   *     return (CustomTeam) CustomTeam.get("custom", player);
   *   }
   * </pre></blockquote>
   *
   * @param id the unique id of the mod
   * @param player the target player's {@link ServerPlayer} object
   * @return the custom team data for the target team, with type {@link Team}
   */
  public static Team get(String id, ServerPlayer player) {
    if (player.getTeam() == null) return null;
    return get(id, player.getTeam().getName());
  }

  /**
   * Refreshes all teams data of all custom SMPs. This method should not be called, as it is automatically called each time
   * the server saves and when teams are modified. Calling it otherwise may result in unexpected bugs.
   */
  @ApiStatus.Internal
  public static void loadAllTeams() {
    try {
      for (PlayerTeam team : SMPLibs.server.getScoreboard().getPlayerTeams()) {
        Map<String, SMPInfo> attachments = ConfigManager.attachments();
        for (String attachment : teams.keySet()) {
          Team toAdd = attachments.get(attachment).teamsClass().getConstructor().newInstance();
          toAdd.playerTeam = team;
          teams.get(attachment).putIfAbsent(attachment, toAdd);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Saves the teams data of all custom SMPs. This method should not be called, as it is automatically called each time
   * the server is saved. Calling it otherwise may result in unexpected bugs.
   */
  @ApiStatus.Internal
  public static void saveAll() {
    teams.forEach((id, values) -> {
      try {
        save(id, values);
      } catch (IOException e) {
        SMPLibs.LOGGER.error("An error occurred while trying to save player data for mod {}: ", id, e);
      }
    });
  }
}