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
    val raw_rs1_cnt = RegInit(false.B)
    val raw_rs2_cnt = RegInit(false.B)
    dontTouch(raw_rs1_cnt)
    dontTouch(raw_rs2_cnt)

    val exu_is_working = ~exu.io_pipe.in.ready | exu.io_pipe.in.valid
    val lsu_is_working = ~lsu.io_pipe.in.ready | lsu.io_pipe.in.valid
    val wbu_is_working = ~wbu.io_pipe.in.ready | wbu.io_pipe.in.valid
    dontTouch(exu_is_working)
    dontTouch(lsu_is_working)
    dontTouch(wbu_is_working)
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

    val rd1_forward_en = Wire(Bool())
    val rd2_forward_en = Wire(Bool())

    //some state
    val exu_rs1_rawing = RegInit(false.B)
    val exu_rs2_rawing = RegInit(false.B)
    val lsu_rs1_rawing = RegInit(false.B)
    val lsu_rs2_rawing = RegInit(false.B)
    val wbu_rs1_rawing = RegInit(false.B)
    val wbu_rs2_rawing = RegInit(false.B)
    exu_rs1_rawing := Mux(rd1_forward_en, false.B, Mux(exu_raw_rs1, true.B, exu_rs1_rawing))
    exu_rs2_rawing := Mux(rd2_forward_en, false.B, Mux(exu_raw_rs2, true.B, exu_rs2_rawing))
    lsu_rs1_rawing := Mux(rd1_forward_en, false.B, Mux(lsu_raw_rs1, true.B, lsu_rs1_rawing))
    lsu_rs2_rawing := Mux(rd2_forward_en, false.B, Mux(lsu_raw_rs2, true.B, lsu_rs2_rawing))
    wbu_rs1_rawing := Mux(rd1_forward_en, false.B, Mux(wbu_raw_rs1, true.B, wbu_rs1_rawing))
    wbu_rs2_rawing := Mux(rd2_forward_en, false.B, Mux(wbu_raw_rs2, true.B, wbu_rs2_rawing))
    val rs1_rawing = exu_rs1_rawing || lsu_rs1_rawing || wbu_rs1_rawing
    val rs2_rawing = exu_rs2_rawing || lsu_rs2_rawing || wbu_rs2_rawing
    val rawing = rs1_rawing || rs2_rawing
    dontTouch(exu_rs1_rawing)
    dontTouch(exu_rs2_rawing)
    dontTouch(lsu_rs1_rawing)
    dontTouch(lsu_rs2_rawing)
    dontTouch(wbu_rs1_rawing)
    dontTouch(wbu_rs2_rawing)
    dontTouch(rs1_rawing)
    dontTouch(rs2_rawing)
    dontTouch(rawing)

    val bt_fwd_fsh_rs1 = RegInit(false.B)
    bt_fwd_fsh_rs1 := Mux(idu.io_pipe.out.fire, false.B, Mux(rd1_forward_en, true.B, bt_fwd_fsh_rs1))
    val bt_fwd_fsh_rs2 = RegInit(false.B)
    bt_fwd_fsh_rs2 := Mux(idu.io_pipe.out.fire, false.B, Mux(rd2_forward_en, true.B, bt_fwd_fsh_rs2))
    val bt_fwd_fsh = bt_fwd_fsh_rs1 || bt_fwd_fsh_rs2
    dontTouch(bt_fwd_fsh_rs1)
    dontTouch(bt_fwd_fsh_rs2)
    dontTouch(bt_fwd_fsh)

    val exu_rs1_rawing_raw = (exu_rs1_rawing || exu_raw_rs1)
    val exu_rs2_rawing_raw = (exu_rs2_rawing || exu_raw_rs2)
    val lsu_rs1_rawing_raw = (lsu_rs1_rawing || lsu_raw_rs1)
    val lsu_rs2_rawing_raw = (lsu_rs2_rawing || lsu_raw_rs2)
    val wbu_rs1_rawing_raw = (wbu_rs1_rawing || wbu_raw_rs1)
    val wbu_rs2_rawing_raw = (wbu_rs2_rawing || wbu_raw_rs2)


    val exu_can_forward_rs1 = exu_rs1_rawing_raw && (exu.io_pipe.in.bits.id2exe_wb_sel =/= WB_MEM || (lsu.io_pipe.out.fire && lsu.io_pipe.out.bits.ls2wb_wb_addr === idu.io.gpr_rs1_addr))
    val exu_can_forward_rs2 = exu_rs2_rawing_raw && (exu.io_pipe.in.bits.id2exe_wb_sel =/= WB_MEM || (lsu.io_pipe.out.fire && lsu.io_pipe.out.bits.ls2wb_wb_addr === idu.io.gpr_rs2_addr))
    val lsu_can_forward_rs1 = lsu_rs1_rawing_raw && (lsu.io_pipe.in.bits.exe2ls_wb_sel =/= WB_MEM || (lsu.io_pipe.out.fire && lsu.io_pipe.out.bits.ls2wb_wb_addr === idu.io.gpr_rs1_addr))//两种情况：第一种、普通的lsu raw用=/= WB_MEM可以转发。第二种、load-use raw用lsu.io.dmem.rvalid可以转发
    val lsu_can_forward_rs2 = lsu_rs2_rawing_raw && (lsu.io_pipe.in.bits.exe2ls_wb_sel =/= WB_MEM || (lsu.io_pipe.out.fire && lsu.io_pipe.out.bits.ls2wb_wb_addr === idu.io.gpr_rs2_addr))
    val wbu_can_forward_rs1 = wbu_rs1_rawing_raw
    val wbu_can_forward_rs2 = wbu_rs2_rawing_raw
    val exu_forward_data = MuxLookup(exu.io_pipe.in.bits.id2exe_wb_sel, 0.U)(Seq(
        WB_PC      -> (exu.io_pipe.out.bits.exe2ls_reg_pc + 4.U),
        WB_CSR     -> exu.io_pipe.out.bits.exe2ls_csr_rdata,
        WB_ALU     -> exu.io_pipe.out.bits.exe2ls_alu_out,
        WB_MEM     -> lsu.io_pipe.out.bits.ls2wb_wb_data
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

    when(exu_rs1_rawing_raw){
        rd1_forward_en := exu_can_forward_rs1 && ~bt_fwd_fsh_rs1
    }.elsewhen(lsu_rs1_rawing_raw){
        rd1_forward_en := lsu_can_forward_rs1 && ~bt_fwd_fsh_rs1
    }.elsewhen(wbu_rs1_rawing_raw){
        rd1_forward_en := wbu_can_forward_rs1 && ~bt_fwd_fsh_rs1
    }.otherwise{
        rd1_forward_en := false.B
    }
    when(exu_rs2_rawing_raw){
        rd2_forward_en := exu_can_forward_rs2 && ~bt_fwd_fsh_rs2
    }.elsewhen(lsu_rs2_rawing_raw){
        rd2_forward_en := lsu_can_forward_rs2 && ~bt_fwd_fsh_rs2
    }.elsewhen(wbu_rs2_rawing_raw){
        rd2_forward_en := wbu_can_forward_rs2 && ~bt_fwd_fsh_rs2
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
    val rd1_forward_data_r = RegEnable(rd1_forward_data, rd1_forward_en)
    val rd2_forward_data_r = RegEnable(rd2_forward_data, rd2_forward_en)
    dontTouch(rd1_forward_en)
    dontTouch(rd2_forward_en)
    dontTouch(rd1_forward_data)
    dontTouch(rd2_forward_data)
    dontTouch(rd1_forward_data_r)
    dontTouch(rd2_forward_data_r)

    raw_rs1_cnt := Mux(rs1_raw && ~rd1_forward_en, true.B, Mux(rd1_forward_en, false.B, raw_rs1_cnt))
    raw_rs2_cnt := Mux(rs2_raw && ~rd2_forward_en, true.B, Mux(rd2_forward_en, false.B, raw_rs2_cnt))

    exu.io_pipe.in.bits.id2exe_rs1_data := RegEnable(Mux(rd1_forward_en || (~rs1_rawing && ~bt_fwd_fsh_rs1), rd1_forward_data, rd1_forward_data_r), idu.io_pipe.out.fire)
    exu.io_pipe.in.bits.id2exe_rs2_data := RegEnable(Mux(rd2_forward_en || (~rs2_rawing && ~bt_fwd_fsh_rs2), rd2_forward_data, rd2_forward_data_r), idu.io_pipe.out.fire)
    idu.io_hazard.stall_flg := (is_raw || rawing) && (Mux(rs1_raw && rs2_raw, ~(rd1_forward_en && rd2_forward_en), ~rd1_forward_en && ~rd2_forward_en) && ~bt_fwd_fsh) || (raw_rs1_cnt || raw_rs2_cnt)


















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