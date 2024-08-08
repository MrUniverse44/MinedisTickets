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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
                        String contentRaw = event.getMessage().getContentRaw();

                        if (
                            contentRaw.startsWith("!ticket assists") || contentRaw.startsWith("!tickets assists") ||
                            contentRaw.startsWith("!t stats") || contentRaw.startsWith("!t info") || contentRaw.startsWith("!t assists") ||
                            contentRaw.startsWith("!tickets stats") || contentRaw.startsWith("!tickets info") ||
                            contentRaw.startsWith("!ticket stats") || contentRaw.startsWith("!ticket info")
                        ) {
                            int total = plugin.getConfiguration().getInt("assists." + event.getAuthor().getName(), 0);

                            channel.sendMessageEmbeds(
                                new EmbedSection(
                                    plugin.getConfiguration().getSection("embed.assists")
                                ).build(
                                    TextReplacer.builder()
                                        .replace("<mention>", event.getAuthor().getAsMention())
                                        .replace("<assists>", String.valueOf(total))
                                )
                            ).queue();
                            return;
                        }

                        if (
                            contentRaw.startsWith("!ticket help") || contentRaw.startsWith("!tickets help") || contentRaw.startsWith("!t help")
                        ) {
                            channel.sendMessageEmbeds(
                                new EmbedSection(
                                    plugin.getConfiguration().getSection("embeds.ticket-help-command")
                                ).build()
                            ).queue();
                        }

                        if (
                            contentRaw.startsWith("!ticket leaderboard") || contentRaw.startsWith("!ticket leader") ||
                            contentRaw.startsWith("!tickets leaderboard") || contentRaw.startsWith("!tickets leader") ||
                            contentRaw.startsWith("!ticket lb") || contentRaw.startsWith("!ticket top") ||
                            contentRaw.startsWith("!t leaderboard") || contentRaw.startsWith("!t leader") ||
                            contentRaw.startsWith("!t lb") || contentRaw.startsWith("!t top") ||
                            contentRaw.startsWith("!tickets lb") || contentRaw.startsWith("!tickets top")
                        ) {
                            Map<String, Integer> staffMap = new HashMap<>();

                            for (String key : plugin.getConfiguration().getSection("assists").getKeys()) {
                                staffMap.put(
                                    key,
                                    plugin.getConfiguration().getInt("assists." + key, 0)
                                );
                            }

                            int position = 1;

                            EmbedSection section = new EmbedSection(plugin.getConfiguration().getSection("embeds.leaderboard"));

                            for (Map.Entry<String, Integer> entry : Leaderboard.sort(staffMap)) {
                                section.addField(
                                    EmbedSection.EmbedField.createField(
                                        false,
                                        "Top " + position + ". " + entry.getKey(),
                                        entry.getValue() + " - " + getStaffTime(entry.getKey())
                                    )
                                );
                                position++;
                            }

                            channel.sendMessageEmbeds(section.build()).queue();
                            return;
                        }
                    }
                }

                String contentRaw = event.getMessage().getContentRaw();

                if (
                    contentRaw.startsWith("!t discharge") ||
                    contentRaw.startsWith("!ticket discharge") ||
                    contentRaw.startsWith("!tickets discharge")
                ) {
                    if (plugin.getTickets().contains(channel)) {
                        Ticket ticket = fetchTicketChannel(channel);

                        if (ticket == null) {
                            return;
                        }

                        if (ticket.getState() == TicketState.IN_PROCESS) {

                            ticket.setAssistant("NOT_SET");
                            ticket.setState(TicketState.WITHOUT_ASSISTANT);
                            ticket.update(plugin);

                            channel.sendMessage(event.getAuthor().getAsMention() + " completed.").queue();
                        }
                    }
                    return;
                }

                if (
                    contentRaw.startsWith("!t id") ||
                    contentRaw.startsWith("!ticket id") ||
                    contentRaw.startsWith("!tickets id")
                ) {
                    if (plugin.getTickets().contains(channel)) {
                        Ticket ticket = fetchTicketChannel(channel);

                        if (ticket == null) {
                            return;
                        }

                        channel.sendMessage(event.getAuthor().getAsMention() + " user id: " + ticket.getUserId()).queue();
                    }
                    return;
                }

                if (
                    contentRaw.startsWith("!ticket assist") ||
                    contentRaw.startsWith("!tickets assist") ||
                    contentRaw.startsWith("!t assist") ||
                    contentRaw.startsWith("!ticket claim") ||
                    contentRaw.startsWith("!tickets claim") ||
                    contentRaw.startsWith("!t claim")
                ) {
                    if (plugin.getTickets().contains(channel)) {
                        Ticket ticket = fetchTicketChannel(channel);

                        if (ticket == null) {
                            return;
                        }

                        if (ticket.getState() == TicketState.WITHOUT_ASSISTANT) {


                            channel.sendMessage("**" + event.getAuthor().getName() + "** now you are an assistant.").queue();
                            ticket.setAssistant(event.getAuthor().getId());
                            ticket.setState(TicketState.IN_PROCESS);


                            ticket.update(plugin);

                            int total = plugin.getConfiguration().getInt("assists." + event.getAuthor().getName(), 0);

                            total++;

                            plugin.getConfiguration().set("assists." + event.getAuthor().getName(), total);
                            plugin.saveConfiguration();
                            plugin.reloadConfiguration();

                            plugin.getJDA().retrieveUserById(ticket.getUser()).queue(
                                user -> {
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
                                }
                            );
                            return;
                        } else {
                            channel.sendMessage("This ticket already has an assistant assigned.").queue();
                        }
                        return;
                    }
                }
                if (
                    contentRaw.startsWith("!t close") ||
                    contentRaw.startsWith("!ticket close") ||
                    contentRaw.startsWith("!tickets close")
                ) {
                    if (plugin.getTickets().contains(channel)) {
                        Ticket ticket = fetchTicketChannel(channel);

                        if (ticket == null) {
                            channel.sendMessage("Ticket was not found").queue();
                            return;
                        }

                        if (ticket.isAssistant(event.getAuthor())) {
                            plugin.getJDA().retrieveUserById(ticket.getUser()).queue(
                                user -> {
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

                                    Category category = channel.getGuild().getCategoryById(categoryID);

                                    if (category == null) {
                                        return;
                                    }

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
                                }
                            );
                            return;
                        } else {
                            channel.sendMessage("Only the assistant can interact with the user.").queue();
                        }
                    }
                    return;
                }
                if (
                    contentRaw.startsWith("!ticket leave") ||
                    contentRaw.startsWith("!t leave") ||
                    contentRaw.startsWith("!tickets leave")
                ) {
                    if (plugin.getTickets().contains(channel)) {
                        Ticket ticket = fetchTicketChannel(channel);

                        if (ticket == null) {
                            channel.sendMessage("Ticket was not found").queue();
                            return;
                        }

                        if (ticket.isAssistant(event.getAuthor())) {
                            plugin.getJDA().retrieveUserById(ticket.getUser()).queue(
                                user -> {
                                    if (user == null) {
                                        channel.sendMessage("Can't find this user").queue();
                                        return;
                                    }

                                    ticket.setAssistant("NOT_SET");
                                    ticket.setState(TicketState.WAITING_RESPONSE);
                                    ticket.update(plugin);

                                    int total = plugin.getConfiguration().getInt("assists." + event.getAuthor().getName(), 0);

                                    total--;

                                    plugin.getConfiguration().set("assists." + event.getAuthor().getName(), total);
                                    plugin.saveConfiguration();
                                    plugin.reloadConfiguration();

                                    channel.sendMessage("Waiting for other assistant").queue();
                                }
                            );
                            return;
                        } else {
                            channel.sendMessage("Only the assistant can chat with the user, using **!ticket reply ** prefix.").queue();
                        }
                    }
                    return;
                }
                if (
                    contentRaw.startsWith("!ticket transfer") ||
                    contentRaw.startsWith("!t transfer") ||
                    contentRaw.startsWith("!tickets transfer")
                ) {
                    if (plugin.getTickets().contains(channel)) {
                        Ticket ticket = fetchTicketChannel(channel);

                        if (ticket == null) {
                            channel.sendMessage("Ticket was not found").queue();
                            return;
                        }

                        if (ticket.isAssistant(event.getAuthor())) {
                            plugin.getJDA().retrieveUserById(ticket.getUser()).queue(
                                user -> {
                                    if (user == null) {
                                        channel.sendMessage("Can't find this user").queue();
                                        return;
                                    }

                                    ticket.setAssistant("NOT_SET");
                                    ticket.setState(TicketState.WAITING_RESPONSE);
                                    ticket.update(plugin);

                                    plugin.saveConfiguration();
                                    plugin.reloadConfiguration();

                                    channel.sendMessage("Waiting for other assistant").queue();
                                }
                            );
                            return;
                        } else {
                            channel.sendMessage("Only the assistant can chat with the user, using **!ticket reply ** prefix.").queue();
                        }
                    }
                    return;
                }
                if (
                    contentRaw.startsWith("!t rename") ||
                    contentRaw.startsWith("!ticket rename") ||
                    contentRaw.startsWith("!tickets rename") ||
                    contentRaw.startsWith("!t rn") ||
                    contentRaw.startsWith("!ticket rn") ||
                    contentRaw.startsWith("!tickets rn")
                ) {
                    if (plugin.getTickets().contains(channel)) {
                        Ticket ticket = fetchTicketChannel(channel);

                        if (ticket == null) {
                            channel.sendMessage("Ticket was not found").queue();
                            return;
                        }

                        if (ticket.isAssistant(event.getAuthor())) {
                            String name = contentRaw.replace("!t rename ", "")
                                .replace("!ticket rename ", "")
                                .replace("!tickets rename ", "")
                                .replace("!t rn ", "")
                                .replace("!tickets rn ", "")
                                .replace("!ticket rn ", "")
                                .replace("!t rename", "")
                                .replace("!ticket rename", "")
                                .replace("!tickets rename", "")
                                .replace("!t rn", "")
                                .replace("!tickets rn", "")
                                .replace("!ticket rn", "")
                                .replace(" ", "-");

                            if (name.length() <= 2) {
                                channel.sendMessage(
                                        "Correct usage: **!t r (channel name)**, nombres muy cortos no son aceptados."
                                ).queue();
                            }

                            plugin.getJDA().retrieveUserById(ticket.getUser()).queue(
                                user -> {
                                    if (user == null) {
                                        channel.sendMessage("Can't find this user").queue();
                                        return;
                                    }

                                    if (name.length() >= 2) {
                                        channel.getManager().setName(name).queue();
                                    }
                                }
                            );
                            return;
                        } else {
                            channel.sendMessage("Only the assistant can chat with the user, using **!reply ** prefix.").queue();
                        }
                    }
                    return;
                }
                if (
                    contentRaw.startsWith("!t reply ") ||
                    contentRaw.startsWith("!t r") ||
                    contentRaw.startsWith("!ticket reply ") ||
                    contentRaw.startsWith("!tickets reply ") ||
                    contentRaw.startsWith("!ticket r ") ||
                    contentRaw.startsWith("!tickets r ")
                ) {

                    if (plugin.getTickets().contains(channel)) {
                        Ticket ticket = fetchTicketChannel(channel);

                        if (ticket == null) {
                            channel.sendMessage("Ticket was not found").queue();
                            return;
                        }

                        if (ticket.isAssistant(event.getAuthor())) {
                            String message = contentRaw
                                .replace("!tickets reply ", "")
                                .replace("!t reply ", "")
                                .replace("!ticket reply ", "")
                                .replace("!t r ", "")
                                .replace("!tickets r ", "")
                                .replace("!ticket r ", "");

                            plugin.getJDA().retrieveUserById(ticket.getUser()).queue(
                                    user -> {
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
                                                                    "Message has been sent, Preview:"
                                                            ).queue();
                                                            channel.sendMessage(
                                                                message
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

                                        List<File> fileList = attachments.stream()
                                                .filter(attachment -> attachment.isImage() || attachment.isVideo())
                                                .map(attachment -> {
                                                    File file = new File(folder, event.getAuthor().getName() + "-" + attachment.getFileName());
                                                    try {
                                                        attachment.getProxy().downloadToFile(file).get();
                                                        return file;
                                                    } catch (Exception e) {
                                                        plugin.getLogger().info("Can't download file from ticket: " + file.getName());
                                                        e.printStackTrace();
                                                        return null;
                                                    }
                                                })
                                                .filter(Objects::nonNull)
                                                .collect(Collectors.toList());

                                        FileUpload[] array = fileList.stream().map(FileUpload::fromData).toArray(FileUpload[]::new);

                                        user.openPrivateChannel().queue(
                                                userChannel -> {
                                                    if (userChannel != null && userChannel.canTalk()) {
                                                        userChannel.sendMessage(
                                                                message
                                                        ).setFiles(array).queue();

                                                        channel.sendMessage(
                                                                "Message sent, Preview:"
                                                        ).queue();

                                                        channel.sendMessage(
                                                            message
                                                        ).setFiles(array).queue();
                                                    } else {
                                                        channel.sendMessage(
                                                                "This user has MD disabled for bots, the bot can't chat with this user.."
                                                        ).queue();
                                                    }
                                                }
                                        );
                                    }
                            );
                            return;
                        } else {
                            channel.sendMessage("Only the assistant can chat with the user, using **!ticket reply ** prefix.").queue();
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

                    List<File> fileList = attachments.stream()
                        .filter(attachment -> attachment.isImage() || attachment.isVideo())
                        .map(attachment -> {
                            File file = new File(folder, event.getAuthor().getName() + "-" + attachment.getFileName());
                            try {
                                attachment.getProxy().downloadToFile(file).get();
                                return file;
                            } catch (Exception e) {
                                plugin.getLogger().info("Can't download file from ticket: " + file.getName());
                                e.printStackTrace();
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                    FileUpload[] array = fileList.stream().map(FileUpload::fromData).toArray(FileUpload[]::new);

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

    public String getStaffTime(String staffId) {
        return "0d 0h 0m 0s";
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
