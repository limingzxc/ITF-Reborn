package net.oilcake.mitelros.mixins.world;

import net.minecraft.BiomeGenBase;
import net.minecraft.GenLayer;
import net.minecraft.GenLayerBiome;
import net.minecraft.WorldType;
import net.oilcake.mitelros.world.BiomeBases;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GenLayerBiome.class})
public class GenLayerBiomeMixin extends GenLayer {
  @Shadow
  private BiomeGenBase[] allowedBiomes;
  
  public GenLayerBiomeMixin(long par1) {
    super(par1);
  }
  
  @Inject(method = {"<init>(JLnet/minecraft/GenLayer;Lnet/minecraft/WorldType;)V"}, at = {@At("RETURN")}, cancellable = true)
  private void inject(long par1, GenLayer par3GenLayer, WorldType par4WorldType, CallbackInfo callbackInfo) {
    this.parent = par3GenLayer;
    if (par4WorldType == WorldType.LARGE_BIOMES) {
      this.allowedBiomes = new BiomeGenBase[] { BiomeGenBase.desert, BiomeGenBase.forest, BiomeGenBase.extremeHills, BiomeGenBase.swampland, BiomeGenBase.plains, BiomeGenBase.taiga, BiomeGenBase.jungle, BiomeBases.BIOME_SAVANNA};
    } else if (par4WorldType == WorldType.DEFAULT) {
      this.allowedBiomes = new BiomeGenBase[] { BiomeGenBase.desert, BiomeGenBase.forest, BiomeGenBase.extremeHills, BiomeGenBase.swampland, BiomeGenBase.plains, BiomeGenBase.taiga, BiomeGenBase.jungle };
    } 
  }
  
  @Shadow
  public int[] getInts(int par1, int par2, int par3, int par4, int z) {
    return new int[] { 1 };
  }
}
