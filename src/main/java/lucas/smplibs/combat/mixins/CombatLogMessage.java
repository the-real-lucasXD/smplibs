package lucas.smplibs.combat.mixins;

import lucas.smplibs.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(CombatTracker.class)
class CombatLogMessage {
  @Shadow @Final private LivingEntity mob;

  @Inject(method = "getDeathMessage", at = @At("HEAD"), cancellable = true)
  private void getDeathMessage(CallbackInfoReturnable<Component> cir) {
    if (mob instanceof ServerPlayer serverPlayer) {
      Player player = Player.get(serverPlayer);
      if (player.inCombat() && player.combat().combatLogged) {
        if (player.combat().attacker != null) {
          cir.setReturnValue(Component.literal(
            String.format("%s combat logged while fighting %s",
              serverPlayer.getScoreboardName(),
              Objects.requireNonNull(player.combat().attacker.serverPlayer()).getScoreboardName()
            )
          ));
        } else cir.setReturnValue(Component.literal(serverPlayer.getScoreboardName() + " combat logged"));
        player.combat().combatLogged = false;
      }
    }
  }
}
