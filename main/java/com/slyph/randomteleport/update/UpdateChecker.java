package com.slyph.randomteleport.update;

import com.slyph.randomteleport.RandomTeleportPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {

    private static final String OWNER = "freadc0de";
    private static final String REPO  = "RandomTeleport";

    private static final Pattern TAG_PATTERN =
            Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");

    private final RandomTeleportPlugin plugin;

    public UpdateChecker(RandomTeleportPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkAsync() {
        new BukkitRunnable() {
            @Override public void run() {
                try {
                    String url = "https://api.github.com/repos/" + OWNER + "/" + REPO + "/releases/latest";
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest req  = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Accept", "application/vnd.github+json")
                            .build();

                    HttpResponse<String> resp =
                            client.send(req, HttpResponse.BodyHandlers.ofString());

                    if (resp.statusCode() != 200) {
                        plugin.getLogger().warning("[UpdateChecker] GitHub API returned " + resp.statusCode());
                        return;
                    }

                    Matcher m = TAG_PATTERN.matcher(resp.body());
                    if (!m.find()) {
                        plugin.getLogger().warning("[UpdateChecker] Unable to parse tag_name in GitHub response");
                        return;
                    }

                    String latest  = m.group(1).replaceFirst("^v", "");
                    String current = plugin.getDescription().getVersion();

                    if (!latest.equalsIgnoreCase(current)) {
                        plugin.getLogger().info(
                                "§e[RandomTeleport] Доступна новая версия §b" + latest +
                                        "§e (текущая §c" + current + "§e)"
                        );
                    } else {
                        plugin.getLogger().info("[RandomTeleport] Плагин актуален (v" + current + ")");
                    }
                } catch (Exception ex) {
                    plugin.getLogger().warning("[UpdateChecker] " + ex.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}
