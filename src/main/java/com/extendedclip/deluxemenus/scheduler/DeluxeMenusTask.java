package com.extendedclip.deluxemenus.scheduler;

/**
 * Abstraction over Bukkit's BukkitTask and Folia's ScheduledTask for cancelling recurring tasks.
 */
public interface DeluxeMenusTask {

    void cancel();
}
