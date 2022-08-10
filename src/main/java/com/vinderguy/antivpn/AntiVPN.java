package com.vinderguy.antivpn;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

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

    public void enable() {
        getConfig().set("enable", true);
        saveConfig();
    }

    public void disable() {
        getConfig().set("enable", false);
        saveConfig();
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

    public boolean isCountryWhitelisted(@NotNull final String country) {
        return _whitelistedCountries.contains(country);
    }

    public boolean isCountryBlacklisted(@NotNull final String country) {
        return _blacklistedCountries.contains(country);
    }

    public boolean isIPAlwaysAllowed(@NotNull final String ip) {
        return _alwaysAllowedIPs.contains(ip);
    }

    public void addAlwaysAllowedIP(@NotNull final String ip) {
        if (_alwaysAllowedIPs.add(ip)) {
            getConfig().set("always-allowed-ips", _alwaysAllowedIPs);
            saveConfig();
        }
    }

    public void removeAlwaysAllowedIP(@NotNull final String ip) {
        if (_alwaysAllowedIPs.remove(ip)) {
            getConfig().set("always-allowed-ips", _alwaysAllowedIPs);
            saveConfig();
        }
    }
}
