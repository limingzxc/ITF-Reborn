package net.oilcake.mitelros.entity.boss;

import net.minecraft.*;
import net.oilcake.mitelros.entity.mob.EntityLichShadow;
import net.oilcake.mitelros.util.AchievementExtend;
import net.oilcake.mitelros.item.Items;

import java.util.ArrayList;
import java.util.List;

public class EntityLich extends EntityBoneLord implements IBossDisplayData {
  private EntityAIAvoidEntity aiAvoidPlayerStrategic = new EntityAIAvoidEntity((EntityCreature)this, EntityPlayer.class, 6.0F, 1.1D, 1.4D);
  
  private EntityAIAvoidEntity aiAvoidPlayerPanic = new EntityAIAvoidEntity((EntityCreature)this, EntityPlayer.class, 32.0F, 1.3D, 1.5D);
  
  private EntityAIAttackOnCollide aiAttackOnCollide = new EntityAIAttackOnCollide((EntityCreature)this, EntityPlayer.class, 1.2D, false);
  
  private int max_num_evasions;
  
  private int num_evasions;
  
  private int spawnCounter;
  
  ItemStack stowed_item_stack;
  
  private boolean attack_mode = true;
  
  public void addRandomWeapon() {
    List<RandomItemListEntry> items = new ArrayList();
    items.add(new RandomItemListEntry(Item.swordGold, 2));
    this.stowed_item_stack = (new ItemStack((Item)Items.shockWand)).randomizeForMob((EntityLiving)this, true);
    RandomItemListEntry entry = (RandomItemListEntry)WeightedRandom.getRandomItem(this.rand, items);
    setHeldItemStack((new ItemStack(entry.item)).randomizeForMob((EntityLiving)this, true));
  }
  
  public EntityLich(World par1World) {
    super(par1World);
    if (par1World != null && onServer())
      this.max_num_evasions = this.num_evasions = 6; 
  }
  
