package net.oilcake.mitelros.block;

import net.minecraft.*;
import net.oilcake.mitelros.item.Items;

import java.util.Random;

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
        if (this.isBlighted(metadata) || this.getGrowth(metadata) > 0 && !this.isMature(metadata)) {
            if (info.wasSnowedUpon()) {
                playCropPopSound(info);
            }
        } else {
            this.dropBlockAsEntityItem(info, this.getSeedItem(), 0, 1, 1F);
        }

    }

    protected int getDeadCropBlockId() {
        return Blocks.beetrootsDead.blockID;
    }

    protected int getMatureYield() {
        return Math.random() < 0.5 ? 2 : 1;
    }

    public int getGrowthStage(int metadata) {
        int growth = this.getGrowth(metadata);
        if (growth == 6) {
            growth = 5;
        }
        return growth / 2;
    }
}
