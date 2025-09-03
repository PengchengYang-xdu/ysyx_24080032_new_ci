package npc.core.idu

import chisel3._
import chisel3.util._
import npc.common.Config._
import npc.common.Instructions._
import npc.core.ifu._

class IDUIO_HAZARD extends Bundle {
    val stall_flg = Input(Bool())
    val flush_flg = Input(Bool())
}



class IDUIO extends Bundle {
    val gpr_rs1_addr = Output(UInt(ADDR_LEN.W))
    val gpr_rs2_addr = Output(UInt(ADDR_LEN.W))
    val gpr_rs1_data = Input(UInt(WORD_LEN.W))
    val gpr_rs2_data = Input(UInt(WORD_LEN.W))
    val gpr_rs1_is_read = Output(Bool())
    val gpr_rs2_is_read = Output(Bool())

    val csr_raddr = Output(UInt(CSR_ADDR_LEN.W))
    val csr_rdata = Input(UInt(WORD_LEN.W))

    val is_mret = Output(Bool())
}

class IDUIO_pipe_out extends Bundle{
    val id2exe_reg_pc = Output(UInt(WORD_LEN.W))
    // val id2exe_op1_data = Output(UInt(WORD_LEN.W))
    // val id2exe_op2_data = Output(UInt(WORD_LEN.W))
    val id2exe_op1_sel = Output(UInt(OP1_LEN.W))
    val id2exe_op2_sel = Output(UInt(OP2_LEN.W))
    val id2exe_rs1_data = Output(UInt(WORD_LEN.W))
    val id2exe_rs2_data = Output(UInt(WORD_LEN.W))
    val id2exe_wb_addr = Output(UInt(ADDR_LEN.W))
    val id2exe_rf_wen = Output(UInt(REN_LEN.W))
    val id2exe_exe_fun = Output(UInt(EXE_FUN_LEN.W))
    val id2exe_wb_sel = Output(UInt(WB_SEL_LEN.W))
    val id2exe_imm_sext = Output(UInt(WORD_LEN.W))
    // val id2exe_imm_i_sext = Output(UInt(WORD_LEN.W))
    // val id2exe_imm_s_sext = Output(UInt(WORD_LEN.W))
    // val id2exe_imm_b_sext = Output(UInt(WORD_LEN.W))
    // val id2exe_imm_u_shifted = Output(UInt(WORD_LEN.W))
    // val id2exe_imm_z_uext = Output(UInt(WORD_LEN.W))
    val id2exe_csr_addr = Output(UInt(CSR_ADDR_LEN.W))
    val id2exe_csr_cmd = Output(UInt(CSR_LEN.W))
    val id2exe_mem_wen = Output(UInt(MEN_LEN.W))
    val id2exe_mem_op = Output(UInt(MEM_OP.W))

    //irq
    val id2exe_is_irq = Output(Bool())
    val id2exe_irq_num = Output(UInt(IRQ_NUM_WIDTH.W))

    //csr
    val id2exe_csr_rdata = Output(UInt(WORD_LEN.W))
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

    val io_hazard = IO(new IDUIO_HAZARD)




