package com.arhenniuss.servercore.player;

import com.arhenniuss.servercore.element.Element;
import com.arhenniuss.servercore.storage.YamlPlayerStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerDataManager {

    private final YamlPlayerStorage storage;
    private final Logger logger;
    private final Map<UUID, PlayerData> cache;

    public PlayerDataManager(YamlPlayerStorage storage, Logger logger) {
        this.storage = storage;
        this.logger = logger;
        this.cache = new HashMap<>();
    }

    /**
     * Loads player data from storage and caches it.
     * Will not override existing cache entry if already loaded.
     *
     * @param uuid the player's UUID
     */
    public void loadPlayer(UUID uuid) {
        if (cache.containsKey(uuid)) {
            return;
        }
        PlayerData data = storage.load(uuid);
        cache.put(uuid, data);
    }

    /**
     * Saves a player's cached data asynchronously.
     *
     * @param uuid the player's UUID
     * @return a CompletableFuture that completes when the save finishes,
     *         or a completed future if the player is not cached
     */
    public CompletableFuture<Void> savePlayer(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) {
            return CompletableFuture.completedFuture(null);
        }
        return storage.save(data);
    }

    /**
     * Saves a player's data synchronously. Used only during shutdown.
     *
     * @param uuid the player's UUID
     */
    public void savePlayerSync(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null)
            return;
        storage.saveSync(data);
    }

    /**
     * Saves the player data asynchronously, then removes from cache only
     * on successful completion. If the player was never loaded, this is a no-op.
     * If save fails, cache is preserved and error is logged.
     *
     * @param uuid the player's UUID
     */
    public void saveAndRemove(UUID uuid) {
        if (!cache.containsKey(uuid)) {
            return;
        }

        savePlayer(uuid).thenRun(() -> {
            cache.remove(uuid);
        }).exceptionally(ex -> {
            logger.log(Level.WARNING, "Failed to save data for " + uuid + ". Keeping in cache.", ex);
            return null;
        });
    }

    /**
     * Removes a player from the in-memory cache without saving.
     *
     * @param uuid the player's UUID
     */
    public void removePlayer(UUID uuid) {
        cache.remove(uuid);
    }

    /**
     * Retrieves cached player data.
     *
     * @param uuid the player's UUID
     * @return the PlayerData, or null if not cached
     */
    public PlayerData getPlayerData(UUID uuid) {
        return cache.get(uuid);
    }

    /**
     * Fast element lookup for ability listeners in Phase 2+.
     * Returns the player's element directly, or null if not cached
     * or no element chosen yet.
     *
     * @param uuid the player's UUID
     * @return the Element, or null
     */
    public Element getElement(UUID uuid) {
        PlayerData data = cache.get(uuid);
        return data != null ? data.getElement() : null;
    }

    /**
     * Returns an unmodifiable snapshot of all currently cached player UUIDs.
     * Used during shutdown to save all online players.
     *
     * @return set of cached UUIDs
     */
    public Set<UUID> getCachedUUIDs() {
        return Set.copyOf(cache.keySet());
    }
}
