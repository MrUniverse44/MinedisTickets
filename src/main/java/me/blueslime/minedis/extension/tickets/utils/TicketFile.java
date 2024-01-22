package me.blueslime.minedis.extension.tickets.utils;

import me.blueslime.minedis.api.MinedisAPI;
import me.blueslime.minedis.extension.tickets.MinedisTickets;
import me.blueslime.minedis.extension.tickets.ticket.Ticket;
import net.dv8tion.jda.api.entities.User;

import java.io.File;

public class TicketFile {

    public static File getTicketFolder(MinedisTickets plugin) {
        File extensions = MinedisAPI.get().getDirectoryFile("extensions");
        File folder = new File(extensions, plugin.getIdentifier());
        return new File(folder, "tickets");
    }

    public static File getCapturesFolder(MinedisTickets plugin) {
        File extensions = MinedisAPI.get().getDirectoryFile("extensions");
        File folder = new File(extensions, plugin.getIdentifier());
        return new File(folder, "captures");
    }

    public static File getTicketCaptures(MinedisTickets plugin, Ticket ticket) {
        File folder = new File(getCapturesFolder(plugin), ticket.getUserName());
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    public static File getUserFile(MinedisTickets plugin, User user) {
        return new File(getTicketFolder(plugin), user.getId() + ".yml");
    }
}
