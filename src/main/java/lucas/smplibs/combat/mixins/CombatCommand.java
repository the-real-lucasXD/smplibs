package lucas.smplibs.combat.mixins;

import com.mojang.brigadier.CommandDispatcher;
import lucas.smplibs.player.Player;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lucas.smplibs.config.Config.configs;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Mixin(Commands.class)
class CombatCommand {
  @Shadow @Final
  private CommandDispatcher<CommandSourceStack> dispatcher;
  
  @Inject(method = "<init>", at = @At("RETURN"))
  private void init(Commands.CommandSelection commandSelection, CommandBuildContext context, CallbackInfo ci) {
    this.dispatcher.register(literal("combat")
      .requires(source -> (
        source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) &&
        configs().combat.duration > 0
      ))
      .then(argument("player", EntityArgument.player())
        .then(literal("clear")
          .executes(ctx -> {
            Player player = Player.get(EntityArgument.getPlayer(ctx, "player"));
            if (!player.inCombat())
              ctx.getSource().sendFailure(Component.literal("The selected player is not in combat!"));
            else {
              player.clearCombat();
              ctx.getSource().sendSuccess(() -> Component.literal(
                "The selected player is now out of combat."
              ), true);
            }
            return 0;
          })
        ).then(literal("enter")
          .then(argument("opponent", EntityArgument.player())
            .executes(ctx -> {
              Player player = Player.get(EntityArgument.getPlayer(ctx, "player"));
              Player opponent = Player.get(EntityArgument.getPlayer(ctx, "opponent"));
              player.enterCombat(opponent);
              opponent.enterCombat(player);
              ctx.getSource().sendSuccess(() -> Component.literal(
                "The selected player is now in combat."
              ), true);
              return 0;
            })
          ).executes(ctx -> {
            Player player = Player.get(EntityArgument.getPlayer(ctx, "player"));
            player.enterCombat(null);
            ctx.getSource().sendSuccess(() -> Component.literal(
              "The selected player is now in combat."
            ), true);
            return 0;
          })
        )
      )
    );
  }
}