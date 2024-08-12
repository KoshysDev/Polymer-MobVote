package com.koshys.votemobs.util;

import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.file.loader.BbModelLoader;
import com.koshys.votemobs.VoteMobs;
import net.minecraft.resources.ResourceLocation;

public class Util {
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(VoteMobs.MODID, path);
    }

    public static Model loadModel(ResourceLocation resourceLocation) {
        return BbModelLoader.load(resourceLocation);
    }
}