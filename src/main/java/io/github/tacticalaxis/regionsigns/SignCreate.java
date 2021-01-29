package io.github.tacticalaxis.regionsigns;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class SignCreate implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            for (Block block : player.getLineOfSight((Set<Material>) null, 100)) {
                if (RegionSigns.getInstance().isSign(block)) {
                    Location lookingAt = block.getLocation();
                    String loc = lookingAt.getWorld().getName() + ";" + lookingAt.getBlockX() + ";" + lookingAt.getBlockY() + ";" + lookingAt.getBlockZ();
                    if (!(RegionSigns.getInstance().signStringLocations().contains(loc))) {
                        List<String> current = ConfigurationManager.getInstance().getMainConfiguration().getStringList("sign-locations");
                        current.add(loc);
                        ConfigurationManager.getInstance().getMainConfiguration().set("sign-locations", current);
                        ConfigurationManager.getInstance().saveMainConfiguration();
                        RegionSigns.getInstance().reloadConfiguration(null);
                        player.sendMessage(ChatColor.GREEN + "Sign added!");
                    } else {
                        player.sendMessage(ChatColor.GREEN + "Sign already exists at that location!");
                    }
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
        }
        return true;
    }
}
