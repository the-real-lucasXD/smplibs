package lucas.smplibs.teams;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lucas.smplibs.SMPInfo;
import lucas.smplibs.SMPLibs;
import lucas.smplibs.config.ConfigManager;
import lucas.smplibs.player.Player;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the utility class for the management of all teams data classes.
 *
 * @since 1.2.0
 */
public final class TeamManager {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final Map<String, Map<String, AttachedTeam>> teams = new ConcurrentHashMap<>();

  static void updateAcrossAllAttachments(@NonNull Team team) {
    try {
      for (SMPInfo info : ConfigManager.attachments().values()) {
        AttachedTeam attachedTeam = info.teamsClass().getConstructor().newInstance();
        attachedTeam.team = team;
        teams.get(info.id()).putIfAbsent(team.name, attachedTeam);
      }
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  @ApiStatus.Internal
  public static void loadAndAttach() {
    try {
      if (ConfigManager.attached()) return;
      Path path = SMPLibs.configDir();
      boolean created = false;
      Path resolved = path.resolve("teams.json");
      if (Files.exists(resolved)) {
        Type type = TypeToken.getParameterized(ConcurrentHashMap.class, String.class, Team.class).getType();
        String json = Files.readString(resolved);
        try {
          Team.teams = GSON.fromJson(json, type);
          if (Team.teams != null) created = true;
        } catch (Exception e) {
          SMPLibs.LOGGER.error("Incomplete or corrupted default teams data, resetting");
          Path oldPath = path.resolve("teams-old.jsonc");
          Files.createDirectories(oldPath.getParent());
          Files.writeString(oldPath, """
          /*
            This file has corrupted or incomplete teams data, as such a new
            default file has been created instead to allow the mod to load
            properly. To restore this set of player data,
            1) Look through this file and search for and fix problems with the
               JSON. Referring to the new teams data file can help.
            2) Remove this comment.
            3) Stop the server.
            4) Replace file contents of the original teams data config with
               the contents of this file.
            5) Restart the server. If the problem(s) have been fixed, the server
               should start properly.
          */
          \n
          \n
          """);
          Files.writeString(oldPath, json, StandardOpenOption.APPEND);
          SMPLibs.LOGGER.info("Successfully saved old teams data to teams-old.jsonc");
        }
      } if (!created) Team.teams = new ConcurrentHashMap<>();
      Team.teams.forEach((name, team) -> team.name = name);

      for (SMPInfo info : ConfigManager.attachments().values()) {
        path = SMPLibs.configDir(info.id());
        created = false;
        resolved = path.resolve("teams.json");
        Map<String, AttachedTeam> map = new ConcurrentHashMap<>();
        if (Files.exists(resolved)) {
          Type type = TypeToken.getParameterized(ConcurrentHashMap.class, String.class, info.teamsClass()).getType();
          String json = Files.readString(resolved);
          try {
            map = GSON.fromJson(json, type);
            if (map != null) created = true;
          } catch (Exception e) {
            SMPLibs.LOGGER.error("Incomplete or corrupted teams data for {}, resetting", info.id());
            Path oldPath = path.resolve("teams-old.jsonc");
            Files.createDirectories(oldPath.getParent());
            Files.writeString(oldPath, """
              /*
                This file has corrupted or incomplete teams data, as such a new
                default file has been created instead to allow the mod to load
                properly. To restore this set of player data,
                1) Look through this file and search for and fix problems with the
                   JSON. Referring to the new teams data file can help.
                2) Remove this comment.
                3) Stop the server.
                4) Replace file contents of the original teams data config with
                   the contents of this file.
                5) Restart the server. If the problem(s) have been fixed, the server
                   should start properly.
              */
              \n
              \n
              """);
            Files.writeString(oldPath, json, StandardOpenOption.APPEND);
            SMPLibs.LOGGER.info("Successfully saved old teams data to {}/teams-old.jsonc", info.id());
          }
        } if (!created) map = new ConcurrentHashMap<>();
        map.forEach((name, team) -> team.team = Team.teams.get(name));
        teams.put(info.id(), map);
      }

      Team.teams.forEach((name, _) -> {
        for (SMPInfo info : ConfigManager.attachments().values()) get(info.id(), name);
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  @ApiStatus.Internal
  public static void save() {
    try {
      Path path = SMPLibs.configDir().resolve("teams.json");
      Files.createDirectories(path.getParent());
      Files.writeString(path, GSON.toJson(Team.teams));

      for (SMPInfo info : ConfigManager.attachments().values()) {
        path = SMPLibs.configDir(info.id()).resolve("teams.json");
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(teams.get(info.id())));
      }
    } catch (Exception e) {
      SMPLibs.LOGGER.error("Failed to save teams data", e);
    }
  }

  /**
   * Retrieve the data of a specific team of an SMP. Note that this method returns the data with {@link AttachedTeam}
   * type. To get the teams data in the correct class, cast it with:
   *  <blockquote><pre>
   *   public static CustomTeam get(String name) {
   *     return (CustomTeam) TeamManager.get("custom", name);
   *   }
   * </pre></blockquote>
   *
   * @param id the unique id of the mod
   * @param name the target team's name
   * @return the custom teams data for the target name, with type {@link AttachedTeam}. If no team is found, a
   *         new object is automatically created and is updated across all teams data registries as well as the default
   *         {@link Team} registry.
   * @throws IllegalArgumentException if the mod of id {@code id} has not been registered
   */
  public static @NonNull AttachedTeam get(String id, String name) {
    if (!teams.containsKey(id)) throw new NoSuchElementException("No such attachment of name " + id + "!");
    Map<String, AttachedTeam> map = teams.get(id);
    if (!map.containsKey(name)) {
      Team team = Team.get(name, false);
      updateAcrossAllAttachments(team);
    } return map.get(name);
  }

  /**
   * Retrieve the data of a specific player of an SMP. Note that this method returns the data with {@link AttachedTeam}
   * type. To get the player data in the correct class, cast it with:
   *  <blockquote><pre>
   *   public static CustomTeam get(ServerPlayer player) {
   *     return (CustomTeam) TeamManager.get("custom", player);
   *   }
   * </pre></blockquote>
   *
   * @param id the unique id of the mod
   * @param player the target player's {@link ServerPlayer} object
   * @return the custom teams data for the target player, with type {@link AttachedTeam}. If no team is found,
   *         {@code null} is returned.
   * @throws IllegalArgumentException if the mod of id {@code id} has not been registered
   */
  public static @Nullable AttachedTeam get(String id, ServerPlayer player) {
    if (!teams.containsKey(id)) throw new NoSuchElementException("No such attachment of name " + id + "!");
    Team team = Player.get(player).getTeam();
    if (team != null) return get(id, team.name);
    else return null;
  }

  /**
   * Deletes a team, if it exists, across all team registries for all mods.
   * @param name the name of the team.
   */
  public static void deleteTeam(String name) {
    Team.teams.remove(name);
    teams.forEach((_, team) -> team.remove(name));
  }
}