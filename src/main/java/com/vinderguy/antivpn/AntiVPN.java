package com.vinderguy.antivpn;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;

public final class AntiVPN extends JavaPlugin {

    private final HashSet<String> _whitelistedCountries = new HashSet<>();

    private final HashSet<String> _blacklistedCountries = new HashSet<>();

    private final HashSet<String> _alwaysAllowedIPs = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reload();
    }

    public void reload() {
        reloadConfig();
        final var config = getConfig();

        _whitelistedCountries.clear();
        _whitelistedCountries.addAll(config.getStringList("whitelisted-countries"));

        _blacklistedCountries.clear();
        _blacklistedCountries.addAll(config.getStringList("blacklisted-countries"));

        _alwaysAllowedIPs.clear();
        _alwaysAllowedIPs.addAll(config.getStringList("always-allowed-ips"));
    }
}
