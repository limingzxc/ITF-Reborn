package net.oilcake.mitelros.mixins.block;

import net.minecraft.BiomeGenBase;
import net.minecraft.BlockBush;
import net.minecraft.BlockGrowingPlant;
import net.minecraft.World;
import net.oilcake.mitelros.api.ITFWorld;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({BlockBush.class})
public abstract class BlockBushMixin extends BlockGrowingPlant {
    public BlockBushMixin(int block_id) {
        super(block_id);
    }

    public float getGrowthRate(World world, int x, int y, int z) {
        float growth_rate = 0.1F + ((((ITFWorld) world).getWorldSeason() == 2) ? 0.15F : 0.0F);
        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
        growth_rate *= getTemperatureGrowthRateModifier(biome.temperature);
        growth_rate *= getHumidityGrowthRateModifier(biome.isHighHumidity());
        growth_rate *= getGlobalGrowthRateModifierFromMITE();
        return growth_rate;
    }
}
