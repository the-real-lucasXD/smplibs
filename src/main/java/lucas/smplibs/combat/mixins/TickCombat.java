package lucas.smplibs.combat.mixins;

import lucas.smplibs.player.Player;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static lucas.smplibs.config.Config.configs;
import static lucas.smplibs.player.Player.players;

@Mixin(MinecraftServer.class)
class TickCombat {
  @Inject(method = "tickServer", at = @At("HEAD"))
  private void tickServer(CallbackInfo ci) {
    for (Player player : players().values()) {
      if (player.online) {
        if (player.inCombat()) {
          player.combat().tick();
          if (Objects.requireNonNull(player.serverPlayer()).isFallFlying() && !configs().combat.allowElytraFlight)
            Objects.requireNonNull(player.serverPlayer()).stopFallFlying();
        }
      }
    }
  }
}