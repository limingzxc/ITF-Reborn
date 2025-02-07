package net.oilcake.mitelros.mixins.entity.mob;

import net.minecraft.*;
import net.oilcake.mitelros.api.ITFSkeleton;
import net.oilcake.mitelros.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Calendar;

@Mixin(EntitySkeleton.class)
public abstract class EntitySkeletonMixin extends EntityMob implements IRangedAttackMob, ITFSkeleton {

    @Unique
    private int num_arrows;

    @Shadow
    private EntityAIArrowAttack aiArrowAttack;

    @Shadow
    private EntityAIAttackOnCollide aiAttackOnCollide;

    @Shadow
    public int forced_skeleton_type;

    protected boolean isWizard;

    public boolean getWizard() {
        return this.isWizard;
    }

    public void setWizard(boolean isWizard) {
        this.isWizard = isWizard;
    }

    public EntitySkeletonMixin(World par1World) {
        super(par1World);
        this.isWizard = false;
    }

    @Inject(method = "<init>(Lnet/minecraft/World;)V", at = @At("RETURN"))
    public void injectCtor(CallbackInfo callbackInfo) {
        this.num_arrows = this.rand.nextInt(3) + (isLongdead() ? 6 : 2) + (isLongdeadGuardian() ? 2 : 0);
        this.tasks.addTask(3, new EntityAIAvoidEntity(this, EntityWolf.class, 10.0F, 1.0D, 1.2D));
    }

    @Inject(method = "readEntityFromNBT", at = @At("TAIL"))
    private void readArrows(NBTTagCompound par1NBTTagCompound, CallbackInfo ci) {
        this.num_arrows = par1NBTTagCompound.getByte("num_arrows");
    }

