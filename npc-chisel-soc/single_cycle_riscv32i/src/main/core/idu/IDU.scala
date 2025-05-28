package npc.core.idu

import chisel3._
import chisel3.util._
import npc.common.Config._
import npc.common.Instructions._
import npc.core.ifu._

class IDUIO extends Bundle {
    val gpr_rs1_addr = Output(UInt(ADDR_LEN.W))
    val gpr_rs2_addr = Output(UInt(ADDR_LEN.W))
    val gpr_rs1_data = Input(UInt(WORD_LEN.W))
    val gpr_rs2_data = Input(UInt(WORD_LEN.W))
}

class IDUIO_pipe_out extends Bundle{
    val id2exe_reg_pc = Output(UInt(WORD_LEN.W))
    val id2exe_op1_data = Output(UInt(WORD_LEN.W))
    val id2exe_op2_data = Output(UInt(WORD_LEN.W))
    val id2exe_rs2_data = Output(UInt(WORD_LEN.W))
    val id2exe_wb_addr = Output(UInt(ADDR_LEN.W))
    val id2exe_rf_wen = Output(UInt(REN_LEN.W))
    val id2exe_exe_fun = Output(UInt(EXE_FUN_LEN.W))
    val id2exe_wb_sel = Output(UInt(WB_SEL_LEN.W))
    val id2exe_imm_i_sext = Output(UInt(WORD_LEN.W))
    val id2exe_imm_s_sext = Output(UInt(WORD_LEN.W))
    val id2exe_imm_b_sext = Output(UInt(WORD_LEN.W))
    val id2exe_imm_u_shifted = Output(UInt(WORD_LEN.W))
    val id2exe_imm_z_uext = Output(UInt(WORD_LEN.W))
    val id2exe_csr_addr = Output(UInt(CSR_ADDR_LEN.W))
    val id2exe_csr_cmd = Output(UInt(CSR_LEN.W))
    val id2exe_mem_wen = Output(UInt(MEN_LEN.W))
    val id2exe_mem_op = Output(UInt(MEM_OP.W))
}

class FENCEI_IO extends Bundle{
    val is_fencei = Output(Bool())
}

class FENCEI_IO_VR extends Bundle{
    val is_fencei_io = Irrevocable(new FENCEI_IO)
}

class IDUIO_pipe extends Bundle {
    val in = Flipped(Decoupled(new IFUIO_pipe_out))
    val out = Decoupled(new IDUIO_pipe_out)
}

class IDU extends Module {
    val io = IO(new IDUIO)
    val io_pipe = IO(new IDUIO_pipe)

    val fencei_io_vr = IO(new FENCEI_IO_VR)













    //main process
    val inst = io_pipe.in.bits.if2id_inst
    val reg_pc = io_pipe.in.bits.if2id_reg_pc
    
    val rs1_addr = inst(19, 15)
    val rs2_addr = inst(24, 20)
    val wb_addr = inst(11, 7)

    val rs1_data = io.gpr_rs1_data
    val rs2_data = io.gpr_rs2_data

    val imm_i = inst(31, 20)
    val imm_i_sext = Cat(Fill(20, imm_i(11)), imm_i)
    val imm_s = Cat(inst(31, 25), inst(11, 7))
    val imm_s_sext = Cat(Fill(20, imm_s(11)), imm_s)
    val imm_b = Cat(inst(31), inst(7), inst(30, 25), inst(11, 8))
    val imm_b_sext = Cat(Fill(19, imm_b(11)), imm_b, 0.U(1.W))
    val imm_j = Cat(inst(31), inst(19, 12), inst(20), inst(30, 21))
    val imm_j_sext = Cat(Fill(11, imm_j(19)), imm_j, 0.U(1.W))
    val imm_u = inst(31,12)
    val imm_u_shifted = Cat(imm_u, Fill(12, 0.U(1.W)))
    val imm_z = inst(19,15)
    val imm_z_uext = Cat(Fill(27, 0.U), imm_z)

