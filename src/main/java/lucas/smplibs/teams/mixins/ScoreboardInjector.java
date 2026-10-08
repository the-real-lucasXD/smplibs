package lucas.smplibs.teams.mixins;

import com.google.gson.Gson;
import com.mojang.serialization.JsonOps;
import lucas.smplibs.player.Player;
import lucas.smplibs.teams.Team;
import lucas.smplibs.teams.TeamManager;
import lucas.smplibs.teams.TeamRule;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.TeamColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Optional;

@Mixin(Scoreboard.class)
abstract class ScoreboardInjector {
  @Shadow public abstract void onTeamAdded(PlayerTeam team);
  @Shadow public abstract void onTeamRemoved(PlayerTeam team);

  @Inject(method = "addPlayerTeam", at = @At("HEAD"), cancellable = true)
  private void addPlayerTeam(String name, CallbackInfoReturnable<PlayerTeam> cir) {
    PlayerTeam team = Team.get(name).asPlayerTeam();
    onTeamAdded(team);
    cir.setReturnValue(team);
  }

  @Inject(method = "addPlayerToTeam", at = @At("HEAD"), cancellable = true)
  private void addPlayerToTeam(String player, PlayerTeam team, CallbackInfoReturnable<Boolean> cir) {
    String uuid = Team.getUUID(player);
    if (uuid == null) cir.setReturnValue(false);
    else {
      Team customTeam = Team.getWithoutCreating(team.getName());
      if (customTeam != null) customTeam.addToTeam(uuid);
      cir.setReturnValue(customTeam != null);
    }
  }

  @Inject(method = "getPlayersTeam", at = @At("HEAD"), cancellable = true)
  private void getPlayersTeam(String name, CallbackInfoReturnable<PlayerTeam> cir) {
    String uuid = Team.getUUID(name);
    if (uuid == null) cir.setReturnValue(null);
    Team team = Player.get(uuid).getTeam();
    if (team == null) cir.setReturnValue(null);
    else cir.setReturnValue(team.asPlayerTeam());
  }

  @Inject(method = "removePlayerTeam", at = @At("HEAD"), cancellable = true)
  private void removePlayerTeam(PlayerTeam team, CallbackInfo ci) {
    TeamManager.deleteTeam(team.getName());
    onTeamRemoved(team);
    ci.cancel();
  }

  @Inject(method = "loadPlayerTeam", at = @At("HEAD"), cancellable = true)
  private void loadPlayerTeam(PlayerTeam.Packed packed, CallbackInfo ci) {
    Gson gson = new Gson();
    Team team = Team.get(packed.name());
    Optional<Component> displayName = packed.displayName();
    String displayNameSerialized = packed.name();
    if (displayName.isPresent()) displayNameSerialized = gson.toJson(
      ComponentSerialization.CODEC
        .encodeStart(JsonOps.INSTANCE, displayName.get())
        .getOrThrow()
    );

    Component prefix = packed.memberNamePrefix();
    String prefixSerialized = gson.toJson(
      ComponentSerialization.CODEC
        .encodeStart(JsonOps.INSTANCE, prefix)
        .getOrThrow()
    );

    Component suffix = packed.memberNameSuffix();
    String suffixSerialized = gson.toJson(
      ComponentSerialization.CODEC
        .encodeStart(JsonOps.INSTANCE, suffix)
        .getOrThrow()
    );

    team.displayName = displayNameSerialized;
    team.color = packed.color().orElse(TeamColor.WHITE);
    team.friendlyFire = packed.allowFriendlyFire();
    team.seeFriendlyInvisibles = packed.seeFriendlyInvisibles();
    team.prefix = prefixSerialized;
    team.suffix = suffixSerialized;
    team.nameTagVisibility = TeamRule.parse(packed.nameTagVisibility());
    team.deathMessageVisibility = TeamRule.parse(packed.deathMessageVisibility());
    team.collision = TeamRule.parse(packed.collisionRule());
    for (String player : packed.players()) {
      String uuid = Team.getUUID(player);
      if (uuid != null) team.addToTeam(Team.getUUID(player));
    }

    ci.cancel();
  }

  @Inject(method = "getPlayerTeam", at = @At("HEAD"), cancellable = true)
  private void getPlayerTeam(String name, CallbackInfoReturnable<PlayerTeam> cir) {
    Team team = Team.getWithoutCreating(name);
    if (team == null) cir.setReturnValue(null);
    else cir.setReturnValue(Team.get(name).asPlayerTeam());
  }

  @Inject(method = "getTeamNames", at = @At("HEAD"), cancellable = true)
  private void getTeamNames(CallbackInfoReturnable<Collection<String>> cir) {
    cir.setReturnValue(Team.teams().keySet());
  }

  @Inject(method = "getPlayerTeams", at = @At("HEAD"), cancellable = true)
  private void getPlayerTeams(CallbackInfoReturnable<Collection<PlayerTeam>> cir) {
    cir.setReturnValue(Team.playerTeams());
  }

  @Inject(method = "removePlayerFromTeam(Ljava/lang/String;)Z", at = @At("HEAD"), cancellable = true)
  private void removePlayerFromTeam(String player, CallbackInfoReturnable<Boolean> cir) {
    cir.setReturnValue(Player.get(Team.getUUID(player)).leaveTeam());
  }

  @Inject(method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V",
    at = @At("HEAD"), cancellable = true)
  private void removePlayerFromTeam(String player, PlayerTeam team, CallbackInfo ci) {
    Player.get(Team.getUUID(player)).leaveTeam();
    ci.cancel();
  }
}
