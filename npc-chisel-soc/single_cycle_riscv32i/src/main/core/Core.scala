package npc.core

import chisel3._
import chisel3.util._
import npc.common.Config._
import npc.common.Instructions._

import npc.core._
import npc.core.ifu._
import npc.core.idu._
import npc.core.exu._
import npc.core.lsu._
import npc.core.wbu._
import npc.perip._
import npc.bus.axi._
import npc.core.icache._

class CoreIO extends Bundle {
    val imem = Flipped(new AXI4WithoutClk)
    val dmem = Flipped(new AXI4WithoutClk)
}

class Core extends Module {
    val io = IO(new CoreIO)

    val gpr = Module(new GPR)
    val csr = Module(new CSR)

    val ifu = Module(new IFU)
    val idu = Module(new IDU)
    val exu = Module(new EXU)
    val lsu = Module(new LSU)
    val wbu = Module(new WBU)

    StageConnect(ifu.io_pipe.out, idu.io_pipe.in)
    StageConnect(idu.io_pipe.out, exu.io_pipe.in)
    StageConnect(exu.io_pipe.out, lsu.io_pipe.in)
    StageConnect(lsu.io_pipe.out, wbu.io_pipe.in)
    StageConnect(wbu.io_pipe.out, ifu.io_pipe.in)

    val icache = Module(new iCache(16, 16, 3, "LRU"))
    io.imem <> icache.io.out
    icache.io.in <> ifu.io.imem
    
    // io.imem <> ifu.io.imem

    ifu.io.br_flg := exu.io.br_flg
    ifu.io.jmp_flg := exu.io.jmp_flg
    ifu.io.br_target := exu.io.br_target
    ifu.io.alu_out := exu.io.alu_out
    ifu.io.csr_mtvec := csr.io.csr_mtvec
    ifu.io.csr_mepc := csr.io.csr_mepc
    csr.io.csr_reg_pc := ifu.io.csr_reg_pc//modified by ypc
    
    idu.io.gpr_rs1_data := gpr.io.gpr_rs1_data
    idu.io.gpr_rs2_data := gpr.io.gpr_rs2_data
    gpr.io.gpr_rs1_addr := idu.io.gpr_rs1_addr
    gpr.io.gpr_rs2_addr := idu.io.gpr_rs2_addr

    io.dmem <> lsu.io.dmem
    lsu.io.csr_rdata := csr.io.csr_rdata

    csr.io.csr_wdata := wbu.io.csr_wdata
    csr.io.csr_addr := wbu.io.csr_addr
    csr.io.csr_cmd := wbu.io.csr_cmd
    gpr.io.gpr_wen := wbu.io.gpr_wen
    gpr.io.gpr_addr := wbu.io.gpr_addr
    gpr.io.gpr_wdata := wbu.io.gpr_wdata

}

object StageConnect {
  def apply[T <: Data](left: DecoupledIO[T], right: DecoupledIO[T]) = {
    val arch = "multi"
    if      (arch == "single")   { right.bits := left.bits }
    else if (arch == "multi")    { right :<>= left }
    else if (arch == "pipeline") { right :<>= RegEnable(left, left.fire) }
    else if (arch == "ooo")      { right :<>= Queue(left, 16) }
  }
}