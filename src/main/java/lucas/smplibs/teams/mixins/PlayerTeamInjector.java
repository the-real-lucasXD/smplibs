package lucas.smplibs.teams.mixins;

import com.google.gson.Gson;
import com.mojang.serialization.JsonOps;
import lucas.smplibs.teams.Team;
import lucas.smplibs.teams.TeamRule;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.TeamColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PlayerTeam.class)
class PlayerTeamInjector {
  @Unique private Team getPlayerTeam() {
    return Team.get(((PlayerTeam) (Object) this).getName());
  }

  @Inject(method = "setAllowFriendlyFire", at = @At("HEAD"))
  private void setAllowFriendlyFire(boolean allowFriendlyFire, CallbackInfo ci) {
    getPlayerTeam().friendlyFire = allowFriendlyFire;
  }

  @Inject(method = "setCollisionRule", at = @At("HEAD"))
  private void setCollisionRule(net.minecraft.world.scores.Team.CollisionRule collisionRule, CallbackInfo ci) {
    getPlayerTeam().collision = TeamRule.parse(collisionRule);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Inject(method = "setColor", at = @At("HEAD"))
  private void setColor(Optional<TeamColor> color, CallbackInfo ci) {
    getPlayerTeam().color = color.orElse(TeamColor.WHITE);
  }

  @Inject(method = "setDeathMessageVisibility", at = @At("HEAD"))
  private void setDeathMessageVisibility(net.minecraft.world.scores.Team.Visibility visibility, CallbackInfo ci) {
    getPlayerTeam().deathMessageVisibility = TeamRule.parse(visibility);
  }

  @Inject(method = "setDisplayName", at = @At("HEAD"))
  private void setDisplayName(Component displayName, CallbackInfo ci) {
    Gson gson = new Gson();
    getPlayerTeam().displayName = gson.toJson(
      ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, displayName).getOrThrow()
    );
  }

  @Inject(method = "setNameTagVisibility", at = @At("HEAD"))
  private void setNameTagVisibility(net.minecraft.world.scores.Team.Visibility visibility, CallbackInfo ci) {
    getPlayerTeam().nameTagVisibility = TeamRule.parse(visibility);
  }

  @Inject(method = "setPlayerPrefix", at = @At("HEAD"))
  private void setPlayerPrefix(Component playerPrefix, CallbackInfo ci) {
    Gson gson = new Gson();
    getPlayerTeam().prefix = gson.toJson(
      ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, playerPrefix).getOrThrow()
    );
  }

  @Inject(method = "setPlayerSuffix", at = @At("HEAD"))
  private void setPlayerSuffix(Component playerSuffix, CallbackInfo ci) {
    Gson gson = new Gson();
    getPlayerTeam().suffix = gson.toJson(
      ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, playerSuffix).getOrThrow()
    );
  }

  @Inject(method = "setSeeFriendlyInvisibles", at = @At("HEAD"))
  private void setSeeFriendlyInvisibles(boolean seeFriendlyInvisibles, CallbackInfo ci) {
    getPlayerTeam().seeFriendlyInvisibles = seeFriendlyInvisibles;
  }
}
