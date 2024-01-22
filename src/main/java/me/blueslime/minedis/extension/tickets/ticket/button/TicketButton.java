package me.blueslime.minedis.extension.tickets.ticket.button;

import me.blueslime.minedis.extension.tickets.ticket.types.TicketType;

public class TicketButton {
    private final boolean isButton;
    private final TicketType type;

    public TicketButton(boolean isButton, TicketType type) {
        this.isButton = isButton;
        this.type = type;
    }

    public TicketType getType() {
        return type;
    }

    public boolean isButton() {
        return isButton;
    }
}
