package net.oilcake.mitelros.item;

import net.minecraft.*;
import net.oilcake.mitelros.api.ITFItem;

import java.util.List;

public class ItemGoldenAppleLegend extends ItemFood {
    public ItemGoldenAppleLegend(int id, int satiation, int nutrition, String texture) {
        super(id, Material.fruit, satiation, nutrition, 1000, false, false, true, texture);
        addMaterial(Material.gold);
        setPlantProduct();
        ((ITFItem) this).setWater(-8);
    }

    public boolean hasEffect(ItemStack par1ItemStack) {
        return (par1ItemStack.getItemSubtype() > 0);
    }

    public EnumRarity f(ItemStack par1ItemStack) {
        return (par1ItemStack.getItemSubtype() == 0) ? EnumRarity.rare : EnumRarity.uncommon;
    }

    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List) {
        par3List.add(new ItemStack(par1, 1, 0));
        par3List.add(new ItemStack(par1, 1, 1));
    }

    public String getUnlocalizedName(ItemStack item_stack) {
        return isEnchantedGoldenApple(item_stack) ? "item.appleGold.enchanted" : super.getUnlocalizedName(item_stack);
    }

    public static boolean isGoldenApple(ItemStack item_stack) {
        return (item_stack != null && item_stack.itemID == Items.goldenAppleLegend.itemID);
    }

    public static boolean isUnenchantedGoldenApple(ItemStack item_stack) {
        return (isGoldenApple(item_stack) && item_stack.getItemSubtype() == 0);
    }

    public static boolean isEnchantedGoldenApple(ItemStack item_stack) {
        return (isGoldenApple(item_stack) && item_stack.getItemSubtype() > 0);
    }

    protected void onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
        if (par1ItemStack.getItemSubtype() == 0 && !par2World.isRemote)
            par3EntityPlayer.addPotionEffect(new PotionEffect(Potion.regeneration.id, 600, 4));
        if (par1ItemStack.getItemSubtype() > 0) {
            if (!par2World.isRemote) {
                par3EntityPlayer.addPotionEffect(new PotionEffect(Potion.regeneration.id, 600, 4));
                par3EntityPlayer.addPotionEffect(new PotionEffect(Potion.resistance.id, 6000, 1));
                par3EntityPlayer.addPotionEffect(new PotionEffect(Potion.fireResistance.id, 6000, 1));
            }
        } else {
            super.onEaten(par1ItemStack, par2World, par3EntityPlayer);
        }
    }

    public void onItemUseFinish(ItemStack item_stack, World world, EntityPlayer player) {
        super.onItemUseFinish(item_stack, world, player);
    }
}
