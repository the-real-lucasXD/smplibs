package lucas.smplibs.combat.mixins.limits;

import lucas.smplibs.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lucas.smplibs.config.Config.configs;

@Mixin(LivingEntity.class)
class Totems {
  @Inject(method = "checkTotemDeathProtection", at = @At("HEAD"), cancellable = true)
  private void pop(DamageSource killingDamage, CallbackInfoReturnable<Boolean> cir) {
    if ((Object) this instanceof ServerPlayer serverPlayer) {
      Player player = Player.get(serverPlayer);
      if (!player.inCombat()) return;
      if (player.combat().limitManager.totemsUsed >= configs().combat.limits.totem
        && configs().combat.limits.totem != -1
      ) cir.setReturnValue(false);
    }
  }
  
  @Inject(method = "checkTotemDeathProtection", at = @At("RETURN"))
  private void postPop(DamageSource killingDamage, CallbackInfoReturnable<Boolean> cir) {
    if (!cir.getReturnValueZ()) return;
    if ((Object) this instanceof ServerPlayer serverPlayer) {
      Player player = Player.get(serverPlayer);
      if (!player.inCombat()) return;
      player.combat().limitManager.totemsUsed++;
      if (configs().combat.limits.totem != -1) serverPlayer.sendSystemMessage(Component.literal(String.format(
        "Totems used: %d/%d", player.combat().limitManager.totemsUsed, configs().combat.limits.totem
      )), true);
    }
  }
}
