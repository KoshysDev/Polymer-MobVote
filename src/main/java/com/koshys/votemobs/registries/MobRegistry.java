package com.koshys.votemobs.registries;

import com.koshys.votemobs.entities.Crab;
import com.koshys.votemobs.entities.Penguin;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.item.PolymerSpawnEggItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biomes;
import com.koshys.votemobs.util.Util;

public class MobRegistry {

    public static final EntityType<Crab> CRAB = register(
            Crab.ID,
            FabricEntityTypeBuilder.createMob()
                    .entityFactory(Crab::new)
                    .spawnGroup(MobCategory.CREATURE)
                    .dimensions(EntityDimensions.scalable(0.8f, 0.6f))
                    .defaultAttributes(Crab::createAttributes)
    );

    public static final EntityType<Penguin> PENGUIN = register(
            Penguin.ID,
            FabricEntityTypeBuilder.createMob()
                    .entityFactory(Penguin::new)
                    .spawnGroup(MobCategory.CREATURE)
                    .dimensions(EntityDimensions.scalable(0.8f, 0.6f))
                    .defaultAttributes(Penguin::createAttributes)
    );


    private static <T extends Entity> EntityType<T> register(ResourceLocation id, FabricEntityTypeBuilder<T> builder) {
        EntityType<T> type = builder.build();
        PolymerEntityUtils.registerType(type);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type);
    }

    public static void registerMobs() {

        // Crab spawn
        BiomeModifications.addSpawn(BiomeSelectors.spawnsOneOf(EntityType.TURTLE)
                        .or(BiomeSelectors.tag(BiomeTags.IS_BEACH))
                        .or(BiomeSelectors.includeByKey(Biomes.BEACH)),
                MobCategory.CREATURE, CRAB, 5, 1, 3);

        //Penguin spawn
        BiomeModifications.addSpawn(BiomeSelectors.spawnsOneOf(EntityType.POLAR_BEAR, EntityType.DOLPHIN)
                        .or(BiomeSelectors.includeByKey(Biomes.SNOWY_BEACH, Biomes.COLD_OCEAN, Biomes.DEEP_COLD_OCEAN,
                                Biomes.FROZEN_OCEAN, Biomes.FROZEN_RIVER, Biomes.DEEP_FROZEN_OCEAN, Biomes.ICE_SPIKES, Biomes.SNOWY_PLAINS)),
                MobCategory.CREATURE, PENGUIN, 10, 2, 6);

        //Eggs
        addSpawnEgg(CRAB, Items.TURTLE_SPAWN_EGG);
        addSpawnEgg(PENGUIN, Items.BAT_SPAWN_EGG);

        PolymerItemGroupUtils.registerPolymerItemGroup(Util.id("spawn-eggs"), ITEM_GROUP);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addSpawnEgg(EntityType type, Item item) {
        Item spawnEgg = new PolymerSpawnEggItem(type, item, new Item.Properties());
        registerItem(Util.id(EntityType.getKey(type).getPath() + "_spawn_egg"), spawnEgg);
    }

    private static void registerItem(ResourceLocation identifier, Item item) {
        Registry.register(BuiltInRegistries.ITEM, identifier, item);
        SPAWN_EGGS.putIfAbsent(identifier, item);
    }

    public static final Object2ObjectOpenHashMap<ResourceLocation, Item> SPAWN_EGGS = new Object2ObjectOpenHashMap<>();
    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab.Builder(null, -1)
            .title(Component.literal("VoteMobs").withStyle(ChatFormatting.WHITE))
            .icon(Items.BAT_SPAWN_EGG::getDefaultInstance)
            .displayItems((parameters, output) -> SPAWN_EGGS.values().forEach(output::accept))
            .build();
}