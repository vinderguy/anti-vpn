package com.vinderguy.antivpn;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jetbrains.annotations.NotNull;

public final class PlayerListener implements Listener {

    private final AntiVPN _plugin;

    public PlayerListener(@NotNull final AntiVPN plugin) {
        _plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerLogin(@NotNull final AsyncPlayerPreLoginEvent e) {
        if (_plugin.isProtectionEnabled() && _plugin.isIPBlocked(e.getAddress().getHostAddress())) e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, Component.text(_plugin.getMessage("connection-blocked")));
    }
}
