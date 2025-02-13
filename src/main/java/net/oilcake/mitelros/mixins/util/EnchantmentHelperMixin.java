package net.oilcake.mitelros.mixins.util;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.*;
import net.oilcake.mitelros.api.ITFEnchantment;
import net.oilcake.mitelros.item.Materials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Shadow
    private static Map mapEnchantmentData(int enchantment_levels, ItemStack item_stack) {
        return null;
    }

    @Shadow
    private static void removeEnchantmentsFromMapThatConflict(Map map, ArrayList enchantments) {
    }

    @ModifyExpressionValue(method = "mapEnchantmentData", at = @At(value = "INVOKE", target = "Lnet/minecraft/Enchantment;canEnchantItem(Lnet/minecraft/Item;)Z"))
    private static boolean noTreasureAndCurse(boolean original, @Local Enchantment enchantment) {
        return original && !((ITFEnchantment) enchantment).isCurse() && !((ITFEnchantment) enchantment).isTreasure();
    }

    /**
     * @author uru can enchant 5
     * @reason
     */
    @Overwrite
    public static List buildEnchantmentList(Random random, ItemStack item_stack, int enchantment_levels) {
        Item item = item_stack.getItem();
        int enchantability = item.getItemEnchantability();
        if (enchantability <= 0)
            return null;
        float randomness = 1.0F + (random.nextFloat() - 0.5F) * 0.5F;
        int adjusted_enchantment_levels = (int) (enchantment_levels * randomness);
        if (adjusted_enchantment_levels < 1)
            adjusted_enchantment_levels = 1;
        ArrayList<EnchantmentData> enchantments_for_item = new ArrayList();
        while (adjusted_enchantment_levels > 0) {
            Map all_possible_enchantments = mapEnchantmentData(adjusted_enchantment_levels, item_stack);
            if (all_possible_enchantments == null)
                break;
            removeEnchantmentsFromMapThatConflict(all_possible_enchantments, enchantments_for_item);
            if (all_possible_enchantments.isEmpty())
                break;
            EnchantmentData enchantment_data = (EnchantmentData) WeightedRandom.getRandomItem(random, all_possible_enchantments.values());
            if (enchantment_data == null)
                break;
            Enchantment enchantment = enchantment_data.enchantmentobj;
            if (enchantments_for_item.size() < ((item.getMaterialForRepairs() == Materials.uru) ? 4 : 2) && all_possible_enchantments.size() > 1 && enchantment.hasLevels() && random.nextInt(2) == 0)
                enchantment_data.enchantmentLevel = random.nextInt(enchantment_data.enchantmentLevel) + 1;
            enchantments_for_item.add(enchantment_data);
            adjusted_enchantment_levels -= enchantment.hasLevels() ? enchantment.getMinEnchantmentLevelsCost(enchantment_data.enchantmentLevel) : enchantment.getMinEnchantmentLevelsCost();
            adjusted_enchantment_levels -= 5;
            if (adjusted_enchantment_levels < 5 || enchantments_for_item.size() > ((item.getMaterialForRepairs() == Materials.uru) ? 4 : 2))
                break;
        }
        ArrayList<EnchantmentData> enchantments_for_item_shuffled = new ArrayList();
        int n = enchantments_for_item.size();
        while (n > 0) {
            int index = random.nextInt(enchantments_for_item.size());
            EnchantmentData enchantment_data = enchantments_for_item.get(index);
            if (enchantment_data != null) {
                enchantments_for_item_shuffled.add(enchantment_data);
                enchantments_for_item.set(index, null);
                n--;
            }
        }
        return (enchantments_for_item_shuffled.size() == 0) ? null : enchantments_for_item_shuffled;
    }
}
