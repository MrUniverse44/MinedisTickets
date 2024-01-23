package me.blueslime.minedis.extension.tickets.listeners;

import me.blueslime.minedis.extension.tickets.MinedisTickets;
import me.blueslime.minedis.extension.tickets.commands.TicketState;
import me.blueslime.minedis.extension.tickets.ticket.Ticket;
import me.blueslime.minedis.extension.tickets.ticket.button.TicketButton;
import me.blueslime.minedis.extension.tickets.ticket.types.TicketType;
import me.blueslime.minedis.extension.tickets.utils.EmbedSection;
import me.blueslime.minedis.extension.tickets.utils.Leaderboard;
import me.blueslime.minedis.extension.tickets.utils.TicketFile;
import me.blueslime.minedis.utils.text.TextReplacer;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ExtensionListeners extends ListenerAdapter {

    private final MinedisTickets plugin;

    public ExtensionListeners(MinedisTickets plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        String id = event.getButton().getId();

        if (id == null) {
            return;
        }

        TicketButton button = TicketType.isButton(id);

        if (!button.isButton()) {
            return;
        }

        if (plugin.getTickets().contains(event.getUser())) {
            return;
        }

        Ticket ticket = new Ticket(plugin, button.getType(), event.getUser());

        if (ticket.isWorking()) {
            plugin.getTickets().getTicketMap().put(
                    event.getUser().getId(),
                    ticket
            );

            PrivateChannel channel = event.getChannel().asPrivateChannel();

            User user = event.getUser();

            event.deferReply(true).queue(
                queue -> queue.sendMessageEmbeds(
                    new EmbedSection(
                            plugin.getConfiguration().getSection("embeds.ticket-created")
                    ).build(
                        TextReplacer.builder()
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
                            )
                    )
                ).queueAfter(1, TimeUnit.SECONDS)
            );

            channel.sendMessageEmbeds(
                    new EmbedSection(
                            plugin.getConfiguration().getSection("embeds.ticket-created")
                    ).build(
                            TextReplacer.builder()
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
                                )
                    )
            ).queue();
        } else {
            event.reply("You already have a ticket open.").queue();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }
        if (event.isFromGuild()) {
            if (event.isFromType(ChannelType.TEXT)) {
                TextChannel channel = event.getChannel().asTextChannel();

                String guildID = plugin.getConfiguration().getString("settings.guild-id", "NOT_SET");

                if (!guildID.isEmpty() && !guildID.equalsIgnoreCase("NOT_SET") && !guildID.equalsIgnoreCase("NOT-SET")) {
                    Guild guild = plugin.getJDA().getGuildById(
                        guildID
                    );

                    if (guild != null) {
                        if (event.getMessage().getContentRaw().startsWith("!assists")) {
                            int total = plugin.getConfiguration().getInt("assists." + event.getAuthor().getEffectiveName(), 0);

                            channel.sendMessage(event.getAuthor().getAsMention() + " you have " + total + " assist(s).").queue();
                            return;
                        }
                        if (event.getMessage().getContentRaw().startsWith("!leaderboard")) {
                            Map<String, Integer> staffMap = new HashMap<>();

                            for (String key : plugin.getConfiguration().getSection("assists").getKeys()) {
                                staffMap.put(
                                    key,
                                    plugin.getConfiguration().getInt("assists." + key, 0)
                                );
                            }

                            StringBuilder builder = new StringBuilder("\n");

                            int position = 1;

                            for (Map.Entry<String, Integer> entry : Leaderboard.sort(staffMap)) {
                                builder.append(position)
                                        .append(". **")
                                        .append(entry.getKey())
                                        .append(": **")
                                        .append(entry.getValue())
                                        .append("\n");
                                position++;
                            }
                            channel.sendMessage("# Tickets Leaderboard" + builder.toString()).queue();
                            return;
                        }
                    }
                }
                if (event.getMessage().getContentRaw().startsWith("!assist")) {
                    if (plugin.getTickets().contains(channel)) {
                        Ticket ticket = fetchTicketChannel(channel);

                        if (ticket == null) {
                            return;
                        }

                        if (ticket.getState() == TicketState.WITHOUT_ASSISTANT) {
                            channel.sendMessage(event.getAuthor().getAsMention() + " now you are the assistant of this ticket.").queue();
                            ticket.setAssistant(event.getAuthor().getId());
                            ticket.setState(TicketState.IN_PROCESS);


                            ticket.update(plugin);

                            int total = plugin.getConfiguration().getInt("assists." + event.getAuthor().getEffectiveName(), 0);

                            total++;

                            plugin.getConfiguration().set("assists." + event.getAuthor().getEffectiveName(), total);
                            plugin.saveConfiguration();
                            plugin.reloadConfiguration();

                            User user = fetchUser(ticket.getUser());

                            if (user == null) {
                                channel.sendMessage("Can't find this user").queue();
                                return;
                            }

                            user.openPrivateChannel().queue(
                                    userChannel -> {
                                        if (userChannel != null && userChannel.canTalk()) {
                                            userChannel.sendMessageEmbeds(
                                                    new EmbedSection(
                                                            plugin.getConfiguration().getSection("embeds.assistant-assigned")
                                                    ).build(
                                                            TextReplacer.builder()
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
                                                                    )
                                                    )
                                            ).queue();
                                        } else {
                                            channel.sendMessage(
                                                    "This user has MD disabled for bots, the bot can't chat with this user.."
                                            ).queue();
                                        }
                                    }
                            );
                            return;
                        } else {
                            channel.sendMessage("This ticket already has an assistant assigned.").queue();
                        }
                        return;
                    }
                }
                if (event.getMessage().getContentRaw().startsWith("!close")) {
                    if (plugin.getTickets().contains(channel)) {
                        Ticket ticket = fetchTicketChannel(channel);

                        if (ticket == null) {
                            channel.sendMessage("Ticket was not found").queue();
                            return;
                        }

                        if (ticket.isAssistant(event.getAuthor())) {

                            User user = fetchUser(ticket.getUser());

                            if (user == null) {
                                channel.sendMessage("Can't find this user").queue();
                                return;
                            }

                            user.openPrivateChannel().queue(
                                    userChannel -> {
                                        if (userChannel != null && userChannel.canTalk()) {
                                            userChannel.sendMessageEmbeds(
                                                new EmbedSection(
                                                    plugin.getConfiguration().getSection("embeds.ticket-closed")
                                                ).build()
                                            ).queue();
                                        } else {
                                            channel.sendMessage(
                                                    "This user has MD disabled for bots, the bot can't chat with this user.."
                                            ).queue();
                                        }
                                    }
                            );

                            channel.sendMessage(
                                    "Ticket has been closed."
                            ).queue();

                            String categoryID = plugin.getConfiguration().getString(ticket.getType().getCategoryPath(), "NOT_SET");

                            if (categoryID.isEmpty() || categoryID.equalsIgnoreCase("NOT_SET") || categoryID.equalsIgnoreCase("NOT-SET")) {
                                plugin.getLogger().info("Ticket category is not set for ticket-id: " + ticket.getId());
                                return;
                            }

                            if (guildID.isEmpty() || guildID.equalsIgnoreCase("NOT_SET") || guildID.equalsIgnoreCase("NOT-SET")) {
                                plugin.getLogger().info("Guild is not set for ticket-id: " + ticket.getId());
                                return;
                            }

                            Guild guild = plugin.getJDA().getGuildById(
                                    guildID
                            );

                            if (guild == null) {
                                plugin.getLogger().info("Guild was not found for ticket-id: " + ticket.getId());
                                return;
                            }

                            Category category = guild.getCategoryById(categoryID);

                            channel.getManager().setParent(
                                category
                            ).queue();

                            File file = new File(TicketFile.getTicketFolder(plugin), ticket.getUser() + ".yml");

                            if (file.exists()) {
                                boolean deleted = file.delete();
                                if (deleted) {
                                    plugin.getTickets().getChannelMap().remove(channel.getId());
                                    plugin.getTickets().getTicketMap().remove(ticket.getUser());
                                }
                            }
                            return;
                        } else {
                            channel.sendMessage("Only the assistant can interact with the user.").queue();
                        }
                    }
                    return;
                }
                if (event.getMessage().getContentRaw().startsWith("!leave ")) {
                    if (plugin.getTickets().contains(channel)) {
                        Ticket ticket = fetchTicketChannel(channel);

                        if (ticket == null) {
                            channel.sendMessage("Ticket was not found").queue();
                            return;
                        }

                        if (ticket.isAssistant(event.getAuthor())) {
                            User user = fetchUser(ticket.getUser());

                            if (user == null) {
                                channel.sendMessage("Can't find this user").queue();
                                return;
                            }

                            ticket.setAssistant("NOT_SET");
                            ticket.setState(TicketState.WAITING_RESPONSE);
                            ticket.update(plugin);

                            int total = plugin.getConfiguration().getInt("assists." + event.getAuthor().getEffectiveName(), 0);

                            total--;

                            plugin.getConfiguration().set("assists." + event.getAuthor().getEffectiveName(), total);
                            plugin.saveConfiguration();
                            plugin.reloadConfiguration();

                            channel.sendMessage("Now you are not the assist of this ticket " + event.getAuthor().getAsMention() + ". Waiting for other assistant").queue();
                            return;
                        } else {
                            channel.sendMessage("Only the assistant can chat with the user, using **!reply ** prefix.").queue();
                        }
                    }
                    return;
                }
                if (event.getMessage().getContentRaw().startsWith("!rename ")) {
                    if (plugin.getTickets().contains(channel)) {
                        Ticket ticket = fetchTicketChannel(channel);

                        if (ticket == null) {
                            channel.sendMessage("Ticket was not found").queue();
                            return;
                        }

                        if (ticket.isAssistant(event.getAuthor())) {
                            String name = event.getMessage().getContentRaw().replace("!rename ", "");

                            User user = fetchUser(ticket.getUser());

                            if (user == null) {
                                channel.sendMessage("Can't find this user").queue();
                                return;
                            }

                            if (name.length() >= 2) {
                                channel.getManager().setName(name).queue();
                            }
                            return;
                        } else {
                            channel.sendMessage("Only the assistant can chat with the user, using **!reply ** prefix.").queue();
                        }
                    }
                    return;
                }
                if (event.getMessage().getContentRaw().startsWith("!reply ")) {
                    if (plugin.getTickets().contains(channel)) {
                        Ticket ticket = fetchTicketChannel(channel);

                        if (ticket == null) {
                            channel.sendMessage("Ticket was not found").queue();
                            return;
                        }

                        if (ticket.isAssistant(event.getAuthor())) {
                            String message = event.getMessage().getContentRaw().replace("!reply ", "");

                            User user = fetchUser(ticket.getUser());

                            if (user == null) {
                                channel.sendMessage("Can't find this user").queue();
                                return;
                            }

                            List<Message.Attachment> attachments = event.getMessage().getAttachments();

                            if (attachments.isEmpty()) {
                                user.openPrivateChannel().queue(
                                    userChannel -> {
                                        if (userChannel != null && userChannel.canTalk()) {
                                            userChannel.sendMessage(
                                                message
                                            ).queue();
                                            channel.sendMessage(
                                                "Your message has been sent to the player!"
                                            ).queue();
                                        } else {
                                            channel.sendMessage(
                                                "This user has MD disabled for bots, the bot can't chat with this user.."
                                            ).queue();
                                        }
                                    }
                                );
                                return;
                            }

                            File folder = TicketFile.getTicketCaptures(plugin, ticket);

                            List<File> fileList = new ArrayList<>();

                            for (Message.Attachment attachment : attachments) {
                                if (attachment.isImage() || attachment.isVideo()) {
                                    try {
                                        attachment.getProxy().downloadToFile(
                                                new File(
                                                        folder,
                                                        event.getAuthor().getName() + "-" + attachment.getFileName() + (attachment.getFileExtension() != null ? attachment.getFileExtension() : "")
                                                )
                                        ).thenAccept(
                                                fileList::add
                                        );
                                    } catch (Exception ignored) {}
                                }
                            }

                            List<FileUpload> uploads = new ArrayList<>();

                            fileList.forEach(f -> uploads.add(FileUpload.fromData(f)));

                            FileUpload[] array = uploads.toArray(new FileUpload[fileList.size()]);

                            user.openPrivateChannel().queue(
                                    userChannel -> {
                                        if (userChannel != null && userChannel.canTalk()) {
                                            userChannel.sendMessage(
                                                    message
                                            ).setFiles(array).queue();
                                            channel.sendMessage(
                                                    "Your message has been sent to the player!"
                                            ).queue();
                                        } else {
                                            channel.sendMessage(
                                                    "This user has MD disabled for bots, the bot can't chat with this user.."
                                            ).queue();
                                        }
                                    }
                            );
                            return;
                        } else {
                            channel.sendMessage("Only the assistant can chat with the user, using **!reply ** prefix.").queue();
                        }
                    }
                }
            }
            return;
        }
        if (event.isFromType(ChannelType.PRIVATE)) {
            PrivateChannel channel = event.getChannel().asPrivateChannel();
            User user = event.getAuthor();

            if (!TicketFile.getUserFile(plugin, user).exists()) {
                MessageEmbed embed = new EmbedSection(
                        plugin.getConfiguration().getSection("embeds.help")
                ).build(
                    TextReplacer.builder()
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
                        )
                );

                List<Button> buttons = new ArrayList<>();

                for (TicketType type : TicketType.values()) {
                    if (plugin.getConfiguration().getBoolean(type.getEnabled(), true)) {
                        buttons.add(
                                Button.primary(
                                        "create_" + type.getSimpleName(),
                                        plugin.getConfiguration().getString(type.getButtonName(), type.getName())
                                )
                        );
                    }
                }
                channel.sendMessageEmbeds(embed).addActionRow(buttons).queue(
                        message -> message.addReaction(Emoji.fromUnicode("✅")).queue()
                );
            } else {
                Ticket ticket = plugin.getTickets().getTicketMap().get(user.getId());

                if (ticket != null && ticket.isWorking()) {

                    TextChannel ticketChannel = plugin.getJDA().getTextChannelById(
                            ticket.getId()
                    );

                    List<Message.Attachment> attachments = event.getMessage().getAttachments();

                    if (attachments.isEmpty()) {
                        if (ticketChannel != null) {
                            ticketChannel.sendMessage(
                                    "**" + user.getName() + "** : " + event.getMessage().getContentRaw()
                            ).queue();
                        }
                        return;
                    }

                    File folder = TicketFile.getTicketCaptures(plugin, ticket);

                    List<File> fileList = new ArrayList<>();

                    for (Message.Attachment attachment : attachments) {
                        if (attachment.isImage() || attachment.isVideo()) {
                            try {
                                attachment.getProxy().downloadToFile(
                                    new File(
                                        folder,
                                        event.getAuthor().getName() + "-" + attachment.getFileName() + (attachment.getFileExtension() != null ? attachment.getFileExtension() : "")
                                    )
                                ).thenAccept(
                                        fileList::add
                                );
                            } catch (Exception ignored) {}
                        }
                    }

                    List<FileUpload> uploads = new ArrayList<>();

                    fileList.forEach(f -> uploads.add(FileUpload.fromData(f)));

                    FileUpload[] array = uploads.toArray(new FileUpload[fileList.size()]);

                    if (ticketChannel != null) {
                        ticketChannel.sendMessage(
                                "**" + user.getName() + "** : " + event.getMessage().getContentRaw()
                        ).setFiles(
                             array
                        ).queue();
                    }
                }
            }
        }
    }

    private User fetchUser(String id) {
        User user = plugin.getJDA().getUserById(id);

        if (user != null) {
            return user;
        }

        String guildID = plugin.getConfiguration().getString("settings.users-guild-id", "NOT_SET");

        if (guildID.isEmpty() || guildID.equalsIgnoreCase("NOT_SET") || guildID.equalsIgnoreCase("NOT-SET")) {
            plugin.getLogger().info("Guild is not set for find user id: " + id);
            return null;
        }

        Guild guild = plugin.getJDA().getGuildById(
                guildID
        );

        if (guild == null) {
            plugin.getLogger().info("Guild was not found for find user id: " + id);
            return null;
        }

        Member member = guild.getMemberById(id);

        if (member != null) {
            return member.getUser();
        }

        return null;
    }

    private Ticket fetchTicketChannel(TextChannel channel) {
        String channelID = plugin.getTickets().getChannelMap().get(
                channel.getId()
        );

        if (channelID == null) {
            channel.sendMessage("Ticket-ID was not found").queue();
            return null;
        }

        return plugin.getTickets().getTicketMap().get(
                channelID
        );
    }
}
