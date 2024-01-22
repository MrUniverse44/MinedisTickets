package me.blueslime.minedis.extension.tickets.ticket;

import me.blueslime.minedis.extension.tickets.MinedisTickets;
import me.blueslime.minedis.extension.tickets.utils.TicketFile;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Tickets {
    private final Map<String, String> channelMap = new ConcurrentHashMap<>();
    private final Map<String, Ticket> ticketMap = new ConcurrentHashMap<>();

    private final MinedisTickets plugin;

    public Tickets(MinedisTickets plugin) {
        this.plugin = plugin;
        load();
    }

    public void unload() {
        channelMap.clear();
        ticketMap.clear();
    }

    public void load() {
        channelMap.clear();
        ticketMap.clear();

        File folder = TicketFile.getTicketFolder(plugin);

        boolean loaded = folder.exists() || folder.mkdirs();

        if (loaded) {
            File[] files = TicketFile.getTicketFolder(plugin).listFiles((dir, name) -> name.endsWith(".yml"));

            if (files != null) {
                for (File file : files) {
                    try {
                        Ticket ticket = new Ticket(plugin, file);

                        if (ticket.isWorking()) {
                            ticketMap.put(
                                ticket.getUser(),
                                ticket
                            );
                        }
                    } catch (IOException ignored) {

                    }
                }
            }
        }
    }

    public Map<String, Ticket> getTicketMap() {
        return ticketMap;
    }

    public Map<String, String> getChannelMap() {
        return channelMap;
    }

    public boolean contains(User user) {
        return ticketMap.containsKey(user.getId());
    }

    public boolean contains(TextChannel channel) {
        return channelMap.containsKey(channel.getId());
    }
}
