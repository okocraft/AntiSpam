package net.okocraft.antispam;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

public class AntiSpamCommand implements CommandExecutor, TabExecutor {

    private final Config config = Config.getConfig();

    AntiSpamCommand(AntiSpam plugin) {
        plugin.getCommand("antispam").setExecutor(this);
        plugin.getCommand("antispam").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(config.getMessage("Languages.Error.NotEnoughArguments"));
            return false;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("reload")) {
            config.reloadConfig();
            sender.sendMessage(config.getMessage("Languages.Info.Reload"));
            return true;
        }

        if (subCommand.equals("ngword")) {
            if (args.length == 1) {
                sender.sendMessage(config.getMessage("Languages.Error.NotEnoughArguments"));
                return false;
            }

            String ngWordSubCommand = args[1].toLowerCase();

            if (ngWordSubCommand.equals("add")) {
                if (args.length == 2) {
                    sender.sendMessage(config.getMessage("Languages.Error.NotEnoughArguments"));
                    return false;
                }

                ChatLimiter.addNgWord(args[2]);
                sender.sendMessage(config.getMessage("Languages.Info.AddNgWord"));
                return true;
            }

            if (ngWordSubCommand.equals("remove")) {
                if (args.length == 2) {
                    sender.sendMessage(config.getMessage("Languages.Error.NotEnoughArguments"));
                    return false;
                }

                ChatLimiter.removeNgWord(args[2]);
                sender.sendMessage(config.getMessage("Languages.Info.RemoveNgWord"));
                return true;
            }

            if (ngWordSubCommand.equals("list")) {
                sender.sendMessage(config.getMessage("Languages.Info.ListNgWordHeader"));
                config.getNgWords().forEach(word -> sender
                        .sendMessage(config.getMessage("Languages.Info.ListNgWord").replaceAll("%word%", word)));
                return true;
            }
        }

        sender.sendMessage(config.getMessage("Languages.Error.ArgumentDoesNotExist"));
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        List<String> subCommands = List.of("reload", "ngword");

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], subCommands, result);
        }

        if (!subCommands.contains(args[0])) {
            return result;
        }

        String subCommand = args[0].toLowerCase();

        if (args.length == 2 && subCommand.equals("ngword")) {
            return StringUtil.copyPartialMatches(args[1], List.of("add", "remove", "list"), result);
        }

        String ngWordSubCommand = args[1].toLowerCase();
        if (!List.of("add", "remove").contains(ngWordSubCommand)) {
            return result;
        }

        if (args.length == 3) {
            switch (ngWordSubCommand) {
            case "add":
                return StringUtil.copyPartialMatches(args[2], List.of("<NewRuleRegex>"), result);
            case "remove":
                return StringUtil.copyPartialMatches(args[2], Config.getConfig().getNgWords(), result);
            }
        }

        return result;
    }

}