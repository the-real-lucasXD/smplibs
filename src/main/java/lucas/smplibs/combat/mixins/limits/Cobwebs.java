package lucas.smplibs.combat.mixins.limits;

import lucas.smplibs.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lucas.smplibs.config.Config.configs;

@Mixin(BlockItem.class)
class Cobwebs {
  @Inject(method = "place", at = @At("HEAD"), cancellable = true)
  private void place(BlockPlaceContext placeContext, CallbackInfoReturnable<InteractionResult> cir) {
    if (((BlockItem) (Object) this).getBlock() != Blocks.COBWEB) return;
    if (placeContext.getPlayer() instanceof ServerPlayer serverPlayer) {
      Player player = Player.get(serverPlayer);
      if (player.inCombat()) {
        if (configs().combat.limits.cobweb == 0) serverPlayer.sendSystemMessage(Component.literal(
          "Cobwebs are disabled!"
        ).withColor(TextColor.RED), true);
        else if (player.combat().limitManager.cobwebUsed >= configs().combat.limits.cobweb
          && configs().combat.limits.cobweb != -1
        ) {
          cir.setReturnValue(InteractionResult.FAIL);
          serverPlayer.sendSystemMessage(Component.literal(
            "You have already used " + configs().combat.limits.cobweb + " cobwebs!"
          ).withColor(TextColor.RED), true);
          player.inventoryRefresh();
        } else {
          player.combat().limitManager.cobwebUsed ++;
          if (configs().combat.limits.cobweb != -1) serverPlayer.sendSystemMessage(Component.literal(String.format(
            "Cobwebs used: %d/%d", player.combat().limitManager.cobwebUsed, configs().combat.limits.cobweb
          )), true);
        }
      }
    }
  }
}