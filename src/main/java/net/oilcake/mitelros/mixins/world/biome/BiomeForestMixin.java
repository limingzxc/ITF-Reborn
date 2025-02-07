package net.oilcake.mitelros.mixins.world.biome;

import net.minecraft.*;
import net.oilcake.mitelros.config.ITFConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin({BiomeGenForest.class})
public class BiomeForestMixin extends BiomeGenBase {
  protected BiomeForestMixin(int par1) {
    super(par1);
  }
  
  @Inject(method = {"<init>(I)V"}, at = {@At("RETURN")})
  public void injectCtor(CallbackInfo callbackInfo) {
    if (((Boolean) ITFConfig.TagCreaturesV2.get()))
      RegenHostileAnimals(); 
  }
  
  private void RegenHostileAnimals() {
    removeEntityFromSpawnableLists(EntityWolf.class);
    this.spawnableCreatureList.add(new SpawnListEntry(EntityWolf.class, 5, 4, 8));
  }
  
  public void decorate(World par1World, Random par2Random, int par3, int par4) {
    super.decorate(par1World, par2Random, par3, par4);
  }
}
