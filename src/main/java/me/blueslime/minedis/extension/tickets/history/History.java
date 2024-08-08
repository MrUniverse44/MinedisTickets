package me.blueslime.minedis.extension.tickets.history;

import me.blueslime.minedis.extension.tickets.MinedisTickets;
import me.blueslime.minedis.extension.tickets.ticket.Ticket;
import me.blueslime.minedis.extension.tickets.utils.TicketFile;
import me.blueslime.minedis.utils.consumer.PluginConsumer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class History {
    private final Configuration configuration;
    private static History INSTANCE = null;
    private final MinedisTickets plugin;

    public static History get(MinedisTickets plugin) {
        if (INSTANCE == null) {
            INSTANCE = new History(plugin);
        }
        return INSTANCE;
    }

    private History(MinedisTickets plugin) {
        this.plugin = plugin;

        File file = getFile();

        this.configuration = PluginConsumer.ofUnchecked(
                () -> ConfigurationProvider.getProvider(YamlConfiguration.class).load(file),
                new Configuration()
        );
    }

    public boolean save(Ticket ticket) {
        List<String> user = configuration.getStringList(ticket.getUserId());

        LocalDateTime date = TicketFile.getCreationDate(plugin, ticket);

        String extra = date == null ?
            "[;split;]Loading.." :
            "[;split;]" +
            (date.getDayOfMonth() < 10 ? "0" + date.getDayOfMonth() : date.getDayOfMonth()) +
            "/" +
            (date.getMonthValue() < 10 ? "0" + date.getMonthValue() : date.getMonthValue()) +
            "/" +
            date.getYear();

        user.add(
            ticket.getId() +
            "[;split;]" +
            ticket.getDescription() +
            "[;split;]" +
            ticket.getState().getName() +
            extra
        );
        configuration.set(ticket.getUserId(), user);
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

        File file = new File(folder, "history.yml");

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
}
