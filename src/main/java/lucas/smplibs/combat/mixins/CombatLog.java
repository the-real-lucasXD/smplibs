package lucas.smplibs.combat.mixins;

import lucas.smplibs.player.Player;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
class CombatLog {
  @Inject(method = "onDisconnect", at = @At("HEAD"))
  private void onDisconnect(DisconnectionDetails details, CallbackInfo ci) {
    ServerPlayer serverPlayer = ((ServerGamePacketListenerImpl) (Object) this).player;
    Player player = Player.get(serverPlayer);
    if (player.inCombat()) {
      player.combat().combatLogged = true;
      serverPlayer.setHealth(0);
      if (player.combat().attacker != null) serverPlayer.die(
        new DamageSource(serverPlayer.damageSources().genericKill().typeHolder(), player.combat().attacker.serverPlayer())
      );
      else serverPlayer.die(serverPlayer.damageSources().genericKill());
    }
    player.online = false;
  }
}
