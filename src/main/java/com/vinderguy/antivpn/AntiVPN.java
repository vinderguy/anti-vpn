package com.vinderguy.antivpn;

import com.google.gson.JsonParser;
import org.bukkit.ChatColor;
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

    private final HashSet<String> _exemptedIPs = new HashSet<>();

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
        return getConfig().getBoolean("enable");
    }

    public void setProtectionEnabled(final boolean enableProtection) {
        getConfig().set("enable", enableProtection);
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
        return _exemptedIPs.contains(ip);
    }

    public void addExemptedIP(@NotNull final String ip) {
        if (_exemptedIPs.add(ip)) {
            getConfig().set("exempted-ips", _exemptedIPs);
            saveConfig();
        }
    }

    public void removeAlwaysAllowedIP(@NotNull final String ip) {
        if (_exemptedIPs.remove(ip)) {
            getConfig().set("exempted-ips", _exemptedIPs);
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
            return (isCountryWhitelistEnabled() && !isCountryWhitelisted(country) || isCountryBlacklistEnabled() && isCountryBlacklisted(country));
        } catch (IOException e) {
            return false;
        }
    }

    public @NotNull String getMessage(@NotNull final String key) {
        final var message = getConfig().getString("messages." + key);
        return message != null ? ChatColor.translateAlternateColorCodes('&', message) : "";
    }
}
