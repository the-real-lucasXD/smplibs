package lucas.smplibs.teams.mixins;

import lucas.smplibs.teams.TeamManager;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerScoreboard.class)
class OnTeamAdded {
  @Inject(method = "onTeamAdded", at = @At("RETURN"))
  private void onTeamAdded(PlayerTeam team, CallbackInfo ci) {
    TeamManager.loadAllTeams();
  }

  @Inject(method = "onTeamRemoved", at = @At("RETURN"))
  private void onTeamRemoved(PlayerTeam team, CallbackInfo ci) {
    TeamManager.loadAllTeams();
  }

  @Inject(method = "onTeamChanged", at = @At("RETURN"))
  private void onTeamChanged(PlayerTeam team, CallbackInfo ci) {
    TeamManager.loadAllTeams();
  }
}
