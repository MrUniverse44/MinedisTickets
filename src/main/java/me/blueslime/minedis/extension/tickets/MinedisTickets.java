package me.blueslime.minedis.extension.tickets;

import me.blueslime.minedis.extension.tickets.listeners.ChatListener;
import me.blueslime.minedis.extension.tickets.ticket.Tickets;
import me.blueslime.minedis.extension.tickets.ticket.types.TicketType;
import me.blueslime.minedis.api.extension.MinedisExtension;

public final class MinedisTickets extends MinedisExtension {

    private Tickets tickets;

    @Override
    public String getIdentifier() {
        return "MTickets";
    }

    @Override
    public String getName() {
        return "Minedis Tickets";
    }

    @Override
    public void onEnabled() {
        getLogger().info("Loading Tickets extension v1.0.0");

        if (!getConfiguration().contains("settings.guild-id")) {
            getConfiguration().set("settings.guild-id", "NOT_SET");
        }

        for (TicketType type : TicketType.values()) {
            if (!getConfiguration().contains(type.getCategoryPath())) {
                getConfiguration().set(type.getCategoryPath(), "NOT_SET");
            }
            if (!getConfiguration().contains(type.getEnabled())) {
                getConfiguration().set(type.getEnabled(), true);
            }
            if (!getConfiguration().contains(type.getChannelFormat())) {
                getConfiguration().set(type.getChannelFormat(), type.getRawName() + "-%user id%");
            }
            if (!getConfiguration().contains(type.getButtonName())) {
                getConfiguration().set(type.getButtonName(), type.getName());
            }
        }

        if (!getConfiguration().contains("embeds.help")) {
            getConfiguration().set("embeds.help.title", "¿Hello %user name%, how we can help you?");
            getConfiguration().set("embeds.help.description", "Here you have options to select, with these options we can help you.");
            getConfiguration().set("embeds.help.color", "YELLOW");
            getConfiguration().set("embeds.help.footer", "mc.spigotmc.org");
        }

        if (!getConfiguration().contains("embeds.ticket-created")) {
            getConfiguration().set("embeds.ticket-created.title", "Ticket created!");
            getConfiguration().set("embeds.ticket-created.description", "Your ticket has been created, please wait for a reply from a staff.");
            getConfiguration().set("embeds.ticket-created.color", "GREEN");
            getConfiguration().set("embeds.ticket-created.footer", "mc.spigotmc.org");
        }

        if (!getConfiguration().contains("embeds.assistant-assigned")) {
            getConfiguration().set("embeds.assistant-assigned.title", "Now you have an assistant!");
            getConfiguration().set("embeds.assistant-assigned.description", "A Staff now is watching your ticket, please wait for a reply from the staff.");
            getConfiguration().set("embeds.assistant-assigned.color", "GREEN");
            getConfiguration().set("embeds.assistant-assigned.footer", "mc.spigotmc.org");
        }

        if (!getConfiguration().contains("embeds.ticket-closed")) {
            getConfiguration().set("embeds.ticket-closed.title", "Ticket has been closed by your assistant!");
            getConfiguration().set("embeds.ticket-closed.description", "Thanks for using our ticket system");
            getConfiguration().set("embeds.ticket-closed.color", "GREEN");
            getConfiguration().set("embeds.ticket-closed.footer", "mc.spigotmc.org");
            getConfiguration().set("embeds.ticket-closed.message", "@here new ticket created!");
        }

        if (!getConfiguration().contains("embeds.ticket-open")) {
            getConfiguration().set("embeds.ticket-open.title", "Ticket created by %user name%!");
            getConfiguration().set("embeds.ticket-open.description", "Ticket type: %ticket type%, if you want to be the assistant of this ticket, please use **!assist** and reply with **!reply (message)**");
            getConfiguration().set("embeds.ticket-open.color", "GREEN");
            getConfiguration().set("embeds.ticket-open.footer", "mc.spigotmc.org");
            getConfiguration().set("embeds.ticket-open.message", "@here new ticket created!");
        }

        saveConfiguration();
        reloadConfiguration();

        registerEventListeners(
            new ChatListener(this)
        );

        registerCommands();

        tickets = new Tickets(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Unloading Tickets Extension of Minedis");

        tickets.unload();
    }

    public Tickets getTickets() {
        return tickets;
    }

    public void registerCommands() {

    }
}
