package com.extendedclip.deluxemenus.scheduler;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler abstraction that works on both Bukkit/Spigot and Folia.
 * On Folia, uses EntityScheduler, GlobalRegionScheduler and AsyncScheduler.
 * On Bukkit, uses the standard Bukkit scheduler.
 */
public interface PluginScheduler {

    void runSync(Plugin plugin, Runnable task);

    void runSyncDelayed(Plugin plugin, Runnable task, long delayTicks);

    void runSync(Plugin plugin, Entity entity, Runnable task);

    void runSyncDelayed(Plugin plugin, Entity entity, Runnable task, long delayTicks);

    void runAsync(Plugin plugin, Runnable task);

    void runAsyncDelayed(Plugin plugin, Runnable task, long delayTicks);

    /**
     * Schedules a repeating task on the async scheduler.
     *
     * @param delayTicks  delay before first run
     * @param periodTicks period between runs
     * @return a task that can be cancelled
     */
    DeluxeMenusTask runAsyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks);

    /**
     * Schedules a repeating task on the entity's region (for placeholder/refresh updates).
     *
     * @param delayTicks  delay before first run
     * @param periodTicks period between runs
     * @return a task that can be cancelled
     */
    DeluxeMenusTask runTimerAtEntity(Plugin plugin, Entity entity, Runnable task, long delayTicks, long periodTicks);

    /**
     * Runs a callable on the entity's region and returns a future with the result.
     * Used for sync operations that need to return a value (e.g. MMOItems getItem).
     */
    <T> CompletableFuture<T> runSyncWithResult(Plugin plugin, Entity entity, Callable<T> callable);

    void cancelTasks(Plugin plugin);
}
