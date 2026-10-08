package lucas.smplibs.listeners;

import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;

/**
 * Contains the listener class to register events to execute when the server is stopping. Register the method with a code
 * like this:
 *
 * <blockquote><pre>
 *   // No limit
 *   ServerStoppingListener.register(() -> {
 *     // your logic here
 *   });
 *
 *   // Limit of 3 calls
 *   ServerStoppingListener.register(() -> {
 *     // your logic here
 *   }, 3);
 * </pre></blockquote>
 * <strong>Note:</strong> The main SMPLibs mod only calls the registered methods once each time the server stops, meaning setting
 * a limit does not usually show any effect. However, other mods are able to and may trigger the listeners
 * again due to the {@code trigger} method being {@code public} for classes outside this package to access
 * it. As such, the limit should be used as a hard cap to prevent wierd behavior if this happens.
 *
 * @since 1.2.0
 */
public final class ServerStoppingListener {
  private static final HashMap<Runnable, Integer> registered = new HashMap<>();

  public static void register(Runnable method) {
    register(method, -1);
  }

  public static void register(Runnable method, Integer limit) {
    registered.put(method, limit);
  }

  @ApiStatus.Internal
  public static void trigger() {
    registered.forEach((method, limit) -> {
      if (limit != -1 && --limit < 0) registered.remove(method);
      else method.run();
    });
  }
}
