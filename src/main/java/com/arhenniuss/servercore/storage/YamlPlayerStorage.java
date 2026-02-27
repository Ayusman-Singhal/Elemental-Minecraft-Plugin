package com.arhenniuss.servercore.storage;

import com.arhenniuss.servercore.element.Element;
import com.arhenniuss.servercore.player.PlayerData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class YamlPlayerStorage {

    private final JavaPlugin plugin;
    private final File dataFolder;

    public YamlPlayerStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    /**
     * Loads player data synchronously from YAML file.
     * Does not touch any Bukkit API — only reads from file system.
     *
     * @param uuid the player's UUID
     * @return the loaded PlayerData (element may be null if not yet chosen)
     */
    public PlayerData load(UUID uuid) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        if (!file.exists()) {
            return new PlayerData(uuid, null);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String elementName = config.getString("element", null);
        Element element = Element.fromName(elementName);
        return new PlayerData(uuid, element);
    }

    /**
     * Saves player data asynchronously. Serializes all values to a YAML
     * string on the calling (main) thread, then writes the raw string
     * to disk on an async thread. No Bukkit API objects cross the thread boundary.
     *
     * @param data the player data to save
     * @return a CompletableFuture that completes when the file is written
     */
    public CompletableFuture<Void> save(PlayerData data) {
        // Serialize to YAML string on main thread — no Bukkit API objects involved
        String uuidString = data.getUuid().toString();
        String yamlContent = serializeToString(uuidString,
                data.hasElement() ? data.getElement().name() : null);
        String fileName = uuidString + ".yml";

        CompletableFuture<Void> future = new CompletableFuture<>();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                writeRawFile(fileName, yamlContent);
                future.complete(null);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + uuidString, e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Saves player data synchronously on the calling thread.
     * Used only during onDisable() to guarantee persistence before shutdown.
     *
     * @param data the player data to save
     */
    public void saveSync(PlayerData data) {
        String uuidString = data.getUuid().toString();
        String yamlContent = serializeToString(uuidString,
                data.hasElement() ? data.getElement().name() : null);
        String fileName = uuidString + ".yml";

        try {
            writeRawFile(fileName, yamlContent);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to synchronously save player data for " + uuidString, e);
        }
    }

    /**
     * Deletes the YAML file for the given player.
     *
     * @param uuid the player's UUID
     */
    public void delete(UUID uuid) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Builds the YAML content as a String on the calling thread.
     * Uses only primitive/String values — safe to produce anywhere.
     */
    private String serializeToString(String uuidString, String elementName) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("uuid", uuidString);
        config.set("element", elementName);
        return config.saveToString();
    }

    /**
     * Writes a pre-serialized YAML string to disk.
     * Contains no Bukkit API calls — safe to call from any thread.
     */
    private void writeRawFile(String fileName, String content) throws IOException {
        File file = new File(dataFolder, fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}
