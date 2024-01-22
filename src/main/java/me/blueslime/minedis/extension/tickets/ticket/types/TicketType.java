package me.blueslime.minedis.extension.tickets.ticket.types;

import me.blueslime.minedis.extension.tickets.ticket.button.TicketButton;
import net.md_5.bungee.config.Configuration;

import java.util.Locale;

public enum TicketType {
    SANCTION_APPEAL,
    STAFF_REPORT,
    USER_REPORT,
    BUG_REPORT,
    SUPPORT;

    public static TicketType fromConfiguration(Configuration configuration, String path) {
        return fromString(configuration.getString(path, "SUPPORT"), SUPPORT);
    }

    public static TicketType fromConfiguration(Configuration configuration, String path, TicketType def) {
        return fromString(configuration.getString(configuration.getString(path)), def);
    }

    public static TicketButton isButton(String id) {
        for (TicketType type : values()) {
            if (id.equals("create_" + type.getSimpleName())) {
                return new TicketButton(
                    true,
                     type
                );
            }
        }
        return new TicketButton(
            false,
            TicketType.SUPPORT
        );
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ENGLISH).replace("_", "-");
    }

    public static TicketType fromString(String param, TicketType defType) {
        if (param == null) {
            return defType;
        }
        switch (param.toLowerCase(Locale.ENGLISH)) {
            default:
            case "support":
            case "helpop":
            case "help":
            case "s":
            case "h":
                return SUPPORT;
            case "sanction":
            case "appeal":
            case "sanction_appeal":
            case "sanction appeal":
            case "sanction-appeal":
            case "appeal_sanction":
            case "appeal sanction":
            case "appeal-sanction":
            case "sa":
                return SANCTION_APPEAL;
            case "report-staff":
            case "report staff":
            case "report_staff":
            case "staff-report":
            case "staff report":
            case "staff_report":
            case "staff":
            case "sreport":
            case "sr":
                return STAFF_REPORT;
            case "report-user":
            case "report_user":
            case "report user":
            case "user-report":
            case "user report":
            case "user_report":
            case "user":
            case "ureport":
            case "ur":
                return USER_REPORT;
            case "report-bug":
            case "report_bug":
            case "report bug":
            case "bug-report":
            case "bug report":
            case "bug_report":
            case "bug":
            case "breport":
            case "br":
                return BUG_REPORT;
        }
    }

    public static TicketType fromString(String param) {
        return fromString(param, TicketType.SUPPORT);
    }

    public String getEnabled() {
        return toConfigurationPath() + "enabled";
    }

    public String getButtonName() {
        return toConfigurationPath() + "button-name";
    }

    public String getCategoryPath() {
        return toConfigurationPath() + "category-id";
    }

    public String getClosedCategoryPath() {
        return toConfigurationPath() + "closed-category-id";
    }

    public String getChannelFormat() {
        return toConfigurationPath() + "channel-name-format";
    }

    /**
     * @return the second text if the enum has '_', it makes split and gets the second part
     */
    public String getRawName() {
        String[] split = toString().toLowerCase(Locale.ENGLISH).split("_");

        if (split.length >= 2) {
            return split[1];
        }
        return split[0];
    }

    /**
     * @return the first text if the enum has '_', it makes split and gets the first part
     */
    public String getSimpleName() {
        String[] split = toString().toLowerCase(Locale.ENGLISH).split("_");

        return split[0];
    }

    public String getName() {
        return toString().toLowerCase(Locale.ENGLISH).replace("_", "-");
    }


    public String toConfigurationPath() {
        return "settings." + getName() + ".";
    }
}
