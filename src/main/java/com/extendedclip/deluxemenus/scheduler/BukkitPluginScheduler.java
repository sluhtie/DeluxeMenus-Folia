package com.extendedclip.deluxemenus.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitPluginScheduler implements PluginScheduler {

    private static final long TICKS_TO_MS = 50L;

    private final ConcurrentHashMap<Integer, BukkitTask> trackedTasks = new ConcurrentHashMap<>();

    @Override
    public void runSync(Plugin plugin, Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void runSyncDelayed(Plugin plugin, Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    @Override
    public void runSync(Plugin plugin, Entity entity, Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    @Override
    public void runSyncDelayed(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    @Override
    public void runAsync(Plugin plugin, Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    @Override
    public void runAsyncDelayed(Plugin plugin, Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    @Override
    public DeluxeMenusTask runAsyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
        trackedTasks.put(bukkitTask.getTaskId(), bukkitTask);
        return new DeluxeMenusTask() {
            @Override
            public void cancel() {
                bukkitTask.cancel();
                trackedTasks.remove(bukkitTask.getTaskId());
            }
        };
    }

    @Override
    public DeluxeMenusTask runTimerAtEntity(Plugin plugin, Entity entity, Runnable task, long delayTicks, long periodTicks) {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
        trackedTasks.put(bukkitTask.getTaskId(), bukkitTask);
        return new DeluxeMenusTask() {
            @Override
            public void cancel() {
                bukkitTask.cancel();
                trackedTasks.remove(bukkitTask.getTaskId());
            }
        };
    }

    @Override
    public <T> CompletableFuture<T> runSyncWithResult(Plugin plugin, Entity entity, Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                future.complete(callable.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    @Override
    public void cancelTasks(Plugin plugin) {
        for (BukkitTask task : trackedTasks.values()) {
            try {
                task.cancel();
            } catch (Exception ignored) {
            }
        }
        trackedTasks.clear();
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}