    //registers
    // val inst_r = RegEnable(io_pipe.in.bits.if2id_inst, 0.U, io_pipe.in.valid & io_pipe.in.ready)
    // val reg_pc_r = RegEnable(io_pipe.in.bits.if2id_reg_pc, 0.U, io_pipe.in.valid & io_pipe.in.ready)




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
                        List(IMM_TYPE_X, ALU_X    , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , NO_FENCEI, RS1_NO_READ, RS2_NO_READ),
        Array(
            //-------------------------------------------------------------------------------------------------------------------------------------------------
            //[[LOAD/STORE指令]]-------------------------------------------------------------------------------------------------------------------------------
            //-------------------------------------------------------------------------------------------------------------------------------------------------
            //LOAD: src1 = rs1, src2 = imm, addr = rs1 + imm, 从内存读数据, 写回rd (2 来源[rs1, imm])
            LW       -> List(IMM_TYPE_I, ALU_ADD  , OP1_RS1, OP2_IMM, MEN_X, REN_S, WB_MEM, CSR_X, MEM_OP_4 , NO_FENCEI, RS1_IS_READ, RS2_NO_READ),
            LB       -> List(IMM_TYPE_I, ALU_ADD  , OP1_RS1, OP2_IMM, MEN_X, REN_S, WB_MEM, CSR_X, MEM_OP_1S, NO_FENCEI, RS1_IS_READ, RS2_NO_READ),
            LH       -> List(IMM_TYPE_I, ALU_ADD  , OP1_RS1, OP2_IMM, MEN_X, REN_S, WB_MEM, CSR_X, MEM_OP_2S, NO_FENCEI, RS1_IS_READ, RS2_NO_READ),
            LBU      -> List(IMM_TYPE_I, ALU_ADD  , OP1_RS1, OP2_IMM, MEN_X, REN_S, WB_MEM, CSR_X, MEM_OP_1U, NO_FENCEI, RS1_IS_READ, RS2_NO_READ),
            LHU      -> List(IMM_TYPE_I, ALU_ADD  , OP1_RS1, OP2_IMM, MEN_X, REN_S, WB_MEM, CSR_X, MEM_OP_2U, NO_FENCEI, RS1_IS_READ, RS2_NO_READ),
            //STORE: src1 = rs1, src2 = imm, addr = rs1 + imm, 把rs2写回内存 (3 来源[rs1, rs2, imm])
            SW       -> List(IMM_TYPE_S, ALU_ADD  , OP1_RS1, OP2_IMM, MEN_S, REN_X, WB_X  , CSR_X, MEM_OP_4 , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            SB       -> List(IMM_TYPE_S, ALU_ADD  , OP1_RS1, OP2_IMM, MEN_S, REN_X, WB_X  , CSR_X, MEM_OP_1S, NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            SH       -> List(IMM_TYPE_S, ALU_ADD  , OP1_RS1, OP2_IMM, MEN_S, REN_X, WB_X  , CSR_X, MEM_OP_2S, NO_FENCEI, RS1_IS_READ, RS2_IS_READ),

            //-------------------------------------------------------------------------------------------------------------------------------------------------
            //[[算数逻辑指令]]----------------------------------------------------------------------------------------------------------------------------------
            //-------------------------------------------------------------------------------------------------------------------------------------------------
            //R型算术指令: src1 = rs1, src2 = rs2, 写回rd (2 来源[rs1, rs2])
            ADD      -> List(IMM_TYPE_X, ALU_ADD  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            SUB      -> List(IMM_TYPE_X, ALU_SUB  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            AND      -> List(IMM_TYPE_X, ALU_AND  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            OR       -> List(IMM_TYPE_X, ALU_OR   , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            XOR      -> List(IMM_TYPE_X, ALU_XOR  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            SLL      -> List(IMM_TYPE_X, ALU_SLL  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            SRL      -> List(IMM_TYPE_X, ALU_SRL  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            SRA      -> List(IMM_TYPE_X, ALU_SRA  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            SLT      -> List(IMM_TYPE_X, ALU_SLT  , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            SLTU     -> List(IMM_TYPE_X, ALU_SLTU , OP1_RS1, OP2_RS2, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            //I型算术指令: src1 = rs1, src2 = imm, 写回rd (2 来源[rs1, imm])
            ADDI     -> List(IMM_TYPE_I, ALU_ADD  , OP1_RS1, OP2_IMM, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_NO_READ),
            ANDI     -> List(IMM_TYPE_I, ALU_AND  , OP1_RS1, OP2_IMM, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_NO_READ),
            ORI      -> List(IMM_TYPE_I, ALU_OR   , OP1_RS1, OP2_IMM, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_NO_READ),
            XORI     -> List(IMM_TYPE_I, ALU_XOR  , OP1_RS1, OP2_IMM, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_NO_READ),
            SLLI     -> List(IMM_TYPE_I, ALU_SLL  , OP1_RS1, OP2_IMM, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_NO_READ),
            SRLI     -> List(IMM_TYPE_I, ALU_SRL  , OP1_RS1, OP2_IMM, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_NO_READ),
            SRAI     -> List(IMM_TYPE_I, ALU_SRA  , OP1_RS1, OP2_IMM, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_NO_READ),
            SLTI     -> List(IMM_TYPE_I, ALU_SLT  , OP1_RS1, OP2_IMM, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_NO_READ),
            SLTIU    -> List(IMM_TYPE_I, ALU_SLTU , OP1_RS1, OP2_IMM, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_NO_READ),

            //-------------------------------------------------------------------------------------------------------------------------------------------------
            //[[分支跳转指令]]----------------------------------------------------------------------------------------------------------------------------------
            //-------------------------------------------------------------------------------------------------------------------------------------------------
            //Branch指令: src1 = rs1, src2 = rs2, 比较两者, 满足则跳target = pc + imm (4 来源[rs1, rs2, imm, pc])
            BEQ      -> List(IMM_TYPE_B, BR_BEQ   , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            BNE      -> List(IMM_TYPE_B, BR_BNE   , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            BGE      -> List(IMM_TYPE_B, BR_BGE   , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            BGEU     -> List(IMM_TYPE_B, BR_BGEU  , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            BLT      -> List(IMM_TYPE_B, BR_BLT   , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            BLTU     -> List(IMM_TYPE_B, BR_BLTU  , OP1_RS1, OP2_RS2, MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_IS_READ),
            //JAL指令: src1 = pc, src2 = imm, 跳target = pc + imm, 写回rd = pc + 4 (2 来源[imm, pc])
            JAL      -> List(IMM_TYPE_J, ALU_ADD  , OP1_PC , OP2_IMM, MEN_X, REN_S, WB_PC , CSR_X, MEM_OP_X , NO_FENCEI, RS1_NO_READ, RS2_NO_READ),
            //JALR指令: src1 = rs1, src2 = imm, 跳target = rs1 + imm, 写回rd = pc + 4 (2 来源[rs1, imm])
            JALR     -> List(IMM_TYPE_I, ALU_JALR , OP1_RS1, OP2_IMM, MEN_X, REN_S, WB_PC , CSR_X, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_NO_READ),

            //-------------------------------------------------------------------------------------------------------------------------------------------------
            //[[常数生成以及PC相对指令]]-------------------------------------------------------------------------------------------------------------------------
            //-------------------------------------------------------------------------------------------------------------------------------------------------
            //LUI指令: src1 = imm, 写回rd = imm << 12 (1 来源[imm])
            LUI      -> List(IMM_TYPE_U, ALU_ADD  , OP1_X  , OP2_IMM, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_NO_READ, RS2_NO_READ),
            //AUIPC指令: src1 = pc, src2 = imm, 写回rd = pc + imm (2 来源[imm, pc])
            AUIPC    -> List(IMM_TYPE_U, ALU_ADD  , OP1_PC , OP2_IMM, MEN_X, REN_S, WB_ALU, CSR_X, MEM_OP_X , NO_FENCEI, RS1_NO_READ, RS2_NO_READ),
        
            //-------------------------------------------------------------------------------------------------------------------------------------------------
            //[[CSR指令]]--------------------------------------------------------------------------------------------------------------------------------------
            //-------------------------------------------------------------------------------------------------------------------------------------------------
            //CSRRW指令: src1 = rs1, csr_addr = inst(31,20), csr读出的值写回rd, 更新csr值为rs1
            //CSRRS指令: src1 = rs1, csr_addr = inst(31,20), csr读出的值写回rd, 更新csr值为rs1 | csr读出的值
            CSRRW    -> List(IMM_TYPE_X, ALU_COPY1, OP1_RS1, OP2_X  , MEN_X, REN_S, WB_CSR, CSR_W, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_NO_READ),
            CSRRS    -> List(IMM_TYPE_X, ALU_COPY1, OP1_RS1, OP2_X  , MEN_X, REN_S, WB_CSR, CSR_S, MEM_OP_X , NO_FENCEI, RS1_IS_READ, RS2_NO_READ),
            //ECALL指令: 触发异常, 写mepc, 跳转到mtvec
            //MRET指令: 恢复pc为mepc
            ECALL    -> List(IMM_TYPE_X, ALU_X    , OP1_X  , OP2_X  , MEN_X, REN_X, WB_X  , CSR_E, MEM_OP_X , NO_FENCEI, RS1_NO_READ, RS2_NO_READ),
            MRET     -> List(IMM_TYPE_X, ALU_X    , OP1_X  , OP2_X  , MEN_X, REN_X, WB_X  , CSR_M, MEM_OP_X , NO_FENCEI, RS1_NO_READ, RS2_NO_READ),




            //其他指令
            EBREAK   -> List(IMM_TYPE_X, ALU_X    , OP1_X  , OP2_X  , MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , NO_FENCEI, RS1_NO_READ, RS2_NO_READ),
            FENCEI   -> List(IMM_TYPE_X, ALU_X    , OP1_X  , OP2_X  , MEN_X, REN_X, WB_X  , CSR_X, MEM_OP_X , IS_FENCEI, RS1_NO_READ, RS2_NO_READ)
        )
    )

    val imm_type :: exe_fun :: op1_sel :: op2_sel :: mem_wen :: rf_wen :: wb_sel :: csr_cmd :: mem_op :: is_fencei :: rs1_is_read :: rs2_is_read :: Nil = csignals
    
    val imm_sext = MuxCase(0.U(WORD_LEN.W), Seq(
        (imm_type === IMM_TYPE_I)  ->  imm_i_sext,
        (imm_type === IMM_TYPE_S)  ->  imm_s_sext,
        (imm_type === IMM_TYPE_J)  ->  imm_j_sext,
        (imm_type === IMM_TYPE_U)  ->  imm_u_shifted,
        (imm_type === IMM_TYPE_B)  ->  imm_b_sext
    ))


    val csr_addr = Mux(csr_cmd === CSR_E, 0x342.U(CSR_ADDR_LEN.W), inst(31,20))
    val csr_raddr = inst(31,20)
    io.csr_raddr := csr_raddr



    //connect
    io.gpr_rs1_addr := rs1_addr
    io.gpr_rs2_addr := rs2_addr
    io.gpr_rs1_is_read := rs1_is_read
    io.gpr_rs2_is_read := rs2_is_read


    io_pipe.out.bits.id2exe_reg_pc := reg_pc//32  must
    io_pipe.out.bits.id2exe_op1_sel := op1_sel//2
    io_pipe.out.bits.id2exe_op2_sel := op2_sel//2
    io_pipe.out.bits.id2exe_rs2_data := rs2_data//32
    io_pipe.out.bits.id2exe_rs1_data := rs1_data//32
    io_pipe.out.bits.id2exe_wb_addr := wb_addr//5
    io_pipe.out.bits.id2exe_rf_wen := rf_wen//2
    io_pipe.out.bits.id2exe_exe_fun := exe_fun//5
    io_pipe.out.bits.id2exe_wb_sel := wb_sel//3
    io_pipe.out.bits.id2exe_imm_sext := imm_sext//32  must
    io_pipe.out.bits.id2exe_csr_addr := csr_addr//12  must
    io_pipe.out.bits.id2exe_csr_cmd := csr_cmd//3
    io_pipe.out.bits.id2exe_mem_wen := mem_wen//2
    io_pipe.out.bits.id2exe_mem_op := mem_op//3

    io_pipe.out.bits.id2exe_csr_rdata := io.csr_rdata

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
    io_pipe.in.ready := in_ready && ~io_hazard.stall_flg
    io_pipe.out.valid := Mux(is_fencei === 1.U, fencei_io_vr.is_fencei_io.ready, out_valid && ~io_hazard.stall_flg)

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
            is_fencei_valid := is_fencei === 1.U
        }
    }






    is_fencei_reg := Mux(fencei_io_vr.is_fencei_io.fire, 0.U, Mux(io_pipe.in.valid, is_fencei, is_fencei_reg))
    
    //irq
    io_pipe.out.bits.id2exe_is_irq := Mux(csr_cmd === CSR_E, true.B, false.B)
    io_pipe.out.bits.id2exe_irq_num := Mux(csr_cmd === CSR_E, IRQ_NUM_ECALL, 0.U)
    io.is_mret := csr_cmd === CSR_M
}

