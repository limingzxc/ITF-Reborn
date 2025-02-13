package net.oilcake.mitelros.block;

import net.minecraft.BlockBreakInfo;
import net.minecraft.BlockCrops;
import net.minecraft.World;
import net.oilcake.mitelros.item.Items;

public class BlockBeetroots extends BlockCrops {
  public BlockBeetroots(int par1) {
    super(par1, 4);
  }
  
  protected int getSeedItem() {
    return Items.seedsBeetroot.itemID;
  }
  
  protected int getCropItem() {
    return Items.beetroot.itemID;
  }
  
  public void breakBlock(World world, int x, int y, int z, int block_id, int metadata) {
    super.breakBlock(world, x, y, z, block_id, metadata);
    BlockBreakInfo info = new BlockBreakInfo(world, x, y, z);
    if (isBlighted(metadata) || (getGrowth(metadata) > 0 && !isMature(metadata))) {
      if (info.wasSnowedUpon())
        playCropPopSound(info); 
    } else if (isMature(metadata)) {
      for (int i = 0; i < Math.random() * 2.0D; i++)
        dropBlockAsEntityItem(info, getSeedItem(), 0, 1, 1.0F); 
    } 
  }
  
  protected int getDeadCropBlockId() {
    return Blocks.beetrootsDead.blockID;
  }
  
  protected int getMatureYield() {
    return (Math.random() < 0.5D) ? 3 : 2;
  }
  
  public int getGrowthStage(int metadata) {
    int growth = getGrowth(metadata);
    if (growth == 6)
      growth = 5; 
    return growth / 2;
  }
}
