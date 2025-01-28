package npc.core.exu

import chisel3._
import chisel3.util._
import npc.common.Config._
import npc.common.Instructions._
import npc.core.idu._

class EXUIO extends Bundle {
    val br_flg = Output(Bool())
    val jmp_flg = Output(Bool())
    val br_target = Output(UInt(WORD_LEN.W))
    val alu_out = Output(UInt(WORD_LEN.W))
}

class EXUIO_pipe_out extends Bundle{
    val exe2ls_reg_pc = Output(UInt(WORD_LEN.W))
    val exe2ls_op1_data = Output(UInt(WORD_LEN.W))
    val exe2ls_rs2_data = Output(UInt(WORD_LEN.W))
    val exe2ls_wb_addr = Output(UInt(ADDR_LEN.W))
    val exe2ls_alu_out = Output(UInt(WORD_LEN.W))
    val exe2ls_rf_wen = Output(UInt(REN_LEN.W))
    val exe2ls_wb_sel = Output(UInt(WB_SEL_LEN.W))
    val exe2ls_csr_addr = Output(UInt(CSR_ADDR_LEN.W))
    val exe2ls_csr_cmd = Output(UInt(CSR_LEN.W))
    val exe2ls_imm_z_uext = Output(UInt(WORD_LEN.W))
    val exe2ls_mem_wen = Output(UInt(MEN_LEN.W))
    val exe2ls_mem_op = Output(UInt(MEM_OP.W))
}

class EXUIO_pipe extends Bundle {
    val in = Flipped(Decoupled(new IDUIO_pipe_out))
    val out = Decoupled(new EXUIO_pipe_out)
}

class EXU extends Module {
    val io = IO(new EXUIO)
    val io_pipe = IO(new EXUIO_pipe)















    //main process
    val alu_out = MuxCase(0.U(WORD_LEN.W), Seq(
        (io_pipe.in.bits.id2exe_exe_fun === ALU_ADD)   -> (io_pipe.in.bits.id2exe_op1_data + io_pipe.in.bits.id2exe_op2_data),
        (io_pipe.in.bits.id2exe_exe_fun === ALU_SUB)   -> (io_pipe.in.bits.id2exe_op1_data - io_pipe.in.bits.id2exe_op2_data),
        (io_pipe.in.bits.id2exe_exe_fun === ALU_AND)   -> (io_pipe.in.bits.id2exe_op1_data & io_pipe.in.bits.id2exe_op2_data),
        (io_pipe.in.bits.id2exe_exe_fun === ALU_OR)    -> (io_pipe.in.bits.id2exe_op1_data | io_pipe.in.bits.id2exe_op2_data),
        (io_pipe.in.bits.id2exe_exe_fun === ALU_XOR)   -> (io_pipe.in.bits.id2exe_op1_data ^ io_pipe.in.bits.id2exe_op2_data),
        (io_pipe.in.bits.id2exe_exe_fun === ALU_SLL)   -> (io_pipe.in.bits.id2exe_op1_data << io_pipe.in.bits.id2exe_op2_data(4, 0))(31, 0),
        (io_pipe.in.bits.id2exe_exe_fun === ALU_SRL)   -> (io_pipe.in.bits.id2exe_op1_data >> io_pipe.in.bits.id2exe_op2_data(4, 0)).asUInt,
        (io_pipe.in.bits.id2exe_exe_fun === ALU_SRA)   -> (io_pipe.in.bits.id2exe_op1_data.asSInt >> io_pipe.in.bits.id2exe_op2_data(4, 0)).asUInt,
        (io_pipe.in.bits.id2exe_exe_fun === ALU_SLT)   -> (io_pipe.in.bits.id2exe_op1_data.asSInt < io_pipe.in.bits.id2exe_op2_data.asSInt).asUInt,
        (io_pipe.in.bits.id2exe_exe_fun === ALU_SLTU)  -> (io_pipe.in.bits.id2exe_op1_data < io_pipe.in.bits.id2exe_op2_data).asUInt,
        (io_pipe.in.bits.id2exe_exe_fun === ALU_JALR)  -> ((io_pipe.in.bits.id2exe_op1_data + io_pipe.in.bits.id2exe_op2_data) & ~1.U(WORD_LEN.W)),
        (io_pipe.in.bits.id2exe_exe_fun === ALU_COPY1) -> io_pipe.in.bits.id2exe_op1_data
    ))

