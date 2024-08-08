package me.blueslime.minedis.extension.tickets.blacklists;

import me.blueslime.minedis.extension.tickets.MinedisTickets;
import me.blueslime.minedis.extension.tickets.utils.TicketFile;
import me.blueslime.minedis.utils.consumer.PluginConsumer;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.util.List;

public class Blacklist {
    private final Configuration configuration;
    private static Blacklist INSTANCE = null;
    private final MinedisTickets plugin;

    public static Blacklist get(MinedisTickets plugin) {
        if (INSTANCE == null) {
            INSTANCE = new Blacklist(plugin);
        }
        return INSTANCE;
    }

    private Blacklist(MinedisTickets plugin) {
        this.plugin = plugin;

        File file = getFile();

        this.configuration = PluginConsumer.ofUnchecked(
            () -> ConfigurationProvider.getProvider(YamlConfiguration.class).load(file),
            new Configuration()
        );
    }

    public boolean addUser(User user) {
        return addUser(user.getId());
    }

    public boolean addUser(String userID) {
        if (configuration.getStringList("list").contains(userID)) {
            return false;
        }
        List<String> users = configuration.getStringList("list");
        users.add(userID);
        configuration.set("list", users);
        applyChanges();
        return true;
    }

    public boolean removeUser(User user) {
        return removeUser(user.getId());
    }

    public boolean removeUser(String userID) {
        if (!configuration.getStringList("list").contains(userID)) {
            return false;
        }
        List<String> users = configuration.getStringList("list");
        users.remove(userID);
        configuration.set("list", users);
        applyChanges();
        return true;
    }

    public void applyChanges() {
        File file = getFile();

        PluginConsumer.process(
            () -> ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file)
        );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File getFile() {
        File folder = TicketFile.getDirectory(plugin);

        File file = new File(folder, "blacklist.yml");

        PluginConsumer.process(
                () -> {
                    if (!folder.exists()) {
                        folder.mkdirs();
                    }
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                }
        );

        return file;
    }

    public boolean isBlacklisted(User user) {
        return isBlacklisted(user.getId());
    }

    public boolean isBlacklisted(String userID) {
        return configuration.getStringList("list").contains(userID);
    }
}
