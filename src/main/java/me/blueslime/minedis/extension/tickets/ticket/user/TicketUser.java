package me.blueslime.minedis.extension.tickets.ticket.user;

import me.blueslime.minedis.extension.tickets.ticket.Ticket;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

public class TicketUser {
    private final List<String> tickets = new ArrayList<>();
    private final String id;
    public Ticket ticket;

    public TicketUser(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<String> getTickets() {
        return tickets;
    }

    public static TicketUser createUser(User user) {
        return new TicketUser(user.getId());
    }
}