    val br_flg = MuxCase(false.B, Seq(
        (io_pipe.in.bits.id2exe_exe_fun === BR_BEQ)    ->  (io_pipe.in.bits.id2exe_op1_data === io_pipe.in.bits.id2exe_op2_data),
        (io_pipe.in.bits.id2exe_exe_fun === BR_BNE)    -> !(io_pipe.in.bits.id2exe_op1_data === io_pipe.in.bits.id2exe_op2_data),
        (io_pipe.in.bits.id2exe_exe_fun === BR_BLT)    ->  (io_pipe.in.bits.id2exe_op1_data.asSInt < io_pipe.in.bits.id2exe_op2_data.asSInt),
        (io_pipe.in.bits.id2exe_exe_fun === BR_BGE)    -> !(io_pipe.in.bits.id2exe_op1_data.asSInt < io_pipe.in.bits.id2exe_op2_data.asSInt),
        (io_pipe.in.bits.id2exe_exe_fun === BR_BLTU)   ->  (io_pipe.in.bits.id2exe_op1_data < io_pipe.in.bits.id2exe_op2_data),
        (io_pipe.in.bits.id2exe_exe_fun === BR_BGEU)   -> !(io_pipe.in.bits.id2exe_op1_data < io_pipe.in.bits.id2exe_op2_data)
    ))

    val br_target = io_pipe.in.bits.id2exe_reg_pc + io_pipe.in.bits.id2exe_imm_b_sext
    val jmp_flg = (io_pipe.in.bits.id2exe_wb_sel === WB_PC)

    //connect
    io.br_flg := br_flg
    io.jmp_flg := jmp_flg
    io.br_target := br_target
    io.alu_out := alu_out

    io_pipe.out.bits.exe2ls_reg_pc := io_pipe.in.bits.id2exe_reg_pc
    io_pipe.out.bits.exe2ls_op1_data := io_pipe.in.bits.id2exe_op1_data
    io_pipe.out.bits.exe2ls_rs2_data := io_pipe.in.bits.id2exe_rs2_data
    io_pipe.out.bits.exe2ls_wb_addr := io_pipe.in.bits.id2exe_wb_addr
    io_pipe.out.bits.exe2ls_alu_out := alu_out
    io_pipe.out.bits.exe2ls_rf_wen := io_pipe.in.bits.id2exe_rf_wen
    io_pipe.out.bits.exe2ls_wb_sel := io_pipe.in.bits.id2exe_wb_sel
    io_pipe.out.bits.exe2ls_csr_addr := io_pipe.in.bits.id2exe_csr_addr
    io_pipe.out.bits.exe2ls_csr_cmd := io_pipe.in.bits.id2exe_csr_cmd
    io_pipe.out.bits.exe2ls_imm_z_uext := io_pipe.in.bits.id2exe_imm_z_uext
    io_pipe.out.bits.exe2ls_mem_wen := io_pipe.in.bits.id2exe_mem_wen
    io_pipe.out.bits.exe2ls_mem_op := io_pipe.in.bits.id2exe_mem_op






















    //handshake between modules
    val in_ready = RegInit(false.B)
    val out_valid = RegInit(false.B)
    io_pipe.in.ready := in_ready
    io_pipe.out.valid := out_valid

    val s_BeforePreFire :: s_AfterPreFire :: Nil = Enum(2)
    val c_state = RegInit(s_BeforePreFire)
    val n_state = WireDefault(c_state)
    dontTouch(n_state)

    c_state := n_state//first phase

    n_state := MuxLookup(c_state, s_BeforePreFire)(Seq(//second phase
        s_BeforePreFire  ->  Mux(io_pipe.in.fire, s_AfterPreFire, s_BeforePreFire),
        s_AfterPreFire   ->  Mux(io_pipe.out.fire, s_BeforePreFire, s_AfterPreFire)
    ))

    switch(n_state){//third phase
        is(s_BeforePreFire){
            in_ready := true.B
            out_valid := false.B
        }
        is(s_AfterPreFire){
            in_ready := false.B
            out_valid := true.B
        }
    }
}