    @Inject(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/EntityMob;onLivingUpdate()V"))
    public void itfUpdate(CallbackInfo ci) {
        if (this.num_arrows == 0 && getHeldItemStack() != null && getHeldItemStack().getItem() instanceof net.minecraft.ItemBow)
            setHeldItemStack(null);
        if (getHeldItemStack() == null && getSkeletonType() == 0) {
            setSkeletonType(2);
            setCombatTask();
        }
    }

    @Inject(method = "writeEntityToNBT", at = @At("TAIL"))
    public void writeArrows(NBTTagCompound par1NBTTagCompound, CallbackInfo ci) {
        par1NBTTagCompound.setByte("num_arrows", (byte) this.num_arrows);
    }

    @Redirect(method = "setCombatTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/EntitySkeleton;getHeldItemStack()Lnet/minecraft/ItemStack;"))
    private ItemStack redirect(EntitySkeleton instance) {
        if (this.num_arrows == 0) {
            return null;
        }
        return instance.getHeldItemStack();
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public EntityLivingData onSpawnWithEgg(EntityLivingData par1EntityLivingData) {
        par1EntityLivingData = super.onSpawnWithEgg(par1EntityLivingData);
        int skeleton_type = (this.forced_skeleton_type >= 0) ? this.forced_skeleton_type : getRandomSkeletonType(this.worldObj);
        if (skeleton_type == 1) {// TODO only changed == 1 stuff
            if (isBoneLord()) {
                setCurrentItemOrArmor(1, (new ItemStack(Items.tungstenBoots)).randomizeForMob(this, false));
                setCurrentItemOrArmor(2, (new ItemStack(Items.tungstenLeggings)).randomizeForMob(this, false));
                setCurrentItemOrArmor(3, (new ItemStack(Items.tungstenChestplate)).randomizeForMob(this, false));
                setCurrentItemOrArmor(4, (new ItemStack(Items.tungstenHelmet)).randomizeForMob(this, false));
                getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(8.0D);
            } else {
                setCurrentItemOrArmor(1, (new ItemStack(Items.tungstenBootsChain)).randomizeForMob(this, false));
                setCurrentItemOrArmor(2, (new ItemStack(Items.tungstenLeggingsChain)).randomizeForMob(this, false));
                setCurrentItemOrArmor(3, (new ItemStack(Items.tungstenChestplateChain)).randomizeForMob(this, false));
                setCurrentItemOrArmor(4, (new ItemStack(Items.tungstenHelmetChain)).randomizeForMob(this, false));
                getEntityAttribute(SharedMonsterAttributes.attackDamage).setAttribute(6.0D);
            }
            if (this.rand.nextInt(24) == 0) {
                this.isWizard = true;
                setCurrentItemOrArmor(0, (new ItemStack(Items.lavaWand)).randomizeForMob(this, false));
                this.tasks.addTask(3, new EntityAIAvoidEntity(this, EntityPlayer.class, 9.0F, 1.0D, 1.0D));
                this.tasks.addTask(4, this.aiArrowAttack);
            } else {
                setCurrentItemOrArmor(0, (new ItemStack(Items.tungstenSword)).randomizeForMob(this, false));
                this.tasks.addTask(4, this.aiAttackOnCollide);
            }
            setSkeletonType(1);
        } else {
            if (skeleton_type == 2) {
                setSkeletonType(2);
                this.tasks.addTask(4, this.aiAttackOnCollide);
            } else if (skeleton_type == 0) {
                this.tasks.addTask(4, this.aiArrowAttack);
            } else {
                Minecraft.setErrorMessage("onSpawnWithEgg: Unrecognized skeleton type " + skeleton_type);
            }
            addRandomEquipment();
        }
        setCanPickUpLoot(true);
        if (getCurrentItemOrArmor(4) == null) {
            Calendar var2 = this.worldObj.getCurrentDate();
            if (var2.get(2) + 1 == 10 && var2.get(5) == 31 && this.rand.nextFloat() < 0.25F) {
                setCurrentItemOrArmor(4, new ItemStack((this.rand.nextFloat() < 0.1F) ? Block.pumpkinLantern : Block.pumpkin));
                this.equipmentDropChances[4] = 0.0F;
            }
        }
        return par1EntityLivingData;
    }

    @Shadow
    public boolean isLongdead() {
        return false;
    }

    @Shadow
    public void setSkeletonType(int par1) {
    }

    @Shadow
    public int getRandomSkeletonType(World world) {
        return -1;
    }

    @Shadow
    public int getSkeletonType() {
        return this.dataWatcher.getWatchableObjectByte(13);
    }

    @Shadow
    public abstract boolean isLongdeadGuardian();

    @Shadow
    public abstract void setHeldItemStack(ItemStack paramItemStack);

    @Shadow
    public abstract boolean isBoneLord();

    @Shadow
    public abstract void setCombatTask();

    @Shadow
    protected abstract void addRandomEquipment();

    @Inject(method = "dropFewItems", at = @At("HEAD"))
    private void wizardDrop(boolean recently_hit_by_player, DamageSource damage_source, CallbackInfo ci) {
        if (this.isWizard) {
            int j = 1 + this.rand.nextInt(2);
            if (!recently_hit_by_player)
                j = 0;
            int k;
            for (k = 0; k < j; k++)
                dropItem(Item.blazePowder.itemID, 1);
            for (k = 0; k < j; k++)
                dropItem(Item.netherStalkSeeds.itemID, 1);
        }
    }

    @Inject(method = "dropFewItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/EntitySkeleton;getSkeletonType()I", ordinal = 1))
    private void itfDropArrow(boolean recently_hit_by_player, DamageSource damage_source, CallbackInfo ci) {
        if (this.getSkeletonType() != 2) {
            int looting = damage_source.getLootingModifier();
            int j = Math.min(this.num_arrows, this.rand.nextInt(2 + looting));
            if (j > 0 && !recently_hit_by_player) {
                j -= this.rand.nextInt(j + 1);
            }
            if (isLongdead() && j > 0) {
                j = (this.rand.nextInt(3) == 0) ? 1 : 0;
            }
            for (int k = 0; k < j; k++) {
                dropItem(isLongdead() ? Item.arrowAncientMetal.itemID : Item.arrowRustedIron.itemID, 1);
            }
        }
    }

    @Redirect(method = "dropFewItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/EntitySkeleton;getSkeletonType()I", ordinal = 1))
    private int doNotDropArrow(EntitySkeleton instance) {
        return 2;
    }
}
