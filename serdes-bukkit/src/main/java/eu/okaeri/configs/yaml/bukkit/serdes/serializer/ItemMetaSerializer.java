package eu.okaeri.configs.yaml.bukkit.serdes.serializer;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemMetaSerializer implements ObjectSerializer<ItemMeta> {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Override
    public boolean supports(@NonNull Class<? super ItemMeta> type) {
        return ItemMeta.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull ItemMeta itemMeta, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        if (itemMeta.hasDisplayName()) {
            data.add("display", serializeMiniMessage(itemMeta.displayName()));
        }

        if (itemMeta.hasLore()) {
            List<String> serializedLore = itemMeta.lore().stream()
                .map(this::serializeMiniMessage)
                .collect(Collectors.toList());
            data.addCollection("lore", serializedLore, String.class);
        }

        if (!itemMeta.getEnchants().isEmpty()) {
            data.addAsMap("enchantments", itemMeta.getEnchants(), Enchantment.class, Integer.class);
        }

        if (!itemMeta.getItemFlags().isEmpty()) {
            data.addCollection("flags", itemMeta.getItemFlags(), ItemFlag.class);
        }
    }

    @Override
    public ItemMeta deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String displayName = data.get("display", String.class);
        List<String> lore = data.containsKey("lore")
            ? data.getAsList("lore", String.class)
            : Collections.emptyList();

        Map<Enchantment, Integer> enchantments = data.containsKey("enchantments")
            ? data.getAsMap("enchantments", Enchantment.class, Integer.class)
            : Collections.emptyMap();

        List<ItemFlag> itemFlags = new ArrayList<>(data.containsKey("flags")
            ? data.getAsList("flags", ItemFlag.class)
            : Collections.emptyList());

        ItemMeta itemMeta = new ItemStack(Material.COBBLESTONE).getItemMeta();
        if (itemMeta == null) {
            throw new IllegalStateException("Cannot extract empty ItemMeta from COBBLESTONE");
        }

        if (displayName != null) {
            itemMeta.displayName(deserializeMiniMessage(displayName));
        }

        itemMeta.lore(lore.stream().map(this::deserializeMiniMessage).collect(Collectors.toList()));

        enchantments.forEach((enchantment, level) -> itemMeta.addEnchant(enchantment, level, true));
        itemMeta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));

        return itemMeta;
    }

    private String serializeMiniMessage(Component component) {
        return MINI_MESSAGE.serialize(component)
            .replace("<!italic>", "");
    }

    private Component deserializeMiniMessage(String text) {
        return MINI_MESSAGE.deserialize("<!italic>" + text);
    }
}
