package pro.akii.pl.fireworkExplosionPotionEffect;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class FireworkExplosionPotionEffect extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getLogger().info("FireworkPotionEffect Plugin Enabled!");
        getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("fireworkeffect")) {
            if (!sender.hasPermission("firework.effect")) {
                sender.sendMessage("You do not have permission to use this command!");
                return false;
            }

            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    launchFirework(player, "SPEED");
                    player.sendMessage(getConfig().getString("messages.launchSuccess"));
                }
            } else if (args.length == 1) {
                String effect = args[0].toUpperCase();
                if (PotionEffectType.getByName(effect) != null) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        launchFirework(player, effect);
                        player.sendMessage(getConfig().getString("messages.launchSuccess"));
                    }
                } else {
                    sender.sendMessage("Invalid effect type! Please use one of the following: SPEED, STRENGTH, REGENERATION.");
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerUseFirework(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getItem().getType().toString().contains("FIREWORK_ROCKET")) {
            ItemStack item = event.getItem();
            ItemMeta meta = item.getItemMeta();

            if (meta instanceof org.bukkit.inventory.meta.FireworkMeta) {
                org.bukkit.inventory.meta.FireworkMeta fireworkMeta = (org.bukkit.inventory.meta.FireworkMeta) meta;

                if (fireworkMeta.hasDisplayName()) {
                    String effectName = fireworkMeta.getDisplayName();
                    if (PotionEffectType.getByName(effectName) != null) {
                        launchFirework(event.getPlayer(), effectName);
                        event.getPlayer().sendMessage("Launched firework with " + effectName + " effect!");
                    }
                }
            }
        }
    }

    private void launchFirework(Player player, String effectName) {
        PotionEffectType effectType = PotionEffectType.getByName(effectName);
        if (effectType == null) {
            effectType = PotionEffectType.SPEED;
        }

        Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
        firework.setFireworkMeta(createFireworkMeta(effectName));

        player.getWorld().playSound(player.getLocation(), "entity.firework_rocket.launch", 1.0f, 1.0f);

        for (Entity entity : player.getNearbyEntities(getConfig().getDouble("fireworkeffect.radius"), getConfig().getDouble("fireworkeffect.radius"), getConfig().getDouble("fireworkeffect.radius"))) {
            if (entity instanceof Player) {
                Player target = (Player) entity;
                target.addPotionEffect(new PotionEffect(effectType, getConfig().getInt("fireworkeffect.duration") * 20, 1));
                target.sendMessage("You received a " + effectName + " effect!");
            }
        }
    }

    private org.bukkit.inventory.meta.FireworkMeta createFireworkMeta(String effectName) {
        org.bukkit.inventory.meta.FireworkMeta meta = (org.bukkit.inventory.meta.FireworkMeta) Bukkit.getItemFactory().getItemMeta(org.bukkit.Material.FIREWORK_ROCKET);

        if (meta != null) {
            List<String> colors = getConfig().getStringList("fireworkeffect.fireworkColors." + effectName);

            if (colors != null && !colors.isEmpty()) {
                FireworkEffect.Builder effectBuilder = FireworkEffect.builder();

                for (String color : colors) {
                    try {
                        if (color.startsWith("#") && color.length() == 7) {
                            int r = Integer.parseInt(color.substring(1, 3), 16);
                            int g = Integer.parseInt(color.substring(3, 5), 16);
                            int b = Integer.parseInt(color.substring(5, 7), 16);
                            effectBuilder.withColor(Color.fromRGB(r, g, b));
                        } else {
                            getLogger().warning("Invalid color format in config: " + color);
                        }
                    } catch (NumberFormatException e) {
                        getLogger().warning("Invalid color format in config: " + color);
                    }
                }

                FireworkEffect effect = effectBuilder.build();
                meta.addEffect(effect);
            } else {
                getLogger().warning("No colors found for effect: " + effectName);
            }

            meta.setPower(1);
        }

        return meta;
    }

    @Override
    public void onDisable() {
        getLogger().info("FireworkPotionEffect Plugin Disabled!");
    }
}
