package com.vinderguy.antivpn;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AntiVPNCommandHandler implements CommandExecutor, TabCompleter {

    private final AntiVPN _plugin;

    public AntiVPNCommandHandler(@NotNull final AntiVPN plugin) {
        _plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (_plugin.isProtectionEnabled()) sendPredefinedMessage(sender, "status-enabled");
            else sendPredefinedMessage(sender, "status-disabled");

            return true;
        }

        if (args[0].equalsIgnoreCase("enable")) {
            _plugin.setProtectionEnabled(true);
            sendPredefinedMessage(sender, "enabled");

            return true;
        }

        if (args[0].equalsIgnoreCase("disable")) {
            _plugin.setProtectionEnabled(false);
            sendPredefinedMessage(sender, "disabled");

            return true;
        }

        if (args[0].equalsIgnoreCase("exempt")) {
            if (args.length == 1) {
                sendPredefinedMessage(sender, "exempt-unknown-action");
                return true;
            }

            if (args[1].equalsIgnoreCase("add")) {
                if (args.length == 2) {
                    sendPredefinedMessage(sender, "exempt-add-unknown-ip");
                    return true;
                }

                _plugin.addExemptedIP(args[2]);
                sender.sendMessage(String.format(_plugin.getMessage("exempt-added"), args[2]));
                return true;
            }

            if (args[1].equalsIgnoreCase("remove")) {
                if (args.length == 2) {
                    sendPredefinedMessage(sender, "exempt-remove-unknown-ip");
                    return true;
                }

                _plugin.removeExemptedIP(args[2]);
                sender.sendMessage(String.format(_plugin.getMessage("exempt-removed"), args[2]));
                return true;
            }

            sendPredefinedMessage(sender, "exempt-unknown-action");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            _plugin.reload();
            sendPredefinedMessage(sender, "reloaded");

            return true;
        }

        sendPredefinedMessage(sender, "unknown-command");
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            final var subcommands = new ArrayList<String>();

            subcommands.add("enable");
            subcommands.add("disable");
            subcommands.add("exempt");
            subcommands.add("reload");

            return subcommands;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("exempt")) {
            final var actions = new ArrayList<String>();

            actions.add("add");
            actions.add("remove");

            return actions;
        }

        return Collections.emptyList();
    }

    private void sendPredefinedMessage(@NotNull final CommandSender recipient, @NotNull final String name) {
        recipient.sendMessage(_plugin.getMessage(name));
    }
}