    val csignals = ListLookup(inst,
                        List(ALU_X    , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , NO_FENCEI),
        Array(
            LW       -> List(ALU_ADD  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_MEM, CSR_X, MEM_OP_4 , NO_FENCEI),
            SW       -> List(ALU_ADD  , OP1_RS1, OP2_IMS, MEN_S, REN_X, WB_X  , CSR_X, MEM_OP_4 , NO_FENCEI),
            LB       -> List(ALU_ADD  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_MEM, CSR_X, MEM_OP_1S, NO_FENCEI),
            LH       -> List(ALU_ADD  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_MEM, CSR_X, MEM_OP_2S, NO_FENCEI),
            LBU      -> List(ALU_ADD  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_MEM, CSR_X, MEM_OP_1U, NO_FENCEI),
            LHU      -> List(ALU_ADD  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_MEM, CSR_X, MEM_OP_2U, NO_FENCEI),
            SB       -> List(ALU_ADD  , OP1_RS1, OP2_IMS, MEN_S, REN_X, WB_X  , CSR_X, MEM_OP_1S, NO_FENCEI),
            SH       -> List(ALU_ADD  , OP1_RS1, OP2_IMS, MEN_S, REN_X, WB_X  , CSR_X, MEM_OP_2S, NO_FENCEI),

            ADD      -> List(ALU_ADD  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            ADDI     -> List(ALU_ADD  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            SUB      -> List(ALU_SUB  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            AND      -> List(ALU_AND  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            OR       -> List(ALU_OR   , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            XOR      -> List(ALU_XOR  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            ANDI     -> List(ALU_AND  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            ORI      -> List(ALU_OR   , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            XORI     -> List(ALU_XOR  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            SLL      -> List(ALU_SLL  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            SRL      -> List(ALU_SRL  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            SRA      -> List(ALU_SRA  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            SLLI     -> List(ALU_SLL  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            SRLI     -> List(ALU_SRL  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            SRAI     -> List(ALU_SRA  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            SLT      -> List(ALU_SLT  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            SLTU     -> List(ALU_SLTU , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            SLTI     -> List(ALU_SLT  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            SLTIU    -> List(ALU_SLTU , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            BEQ      -> List(BR_BEQ   , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , NO_FENCEI),
            BNE      -> List(BR_BNE   , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , NO_FENCEI),
            BGE      -> List(BR_BGE   , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , NO_FENCEI),
            BGEU     -> List(BR_BGEU  , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , NO_FENCEI),
            BLT      -> List(BR_BLT   , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , NO_FENCEI),
            BLTU     -> List(BR_BLTU  , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , NO_FENCEI),
            JAL      -> List(ALU_ADD  , OP1_PC , OP2_IMJ, MEN_X, REN_S, WB_PC , CSR_X, MEM_OP_X , NO_FENCEI),
            JALR     -> List(ALU_JALR , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_PC , CSR_X, MEM_OP_X , NO_FENCEI),
            LUI      -> List(ALU_ADD  , OP1_X  , OP2_IMU, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            AUIPC    -> List(ALU_ADD  , OP1_PC , OP2_IMU, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI),
            CSRRW    -> List(ALU_COPY1, OP1_RS1, OP2_X  , MEN_X, REN_S, WB_CSR, CSR_W, MEM_OP_X , NO_FENCEI),
            CSRRWI   -> List(ALU_COPY1, OP1_IMZ, OP2_X  , MEN_X, REN_S, WB_CSR, CSR_W, MEM_OP_X , NO_FENCEI),
            CSRRS    -> List(ALU_COPY1, OP1_RS1, OP2_X  , MEN_X, REN_S, WB_CSR, CSR_S, MEM_OP_X , NO_FENCEI),
            CSRRSI   -> List(ALU_COPY1, OP1_IMZ, OP2_X  , MEN_X, REN_S, WB_CSR, CSR_S, MEM_OP_X , NO_FENCEI),
            CSRRC    -> List(ALU_COPY1, OP1_RS1, OP2_X  , MEN_X, REN_S, WB_CSR, CSR_C, MEM_OP_X , NO_FENCEI),
            CSRRCI   -> List(ALU_COPY1, OP1_IMZ, OP2_X  , MEN_X, REN_S, WB_CSR, CSR_C, MEM_OP_X , NO_FENCEI),
            ECALL    -> List(ALU_X    , OP1_X  , OP2_X  , MEN_X, REN_X, WB_X  , CSR_E, MEM_OP_X , NO_FENCEI),
            MRET     -> List(ALU_X    , OP1_X  , OP2_X  , MEN_X, REN_X, WB_X  , CSR_M, MEM_OP_X , NO_FENCEI),//modified by ypc
            EBREAK   -> List(ALU_X    , OP1_X  , OP2_X  , MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , NO_FENCEI),
            FENCEI   -> List(ALU_X    , OP1_X  , OP2_X  , MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , IS_FENCEI)
        )
    )

    val exe_fun :: op1_sel :: op2_sel :: mem_wen :: rf_wen :: wb_sel :: csr_cmd :: mem_op :: is_fencei :: Nil = csignals
    
    val op1_data = MuxCase(0.U(WORD_LEN.W), Seq(
        (op1_sel === OP1_RS1)  ->  rs1_data,
        (op1_sel === OP1_PC)   ->  reg_pc,
        (op1_sel === OP1_IMZ)  ->  imm_z_uext
    ))

    val op2_data = MuxCase(0.U(WORD_LEN.W), Seq(
        (op2_sel === OP2_RS2)  ->  rs2_data,
        (op2_sel === OP2_IMI)  ->  imm_i_sext,
        (op2_sel === OP2_IMS)  ->  imm_s_sext,
        (op2_sel === OP2_IMJ)  ->  imm_j_sext,
        (op2_sel === OP2_IMU)  ->  imm_u_shifted
    ))

    val csr_addr = Mux(csr_cmd === CSR_E, 0x342.U(CSR_ADDR_LEN.W), inst(31,20))



    //connect
    io.gpr_rs1_addr := rs1_addr
    io.gpr_rs2_addr := rs2_addr


    io_pipe.out.bits.id2exe_reg_pc := reg_pc
    io_pipe.out.bits.id2exe_op1_data := op1_data
    io_pipe.out.bits.id2exe_op2_data := op2_data
    io_pipe.out.bits.id2exe_rs2_data := rs2_data
    io_pipe.out.bits.id2exe_wb_addr := wb_addr
    io_pipe.out.bits.id2exe_rf_wen := rf_wen
    io_pipe.out.bits.id2exe_exe_fun := exe_fun
    io_pipe.out.bits.id2exe_wb_sel := wb_sel
    io_pipe.out.bits.id2exe_imm_i_sext := imm_i_sext
    io_pipe.out.bits.id2exe_imm_s_sext := imm_s_sext
    io_pipe.out.bits.id2exe_imm_b_sext := imm_b_sext
    io_pipe.out.bits.id2exe_imm_u_shifted := imm_u_shifted
    io_pipe.out.bits.id2exe_imm_z_uext := imm_z_uext
    io_pipe.out.bits.id2exe_csr_addr := csr_addr
    io_pipe.out.bits.id2exe_csr_cmd := csr_cmd
    io_pipe.out.bits.id2exe_mem_wen := mem_wen
    io_pipe.out.bits.id2exe_mem_op := mem_op

    class Ebreak extends BlackBox with HasBlackBoxPath{
        val io = IO(new Bundle{
            val inst = Input(UInt(WORD_LEN.W))
        })
        addPath("/home/yangpengcheng/ysyx/ysyx/ysyx-workbench/npc-chisel/single_cycle_riscv32i/src/main/core/idu/Ebreak.sv")
    }

    val ebreak = Some(Module(new Ebreak))
    ebreak.get.io.inst := inst



















    //handshake between modules
    val is_fencei_valid = RegInit(false.B)
    fencei_io_vr.is_fencei_io.valid := is_fencei_valid
    val is_fencei_reg = RegInit(0.U)
    fencei_io_vr.is_fencei_io.bits.is_fencei := is_fencei_reg

    val in_ready = RegInit(false.B)
    val out_valid = RegInit(false.B)
    io_pipe.in.ready := in_ready
    io_pipe.out.valid := Mux(is_fencei === 1.U, fencei_io_vr.is_fencei_io.ready, out_valid)

    val s_BeforePreFire :: s_AfterPreFire :: Nil = Enum(2)
    val c_state = RegInit(s_BeforePreFire)
    val n_state = WireDefault(c_state)
    dontTouch(n_state)

    c_state := n_state//first phase

    n_state := MuxLookup(c_state, s_BeforePreFire)(Seq(//second phase
        s_BeforePreFire  ->  Mux(io_pipe.in.fire, s_AfterPreFire, s_BeforePreFire),
        s_AfterPreFire   ->  Mux(Mux(is_fencei === 1.U, fencei_io_vr.is_fencei_io.fire, io_pipe.out.fire), s_BeforePreFire, s_AfterPreFire)
    ))

    switch(n_state){//third phase
        is(s_BeforePreFire){
            in_ready := true.B
            out_valid := false.B
            is_fencei_valid := false.B
        }
        is(s_AfterPreFire){
            in_ready := false.B
            out_valid := true.B
            is_fencei_valid := Mux(is_fencei === 1.U, true.B, false.B)
        }
    }






    is_fencei_reg := Mux(fencei_io_vr.is_fencei_io.fire, 0.U, Mux(io_pipe.in.valid, is_fencei, is_fencei_reg))
}

