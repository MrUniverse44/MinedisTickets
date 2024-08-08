package me.blueslime.minedis.extension.tickets;

import me.blueslime.minedis.extension.tickets.listeners.ExtensionListeners;
import me.blueslime.minedis.extension.tickets.ticket.Tickets;
import me.blueslime.minedis.extension.tickets.ticket.types.TicketType;
import me.blueslime.minedis.api.extension.MinedisExtension;

import java.util.ArrayList;

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

        if (!getConfiguration().contains("settings.users-guild-id")) {
            getConfiguration().set("settings.users-guild-id", "NOT_SET");
        }

        if (!getConfiguration().contains("embeds.ticket-help-command")) {
            getConfiguration().set("embeds.ticket-help-command.title", "Ticket Commands");
            getConfiguration().set(
                "embeds.ticket-help-command.description",
                "Here you have the command list for your tickets:\n" +
                "```Yaml\n" +
                "  - !ticket blacklist (user id) - Blacklist an user to block ticket creation.\n" +
                "  - !ticket warn (user id) - Warn a user from a bad usage of tickets.\n" +
                "  - !ticket description (info) - Set the description of your ticket.\n" +
                "  - !ticket history (user id) - Check the ticket history\n" +
                "  - !ticket close (ACCEPTED, REJECT) - Accept or reject a ticket\n" +
                "  - !ticket leave - Leave from the ticket\n" +
                "  - !ticket transfer - Transfer ticket assistant to other staff\n" +
                "  - !ticket discharge - Force Remove an assistant of a ticket\n" +
                "  - !ticket [rename,rn] (New name for the ticket channel)\n" +
                "  - !ticket [reply,r] (message) - Reply to a ticket, the plugin supports images and video files too\n" +
                "  - !ticket [assist,claim] - Be an assistant of a ticket\n" +
                "  - !ticket [assists,stats,info] - Show statistics of a week\n" +
                "  - !ticket [leaderboard,leader,lb,top] - Check the leaderboard of staffs." +
                "```"
            );
            getConfiguration().set("embeds.ticket-help-command.color", "YELLOW");
            getConfiguration().set("embeds.ticket-help-command.footer", "mc.spigotmc.org");
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

        if (!getConfiguration().contains("embeds.blacklist")) {
            getConfiguration().set("embeds.blacklist.title", "%user name%, You are in the blacklist");
            getConfiguration().set("embeds.blacklist.description", "You can't open a ticket");
            getConfiguration().set("embeds.blacklist.color", "RED");
            getConfiguration().set("embeds.blacklist.footer", "mc.spigotmc.org");
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

        if (!getConfiguration().contains("embed.assists")) {
            getConfiguration().set("embeds.assists.title", "Staff Assists!");
            getConfiguration().set("embeds.assists.description", "<mention> you have <assists> assist(s)");
            getConfiguration().set("embeds.assists.color", "WHITE");
            getConfiguration().set("embeds.assists.footer", "mc.spigotmc.org");
        }

        if (!getConfiguration().contains("embeds.leaderboard")) {
            getConfiguration().set("embeds.leaderboard.title", "Staff Leaderboard!");
            getConfiguration().set("embeds.leaderboard.description", "Staff TOP");
            getConfiguration().set("embeds.leaderboard.color", "WHITE");
            getConfiguration().set("embeds.leaderboard.footer", "mc.spigotmc.org");
        }

        if (!getConfiguration().contains("blacklisted-users")) {
            getConfiguration().set("blacklisted-users", new ArrayList<String>());
        }

        saveConfiguration();
        reloadConfiguration();

        for (Object listener : new ArrayList<>(getJDA().getRegisteredListeners())) {
            String name = listener.getClass().getName();
            if (
                name.contains("ExtensionListeners") &&
                name.contains("minedis") &&
                name.contains("tickets")
            ) {
                getJDA().removeEventListener(listener);
            }
        }

        registerEventListeners(
            new ExtensionListeners(this)
        );

        registerCommands();

        tickets = new Tickets(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Unloading Tickets Extension of Minedis");

        if (tickets != null) {
            tickets.unload();
        }
    }

    public Tickets getTickets() {
        return tickets;
    }

    public void registerCommands() {

    }
}
