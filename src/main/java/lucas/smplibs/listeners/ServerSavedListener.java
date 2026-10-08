package lucas.smplibs.listeners;

import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;

/**
 * Contains the listener class to register events to execute when the server saves. Register the method with a code
 * like this:
 *
 * <blockquote><pre>
 *   // No limit
 *   ServerSavedListener.register(() -> {
 *     // your logic here
 *   });
 *
 *   // Limit of 3 calls
 *   ServerSavedListener.register(() -> {
 *     // your logic here
 *   }, 3);
 * </pre></blockquote>
 *
 * @since 1.2.0
 */
public final class ServerSavedListener {
  private static final HashMap<Runnable, Integer> registered = new HashMap<>();

  public static void register(Runnable method) { register(method, -1); }
  public static void register(Runnable method, Integer limit) { registered.put(method, limit); }

  @ApiStatus.Internal
  public static void trigger() {
    registered.forEach((method, limit) -> {
      if (limit != -1 && --limit < 0) registered.remove(method);
      else method.run();
    });
  }
}
