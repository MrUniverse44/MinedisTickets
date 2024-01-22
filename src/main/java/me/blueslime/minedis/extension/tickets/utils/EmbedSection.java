package me.blueslime.minedis.extension.tickets.utils;

import me.blueslime.minedis.utils.text.TextReplacer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.md_5.bungee.config.Configuration;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EmbedSection {
    private static final TextReplacer EMPTY = TextReplacer.builder();
    private final List<EmbedField> fieldList = new ArrayList<>();
    private String description = "";
    private String thumbnail = null;
    private String footer = null;
    private String author = null;
    private String title = null;
    private String image = null;
    private String url = null;
    private Color color = Color.YELLOW;

    public EmbedSection(Configuration configuration) {
        if (configuration == null) {
            return;
        }
        if (configuration.contains("description")) {
            description = configuration.getString("description", " ");
        }
        if (configuration.contains("footer")) {
            footer = configuration.getString("footer", " ");
        }
        if (configuration.contains("title")) {
            title = configuration.getString("title", " ");
        }
        if (configuration.contains("color")) {
            color = ColorUtils.getColor(
                    configuration.getString("color", "YELLOW")
            );
        }
        if (configuration.contains("thumbnail")) {
            thumbnail = configuration.getString("thumbnail", "");
        }
        if (configuration.contains("image")) {
            image = configuration.getString("image", "");
        }
        if (configuration.contains("author")) {
            author = configuration.getString("author", "");
        }
        if (configuration.contains("url")) {
            url = configuration.getString("url", "");
        }

        if (configuration.contains("fields")) {
            for (String key : configuration.getSection("fields").getKeys()) {
                fieldList.add(
                        new EmbedField(
                                configuration.getBoolean("fields." + key + ".inline", true),
                                configuration.getString("fields." + key + ".name", " "),
                                configuration.getString("fields." + key + ".value", " ")
                        )
                );
            }
        }
    }

    public MessageEmbed build() {
        return build(EMPTY);
    }

    public MessageEmbed build(TextReplacer replacer) {

        EmbedBuilder builder = new EmbedBuilder().setColor(color);

        if (description.isEmpty()) {
            builder.setDescription(" ");
        } else {
            builder.setDescription(
                    replacer.apply(description)
            );
        }

        if (thumbnail != null) {
            builder.setThumbnail(
                    replacer.apply(
                            thumbnail
                    )
            );
        }

        if (footer != null) {
            String[] split = footer.split("<split>");

            if (split.length == 1) {
                builder.setFooter(
                        replacer.apply(footer)
                );
            } else {
                builder.setFooter(
                        replacer.apply(split[0]),
                        replacer.apply(split[1])
                );
            }
        }

        if (!fieldList.isEmpty()) {
            for (EmbedField field : fieldList) {
                builder.addField(
                        replacer.apply(field.getName()),
                        replacer.apply(field.getValue()),
                        field.isInline()
                );
            }
        }

        if (author != null) {
            String[] split = author.split("<split>");

            if (split.length == 1) {
                builder.setAuthor(
                        replacer.apply(author)
                );
            } else if (split.length == 2) {
                builder.setAuthor(
                        replacer.apply(split[0]),
                        replacer.apply(split[1])
                );
            } else {
                builder.setAuthor(
                        replacer.apply(split[0]),
                        replacer.apply(split[1]),
                        replacer.apply(split[2])
                );
            }

        }

        if (title != null) {
            String[] split = title.split("<split>");
            if (split.length == 1) {
                builder.setTitle(
                        replacer.apply(title)
                );
            } else {
                builder.setTitle(
                        replacer.apply(split[0]),
                        replacer.apply(split[1])
                );
            }
        }

        if (image != null) {
            builder.setImage(
                    replacer.apply(image)
            );
        }

        if (url != null) {
            builder.setUrl(
                    replacer.apply(url)
            );
        }

        return builder.build();
    }

    public static class EmbedField {

        private final boolean inline;
        private final String value;
        private final String name;

        public EmbedField(boolean inline, String name, String value) {
            this.inline = inline;
            this.value = value;
            this.name = name;
        }

        public boolean isInline() {
            return inline;
        }

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }
}


