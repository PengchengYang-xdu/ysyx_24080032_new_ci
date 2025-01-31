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

class IDUIO_pipe extends Bundle {
    val in = Flipped(Decoupled(new IFUIO_pipe_out))
    val out = Decoupled(new IDUIO_pipe_out)
}

class IDU extends Module {
    val io = IO(new IDUIO)
    val io_pipe = IO(new IDUIO_pipe)













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
                        List(ALU_X    , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X ),
        Array(
            LW       -> List(ALU_ADD  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_MEM, CSR_X, MEM_OP_4 ),
            SW       -> List(ALU_ADD  , OP1_RS1, OP2_IMS, MEN_S, REN_X, WB_X  , CSR_X, MEM_OP_4 ),
            LB       -> List(ALU_ADD  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_MEM, CSR_X, MEM_OP_1S),
            LH       -> List(ALU_ADD  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_MEM, CSR_X, MEM_OP_2S),
            LBU      -> List(ALU_ADD  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_MEM, CSR_X, MEM_OP_1U),
            LHU      -> List(ALU_ADD  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_MEM, CSR_X, MEM_OP_2U),
            SB       -> List(ALU_ADD  , OP1_RS1, OP2_IMS, MEN_S, REN_X, WB_X  , CSR_X, MEM_OP_1S),
            SH       -> List(ALU_ADD  , OP1_RS1, OP2_IMS, MEN_S, REN_X, WB_X  , CSR_X, MEM_OP_2S),

            ADD      -> List(ALU_ADD  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            ADDI     -> List(ALU_ADD  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            SUB      -> List(ALU_SUB  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            AND      -> List(ALU_AND  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            OR       -> List(ALU_OR   , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            XOR      -> List(ALU_XOR  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            ANDI     -> List(ALU_AND  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            ORI      -> List(ALU_OR   , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            XORI     -> List(ALU_XOR  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            SLL      -> List(ALU_SLL  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            SRL      -> List(ALU_SRL  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            SRA      -> List(ALU_SRA  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            SLLI     -> List(ALU_SLL  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            SRLI     -> List(ALU_SRL  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            SRAI     -> List(ALU_SRA  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            SLT      -> List(ALU_SLT  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            SLTU     -> List(ALU_SLTU , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            SLTI     -> List(ALU_SLT  , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            SLTIU    -> List(ALU_SLTU , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            BEQ      -> List(BR_BEQ   , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X ),
            BNE      -> List(BR_BNE   , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X ),
            BGE      -> List(BR_BGE   , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X ),
            BGEU     -> List(BR_BGEU  , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X ),
            BLT      -> List(BR_BLT   , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X ),
            BLTU     -> List(BR_BLTU  , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X ),
            JAL      -> List(ALU_ADD  , OP1_PC , OP2_IMJ, MEN_X, REN_S, WB_PC , CSR_X, MEM_OP_X ),
            JALR     -> List(ALU_JALR , OP1_RS1, OP2_IMI, MEN_X, REN_S, WB_PC , CSR_X, MEM_OP_X ),
            LUI      -> List(ALU_ADD  , OP1_X  , OP2_IMU, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            AUIPC    -> List(ALU_ADD  , OP1_PC , OP2_IMU, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X ),
            CSRRW    -> List(ALU_COPY1, OP1_RS1, OP2_X  , MEN_X, REN_S, WB_CSR, CSR_W, MEM_OP_X ),
            CSRRWI   -> List(ALU_COPY1, OP1_IMZ, OP2_X  , MEN_X, REN_S, WB_CSR, CSR_W, MEM_OP_X ),
            CSRRS    -> List(ALU_COPY1, OP1_RS1, OP2_X  , MEN_X, REN_S, WB_CSR, CSR_S, MEM_OP_X ),
            CSRRSI   -> List(ALU_COPY1, OP1_IMZ, OP2_X  , MEN_X, REN_S, WB_CSR, CSR_S, MEM_OP_X ),
            CSRRC    -> List(ALU_COPY1, OP1_RS1, OP2_X  , MEN_X, REN_S, WB_CSR, CSR_C, MEM_OP_X ),
            CSRRCI   -> List(ALU_COPY1, OP1_IMZ, OP2_X  , MEN_X, REN_S, WB_CSR, CSR_C, MEM_OP_X ),
            ECALL    -> List(ALU_X    , OP1_X  , OP2_X  , MEN_X, REN_X, WB_X  , CSR_E, MEM_OP_X ),
            MRET     -> List(ALU_X    , OP1_X  , OP2_X  , MEN_X, REN_X, WB_X  , CSR_M, MEM_OP_X ),//modified by ypc
            EBREAK   -> List(ALU_X    , OP1_X  , OP2_X  , MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X )
        )
    )

    val exe_fun :: op1_sel :: op2_sel :: mem_wen :: rf_wen :: wb_sel :: csr_cmd :: mem_op :: Nil = csignals
    
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

