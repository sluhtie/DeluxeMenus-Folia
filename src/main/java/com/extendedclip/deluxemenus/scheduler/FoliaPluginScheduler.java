package com.extendedclip.deluxemenus.scheduler;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Folia-compatible scheduler using Paper's region-based and async schedulers.
 * Only loaded when Folia/Paper scheduler classes are present.
 */
public class FoliaPluginScheduler implements PluginScheduler {

    private static final long TICKS_TO_MILLIS = 50L;

    private final Plugin plugin;
    private final GlobalRegionScheduler globalRegionScheduler;
    private final AsyncScheduler asyncScheduler;
    private final ConcurrentLinkedQueue<ScheduledTask> trackedTasks = new ConcurrentLinkedQueue<>();

    public FoliaPluginScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.globalRegionScheduler = plugin.getServer().getGlobalRegionScheduler();
        this.asyncScheduler = plugin.getServer().getAsyncScheduler();
    }

    @Override
    public void runSync(Plugin plugin, Runnable task) {
        globalRegionScheduler.run(plugin, t -> task.run());
    }

    @Override
    public void runSyncDelayed(Plugin plugin, Runnable task, long delayTicks) {
        ScheduledTask scheduled = globalRegionScheduler.runDelayed(plugin, t -> task.run(), delayTicks);
        if (scheduled != null) {
            trackedTasks.add(scheduled);
        }
    }

    @Override
    public void runSync(Plugin plugin, Entity entity, Runnable task) {
        entity.getScheduler().run(plugin, t -> task.run(), null);
    }

    @Override
    public void runSyncDelayed(Plugin plugin, Entity entity, Runnable task, long delayTicks) {
        entity.getScheduler().runDelayed(plugin, t -> task.run(), null, delayTicks);
    }

    @Override
    public void runAsync(Plugin plugin, Runnable task) {
        asyncScheduler.runNow(plugin, t -> task.run());
    }

    @Override
    public void runAsyncDelayed(Plugin plugin, Runnable task, long delayTicks) {
        long delayMs = delayTicks * TICKS_TO_MILLIS;
        ScheduledTask scheduled = asyncScheduler.runDelayed(plugin, t -> task.run(), delayMs, TimeUnit.MILLISECONDS);
        if (scheduled != null) {
            trackedTasks.add(scheduled);
        }
    }

    @Override
    public DeluxeMenusTask runAsyncTimer(Plugin plugin, Runnable task, long delayTicks, long periodTicks) {
        long initialDelayMs = delayTicks * TICKS_TO_MILLIS;
        long periodMs = periodTicks * TICKS_TO_MILLIS;
        ScheduledTask scheduled = asyncScheduler.runAtFixedRate(plugin, t -> task.run(), initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
        if (scheduled != null) {
            trackedTasks.add(scheduled);
        }
        return new DeluxeMenusTask() {
            @Override
            public void cancel() {
                scheduled.cancel();
                trackedTasks.remove(scheduled);
            }
        };
    }

    @Override
    public DeluxeMenusTask runTimerAtEntity(Plugin plugin, Entity entity, Runnable task, long delayTicks, long periodTicks) {
        ScheduledTask[] ref = new ScheduledTask[1];
        ref[0] = entity.getScheduler().runAtFixedRate(plugin, t -> task.run(), null, delayTicks, periodTicks);
        if (ref[0] != null) {
            trackedTasks.add(ref[0]);
        }
        return new DeluxeMenusTask() {
            @Override
            public void cancel() {
                if (ref[0] != null) {
                    ref[0].cancel();
                    trackedTasks.remove(ref[0]);
                }
            }
        };
    }

    @Override
    public <T> CompletableFuture<T> runSyncWithResult(Plugin plugin, Entity entity, Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        entity.getScheduler().run(plugin, t -> {
            try {
                future.complete(callable.call());
            } catch (Throwable ex) {
                future.completeExceptionally(ex);
            }
        }, null);
        return future;
    }

    @Override
    public void cancelTasks(Plugin plugin) {
        ScheduledTask task;
        while ((task = trackedTasks.poll()) != null) {
            try {
                task.cancel();
            } catch (Exception ignored) {
            }
        }
    }
}
