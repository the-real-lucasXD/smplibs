package lucas.smplibs.mixins;

import lucas.smplibs.SMPLibs;
import lucas.smplibs.player.Player;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
class OnPlayerJoin {
  @Inject(method = "placeNewPlayer", at = @At("HEAD"), cancellable = true)
  private void placeNewPlayer(Connection connection, ServerPlayer player,
                              CommonListenerCookie cookie, CallbackInfo ci) {
    try {
      Player defaultPlayer = Player.get(player);
      defaultPlayer.online = true;
    } catch (Exception e) {
      SMPLibs.LOGGER.error("An error occurred while trying to load configs for {}", player.getScoreboardName(), e);
      var reason = Component.literal("Error while loading player data");
      connection.send(new ClientboundDisconnectPacket(reason));
      connection.disconnect(reason);
      ci.cancel();
    }
  }
}
