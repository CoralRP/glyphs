package ru.brikster.glyphs.bukkit;

import com.github.stefvanschie.inventoryframework.adventuresupport.ComponentHolder;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.brikster.glyphs.compile.GlyphCompiler;
import ru.brikster.glyphs.glyph.Glyph;
import ru.brikster.glyphs.glyph.GlyphComponentBuilder;
import ru.brikster.glyphs.glyph.GlyphComponentBuilder.PositionType;
import ru.brikster.glyphs.glyph.image.ImageGlyph;
import ru.brikster.glyphs.glyph.image.TextureProperties;
import ru.brikster.glyphs.glyph.space.SpacesGlyph;
import ru.brikster.glyphs.resources.GlyphResources;
import team.unnamed.creative.file.FileResource;
import team.unnamed.creative.file.FileTree;
import team.unnamed.creative.metadata.Metadata;
import team.unnamed.creative.metadata.PackMeta;
import team.unnamed.creative.texture.Texture;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipOutputStream;

public final class ExamplePlugin extends JavaPlugin {

    private static final GlyphCompiler COMPILER = GlyphCompiler.instance();

    private ChestGui chestGui;

    @Override
    public void onEnable() {
        var spaces = SpacesGlyph.create(GlyphResources.SPACE_IMAGE_WRITABLE);
        var guiBackground = ImageGlyph.of(Texture.of(
                        Key.key(Glyph.DEFAULT_NAMESPACE, "gui/gui_background"),
                        GlyphResources.resourceFromJar("gui_background.png")),
                new TextureProperties(256, 19));
        var font = GlyphResources.minecraftFontGlyphCollection(
                List.of(new TextureProperties(12, -6),
                        new TextureProperties(8, -24),
                        new TextureProperties(8, -36)));

        var resources = COMPILER.compile(spaces, guiBackground, font);
        createResourcepack(resources);

        var titleComponent = GlyphComponentBuilder.gui(spaces)
                .append(guiBackground)
                .append(16, font.translate(12, -6, "Example text"))
                .append(16, font.translate(8, -24, "Hello "))
                .append(PositionType.RELATIVE, font.translate(8, -24, "world..."))
                .append(PositionType.ABSOLUTE, 16, font.translate(8, -36, "Hello world...", NamedTextColor.LIGHT_PURPLE))
                .build();

        getLogger().info(GsonComponentSerializer.gson().serialize(titleComponent));

        this.chestGui = new ChestGui(4, ComponentHolder.of(titleComponent));

        Objects.requireNonNull(getCommand("glyphs")).setExecutor(this);
    }

    @SneakyThrows
    private void createResourcepack(Collection<FileResource> resources) {
        File file = new File(getDataFolder(), "pack.zip");
        getDataFolder().mkdirs();
        file.createNewFile();
        try (FileTree tree = FileTree.zip(new ZipOutputStream(new FileOutputStream(file)))) {
            tree.write(Metadata.builder()
                    .add(PackMeta.of(9, "Example resourcepack"))
                    .build());

            resources.forEach(tree::write);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (sender instanceof Player) {
            chestGui.show((HumanEntity) sender);
        } else {
            sender.sendMessage("You're not a player");
        }
        return true;
    }

}