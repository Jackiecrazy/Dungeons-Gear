package com.infamous.dungeons_gear.enchantments.melee_ranged;

import com.infamous.dungeons_gear.damagesources.OffhandAttackDamageSource;
import com.infamous.dungeons_gear.enchantments.ModEnchantmentTypes;
import com.infamous.dungeons_gear.enchantments.lists.MeleeRangedEnchantmentList;
import com.infamous.dungeons_gear.enchantments.types.DungeonsEnchantment;
import com.infamous.dungeons_gear.items.interfaces.IMeleeWeapon;
import com.infamous.dungeons_gear.utilties.AreaOfEffectHelper;
import com.infamous.dungeons_gear.utilties.ModEnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.infamous.dungeons_gear.DungeonsGear.MODID;

import net.minecraft.enchantment.Enchantment.Rarity;

@Mod.EventBusSubscriber(modid = MODID)
public class GravityEnchantment extends DungeonsEnchantment {

    public static final String INTRINSIC_GRAVITY_TAG = "IntrinsicGravity";

    public GravityEnchantment() {
        super(Rarity.RARE, ModEnchantmentTypes.MELEE_RANGED, new EquipmentSlotType[]{
            EquipmentSlotType.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public void doPostAttack(LivingEntity user, Entity target, int level) {
        if(!(target instanceof LivingEntity)) return;
        ItemStack mainhand = user.getMainHandItem();
        boolean uniqueWeaponFlag = hasGravityBuiltIn(mainhand);
        if( user.getLastHurtMobTimestamp()==user.tickCount)return;
        if(uniqueWeaponFlag) level++;
        AreaOfEffectHelper.pullInNearbyEntities(user, (LivingEntity)target, level * 3, ParticleTypes.PORTAL);
    }

    private static boolean hasGravityBuiltIn(ItemStack mainhand) {
        return mainhand.getItem() instanceof IMeleeWeapon && ((IMeleeWeapon) mainhand.getItem()).hasGravityBuiltIn(mainhand);
    }

    @SubscribeEvent
    public static void onHammerOfGravityAttack(LivingAttackEvent event){
        if(event.getSource().getDirectEntity() instanceof AbstractArrowEntity) return;
        if(event.getSource() instanceof OffhandAttackDamageSource) return;
        if(!(event.getSource().getEntity() instanceof LivingEntity)) return;
        LivingEntity attacker = (LivingEntity)event.getSource().getEntity();
        LivingEntity victim = event.getEntityLiving();
        ItemStack mainhand = attacker.getMainHandItem();
        if((hasGravityBuiltIn(mainhand)
                && !ModEnchantmentHelper.hasEnchantment(mainhand, MeleeRangedEnchantmentList.GRAVITY))){
            AreaOfEffectHelper.pullInNearbyEntities(attacker, victim, 3, ParticleTypes.PORTAL);
        }
    }

    @SubscribeEvent
    public static void onGravityCrossbowImpact(ProjectileImpactEvent.Arrow event){
        RayTraceResult rayTraceResult = event.getRayTraceResult();
        //if(!EnchantUtils.arrowHitLivingEntity(rayTraceResult)) return;
        AbstractArrowEntity arrow = event.getArrow();
        if(!ModEnchantmentHelper.shooterIsLiving(arrow)) return;
        LivingEntity shooter = (LivingEntity)arrow.getOwner();
        int gravityLevel = ModEnchantmentHelper.enchantmentTagToLevel(arrow, MeleeRangedEnchantmentList.GRAVITY);
        boolean uniqueWeaponFlag = arrow.getTags().contains(INTRINSIC_GRAVITY_TAG);
        if(uniqueWeaponFlag
                && !(gravityLevel > 0)){
            if(rayTraceResult instanceof EntityRayTraceResult){
                EntityRayTraceResult entityRayTraceResult = (EntityRayTraceResult) rayTraceResult;
                if(entityRayTraceResult.getEntity() instanceof LivingEntity){
                    LivingEntity victim = (LivingEntity) ((EntityRayTraceResult)rayTraceResult).getEntity();
                    AreaOfEffectHelper.pullInNearbyEntities(shooter, victim, 3, ParticleTypes.PORTAL);
                }
            }
            if(rayTraceResult instanceof BlockRayTraceResult){
                BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult)rayTraceResult;
                BlockPos blockPos = blockRayTraceResult.getBlockPos();
                AreaOfEffectHelper.pullInNearbyEntitiesAtPos(shooter, blockPos, 3, ParticleTypes.PORTAL);
            }
        }else if(gravityLevel > 0){
            if(uniqueWeaponFlag) gravityLevel++;
            if(rayTraceResult instanceof BlockRayTraceResult){
                BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult)rayTraceResult;
                BlockPos blockPos = blockRayTraceResult.getBlockPos();
                AreaOfEffectHelper.pullInNearbyEntitiesAtPos(shooter, blockPos, 3 * gravityLevel, ParticleTypes.PORTAL);
            }
        }
    }
}
