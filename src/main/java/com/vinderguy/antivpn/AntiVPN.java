package com.vinderguy.antivpn;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;

public final class AntiVPN extends JavaPlugin {

    private final HashSet<String> _whitelistedCountries = new HashSet<>();

    private final HashSet<String> _blacklistedCountries = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
    }
}