  public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
    super.writeEntityToNBT(par1NBTTagCompound);
    par1NBTTagCompound.setByte("max_num_evasions", (byte)this.max_num_evasions);
    par1NBTTagCompound.setByte("num_evasions", (byte)this.num_evasions);
  }
  
  public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
    super.readEntityFromNBT(par1NBTTagCompound);
    this.max_num_evasions = par1NBTTagCompound.getByte("max_num_evasions");
    this.num_evasions = par1NBTTagCompound.getByte("num_evasions");
  }
  
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    setEntityAttribute(SharedMonsterAttributes.followRange, 128.0D);
    setEntityAttribute(SharedMonsterAttributes.movementSpeed, 0.27000001072883606D);
    setEntityAttribute(SharedMonsterAttributes.attackDamage, 9.0D);
    setEntityAttribute(SharedMonsterAttributes.maxHealth, 75.0D);
  }
  
  protected void addRandomEquipment() {
    addRandomWeapon();
    setBoots((new ItemStack((Item)Items.BootsAncientmetalsacred)).randomizeForMob((EntityLiving)this, true));
    setLeggings((new ItemStack((Item)Items.LeggingsAncientmetalsacred)).randomizeForMob((EntityLiving)this, true));
    setCuirass((new ItemStack((Item)Items.ChestplateAncientmetalsacred)).randomizeForMob((EntityLiving)this, true));
    setHelmet((new ItemStack((Item)Items.HelmetAncientmetalsacred)).randomizeForMob((EntityLiving)this, true));
  }
  
  public void onUpdate() {
    super.onUpdate();
    if (!(getWorld()).isRemote) {
      this.spawnCounter++;
      if (this.spawnCounter > 300 && !this.attack_mode) {
        if (getTarget() != null) {
          this.worldObj.playSoundEffect((getTarget()).posX + 0.5D, (getTarget()).posY + 0.5D, (getTarget()).posZ + 0.5D, "ambient.weather.thunder", 50.0F + this.rand.nextFloat(), 0.8F + this.rand.nextFloat() * 0.2F);
          this.worldObj.playSoundEffect((getTarget()).posX, (getTarget()).posY, (getTarget()).posZ, "random.explode", 2.0F, 0.5F + this.rand.nextFloat() * 0.2F);
          this.worldObj.spawnParticle(EnumParticle.witchMagic, this.posX + (this.rand.nextDouble() - 0.5D) * this.width, this.posY + this.rand.nextDouble() * this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * this.width, 0.0D, 0.0D, 0.0D);
          getTarget().attackEntityFrom(new Damage(DamageSource.divine_lightning, 5.0F));
        } 
        EntityLichShadow guardian = new EntityLichShadow(this.worldObj);
        guardian.setPosition(this.posX + this.rand.nextInt(8) - this.rand.nextInt(8), this.posY, this.posZ - this.rand.nextInt(8) + this.rand.nextInt(8));
        guardian.refreshDespawnCounter(-9600);
        this.worldObj.spawnEntityInWorld((Entity)guardian);
        guardian.onSpawnWithEgg(null);
        guardian.entityFX(EnumEntityFX.summoned);
        this.spawnCounter = 0;
      } 
    } 
  }
  
  public void onLivingUpdate() {
    super.onLivingUpdate();
    if (onServer() && getHealth() > 0.0F) {
      int ticks_existed_with_offset = getTicksExistedWithOffset();
      if (this.num_evasions < this.max_num_evasions && ticks_existed_with_offset % 600 == 0)
        this.num_evasions++; 
      if (hasPath() && (getTarget() != null || this.fleeing) && ticks_existed_with_offset % 10 == 0 && this.rand.nextInt(3) == 0) {
        PathEntity path = getPathToEntity();
        if (!path.isFinished()) {
          int n = path.getNumRemainingPathPoints();
          if (n > 1) {
            int path_index_advancement = MathHelper.clamp_int(this.rand.nextInt(n), 1, 4);
            PathPoint path_point = path.getPathPointFromCurrentIndex(path_index_advancement);
            if (path_point.distanceSqTo((Entity)this) > 3.0D && tryTeleportTo(path_point.xCoord + 0.5D, path_point.yCoord, path_point.zCoord + 0.5D))
              path.setCurrentPathIndex(path.getCurrentPathIndex() + path_index_advancement - 1); 
          } 
        } 
      } 
    } 
    if (this.stowed_item_stack != null && (getHeldItemStack() == null || getTicksExistedWithOffset() % 10 == 0))
      if (getHeldItemStack() == null) {
        swapHeldItemStackWithStowed();
      } else {
        EntityLivingBase entityLivingBase = getTarget();
        if (entityLivingBase != null && canSeeTarget(true)) {
          double distance = getDistanceToEntity((Entity)entityLivingBase);
          if ((getHeldItemStack()).itemID == Items.shockWand.itemID) {
            if (distance < 3.0D && 
              getHealth() >= 20.0F) {
              swapHeldItemStackWithStowed();
              this.attack_mode = true;
              this.tasks.removeTask((EntityAIBase)this.aiAvoidPlayerStrategic);
              this.tasks.addTask(3, (EntityAIBase)this.aiAttackOnCollide);
            } 
          } else if (distance > 3.0D) {
            this.tasks.removeTask((EntityAIBase)this.aiAttackOnCollide);
            if (getHealth() < 20.0F) {
              this.tasks.addTask(3, (EntityAIBase)this.aiAvoidPlayerPanic);
            } else {
              this.tasks.addTask(3, (EntityAIBase)this.aiAvoidPlayerStrategic);
            } 
            swapHeldItemStackWithStowed();
            this.attack_mode = false;
          } 
        } 
      }  
  }
  
  public boolean tryTeleportTo(double pos_x, double pos_y, double pos_z) {
    if (!this.isDead && getHealth() > 0.0F) {
      int x = MathHelper.floor_double(pos_x);
      int y = MathHelper.floor_double(pos_y);
      int z = MathHelper.floor_double(pos_z);
      if (y >= 1 && this.worldObj.blockExists(x, y, z))
        while (true) {
          y--;
          if (this.worldObj.isBlockSolid(x, y, z)) {
            y++;
            if (!this.worldObj.isBlockSolid(x, y, z) && !this.worldObj.isLiquidBlock(x, y, z)) {
              double delta_pos_x = pos_x - this.posX;
              double delta_pos_y = pos_y - this.posY;
              double delta_pos_z = pos_z - this.posZ;
              AxisAlignedBB bb = this.boundingBox.translateCopy(delta_pos_x, delta_pos_y, delta_pos_z);
              if (this.worldObj.getCollidingBoundingBoxes((Entity)this, bb).isEmpty() && !this.worldObj.isAnyLiquid(bb)) {
                World var10000 = this.worldObj;
                double distance = World.getDistanceFromDeltas(delta_pos_x, delta_pos_y, delta_pos_z);
                this.worldObj.blockFX(EnumBlockFX.particle_trail, x, y, z, (new SignalData()).setByte(EnumParticle.runegate.ordinal()).setShort((int)(16.0D * distance)).setApproxPosition(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)));
                this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "mob.endermen.portal", 1.0F, 1.0F);
                setPosition(pos_x, pos_y, pos_z);
                this.send_position_update_immediately = true;
                return true;
              } 
              return false;
            } 
            return false;
          } 
          if (y < 1)
            return false; 
          pos_y--;
        }  
      return false;
    } 
    return false;
  }
  
  public boolean tryTeleportAwayFrom(Entity entity, double min_distance) {
    if (!this.isDead && getHealth() > 0.0F) {
      double min_distance_sq = min_distance * min_distance;
      int x = getBlockPosX();
      int y = getFootBlockPosY();
      int z = getBlockPosZ();
      double threat_pos_x = (entity == null) ? this.posX : entity.posX;
      double threat_pos_z = (entity == null) ? this.posZ : entity.posZ;
      for (int attempts = 0; attempts < 64; attempts++) {
        int dx = this.rand.nextInt(11) - 5;
        int dy = this.rand.nextInt(9) - 4;
        int dz = this.rand.nextInt(11) - 5;
        if (Math.abs(dx) >= 3 || Math.abs(dz) >= 3) {
          int try_x = x + dx;
          int try_y = y + dy;
          int try_z = z + dz;
          double try_pos_x = try_x + 0.5D;
          double try_pos_z = try_z + 0.5D;
          World var10000 = this.worldObj;
          if (World.getDistanceSqFromDeltas(try_pos_x - threat_pos_x, try_pos_z - threat_pos_z) >= min_distance_sq && try_y >= 1 && this.worldObj.blockExists(try_x, try_y, try_z)) {
            do {
              try_y--;
            } while (!this.worldObj.isBlockSolid(try_x, try_y, try_z) && try_y >= 1);
            if (try_y >= 1) {
              try_y++;
              if (!this.worldObj.isBlockSolid(try_x, try_y, try_z) && !this.worldObj.isLiquidBlock(try_x, try_y, try_z) && tryTeleportTo(try_pos_x, try_y, try_pos_z)) {
                EntityPlayer target = findPlayerToAttack(Math.min(getMaxTargettingRange(), 24.0F));
                if (target != null && target != getTarget())
                  setTarget((EntityLivingBase)target); 
                return true;
              } 
            } 
          } 
        } 
      } 
      return false;
    } 
    return false;
  }
  
  public EntityDamageResult attackEntityAsMob(Entity target) {
    EntityDamageResult result = super.attackEntityAsMob(target);
    if (result != null && !result.entityWasDestroyed()) {
      tryTeleportAwayFrom((Entity)getTarget(), 8.0D);
      return result;
    } 
    return result;
  }
  
  public EntityDamageResult attackEntityFrom(Damage damage) {
    boolean can_evade = (!damage.isFallDamage() && !damage.isFireDamage() && !damage.isPoison());
    if (can_evade && (this.num_evasions > 0 || (getHealth() < 20.0F && this.rand.nextInt(8) != 0))) {
      if (this.num_evasions > 0)
        this.num_evasions--; 
      if (tryTeleportAwayFrom((Entity)getTarget(), 6.0D)) {
        if (getHealth() >= 20.0F) {
          EntityLichShadow shadow = new EntityLichShadow(this.worldObj);
          shadow.setPosition(this.posX, this.posY, this.posZ);
          shadow.refreshDespawnCounter(-9600);
          this.worldObj.spawnEntityInWorld((Entity)shadow);
          shadow.onSpawnWithEgg(null);
          shadow.entityFX(EnumEntityFX.summoned);
        } 
        return null;
      } 
    } 
    return super.attackEntityFrom(damage);
  }
  
  public void swapHeldItemStackWithStowed() {
    ItemStack item_stack = this.stowed_item_stack;
    this.stowed_item_stack = getHeldItemStack();
    setHeldItemStack(item_stack);
  }
  
  public int getExperienceValue() {
    return super.getExperienceValue() * 20;
  }
  
  protected void dropFewItems(boolean recently_hit_by_player, DamageSource damage_source) {
    dropItemStack(new ItemStack(Items.forgingnote.itemID, 1, 0), 0.0F);
    int looting = damage_source.getLootingModifier();
    int num_drops = this.rand.nextInt(3 + looting) - 1;
    if (num_drops > 0 && !recently_hit_by_player)
      num_drops -= this.rand.nextInt(num_drops + 1); 
    for (int i = 0; i < num_drops; i++)
      dropItem(Items.AncientmetalArmorPiece.itemID, 1); 
    if (recently_hit_by_player && !this.has_taken_massive_fall_damage && this.rand.nextInt(getBaseChanceOfRareDrop()) < 5 + looting * 2)
      dropItem(Items.goldenAppleLegend);
    if (recently_hit_by_player && !this.has_taken_massive_fall_damage && this.rand.nextInt(getBaseChanceOfRareDrop()) < 5 + looting * 2)
      dropItemStack(new ItemStack(Item.skull.itemID, 1, 0), 0.0F); 
  }
  
  public void onDeath(DamageSource par1DamageSource) {
    super.onDeath(par1DamageSource);
    List<Entity> targets = getNearbyEntities(48.0F, 48.0F);
    for (int i = 0; i < targets.size(); i++) {
      EntityPlayer entityPlayer = (targets.get(i) instanceof EntityPlayer) ? (EntityPlayer)targets.get(i) : null;
      if (entityPlayer != null)
        entityPlayer.triggerAchievement((StatBase)AchievementExtend.lichHunter); 
    } 
  }
  
  public Class getTroopClass() {
    return EntityLichShadow.class;
  }
}
