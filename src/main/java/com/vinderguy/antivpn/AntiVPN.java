package com.vinderguy.antivpn;

import com.google.gson.JsonParser;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
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

    public boolean isCountryWhitelistEnabled() {
        return getConfig().getBoolean("enable-country-whitelist");
    }

    public boolean isCountryWhitelisted(@NotNull final String country) {
        return _whitelistedCountries.contains(country);
    }

    public boolean isCountryBlacklistEnabled() {
        return getConfig().getBoolean("enable-country-blacklist");
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

    public boolean isIPBlocked(@NotNull final String ip) {
        if (isIPAlwaysAllowed(ip)) return false;

        try {
            final var config = getConfig();
            final var request = new URL(String.format("https://vpnapi.io/api/%s?key=%s", ip, config.getString("api-key"))).openConnection();

            request.connect();

            final var rootElement = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent())).getAsJsonObject();

            final var securityElement = rootElement.get("security").getAsJsonObject();
            if (config.getBoolean("block-vpn") && securityElement.get("vpn").getAsBoolean()) return true;
            if (config.getBoolean("block-proxy") && securityElement.get("proxy").getAsBoolean()) return true;
            if (config.getBoolean("block-tor") && securityElement.get("tor").getAsBoolean()) return true;
            if (config.getBoolean("block-relay") && securityElement.get("relay").getAsBoolean()) return true;

            final var country = rootElement.get("location").getAsJsonObject().get("country_code").getAsString();
            return (config.getBoolean("enable-country-whitelist") && !isCountryWhitelisted(country) || config.getBoolean("enable-country-blacklist") && isCountryBlacklisted(country));
        } catch (IOException e) {
            return false;
        }
    }
}
