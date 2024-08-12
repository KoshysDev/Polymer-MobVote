package com.koshys.votemobs;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import com.koshys.votemobs.entities.Crab;
import com.koshys.votemobs.registries.MobRegistry;
import com.koshys.votemobs.registries.SoundRegistry;

public class VoteMobs implements ModInitializer {
	public static final String MODID = "votemobs";

	@Override
	public void onInitialize() {
		PolymerResourcePackUtils.addModAssets(MODID);
		PolymerResourcePackUtils.markAsRequired();

		SoundRegistry.registerSounds();
		MobRegistry.registerMobs();
	}
}