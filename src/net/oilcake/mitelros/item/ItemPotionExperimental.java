package net.oilcake.mitelros.item;

import net.minecraft.*;

import java.util.Random;

public class ItemPotionExperimental extends Item{
        public ItemPotionExperimental(int id) {
            super(id, Material.glass, "experimental_potion");
            this.setMaxStackSize(1);
            this.setCraftingDifficultyAsComponent(25.0F);
            this.setCreativeTab(CreativeModeTab.tabMisc);
        }

        public void onItemUseFinish(ItemStack item_stack, World world, EntityPlayer player) {
            double rand = Math.random();
            if (player.onServer()) {
                if(rand<0.25){
                    player.addPotionEffect((new MobEffect(MobEffectList.regeneration.id, 400, 0)));
                }
                if(rand>0.15&&rand<0.7){
                    player.addPotionEffect((new MobEffect(MobEffectList.fireResistance.id, 400, 0)));
                }
                if(rand>0.4&&rand<0.95){
                    player.addPotionEffect((new MobEffect(MobEffectList.resistance.id, 400, 0)));
                }
                if(rand>0.9){
                    player.addPotionEffect((new MobEffect(MobEffectList.poison.id, 400, 0)));
                }
            }
            super.onItemUseFinish(item_stack, world, player);
        }

        public int getMaxItemUseDuration(ItemStack par1ItemStack) {
            return 32;
        }

        public boolean isDrinkable(int item_subtype) {
            return true;
        }

        public Item getItemProducedOnItemUseFinish() {
            return glassBottle;
        }
}
