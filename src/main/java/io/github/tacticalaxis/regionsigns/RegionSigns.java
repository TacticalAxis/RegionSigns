package io.github.tacticalaxis.regionsigns;

import me.wiefferink.areashop.AreaShop;
import me.wiefferink.areashop.regions.GeneralRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class RegionSigns extends JavaPlugin implements CommandExecutor, Listener {

    private static RegionSigns instance;
    private static AreaShop areaShop;

    public static RegionSigns getInstance() {
        return instance;
    }

    public static AreaShop getAreaShop() {
        return areaShop;
    }

    @Override
    public void onEnable() {
        instance = this;
        try {
            areaShop = (AreaShop) Bukkit.getPluginManager().getPlugin("AreaShop");
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "AreaShop plugin not found. Disabling.");
            getServer().getPluginManager().disablePlugin(this);
        }
        getCommand("rs").setExecutor(this);
        getCommand("setsign").setExecutor(new SignCreate());
        ConfigurationManager.getInstance().setupConfiguration();
        setSigns();
        getServer().getPluginManager().registerEvents(this, this);
    }


    @Override
    public void onDisable() {
        instance = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        reloadConfiguration(sender);
        return true;
    }

    public ArrayList<String> getNonRentedRegions() {
        ArrayList<String> nonRented = new ArrayList<>();
        for (GeneralRegion region : getAreaShop().getFileManager().getRegions()) {
            if (region.getOwner() == null) {
                nonRented.add(region.getName());
            }
        }
        nonRented.sort(String::compareToIgnoreCase);;
        return nonRented;
    }

    public ArrayList<String> signStringLocations() {
        return (ArrayList<String>) ConfigurationManager.getInstance().getMainConfiguration().getStringList("sign-locations");
    }

    public ArrayList<Location> signLocations() {
        ArrayList<String> signs = signStringLocations();
        ArrayList<Location> locations = new ArrayList<>();
        for (String sign : signs) {
            String[] data = sign.split(";");
            locations.add(new Location(Bukkit.getWorld(data[0]), Double.parseDouble(data[1]),Double.parseDouble(data[2]),Double.parseDouble(data[3])));
        }
        return locations;
    }

    public void reloadConfiguration(CommandSender sender) {
        ConfigurationManager.getInstance().reloadConfiguration();
        if (sender != null) {
            sender.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
        }
    }

    public void setSigns() {
        new BukkitRunnable() {
            @Override
            public void run() {
                ArrayList<String> copy = getNonRentedRegions();
                int current = 0;
                for (Location location : signLocations()) {
                    if (isSign(location.getBlock())) {
                        Sign sign = (Sign) location.getBlock().getState();
                        int signline = 0;
                        for (String s : sign.getLines()) {
                            try {
                                sign.setLine(signline, copy.get(current));
                            } catch (Exception e) {
                                sign.setLine(signline, "------");
                            }
                            signline ++;
                            current ++;
                        }
                        sign.update();
                    }
                }
            }
        }.runTaskTimer(this, 0L, 60L);
    }

    public boolean isSign(Block block) {
        return block.getType() == Material.SIGN || block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN;
    }

    @EventHandler
    public void breakBlock(BlockBreakEvent event) {
        if (signLocations().contains(event.getBlock().getLocation())) {
            Location blockLocation = event.getBlock().getLocation();
            String loc = blockLocation.getWorld().getName() + ";" + blockLocation.getBlockX() + ";" + blockLocation.getBlockY() + ";" + blockLocation.getBlockZ();
            List<String> all = new ArrayList<>();
            for (String s : signStringLocations()) {
                if (!(s.toLowerCase().equalsIgnoreCase(loc))) {
                    all.add(s);
                }
            }
            ConfigurationManager.getInstance().getMainConfiguration().set("sign-locations", all);
            ConfigurationManager.getInstance().saveMainConfiguration();
            RegionSigns.getInstance().reloadConfiguration(null);
            event.getPlayer().sendMessage(ChatColor.RED + "Removed sign location");
        }
    }
}