package cn.qiuye.gtl_extend.mixin.gtl;

import org.gtlcore.gtlcore.api.machine.multiblock.ParallelMachine;
import org.gtlcore.gtlcore.api.machine.trait.ILockRecipe;
import org.gtlcore.gtlcore.api.recipe.IParallelLogic;
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper;
import org.gtlcore.gtlcore.common.machine.trait.MultipleRecipesLogic;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;

import com.google.common.primitives.Ints;
import org.spongepowered.asm.mixin.*;

@Mixin(MultipleRecipesLogic.class)
public class MultipleRecipesLogicMixin extends RecipeLogic implements ILockRecipe {

    @Mutable
    @Final
    @Shadow(remap = false)
    private final ParallelMachine parallel;
    @Mutable
    @Final
    @Shadow(remap = false)
    private final BiPredicate<CompoundTag, IRecipeLogicMachine> dataCheck;
    @Unique
    private static final long MAX_THREADS = 1024;

    public MultipleRecipesLogicMixin(ParallelMachine machine, BiPredicate<CompoundTag, IRecipeLogicMachine> dataCheck) {
        super((IRecipeLogicMachine) machine);
        this.parallel = machine;
        this.dataCheck = dataCheck;
    }

    @Shadow(remap = false)
    public WorkableElectricMultiblockMachine getMachine() {
        return (WorkableElectricMultiblockMachine) super.getMachine();
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    private GTRecipe getRecipe() {
        if (!this.machine.hasProxies()) {
            return null;
        } else {
            long maxEUt = this.getMachine().getOverclockVoltage();
            if (maxEUt <= 0L) {
                return null;
            } else {
                Iterator<GTRecipe> iterator = this.lookupRecipeIterator();
                GTRecipe output = GTRecipeBuilder.ofRaw().buildRawRecipe();
                output.outputs.put(ItemRecipeCapability.CAP, new ArrayList<>());
                output.outputs.put(FluidRecipeCapability.CAP, new ArrayList<>());
                long totalEu = 0L;
                long remain = (long) this.parallel.getMaxParallel() * MAX_THREADS;

                while (remain > 0L && iterator.hasNext()) {
                    GTRecipe match = iterator.next();
                    if (match != null) {
                        long p = IParallelLogic.getMaxParallel(this.machine, match, remain);
                        if (p > 0L) {
                            if (p > 1L) {
                                match = match.copy(ContentModifier.multiplier((double) p), false);
                            }

                            match.parallels = Ints.saturatedCast(p);
                            IParallelLogic.getRecipeOutputChance(this.machine, match);
                            remain -= p;
                            if (RecipeRunnerHelper.handleRecipeInput(this.machine, match)) {
                                totalEu += RecipeHelper.getInputEUt(match) * (long) match.duration;
                                List<Content> item = match.outputs.get(ItemRecipeCapability.CAP);
                                if (item != null) {
                                    output.outputs.get(ItemRecipeCapability.CAP).addAll(item);
                                }

                                List<Content> fluid = match.outputs.get(FluidRecipeCapability.CAP);
                                if (fluid != null) {
                                    output.outputs.get(FluidRecipeCapability.CAP).addAll(fluid);
                                }
                            }

                            if (totalEu / maxEUt > 10000L) {
                                break;
                            }
                        }
                    }
                }

                if (output.outputs.get(ItemRecipeCapability.CAP).isEmpty() && output.outputs.get(FluidRecipeCapability.CAP).isEmpty()) {
                    return null;
                } else {
                    double d = (double) totalEu / (double) maxEUt;
                    long eut = d > (double) 20.0F ? maxEUt : (long) ((double) maxEUt * d / (double) 20.0F);
                    output.tickInputs.put(EURecipeCapability.CAP, List.of(new Content(eut, 10000, 10000, 0, null, null)));
                    output.duration = Math.min((int) Math.max(d, 1.0F), 20);
                    return output;
                }
            }
        }
    }

    @Shadow(remap = false)
    private Iterator<GTRecipe> lookupRecipeIterator() {
        if (this.isLock()) {
            if (this.getLockRecipe() == null) {
                this.setLockRecipe(this.machine.getRecipeType().getLookup().find(this.machine, this::checkRecipe));
            } else if (!this.checkRecipe(this.getLockRecipe())) {
                return Collections.emptyIterator();
            }

            return Collections.singleton(this.getLockRecipe()).iterator();
        } else {
            return this.machine.getRecipeType().getLookup().getRecipeIterator(this.machine, this::checkRecipe);
        }
    }

    @Shadow(remap = false)
    private boolean checkRecipe(GTRecipe recipe) {
        return RecipeRunnerHelper.matchRecipe(this.machine, recipe) && recipe.data.getInt("euTier") <= this.getMachine().getTier() && recipe.checkConditions(this).isSuccess() && (this.dataCheck == null || this.dataCheck.test(recipe.data, this.machine));
    }
}
