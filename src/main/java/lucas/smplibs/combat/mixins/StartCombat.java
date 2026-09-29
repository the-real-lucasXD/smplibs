package lucas.smplibs.combat.mixins;

import lucas.smplibs.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
class StartCombat {
  @Inject(method = "hurtServer", at = @At("HEAD"))
  private void hurt(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
    Player victim = Player.get((ServerPlayer) (Object) this);

    Entity indirectSource = source.getEntity();
    Entity directSource = source.getDirectEntity();

    Player attacker = null;
    if (indirectSource instanceof ServerPlayer serverPlayer) attacker = Player.get(serverPlayer);
    else if (directSource instanceof ServerPlayer serverPlayer) attacker = Player.get(serverPlayer);

    if (attacker != null && !attacker.equals(victim)) {
      victim.enterCombat(attacker);
      attacker.enterCombat(victim);
    }
  }
}
