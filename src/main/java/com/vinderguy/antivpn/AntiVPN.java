package com.vinderguy.antivpn;

import com.google.gson.JsonParser;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public final class AntiVPN extends JavaPlugin {

    private final HashSet<String> _whitelistedCountries = new HashSet<>();

    private final HashSet<String> _blacklistedCountries = new HashSet<>();

    private final HashSet<String> _exemptedIPs = new HashSet<>();

    private final HashSet<String> _cachedBlockedIPs = new HashSet<>();

    private final HashSet<String> _cachedAllowedIPs = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reload();

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        final var antiVPNCommand = getCommand("antivpn");
        if (antiVPNCommand != null) {
            final var antiVPNCommandHandler = new AntiVPNCommandHandler(this);

            antiVPNCommand.setExecutor(antiVPNCommandHandler);
            antiVPNCommand.setTabCompleter(antiVPNCommandHandler);
        }
    }

    public boolean isProtectionEnabled() {
        return getConfig().getBoolean("enable-protection");
    }

    public void setProtectionEnabled(final boolean enableProtection) {
        getConfig().set("enable-protection", enableProtection);
        saveConfig();
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

    public boolean isIPExempted(@NotNull final String ip) {
        return _exemptedIPs.contains(ip);
    }

    public boolean addExemptedIP(@NotNull final String ip) {
        if (_exemptedIPs.add(ip)) {
            getConfig().set("exempted-ips", List.copyOf(_exemptedIPs));
            saveConfig();

            return true;
        }

        return false;
    }

    public boolean removeExemptedIP(@NotNull final String ip) {
        if (_exemptedIPs.remove(ip)) {
            getConfig().set("exempted-ips", List.copyOf(_exemptedIPs));
            saveConfig();

            return true;
        }

        return false;
    }

    public boolean isIPBlocked(@NotNull final String ip) {
        if (isIPExempted(ip) || _cachedAllowedIPs.contains(ip)) return false;
        if (_cachedBlockedIPs.contains(ip)) return true;

        try {
            final var config = getConfig();
            final var request = new URL(String.format("https://vpnapi.io/api/%s?key=%s", ip, config.getString("api-key"))).openConnection();

            request.connect();

            final var rootObject = JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent())).getAsJsonObject();
            final var securityElement = rootObject.get("security").getAsJsonObject();

            if (config.getBoolean("block-vpns") && securityElement.get("vpn").getAsBoolean()) {
                _cachedBlockedIPs.add(ip);
                return true;
            }

            if (config.getBoolean("block-proxies") && securityElement.get("proxy").getAsBoolean()) {
                _cachedBlockedIPs.add(ip);
                return true;
            }

            if (config.getBoolean("block-tor-nodes") && securityElement.get("tor").getAsBoolean()) {
                _cachedBlockedIPs.add(ip);
                return true;
            }

            if (config.getBoolean("block-relays") && securityElement.get("relay").getAsBoolean()) {
                _cachedBlockedIPs.add(ip);
                return true;
            }

            final var country = rootObject.get("location").getAsJsonObject().get("country_code").getAsString();
            if (isCountryWhitelistEnabled() && !isCountryWhitelisted(country) || isCountryBlacklistEnabled() && isCountryBlacklisted(country)) {
                _cachedBlockedIPs.add(ip);
                return true;
            }
        } catch (@NotNull final Exception e) {
            getLogger().warning(String.format("Failed to verify IP: \"%s\". Error: %s", ip, e.getMessage()));
        }

        _cachedAllowedIPs.add(ip);
        return false;
    }

    public @NotNull Collection<String> getCachedBlockedIPs() {
        return _cachedBlockedIPs;
    }

    public @NotNull String getMessage(@NotNull final String key) {
        final var message = getConfig().getString("messages." + key);
        return message != null ? ChatColor.translateAlternateColorCodes('&', message) : "";
    }

    public void reload() {
        reloadConfig();

        final var config = getConfig();

        _whitelistedCountries.clear();
        _whitelistedCountries.addAll(config.getStringList("whitelisted-countries"));

        _blacklistedCountries.clear();
        _blacklistedCountries.addAll(config.getStringList("blacklisted-countries"));

        _exemptedIPs.clear();
        _exemptedIPs.addAll(config.getStringList("exempted-ips"));

        _cachedBlockedIPs.clear();
        _cachedAllowedIPs.clear();
    }
}
