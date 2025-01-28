package npc.perip

import chisel3._
import chisel3.util._
import npc.common.Config._
import npc.common.Instructions._
import npc.bus.axi._


// class Imem extends BlackBox with HasBlackBoxPath{
//     val io = IO(new AXI_lite)
//     addPath("/home/ypc/Desktop/ysyx/ysyx-workbench/npc-chisel/single_cycle_riscv32i/src/main/mem/Imem.sv")
// }

// class Dmem extends BlackBox with HasBlackBoxPath{
//     val io = IO(new AXI_lite)
//     addPath("/home/ypc/Desktop/ysyx/ysyx-workbench/npc-chisel/single_cycle_riscv32i/src/main/mem/Dmem.sv")
// }

class Mem extends BlackBox with HasBlackBoxPath{
    val io = IO(new AXI_lite)
    addPath("/home/ypc/Desktop/ysyx/ysyx-workbench/npc-chisel/single_cycle_riscv32i/src/main/perip/Mem.sv")
}

class Uart extends BlackBox with HasBlackBoxPath{
    val io = IO(new AXI_lite)
    addPath("/home/ypc/Desktop/ysyx/ysyx-workbench/npc-chisel/single_cycle_riscv32i/src/main/perip/Uart.sv")
}

class Clint extends BlackBox with HasBlackBoxPath{
    val io = IO(new AXI_lite)
    addPath("/home/ypc/Desktop/ysyx/ysyx-workbench/npc-chisel/single_cycle_riscv32i/src/main/perip/Clint.sv")
}
