package cn.qiuye.gtl_extend.common.data;

import cn.qiuye.gtl_extend.api.registries.GTLEXRegistration;
import cn.qiuye.gtl_extend.api.registries.MachinesRegister;
import cn.qiuye.gtl_extend.common.data.machines.MultiBlockMachineA;
import cn.qiuye.gtl_extend.common.machine.generator.DragonEggEnergyMachine;
import cn.qiuye.gtl_extend.common.machine.generator.MagicEnergyMachine;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.renderer.machine.SimpleGeneratorMachineRenderer;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import net.minecraft.network.chat.Component;

import static cn.qiuye.gtl_extend.common.data.machines.MultiBlockMachineA.GTL_EX_ADD;
import static com.gregtechceu.gtceu.api.GTValues.*;

public class GTL_Extend_Machines {

    public static final MachineDefinition[] PRIMITIVE_MAGIC_ENERGY;
    public static final MachineDefinition[] DRAGON_EGG_ENERGY;

    static {
        GTLEXRegistration.REGISTRATE.creativeModeTab(() -> GTL_Extend_CreativeModeTabs.MACHINES_ITEM);
    }

    static {
        PRIMITIVE_MAGIC_ENERGY = MachinesRegister.registerTieredMachines(
                "primitive_magic_energy",
                MagicEnergyMachine::new,
                (tier, builder) -> builder
                        .langValue("%s Primitive Magic Energy %s".formatted(VLVH[tier], VLVT[tier]))
                        .rotationState(RotationState.NON_Y_AXIS)
                        .renderer(() -> new SimpleGeneratorMachineRenderer(tier,
                                GTCEu.id("block/generators/primitive_magic_energy")))
                        .tooltips(Component.translatable("gtceu.machine.primitive_magic_energy.tooltip.0"))
                        .tooltips(Component.translatable("gtceu.universal.tooltip.ampere_out", 64))
                        .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_out",
                                FormattingUtil.formatNumbers(V[tier]), VNF[tier]))
                        .tooltips(Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                FormattingUtil.formatNumbers(V[tier] * 2048L)))
                        .tooltipBuilder(GTL_EX_ADD)
                        .register(),
                MV, HV, EV, IV, LuV, ZPM);

        DRAGON_EGG_ENERGY = MachinesRegister.registerTieredMachines(
                "primitive_dragon_egg_energy",
                DragonEggEnergyMachine::new,
                (tier, builder) -> builder
                        .langValue("%s Primitive Dradon Egg Energy %s".formatted(VLVH[tier], VLVT[tier]))
                        .rotationState(RotationState.NON_Y_AXIS)
                        .renderer(() -> new SimpleGeneratorMachineRenderer(tier,
                                GTCEu.id("block/generators/primitive_magic_energy")))
                        .tooltips(Component.translatable("gtceu.machine.primitive_dradon_egg_energy.tooltip.0"))
                        .tooltips(Component.translatable("gtceu.universal.tooltip.ampere_out", 256))
                        .tooltips(Component.translatable("gtceu.universal.tooltip.voltage_out",
                                FormattingUtil.formatNumbers(VEX[tier]), VNF[tier]))
                        .tooltips(Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                FormattingUtil.formatNumbers(VEX[tier] * 16384)))
                        .tooltipBuilder(GTL_EX_ADD)
                        .register(),
                UV, UHV, UEV, UIV);
    }

    public static void init() {
        MultiBlockMachineA.init();
    }
}
