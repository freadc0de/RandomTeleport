package com.slyph.randomteleport.update;

import com.slyph.randomteleport.RandomTeleportPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {

    private static final String OWNER = "slyphmp4";
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
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Accept", "application/vnd.github+json")
                            .build();

                    HttpClient client = HttpClient.newHttpClient();
                    HttpResponse<String> resp =
                            client.send(req, HttpResponse.BodyHandlers.ofString());

                    if (resp.statusCode() != 200) {
                        plugin.getLogger().warning("[UpdateChecker] GitHub API returned " + resp.statusCode());
                        return;
                    }

                    Matcher m = TAG_PATTERN.matcher(resp.body());
                    if (!m.find()) {
                        plugin.getLogger().warning("[UpdateChecker] Can't parse tag_name from GitHub response");
                        return;
                    }

                    String latest  = m.group(1).replaceFirst("^v", "");
                    String current = plugin.getDescription().getVersion();

                    if (latest.equalsIgnoreCase(current)) {
                        banner(List.of(
                                "",
                                "RandomTeleport actual",
                                "Version : " + current,
                                ""
                        ));
                    } else {
                        banner(List.of(
                                "",
                                "New version update!",
                                "Your : " + current,
                                "Latest    : " + latest,
                                "Download  : github.com/" + OWNER + "/" + REPO + "/releases",
                                ""
                        ));
                    }
                } catch (Exception ex) {
                    plugin.getLogger().warning(ex.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void banner(List<String> lines) {
        int max = lines.stream().mapToInt(String::length).max().orElse(0);
        String border = "═".repeat(max + 4);
        plugin.getLogger().info(border);
        lines.forEach(l -> plugin.getLogger().info("  " + l));
        plugin.getLogger().info(border);
    }
}
