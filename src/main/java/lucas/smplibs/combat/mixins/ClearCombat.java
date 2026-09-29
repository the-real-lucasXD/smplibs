package lucas.smplibs.combat.mixins;

import lucas.smplibs.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerPlayer.class)
class ClearCombat {
  @Inject(method = "die", at = @At("RETURN"))
  private void die(DamageSource source, CallbackInfo ci) {
    Player player = Objects.requireNonNull(Player.get((ServerPlayer) (Object) this));
    if (player.inCombat()) {
      if (player.combat().attacker != null && player.combat().attacker.inCombat()) {
        player.combat().attacker.combat().attacker = null;
      } player.clearCombat();
    }
  }
}
