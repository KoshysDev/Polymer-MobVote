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
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
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

import java.util.Random;

public class Crab extends Animal implements AnimatedEntity, PlayerRideable {
    public static final ResourceLocation ID = Util.id("crab");
    public static final Model MODEL = Util.loadModel(ID);
    private final EntityHolder<Crab> holder;
    private int feedingTimer = 0; // A timer to control how often the crab can eat
    private final Random random = new Random();

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.MAX_HEALTH, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    @Override
    public EntityHolder<Crab> getHolder() {
        return this.holder;
    }

    public Crab(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.moveControl = new MoveControl(this);
        this.jumpControl = new JumpControl(this);

        this.holder = new LivingEntityHolder<>(this, MODEL);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));

        // Tempt goal for feeding
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, Ingredient.of(Items.SUGAR, Items.SUGAR_CANE, Items.BAMBOO), true) {
            @Override
            public boolean canUse() {
                return super.canUse() && feedingTimer <= 0;
            }
        });
        this.goalSelector.addGoal(4, new PanicGoal(this, 0.6));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0F));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        // Check if the stack is an item you want the crab to eat
        Item item = stack.getItem();
        return item == Items.SUGAR || item == Items.SUGAR_CANE || item == Items.BAMBOO;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount % 2 == 0) {
            AnimationHelper.updateWalkAnimation(this, this.holder);
            AnimationHelper.updateHurtVariant(this, this.holder);
        }

        // Handle feeding timer
        if (feedingTimer > 0) {
            feedingTimer--;
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @Override
    @NotNull
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        if (!this.isBaby() && interactionHand == InteractionHand.MAIN_HAND && !player.getMainHandItem().isEmpty()) {
            ItemStack stack = player.getMainHandItem();
            if (isFood(stack) && feedingTimer <= 0) {
                // Player is feeding the crab
                feed(player);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    private void feed(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (isFood(stack)) {
            // Play feeding animation
            //this.level().addParticle(ParticleTypes.HEART, this.getX(), this.getY() + 0.5, this.getZ(), 0, 0, 0);
            stack.shrink(1);
            //player.playSound(VoteMobs.SOUNDS.CRAB_EAT, 1.0F, 1.0F);

            // Handle item drop
            if (random.nextBoolean()) { // 50/50 chance
                Item dropItem = Items.KELP; // Change this to your desired item
                this.spawnAtLocation(new ItemStack(dropItem));
            }

            // Reset timer
            feedingTimer = random.nextInt(20*20) + 500; // Random between 25 and 75 seconds
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);

        // Drop Big Claw Part (5% chance)
        if (random.nextInt(20) == 0) {
            this.spawnAtLocation(new ItemStack(Items.BLACK_DYE));
            //this.spawnAtLocation(new ItemStack(VoteMobs.BIG_CLAW_PART));
        }

        // Drop Small Claw Part (5% chance)
        if (random.nextInt(20) == 0) {
            //this.spawnAtLocation(new ItemStack(VoteMobs.SMALL_CLAW_PART));
            this.spawnAtLocation(new ItemStack(Items.RED_DYE));
        }
    }

    @Override
    @NotNull
    protected PathNavigation createNavigation(Level level) {
        return new LessSpinnyGroundPathNavigation(this, level);
    }
}