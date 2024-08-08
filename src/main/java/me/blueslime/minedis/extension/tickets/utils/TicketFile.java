package me.blueslime.minedis.extension.tickets.utils;

import me.blueslime.minedis.api.MinedisAPI;
import me.blueslime.minedis.extension.tickets.MinedisTickets;
import me.blueslime.minedis.extension.tickets.ticket.Ticket;
import me.blueslime.minedis.utils.consumer.PluginConsumer;
import net.dv8tion.jda.api.entities.User;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TicketFile {

    public static File getTicketFolder(MinedisTickets plugin) {
        File extensions = MinedisAPI.get().getDirectoryFile("extensions");
        File folder = new File(extensions, plugin.getIdentifier());
        return new File(folder, "tickets");
    }

    public static File getDirectory(MinedisTickets plugin) {
        File extensions = MinedisAPI.get().getDirectoryFile("extensions");
        return new File(extensions, plugin.getIdentifier());
    }

    public static File getCapturesFolder(MinedisTickets plugin) {
        File extensions = MinedisAPI.get().getDirectoryFile("extensions");
        File folder = new File(extensions, plugin.getIdentifier());
        return new File(folder, "captures");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getTicketCaptures(MinedisTickets plugin, Ticket ticket) {
        File folder = new File(getCapturesFolder(plugin), ticket.getUserName());
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    public static LocalDateTime getCreationDate(MinedisTickets plugin, Ticket ticket) {

        BasicFileAttributes attributes;

        File file = ticket.getFile(plugin);

        if (!file.exists()) {
            return null;
        }

        attributes = PluginConsumer.ofUnchecked(
            () -> Files.readAttributes(file.toPath(), BasicFileAttributes.class)
        );

        if (attributes == null) {
            return null;
        }

        Instant instant = attributes.creationTime().toInstant();

        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public static File getUserFile(MinedisTickets plugin, User user) {
        return new File(getTicketFolder(plugin), user.getId() + ".yml");
    }
}
