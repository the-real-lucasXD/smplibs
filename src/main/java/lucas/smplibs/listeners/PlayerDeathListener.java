package lucas.smplibs.listeners;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Contains the listener classes to register events to execute when players die, reducing the need for mixins.
 *
 * @since 1.1.0
 */
public final class PlayerDeathListener {
  /**
   * The listener class that triggers whenever a player dies to another player. Register the method with a code like this:
   * <blockquote><pre>
   *   // No limit
   *   PlayerDeathListener.ByPlayer.register((died, killer) -> {
   *     // your logic here
   *   })
   *
   *   // Limit of 10 calls (Only works for first 10 deaths of server start)
   *   PlayerDeathListener.ByPlayer.register((died, killer) -> {
   *     // your logic here
   *   }, 10)
   * </pre></blockquote>
   */
  public static final class ByPlayer {
    private static final HashMap<BiConsumer<@NonNull ServerPlayer, @NonNull ServerPlayer>, Integer> registered = new HashMap<>();

    public static void register(BiConsumer<@NonNull ServerPlayer, @NonNull ServerPlayer> method) {
      register(method, -1);
    }

    public static void register(BiConsumer<@NonNull ServerPlayer, @NonNull ServerPlayer> method, Integer limit) {
      registered.put(method, limit);
    }

    @ApiStatus.Internal
    public static void trigger(ServerPlayer died, ServerPlayer killer) {
      registered.forEach((method, limit) -> {
        if (limit != -1 && --limit < 0) registered.remove(method);
        else method.accept(died, killer);
      });
    }
  }

  /**
   * The listener class that triggers whenever a player dies from any source, including by other players.
   * Register the method with a code like this:
   * <blockquote><pre>
   *   // No limit
   *   PlayerDeathListener.Any.register((died) -> {
   *     // your logic here
   *   })
   *
   *   // Limit of 10 calls (Only works for first 10 deaths of server start)
   *   PlayerDeathListener.Any.register((died) -> {
   *     // your logic here
   *   }, 10)
   * </pre></blockquote>
   */
  public static class Any {
    private static final HashMap<Consumer<@NonNull ServerPlayer>, Integer> registered = new HashMap<>();

    public static void register(Consumer<@NonNull ServerPlayer> method) {
      register(method, -1);
    }

    public static void register(Consumer<@NonNull ServerPlayer> method, Integer limit) {
      registered.put(method, limit);
    }

    @ApiStatus.Internal
    public static void trigger(ServerPlayer died) {
      registered.forEach((method, limit) -> {
        if (limit != -1 && --limit < 0) registered.remove(method);
        else method.accept(died);
      });
    }
  }
}
