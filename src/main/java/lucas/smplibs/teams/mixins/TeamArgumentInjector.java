package lucas.smplibs.teams.mixins;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lucas.smplibs.teams.Team;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.TeamArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(TeamArgument.class)
class TeamArgumentInjector {
  @Inject(method = "listSuggestions", at = @At("HEAD"), cancellable = true)
  public <S> void injectCustomTeams(CommandContext<S> contextBuilder, SuggestionsBuilder builder,
                                    CallbackInfoReturnable<CompletableFuture<Suggestions>> cir) {
    if (contextBuilder.getSource() instanceof SharedSuggestionProvider) {
      Collection<String> myCustomTeams = Team.teams().keySet();
      cir.setReturnValue(SharedSuggestionProvider.suggest(myCustomTeams, builder));
    }
  }
}
