package lucas.smplibs.listeners;

import lucas.smplibs.SMPLibs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.function.Function;

/**
 * Contains the manager to display multiple system messages simultaneously. Should only be used for
 * messages that should be displayed during the entire gameplay (e.g. points), not for one-time messages (e.g. alerts,
 * points gained messages, etc.) Register the component with a code like this:
 *
 * <blockquote><pre>
 *   SystemMessageManager.register((player) -> {
 *     // your logic here
 *     return Component.literal("Player: " + player.getScoreboardName());
 *   })
 * </pre></blockquote>
 *
 * @since 1.1.1
 */
public class SystemMessageManager {
  private static final ArrayList<Function<ServerPlayer, MutableComponent>> registered = new ArrayList<>();

  public static void register(Function<ServerPlayer, MutableComponent> method) { registered.add(method); }

  @ApiStatus.Internal
  public static void tick() {
    for (ServerPlayer player : SMPLibs.server.getPlayerList().getPlayers()) {
      MutableComponent res = Component.empty();
      for (int i=0; i<registered.size(); i++) {
        res.append(registered.get(i).apply(player));
        if (i < registered.size() - 1) res.append(" | ");
      } player.sendSystemMessage(res, true);
    }
  }
}
