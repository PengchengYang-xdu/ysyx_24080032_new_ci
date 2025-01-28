package npc.core

import chisel3._
import chisel3.util._
import npc.common.Config._
import npc.common.Instructions._

class CSRIO extends Bundle {
    val csr_mtvec = Output(UInt(WORD_LEN.W))
    val csr_mepc = Output(UInt(WORD_LEN.W))

    val csr_rdata = Output(UInt(WORD_LEN.W))
    val csr_wdata = Input(UInt(WORD_LEN.W))
    val csr_addr = Input(UInt(CSR_ADDR_LEN.W))
    val csr_cmd = Input(UInt(CSR_LEN.W))

    val csr_reg_pc = Input(UInt(WORD_LEN.W))
}

class CSR extends Module {
    val io = IO(new CSRIO)

    val csr = Mem(6, UInt(WORD_LEN.W))

    val csr_addr_process = Wire(UInt(CSR_ADDR.W))
    csr_addr_process := MuxLookup(io.csr_addr, 0.U)(Seq(
        0x300.U(CSR_ADDR_LEN.W)  ->  CSR_MSTATUS_ADDR,
        0x305.U(CSR_ADDR_LEN.W)  ->  CSR_MTVEC_ADDR,
        0x341.U(CSR_ADDR_LEN.W)  ->  CSR_MEPC_ADDR,
        0x342.U(CSR_ADDR_LEN.W)  ->  CSR_MCAUSE_ADDR,
        0xf11.U(CSR_ADDR_LEN.W)  ->  CSR_MVENDORID_ADDR,
        0xf12.U(CSR_ADDR_LEN.W)  ->  CSR_MARCHID_ADDR
    ))



    io.csr_mtvec := csr(CSR_MTVEC_ADDR) //mtvec
    io.csr_mepc := csr(CSR_MEPC_ADDR) //mepc

    io.csr_rdata := csr(csr_addr_process)

    when(io.csr_cmd > 0.U){
        when(io.csr_cmd === CSR_E){//modified by ypc
            csr(CSR_MEPC_ADDR) := io.csr_reg_pc
        }
        csr(csr_addr_process) := io.csr_wdata
    }

    csr(CSR_MSTATUS_ADDR) := 0x1800.U //mstatus
    csr(CSR_MVENDORID_ADDR) := "h79737978".U //mvendorid
    csr(CSR_MARCHID_ADDR) := 24080032.U //marchid
}
