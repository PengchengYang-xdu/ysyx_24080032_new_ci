package npc.core

import chisel3._
import chisel3.util._
import npc.common.Config._
import npc.common.Instructions._

class GPRIO extends Bundle {
    val gpr_rs1_addr = Input(UInt(ADDR_LEN.W))
    val gpr_rs2_addr = Input(UInt(ADDR_LEN.W))
    val gpr_rs1_data = Output(UInt(WORD_LEN.W))
    val gpr_rs2_data = Output(UInt(WORD_LEN.W))
    
    val gpr_wen = Input(UInt(REN_LEN.W))
    val gpr_addr = Input(UInt(ADDR_LEN.W))
    val gpr_wdata = Input(UInt(WORD_LEN.W))
}

class GPR extends Module {
    val io = IO(new GPRIO)

    // val gpr = Mem(32, UInt(WORD_LEN.W))
    val gpr = Mem(16, UInt(WORD_LEN.W))

    io.gpr_rs1_data := Mux((io.gpr_rs1_addr =/= 0.U(ADDR_LEN.W)), gpr(io.gpr_rs1_addr), 0.U(WORD_LEN.W))
    io.gpr_rs2_data := Mux((io.gpr_rs2_addr =/= 0.U(ADDR_LEN.W)), gpr(io.gpr_rs2_addr), 0.U(WORD_LEN.W))

    when(io.gpr_wen === REN_S && io.gpr_addr =/= 0.U(ADDR_LEN.W)){
        gpr(io.gpr_addr) := io.gpr_wdata
    }
}
