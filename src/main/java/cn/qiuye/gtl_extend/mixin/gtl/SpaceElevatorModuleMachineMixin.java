package cn.qiuye.gtl_extend.mixin.gtl;

import org.gtlcore.gtlcore.common.data.GTLRecipeModifiers;
import org.gtlcore.gtlcore.common.machine.multiblock.electric.SpaceElevatorModuleMachine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

@Mixin(SpaceElevatorModuleMachine.class)
public class SpaceElevatorModuleMachineMixin extends WorkableElectricMultiblockMachine {

    public SpaceElevatorModuleMachineMixin(IMachineBlockEntity holder, boolean SEPMTier, Object... args) {
        super(holder, args);
        this.SEPMTier = SEPMTier;
    }

    @Unique
    private int gtl_extend$tier = 0;

    @Shadow(remap = false)
    private int SpaceElevatorTier = 0, ModuleTier = 0;

    @Mutable
    @Final
    @Shadow(remap = false)
    private final boolean SEPMTier;

    @Shadow(remap = false)
    private void getSpaceElevatorTier() {}

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    @Nullable
    public static GTRecipe recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe, @NotNull OCParams params,
                                          @NotNull OCResult result) {
        if (machine instanceof SpaceElevatorModuleMachineMixin moduleMachine) {
            moduleMachine.getSpaceElevatorTier();
            if (moduleMachine.SpaceElevatorTier < 1) {
                return null;
            }
            if (moduleMachine.SEPMTier && recipe.data.getInt("SEPMTier") > moduleMachine.ModuleTier) {
                return null;
            }
            moduleMachine.gtl_extend$tier = moduleMachine.getTier();
            GTRecipe recipe1 = GTLRecipeModifiers.reduction(machine, recipe, 1, Math.pow(0.8, moduleMachine.SpaceElevatorTier - 1));
            if (recipe1 != null) {
                recipe1 = GTRecipeModifiers.accurateParallel(machine, recipe1, moduleMachine.gtl_extend$Parallel(), false).getFirst();
                if (recipe1 != null) return RecipeHelper.applyOverclock(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK, recipe1, moduleMachine.getOverclockVoltage(), params, result);
            }
        }
        return null;
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
        if (!this.isFormed) return;
        if (getOffsetTimer() % 10 == 0) {
            getSpaceElevatorTier();
        }
        textList.add(Component.translatable("gtceu.multiblock.parallel", Component.literal(FormattingUtil.formatNumbers(gtl_extend$Parallel())).withStyle(ChatFormatting.DARK_PURPLE)).withStyle(ChatFormatting.GRAY));
        textList.add(Component.literal((SpaceElevatorTier < 1 ? "未" : "已") + "连接正在运行的太空电梯"));
        textList.add(Component.translatable("gtceu.machine.duration_multiplier.tooltip", FormattingUtil.formatPercent(Math.pow(0.8, SpaceElevatorTier - 1))));
    }

    @Unique
    private int gtl_extend$Parallel() {
        double var = Math.pow(4, ModuleTier - 1);
        double var1 = var * gtl_extend$tier;
        int parallel = (int) var1;
        return parallel;
    }
}
