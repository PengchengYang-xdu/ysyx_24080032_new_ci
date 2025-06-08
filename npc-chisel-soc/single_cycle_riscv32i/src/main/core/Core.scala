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

    // StageConnect(ifu.io_pipe.out, idu.io_pipe.in)
    // StageConnect(idu.io_pipe.out, exu.io_pipe.in)
    // StageConnect(exu.io_pipe.out, lsu.io_pipe.in)
    // StageConnect(lsu.io_pipe.out, wbu.io_pipe.in)
    // StageConnect(wbu.io_pipe.out, ifu.io_pipe.in)

    pipelineConnect(ifu.io_pipe.out, idu.io_pipe.in)
    pipelineConnect(idu.io_pipe.out, exu.io_pipe.in)
    pipelineConnect(exu.io_pipe.out, lsu.io_pipe.in)
    pipelineConnect(lsu.io_pipe.out, wbu.io_pipe.in)

    // StageConnect(wbu.io_pipe.out, ifu.io_pipe.in)

    val icache = Module(new iCache(8, 4, 1, "LRU"))
    io.imem <> icache.io.out
    icache.io.in <> ifu.io.imem

    idu.fencei_io_vr.is_fencei_io <> icache.fencei_io_vr.is_fencei_io





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

    //data hazard
    val exu_is_working = ~exu.io_pipe.in.ready | exu.io_pipe.in.valid
    val lsu_is_working = ~lsu.io_pipe.in.ready | lsu.io_pipe.in.valid
    val wbu_is_working = ~wbu.io_pipe.in.ready | wbu.io_pipe.in.valid
    val wbu_is_working_r = RegNext(wbu_is_working)
    val wbu_end_flg = wbu_is_working_r & ~wbu_is_working
    val wbu_end_flg_r = RegNext(wbu_end_flg)
    dontTouch(exu_is_working)
    dontTouch(lsu_is_working)
    dontTouch(wbu_is_working)
    dontTouch(wbu_is_working_r)
    dontTouch(wbu_end_flg)
    dontTouch(wbu_end_flg_r)
    val exu_raw = dataConflictWithStage(idu, exu_is_working, exu.io_pipe.in.bits.id2exe_wb_addr, exu.io_pipe.in.bits.id2exe_rf_wen === REN_S)
    val lsu_raw = dataConflictWithStage(idu, lsu_is_working, lsu.io_pipe.in.bits.exe2ls_wb_addr, lsu.io_pipe.in.bits.exe2ls_rf_wen === REN_S)
    val wbu_raw = dataConflictWithStage(idu, wbu_is_working, wbu.io_pipe.in.bits.ls2wb_wb_addr, wbu.io_pipe.in.bits.ls2wb_rf_wen === REN_S)
    val is_raw = exu_raw || lsu_raw || wbu_raw
    val exu_raw_rs1 = exu_raw && dataConflict(idu.io.gpr_rs1_addr, exu.io_pipe.in.bits.id2exe_wb_addr)
    val exu_raw_rs2 = exu_raw && dataConflict(idu.io.gpr_rs2_addr, exu.io_pipe.in.bits.id2exe_wb_addr)
    val lsu_raw_rs1 = lsu_raw && dataConflict(idu.io.gpr_rs1_addr, lsu.io_pipe.in.bits.exe2ls_wb_addr)
    val lsu_raw_rs2 = lsu_raw && dataConflict(idu.io.gpr_rs2_addr, lsu.io_pipe.in.bits.exe2ls_wb_addr)
    val wbu_raw_rs1 = wbu_raw && dataConflict(idu.io.gpr_rs1_addr, wbu.io_pipe.in.bits.ls2wb_wb_addr)
    val wbu_raw_rs2 = wbu_raw && dataConflict(idu.io.gpr_rs2_addr, wbu.io_pipe.in.bits.ls2wb_wb_addr)
    val rs1_raw = exu_raw_rs1 || lsu_raw_rs1 || wbu_raw_rs1
    val rs2_raw = exu_raw_rs2 || lsu_raw_rs2 || wbu_raw_rs2
    dontTouch(exu_raw)
    dontTouch(lsu_raw)
    dontTouch(wbu_raw)
    dontTouch(is_raw)
    dontTouch(exu_raw_rs1)
    dontTouch(exu_raw_rs2)
    dontTouch(lsu_raw_rs1)
    dontTouch(lsu_raw_rs2)
    dontTouch(wbu_raw_rs1)
    dontTouch(wbu_raw_rs2)
    dontTouch(rs1_raw)
    dontTouch(rs2_raw)

    //记录发生raw的寄存器
    val rs1_raw_valid = RegInit(false.B)
    val rs2_raw_valid = RegInit(false.B)

    val rs1_raw_rd = Reg(UInt(ADDR_LEN.W))
    val rs2_raw_rd = Reg(UInt(ADDR_LEN.W))

    // 记录来自哪段
    val rs1_raw_from_exu = RegInit(false.B)
    val rs1_raw_from_lsu = RegInit(false.B)
    val rs1_raw_from_wbu = RegInit(false.B)

    val rs2_raw_from_exu = RegInit(false.B)
    val rs2_raw_from_lsu = RegInit(false.B)
    val rs2_raw_from_wbu = RegInit(false.B)

    when(rs1_raw){
        rs1_raw_rd := MuxCase(0.U, Seq(
            exu_raw_rs1 -> exu.io_pipe.in.bits.id2exe_wb_addr,
            lsu_raw_rs1 -> lsu.io_pipe.in.bits.exe2ls_wb_addr,
            wbu_raw_rs1 -> wbu.io_pipe.in.bits.ls2wb_wb_addr,
        ))
        rs1_raw_valid := true.B
        rs1_raw_from_exu := exu_raw_rs1
        rs1_raw_from_lsu := lsu_raw_rs1
        rs1_raw_from_wbu := wbu_raw_rs1
    }
    when(rs2_raw){
        rs2_raw_rd := MuxCase(0.U, Seq(
            exu_raw_rs2 -> exu.io_pipe.in.bits.id2exe_wb_addr,
            lsu_raw_rs2 -> lsu.io_pipe.in.bits.exe2ls_wb_addr,
            wbu_raw_rs2 -> wbu.io_pipe.in.bits.ls2wb_wb_addr,
        ))
        rs2_raw_valid := true.B
        rs2_raw_from_exu := exu_raw_rs2
        rs2_raw_from_lsu := lsu_raw_rs2
        rs2_raw_from_wbu := wbu_raw_rs2
    }

    val rs1_resolved = rs1_raw_valid && wbu_end_flg && wbu.io_pipe.in.bits.ls2wb_wb_addr === rs1_raw_rd
    val rs2_resolved = rs2_raw_valid && wbu_end_flg && wbu.io_pipe.in.bits.ls2wb_wb_addr === rs2_raw_rd

    val rs1_resolved_r = RegNext(rs1_resolved)
    val rs2_resolved_r = RegNext(rs2_resolved)

    when(rs1_resolved_r) {rs1_raw_valid := false.B}
    when(rs2_resolved_r) {rs2_raw_valid := false.B}

    idu.io_hazard.stall_flg := rs1_raw_valid | rs2_raw_valid | is_raw













    //Struc hazard
    /*fix in xbar*/





















    //control hazard
    val exu_out_valid_rise = exu.io_pipe.out.valid & ~RegNext(exu.io_pipe.out.valid)
    val is_ctrl_hazard = ((exu.io.br_flg && exu.io.br_target =/= ifu.io_pipe.out.bits.if2id_reg_pc) || (exu.io.jmp_flg && exu.io.alu_out =/= ifu.io_pipe.out.bits.if2id_reg_pc)) && exu_out_valid_rise
    dontTouch(is_ctrl_hazard)
    ifu.io_hazard.flush_flg := is_ctrl_hazard
    idu.io_hazard.flush_flg := is_ctrl_hazard
    exu.io_hazard.flush_flg := is_ctrl_hazard
    //auto fetch logic begin
    // wbu.io_pipe.out.ready := true.B

    // val auto_valid = RegInit(false.B)
    // auto_valid := Mux(ifu.io_hazard.flush_flg, true.B, RegEnable(true.B, false.B, ifu.io_pipe.in.ready))
    // ifu.io_pipe.in.valid := auto_valid
    // //auto fetch logic end
    // when(idu.io_hazard.flush_flg){idu.io_pipe.in.valid := false.B}
    // when(exu.io_hazard.flush_flg){exu.io_pipe.in.valid := false.B}

    wbu.io_pipe.out.ready := true.B
    val ready_r = RegNext(ifu.io_pipe.in.ready)
    ifu.io_pipe.in.valid := ifu.io_pipe.in.ready & ready_r

    when(idu.io_hazard.flush_flg){idu.io_pipe.in.valid := false.B}
    when(exu.io_hazard.flush_flg){exu.io_pipe.in.valid := false.B}





























    def pipelineConnect[T <: Data, T2 <: Data](prevOut: DecoupledIO[T], thisIn: DecoupledIO[T]) = {
        prevOut.ready := thisIn.ready
        thisIn.bits := RegEnable(prevOut.bits, prevOut.valid && thisIn.ready)
        thisIn.valid := RegEnable(prevOut.valid, thisIn.ready);
    }

    def dataConflict(rs: UInt, rd: UInt) = (rs === rd)
    def dataConflictWithStage(stage_left: IDU, stage_right_is_working: Bool, rd: UInt, is_w: Bool) = {
        val rs1 = stage_left.io.gpr_rs1_addr
        val rs2 = stage_left.io.gpr_rs2_addr
        val is_working = stage_right_is_working
        val rs1_is_zero = rs1 === 0.U
        val rs2_is_zero = rs2 === 0.U
        val rs1_is_read = stage_left.io.gpr_rs1_is_read
        val rs2_is_read = stage_left.io.gpr_rs2_is_read
        
        val stage_left_valid_r = RegNext(stage_left.io_pipe.in.valid)
        ((rs1_is_read && ~rs1_is_zero && dataConflict(rs1, rd)) || (rs2_is_read && ~rs2_is_zero && dataConflict(rs2, rd))) && is_working && is_w && stage_left_valid_r
    }

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