package me.blueslime.minedis.extension.tickets.commands;

import java.util.Locale;

public enum TicketState {
    WITHOUT_ASSISTANT,
    WAITING_RESPONSE,
    IN_PROCESS;

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ENGLISH);
    }

    public String getName() {
        return toString().replace("_", "-");
    }

    public static TicketState fromState(String text) {
        text = text.toLowerCase(Locale.ENGLISH);
        for (TicketState state : values()) {
            if (text.equals(state.getName()) || text.equals(state.toString())) {
                return state;
            }
        }
        return TicketState.WITHOUT_ASSISTANT;
    }
}
