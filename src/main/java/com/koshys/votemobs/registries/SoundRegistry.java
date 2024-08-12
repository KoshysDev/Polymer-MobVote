package com.koshys.votemobs.registries;

import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class SoundRegistry {
    public static final SoundEvent CRAB_AMBIENT = SoundEvents.TURTLE_AMBIENT_LAND; // Use existing vanilla sound
    public static final SoundEvent CRAB_HURT = SoundEvents.TURTLE_HURT; // Use existing vanilla sound
    public static final SoundEvent CRAB_DEATH = SoundEvents.TURTLE_DEATH; // Use existing vanilla sound

    public static void registerSounds() {
    }
}