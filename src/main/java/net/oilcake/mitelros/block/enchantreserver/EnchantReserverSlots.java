package net.oilcake.mitelros.block.enchantreserver;

import net.minecraft.*;

public class EnchantReserverSlots extends InventoryBasic {
    public static final int slotSize = 2;

    public TileEntityEnchantReserver tileEntityEnchantReserver;

    private ContainerEnchantReserver container;

    private final Slot input;

    private final Slot output;

    public int getSize() {
        return 2;
    }

    public int getInputIndex() {
        return 0;
    }

    public int getOutputIndex() {
        return 1;
    }

    public void updateInfo() {
        if (this.container != null)
            this.container.updateInfo();
    }

    public EnchantReserverSlots(IInventory iInventory) {
        super("EnchantReserver", true, 2);
        if (iInventory instanceof TileEntityEnchantReserver) {
            this.tileEntityEnchantReserver = (TileEntityEnchantReserver) iInventory;
        } else {
            this.tileEntityEnchantReserver = null;
        }
        this.input = new Slot(iInventory, getInputIndex(), 63, 19) {
            @Override
            public boolean isItemValid(ItemStack par1ItemStack) {
                return (par1ItemStack.getItem() instanceof net.minecraft.ItemRock) ||
                        (par1ItemStack.getItem().itemID == Item.dyePowder.itemID && par1ItemStack.getItemSubtype() == 4);
            }
        };
        this.output = new Slot(iInventory, getOutputIndex(), 63, 51) {
            @Override
            public boolean isItemValid(ItemStack par1ItemStack) {
                return (par1ItemStack.getItem() instanceof net.minecraft.ItemPotion || par1ItemStack.getItem() instanceof net.minecraft.ItemNugget);
            }
        };
    }

    public ItemStack getInPutStack() {
        return this.input.getStack();
    }

    public ItemStack getOutPutStack() {
        return this.output.getStack();
    }

    public Slot getInPut() {
        return this.input;
    }

    public Slot getOutPut() {
        return this.output;
    }

    public void dropItems(World world, int x, int y, int z) {
        for (int var2 = 0; var2 < this.tileEntityEnchantReserver.getSizeInventory(); var2++) {
            ItemStack var3 = this.tileEntityEnchantReserver.getItem(var2);
            if (var3 != null) {
                float var10 = world.rand.nextFloat() * 0.8F + 0.1F;
                float var11 = world.rand.nextFloat() * 0.8F + 0.1F;
                EntityItem var14;
                for (float var12 = world.rand.nextFloat() * 0.8F + 0.1F; var3.stackSize > 0; world.spawnEntityInWorld((Entity) var14)) {
                    int var13 = world.rand.nextInt(21) + 10;
                    if (var13 > var3.stackSize)
                        var13 = var3.stackSize;
                    var3.stackSize -= var13;
                    var14 = new EntityItem(world, (x + var10), (y + var11), (z + var12), new ItemStack(var3.itemID, var13, var3.getItemSubtype()));
                    if (var3.isItemDamaged())
                        var14.getEntityItem().setItemDamage(var3.getItemDamage());
                    float var15 = 0.05F;
                    var14.motionX = ((float) world.rand.nextGaussian() * var15);
                    var14.motionY = ((float) world.rand.nextGaussian() * var15 + 0.2F);
                    var14.motionZ = ((float) world.rand.nextGaussian() * var15);
                    if (var3.getItem().hasQuality())
                        var14.getEntityItem().setQuality(var3.getQuality());
                    if (var3.hasTagCompound())
                        var14.getEntityItem().setTagCompound((NBTTagCompound) var3.getTagCompound().copy());
                }
            }
        }
    }

    public void writeToNBT(NBTTagCompound nbt) {
        tryWriteSlotStack(nbt, this.input, "Input");
        tryWriteSlotStack(nbt, this.output, "Output");
    }

    public void readFromNBT(NBTTagCompound nbt, TileEntityEnchantReserver tileEntityEnchantReserver) {
        tileEntityEnchantReserver.setItem(getInputIndex(), ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("Input")));
        tileEntityEnchantReserver.setItem(getOutputIndex(), ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("Output")));
    }

    private void tryWriteSlotStack(NBTTagCompound nbt, Slot slot, String name) {
        if (slot.getStack() != null)
            nbt.setCompoundTag(name, slot.getStack().writeToNBT(new NBTTagCompound()));
    }

    void onContainerClosed() {
        if (!this.container.world.isRemote && this.tileEntityEnchantReserver != null)
            this.tileEntityEnchantReserver.closeChest();
    }

    public void initSlots(ContainerEnchantReserver enchantReserver) {
        enchantReserver.addSlot(this.input);
        enchantReserver.addSlot(this.output);
        if (this.tileEntityEnchantReserver != null)
            this.tileEntityEnchantReserver.openChest();
        this.container = enchantReserver;
    }
}
