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
    wbu.io_pipe.out.ready := true.B
    val ready_r = RegNext(ifu.io_pipe.in.ready)
    ifu.io_pipe.in.valid := RegEnable(true.B, ifu.io_pipe.in.valid, ifu.io_pipe.in.ready & ready_r)

    pipelineConnect(ifu.io_pipe.out, idu.io_pipe.in)
    pipelineConnect(idu.io_pipe.out, exu.io_pipe.in)
    pipelineConnect(exu.io_pipe.out, lsu.io_pipe.in)
    pipelineConnect(lsu.io_pipe.out, wbu.io_pipe.in)











    val icache = Module(new iCache(8, 4, 1, "FIFO"))
    io.imem <> icache.io.out
    icache.io.in <> ifu.io.imem

    idu.fencei_io_vr.is_fencei_io <> icache.fencei_io_vr.is_fencei_io





    
    idu.io.gpr_rs1_data := gpr.io.gpr_rs1_data
    idu.io.gpr_rs2_data := gpr.io.gpr_rs2_data
    gpr.io.gpr_rs1_addr := idu.io.gpr_rs1_addr
    gpr.io.gpr_rs2_addr := idu.io.gpr_rs2_addr

    io.dmem <> lsu.io.dmem

    csr.io.csr_raddr := idu.io.csr_raddr
    idu.io.csr_rdata := csr.io.csr_rdata
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
    dontTouch(exu_is_working)
    dontTouch(lsu_is_working)
    dontTouch(wbu_is_working)
    dontTouch(wbu_is_working_r)
    dontTouch(wbu_end_flg)
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
    val exu_can_forward_rs1 = exu_raw_rs1 && (exu.io_pipe.in.bits.id2exe_wb_sel =/= WB_MEM)
    val exu_can_forward_rs2 = exu_raw_rs2 && (exu.io_pipe.in.bits.id2exe_wb_sel =/= WB_MEM)
    val lsu_can_forward_rs1 = lsu_raw_rs1 && (lsu.io_pipe.in.bits.exe2ls_wb_sel =/= WB_MEM || lsu.io.dmem.rvalid) //两种情况：第一种、普通的lsu raw用=/= WB_MEM可以转发。第二种、load-use raw用lsu.io.dmem.rvalid可以转发
    val lsu_can_forward_rs2 = lsu_raw_rs2 && (lsu.io_pipe.in.bits.exe2ls_wb_sel =/= WB_MEM || lsu.io.dmem.rvalid)
    val wbu_can_forward_rs1 = wbu_raw_rs1
    val wbu_can_forward_rs2 = wbu_raw_rs2
    val exu_forward_data = MuxLookup(exu.io_pipe.in.bits.id2exe_wb_sel, 0.U)(Seq(
        WB_PC      -> (exu.io_pipe.out.bits.exe2ls_reg_pc + 4.U),
        WB_CSR     -> exu.io_pipe.out.bits.exe2ls_csr_rdata,
        WB_ALU     -> exu.io_pipe.out.bits.exe2ls_alu_out
    ))
    val lsu_forward_data = lsu.io_pipe.out.bits.ls2wb_wb_data
    val wbu_forward_data = wbu.io.gpr_wdata
    dontTouch(exu_can_forward_rs1)
    dontTouch(exu_can_forward_rs2)
    dontTouch(lsu_can_forward_rs1)
    dontTouch(lsu_can_forward_rs2)
    dontTouch(wbu_can_forward_rs1)
    dontTouch(wbu_can_forward_rs2)
    dontTouch(exu_forward_data)
    dontTouch(lsu_forward_data)
    dontTouch(wbu_forward_data)

    val rd1_forward_en = Wire(Bool())
    when(exu_raw_rs1){
        rd1_forward_en := exu_can_forward_rs1
    }.elsewhen(lsu_raw_rs1){
        rd1_forward_en := lsu_can_forward_rs1
    }.elsewhen(wbu_raw_rs1){
        rd1_forward_en := wbu_can_forward_rs1
    }.otherwise{
        rd1_forward_en := false.B
    }
    val rd2_forward_en = Wire(Bool())
    when(exu_raw_rs2){
        rd2_forward_en := exu_can_forward_rs2
    }.elsewhen(lsu_raw_rs2){
        rd2_forward_en := lsu_can_forward_rs2
    }.elsewhen(wbu_raw_rs2){
        rd2_forward_en := wbu_can_forward_rs2
    }.otherwise{
        rd2_forward_en := false.B
    }
    val rd1_forward_data = Mux(rd1_forward_en, MuxCase(0.U, Seq(
        exu_can_forward_rs1 -> exu_forward_data,
        lsu_can_forward_rs1 -> lsu_forward_data,
        wbu_can_forward_rs1 -> wbu_forward_data
    )), idu.io_pipe.out.bits.id2exe_rs1_data)
    val rd2_forward_data = Mux(rd2_forward_en, MuxCase(0.U, Seq(
        exu_can_forward_rs2 -> exu_forward_data,
        lsu_can_forward_rs2 -> lsu_forward_data,
        wbu_can_forward_rs2 -> wbu_forward_data
    )), idu.io_pipe.out.bits.id2exe_rs2_data)
    dontTouch(rd1_forward_en)
    dontTouch(rd2_forward_en)
    dontTouch(rd1_forward_data)
    dontTouch(rd2_forward_data)
    // exu.io_pipe.in.bits.id2exe_rs1_data := RegEnable(rd1_forward_data, idu.io_pipe.out.fire)
    // exu.io_pipe.in.bits.id2exe_rs2_data := RegEnable(rd2_forward_data, idu.io_pipe.out.fire)



















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

    val rs1_resolved = rs1_raw_valid && wbu_end_flg && wbu.io_pipe.in.bits.ls2wb_wb_addr === rs1_raw_rd && wbu.io_pipe.in.bits.ls2wb_rf_wen === REN_S
    val rs2_resolved = rs2_raw_valid && wbu_end_flg && wbu.io_pipe.in.bits.ls2wb_wb_addr === rs2_raw_rd && wbu.io_pipe.in.bits.ls2wb_rf_wen === REN_S

    val rs1_resolved_r = RegNext(rs1_resolved)
    val rs2_resolved_r = RegNext(rs2_resolved)

    when(rs1_resolved_r) {rs1_raw_valid := false.B}
    when(rs2_resolved_r) {rs2_raw_valid := false.B}

    idu.io_hazard.stall_flg := rs1_raw_valid | rs2_raw_valid | is_raw


















    //Struc hazard
    /*fix in xbar*/





















    //control hazard
    //先只实现ecall的异常处理, 只会产生在idu阶段
    val is_irq = RegNext(wbu.io.irq_valid && wbu.io.is_irq)
    dontTouch(is_irq)
    csr.io.csr_is_irq := is_irq
    csr.io.csr_reg_pc := wbu.io_pipe.in.bits.ls2wb_reg_pc//pipe line irq
    csr.io.csr_irq_num := wbu.io.irq_num

    val is_mret_rise = idu.io.is_mret & ~RegNext(idu.io.is_mret)
    ifu.io_hazard.is_mret_rise := is_mret_rise
    val is_mret_r = RegInit(false.B)
    is_mret_r := Mux(is_mret_rise, true.B, Mux(ifu.io_pipe.in.ready & ifu.io_pipe.in.valid, false.B, is_mret_r))

    val is_ctrl_hazard = ((exu.io.br_flg && exu.io.br_target =/= ifu.io_pipe.out.bits.if2id_reg_pc) || (exu.io.jmp_flg && exu.io.alu_out =/= ifu.io_pipe.out.bits.if2id_reg_pc)) && exu.io_pipe.out.valid
    dontTouch(is_ctrl_hazard)

    val is_ctrl_hazard_r = RegInit(false.B)
    val is_irq_r = RegInit(false.B)
    is_ctrl_hazard_r := Mux(is_ctrl_hazard, true.B, Mux(ifu.io_pipe.in.ready & ifu.io_pipe.in.valid, false.B, is_ctrl_hazard_r))
    is_irq_r := Mux(is_irq, true.B, Mux(ifu.io_pipe.in.ready & ifu.io_pipe.in.valid, false.B, is_irq_r))

    ifu.io_hazard.flush_flg := is_ctrl_hazard | is_irq
    idu.io_hazard.flush_flg := is_ctrl_hazard | is_irq
    exu.io_hazard.flush_flg := is_irq
    lsu.io_hazard.flush_flg := is_irq
    wbu.io_hazard.flush_flg := is_irq

    //ifu next pc process
    val sel_br = exu.io.br_flg
    val sel_jmp = exu.io.jmp_flg
    val sel_mret = idu.io.is_mret
    val pc_next_normal = Mux1H(Seq(
        sel_br   -> exu.io.br_target,
        sel_jmp  -> exu.io.alu_out,
        sel_mret -> csr.io.csr_mepc
    ))
    val pc_real_next = Mux(is_irq_r, csr.io.csr_mtvec, Mux(is_ctrl_hazard_r | is_mret_r, pc_next_normal, ifu.io_hazard.pc_plus4))
    ifu.io_hazard.pc_real_next := pc_real_next


    when(ifu.io_hazard.flush_flg){ifu.io_pipe.in.valid := false.B}
    when(idu.io_hazard.flush_flg){idu.io_pipe.in.valid := false.B}
    when(exu.io_hazard.flush_flg){exu.io_pipe.in.valid := false.B}
    when(lsu.io_hazard.flush_flg){lsu.io_pipe.in.valid := false.B}
    when(wbu.io_hazard.flush_flg){wbu.io_pipe.in.valid := false.B}










































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