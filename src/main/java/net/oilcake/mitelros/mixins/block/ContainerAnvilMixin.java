package net.oilcake.mitelros.mixins.block;

import net.minecraft.*;
import net.oilcake.mitelros.api.ITFEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({ContainerRepair.class})
public class ContainerAnvilMixin {
    @Shadow
    private IInventory inputSlots;

    @Redirect(method = {"updateRepairOutput()V"}, at = @At(ordinal = 0, value = "INVOKE", target = "Lnet/minecraft/ItemStack;isEnchantable()Z"))
    public boolean isEnchantable(ItemStack item_stack_in_first_slot) {
        if (item_stack_in_first_slot.isEnchantable())
            return true;
        ItemStack bookStack = this.inputSlots.getStackInSlot(1);
        if (bookStack != null && bookStack.getItem() instanceof net.minecraft.ItemEnchantedBook) {
            NBTTagList nbtList = bookStack.getStoredEnchantmentTagList();
            if (nbtList != null)
                for (int i = 0; i < nbtList.tagCount(); i++) {
                    short id = ((NBTTagCompound) nbtList.tagAt(i)).getShort("id");
                    if (Enchantment.enchantmentsList[id] != null)
                        return ((ITFEnchantment) Enchantment.enchantmentsList[id]).isTreasure();
                }
        }
        return false;
    }

    @Redirect(method = {"updateRepairOutput()V"}, at = @At(ordinal = 0, value = "INVOKE", target = "Lnet/minecraft/ItemStack;isItemEnchanted()Z"))
    public boolean updateRepairOutputEnableEnhanted(ItemStack item_stack_in_first_slot) {
        ItemStack bookStack = this.inputSlots.getStackInSlot(1);
        if (bookStack != null && bookStack.getItem() instanceof net.minecraft.ItemEnchantedBook) {
            NBTTagList nbtList = bookStack.getStoredEnchantmentTagList();
            if (nbtList != null)
                for (int i = 0; i < nbtList.tagCount(); i++) {
                    short id = ((NBTTagCompound) nbtList.tagAt(i)).getShort("id");
                    if (Enchantment.enchantmentsList[id] != null) {
                        if (item_stack_in_first_slot.isItemEnchanted())
                            return !(((ITFEnchantment) Enchantment.enchantmentsList[id]).isTreasure());
                        return false;
                    }
                }
        }
        return true;
    }
}
