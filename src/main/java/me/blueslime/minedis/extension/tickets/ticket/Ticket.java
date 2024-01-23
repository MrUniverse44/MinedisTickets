package me.blueslime.minedis.extension.tickets.ticket;

import me.blueslime.minedis.extension.tickets.MinedisTickets;
import me.blueslime.minedis.extension.tickets.commands.TicketState;
import me.blueslime.minedis.extension.tickets.ticket.types.TicketType;
import me.blueslime.minedis.extension.tickets.utils.EmbedSection;
import me.blueslime.minedis.extension.tickets.utils.TicketFile;
import me.blueslime.minedis.utils.text.TextReplacer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Ticket {
    private final String name;
    private final String user;
    private TicketState state;
    private String assistant = "NOT_SET";
    private TicketType type;
    private String id;
    private boolean work;

    public Ticket(MinedisTickets plugin, TicketType type, User user) {
        String categoryID = plugin.getConfiguration().getString(type.getCategoryPath(), "NOT_SET");

        this.user = user.getId();
        this.name = user.getName();

        File folder = TicketFile.getTicketFolder(plugin);
        boolean loaded = folder.exists() || folder.mkdirs();
        File ticketFile = new File(folder, this.user + ".yml");

        this.state = TicketState.WITHOUT_ASSISTANT;
        this.type = type;

        if (ticketFile.exists()) {
            this.id = "";
            this.work = false;
            plugin.getLogger().info("Ticket already exists for " + name);
            return;
        }

        if (categoryID.isEmpty() || categoryID.equalsIgnoreCase("NOT_SET") || categoryID.equalsIgnoreCase("NOT-SET")) {
            this.id = "";
            this.work = false;
            plugin.getLogger().info("Ticket category is not set for " + name);
            return;
        }

        String guildID = plugin.getConfiguration().getString("settings.guild-id", "NOT_SET");

        if (guildID.isEmpty() || guildID.equalsIgnoreCase("NOT_SET") || guildID.equalsIgnoreCase("NOT-SET")) {
            this.id = "";
            this.work = false;
            plugin.getLogger().info("Guild is not set for " + name);
            return;
        }

        Guild guild = plugin.getJDA().getGuildById(
            guildID
        );

        if (guild == null) {
            this.id = "";
            this.work = false;
            plugin.getLogger().info("Guild was not found for " + name);
            return;
        }

        Category category = guild.getCategoryById(categoryID);

        if (category == null) {
            this.id = "";
            this.work = false;
            plugin.getLogger().info("Category was not found for " + name);
            return;
        }

        String name = plugin.getConfiguration().getString(
                type.getChannelFormat(),
                type.getRawName() + "-%user id%"
        );

        name = name.replace(
            "%user id%",
            user.getId()
        ).replace(
            "%user name%",
            user.getName()
        ).replace(
            "%user effective name%",
            user.getEffectiveName()
        ).replace(
            "%user global name%",
            user.getGlobalName() == null ? user.getName() : user.getGlobalName()
        ).replace(
            "%ticket type%",
            plugin.getConfiguration().getString(type.getName())
        );

        category.createTextChannel(
            name
        ).queue(
            channel -> {
                this.id = channel.getId();

                TextReplacer replacer = TextReplacer.builder()
                    .replace(
                        "%user id%",
                        user.getId()
                    ).replace(
                        "%user name%",
                        user.getName()
                    ).replace(
                        "%user effective name%",
                        user.getEffectiveName()
                    ).replace(
                        "%user global name%",
                        user.getGlobalName() == null ? user.getName() : user.getGlobalName()
                    ).replace(
                        "%ticket type%",
                        plugin.getConfiguration().getString(type.getButtonName(), type.getName())
                    );

                channel.sendMessageEmbeds(
                    new EmbedSection(
                        plugin.getConfiguration().getSection("embeds.ticket-open")
                    ).build(
                        replacer
                    )
                ).queue(
                    message -> message.addReaction(Emoji.fromUnicode("✅")).queue()
                );

                channel.sendMessage(
                    replacer.apply(
                        plugin.getConfiguration().getString("embeds.ticket-open.message", "@here new ticket created!")
                    )
                ).queue(
                    message -> message.addReaction(Emoji.fromUnicode("✅")).queue()
                );

                plugin.getTickets().getChannelMap().put(
                    channel.getId(),
                    user.getId()
                );
            }
        );

        if (loaded) {
            try {
                if (!ticketFile.exists()) {
                    ticketFile.createNewFile();
                }
                Configuration ticketConfiguration = new Configuration();

                ticketConfiguration.set("ticket.channel-id", this.id);
                ticketConfiguration.set("ticket.user-name", this.name);
                ticketConfiguration.set("ticket.assistant", "NOT_SET");
                ticketConfiguration.set("ticket.user-id", this.user);
                ticketConfiguration.set("ticket.state", state.toString());
                ticketConfiguration.set("ticket.type", type.toString());

                ConfigurationProvider.getProvider(YamlConfiguration.class).save(ticketConfiguration, ticketFile);

                this.work = true;
            } catch (IOException ignored) {

            }
        }
    }

    public Ticket(MinedisTickets plugin, File file) throws IOException {
        Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);

        this.assistant = configuration.getString("ticket.assistant", "NOT_SET");
        this.name = configuration.getString("ticket.user-name");
        this.user = configuration.getString("ticket.user-id");
        this.id = configuration.getString("ticket.channel-id");

        this.state = TicketState.fromState(
            configuration.getString("ticket.state")
        );

        this.type = TicketType.fromString(
            configuration.getString("tickets.type")
        );

        if (name == null || user == null || state == null || type == null || id == null) {
            this.state = TicketState.WITHOUT_ASSISTANT;
            this.type = TicketType.SUPPORT;
            this.work = false;
            this.id = "";
            return;
        }

        this.work = true;

        update(plugin);
    }

    public boolean isAssistant(Member member) {
        return isAssistant(member.getUser());
    }

    public boolean isAssistant(User user) {
        return assistant.equalsIgnoreCase(user.getId());
    }

    public void setWork(boolean work) {
        this.work = work;
    }

    public void setState(TicketState state) {
        this.state = state;
    }

    public TicketState getState() {
        return state;
    }

    public void setAssistant(String assistant) {
        this.assistant = assistant;
    }

    public String getAssistant() {
        return assistant;
    }

    public String getUser() {
        return user;
    }

    public String getId() {
        return id;
    }

    public String getUserName() {
        return name;
    }

    public boolean isWorking() {
        return work;
    }

    public void update(MinedisTickets plugin) {
        File folder = TicketFile.getTicketFolder(plugin);
        boolean loaded = folder.exists() || folder.mkdirs();
        File ticketFile = new File(folder, this.user + ".yml");

        if (loaded) {
            try {
                if (!ticketFile.exists()) {
                    ticketFile.createNewFile();
                }
                Configuration ticketConfiguration = new Configuration();

                ticketConfiguration.set("ticket.channel-id", this.id);
                ticketConfiguration.set("ticket.user-name", this.name);
                ticketConfiguration.set("ticket.assistant", "NOT_SET");
                ticketConfiguration.set("ticket.user-id", this.user);
                ticketConfiguration.set("ticket.state", state.toString());
                ticketConfiguration.set("ticket.type", type.toString());

                ConfigurationProvider.getProvider(YamlConfiguration.class).save(ticketConfiguration, ticketFile);
            } catch (IOException ignored) {

            }
        }
    }

    public TicketType getType() {
        return type;
    }
}
