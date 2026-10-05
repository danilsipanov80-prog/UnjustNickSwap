package ru.example.unjustnickswap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class UnjustNickSwap extends JavaPlugin implements Listener {

    private final Map<UUID, String> originalNames = new HashMap<>();
    private final Map<UUID, String> fakeNames = new HashMap<>();
    private final Random random = new Random();

    // Список популярных ников — можешь добавлять свои
    private final String[] famousNames = {
        "Notch", "Dream", "Technoblade", "Herobrine", "Steve", "Alex",
        "Ph1LzA", "TommyInnit", "Wilbur", "Sapnap", "GeorgeNotFound",
        "CaptainSparklez", "DanTDM", "SkyDoesMinecraft", "Stampy",
        "PopularMMOs", "PrestonPlayz", "SSundee", "JeromeASF",
        "BajanCanadian", "Vikkstar", "MrBeast", "PewDiePie"
    };

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("UnjustNickSwap включен!");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
public void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();
    UUID uuid = player.getUniqueId();

    if (!originalNames.containsKey(uuid)) {
        originalNames.put(uuid, player.getName());
    }

    String fakeName = generateFakeName();
    fakeNames.put(uuid, fakeName);

    applyFakeName(player, fakeName);
}
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (fakeNames.containsKey(uuid)) {
            applyFakeName(player, fakeNames.get(uuid));
        }
    }

    /**
     * Берёт случайный популярный ник и слегка его изменяет.
     */
    private String generateFakeName() {
        String base = famousNames[random.nextInt(famousNames.length)];
        return slightlyModify(base);
    }

    /**
     * Слегка меняет ник: заменяет буквы на похожие цифры
     * и иногда добавляет "_" в конец.
     */
    private String slightlyModify(String name) {
        StringBuilder sb = new StringBuilder();

        for (char c : name.toCharArray()) {
            // С вероятностью 40% меняем символ на похожий
            if (random.nextInt(100) < 40) {
                switch (Character.toLowerCase(c)) {
                    case 'o': sb.append('0'); continue;
                    case 'i': sb.append(random.nextBoolean() ? '1' : 'l'); continue;
                    case 'e': sb.append('3'); continue;
                    case 'a': sb.append('4'); continue;
                    case 's': sb.append('5'); continue;
                    case 't': sb.append('7'); continue;
                    case 'b': sb.append('8'); continue;
                    case 'g': sb.append('9'); continue;
                    default: break;
                }
            }
            sb.append(c);
        }

        // С вероятностью 30% добавляем "_" в конец
        if (random.nextInt(100) < 30) {
            sb.append('_');
        }

        return sb.toString();
    }

    private void applyFakeName(Player player, String fakeName) {
        player.setDisplayName(ChatColor.WHITE + fakeName);
        player.setPlayerListName(ChatColor.WHITE + fakeName);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("nickunjustswap")) {
            return false;
        }

        if (args.length < 2 || !args[0].equalsIgnoreCase("reset")) {
            sender.sendMessage(ChatColor.RED + "Использование: /nickunjustswap reset <ник>");
            return true;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null) {
            // Попробуем найти по фейковому нику
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (fakeNames.containsKey(online.getUniqueId())
                        && fakeNames.get(online.getUniqueId()).equalsIgnoreCase(targetName)) {
                    target = online;
                    break;
                }
            }
        }

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден!");
            return true;
        }

        UUID uuid = target.getUniqueId();

        if (originalNames.containsKey(uuid)) {
            String original = originalNames.get(uuid);
            target.setDisplayName(original);
            target.setPlayerListName(original);
            originalNames.remove(uuid);
            fakeNames.remove(uuid);
            sender.sendMessage(ChatColor.GREEN + "Ник игрока " + original + " восстановлен!");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "У этого игрока нет фейкового ника.");
        }

        return true;
    }
}
