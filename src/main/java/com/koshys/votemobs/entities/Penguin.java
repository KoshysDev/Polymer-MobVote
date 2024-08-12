package com.koshys.votemobs.entities;

import com.koshys.votemobs.VoteMobs;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.koshys.votemobs.entities.navigation.LessSpinnyGroundPathNavigation;
import com.koshys.votemobs.util.AnimationHelper;
import com.koshys.votemobs.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.effect.*;

import java.util.Objects;
import java.util.Random;

public class Penguin extends Animal implements AnimatedEntity, PlayerRideable {
    public static final ResourceLocation ID = Util.id("penguin");
    public static final Model MODEL = Util.loadModel(ID);
    private final EntityHolder<Penguin> holder;
    private int slipTimer = 0; // Timer for slip animation
    private final Random random = new Random();

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.MAX_HEALTH, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    public EntityHolder<Penguin> getHolder() {
        return this.holder;
    }

    public Penguin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.moveControl = new MoveControl(this);
        this.jumpControl = new JumpControl(this);

        this.holder = new LivingEntityHolder<>(this, MODEL);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new PanicGoal(this, 0.6));
        this.goalSelector.addGoal(4, new FollowBoatGoal(this));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(9, new TryFindWaterGoal(this));
        this.goalSelector.addGoal(1, new RandomSwimmingGoal(this, 1.0, 10));
        this.goalSelector.addGoal(8, new NearestAttackableTargetGoal<>(this, Salmon.class, false));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount % 2 == 0) {
            AnimationHelper.updateWalkAnimation(this, this.holder);
            AnimationHelper.updateHurtVariant(this, this.holder);
        }

        // Handle slip timer
        if (slipTimer > 0) {
            slipTimer--;
        }

        // Check for slip on snow
        if (this.onGround() && (this.level().getBlockState(this.blockPosition().below()).is(Blocks.SNOW) || this.level().getBlockState(this.blockPosition().below()).is(Blocks.SNOW_BLOCK) || this.level().getBlockState(this.blockPosition().below()).is(Blocks.ICE))) {
            if (slipTimer <= 0 && random.nextInt(40) == 0) { // 0.5% chance to slip
                this.holder.getAnimator().playAnimation("slip", 1); // Play slip animation
                slipTimer = 600; // Reset slip timer
                this.level().addParticle(ParticleTypes.ITEM_SNOWBALL, this.getX(), this.getY() + 0.5, this.getZ(), 0, 0, 0);
            }
        }

        // Update animations based on water
        if (this.isInWater()) {
            // Adjust movement speed in water
            Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.8); // Dolphin-like speed

            if (this.walkAnimation.isMoving() && this.walkAnimation.speed() > 0.02) {
                this.holder.getAnimator().playAnimation("walk_swing", 0);
                this.holder.getAnimator().pauseAnimation("idle_swing");
            } else {
                this.holder.getAnimator().pauseAnimation("walk_swing");
                this.holder.getAnimator().playAnimation("idle_swing", 0, true);
            }
        } else {
            // Reset movement speed on land
            Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(0.25);

            // Stop swing animations outside water
            this.holder.getAnimator().pauseAnimation("walk_swing");
            this.holder.getAnimator().pauseAnimation("idle_swing");
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    @NotNull
    protected PathNavigation createNavigation(Level level) {
        return new LessSpinnyGroundPathNavigation(this, level);
    }
}