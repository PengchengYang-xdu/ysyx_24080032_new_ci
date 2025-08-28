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

    val csr_raddr = Input(UInt(CSR_ADDR_LEN.W))

    val csr_reg_pc = Input(UInt(WORD_LEN.W))
    val csr_irq_num = Input(UInt(IRQ_NUM_WIDTH.W))

    val csr_is_irq = Input(Bool())
}

class CSR extends Module {
    val io = IO(new CSRIO)
// 面积优化
    val mstatus   = RegInit(0x1800.U(WORD_LEN.W))
    val mtvec     = Reg(UInt(WORD_LEN.W))
    val mepc      = Reg(UInt(WORD_LEN.W))
    val mcause    = Reg(UInt(WORD_LEN.W))
    val mvendorid = "h79737978".U(WORD_LEN.W)
    val marchid   = 24080032.U(WORD_LEN.W)

    val rdata = WireDefault(0.U(WORD_LEN.W))
    switch(io.csr_raddr) {
        is("h300".U) { rdata := mstatus }
        is("h305".U) { rdata := mtvec }
        is("h341".U) { rdata := mepc }
        is("h342".U) { rdata := mcause }
        is("hf11".U) { rdata := mvendorid }
        is("hf12".U) { rdata := marchid }
    }
    io.csr_rdata := rdata
    io.csr_mtvec := mtvec
    io.csr_mepc  := mepc

    when(io.csr_cmd === CSR_E) {
        mepc   := io.csr_reg_pc
        mcause := io.csr_irq_num
    } .elsewhen(io.csr_cmd === CSR_S || io.csr_cmd === CSR_W && ~io.csr_is_irq) {
        switch(io.csr_addr) {
            is("h305".U) { mtvec := io.csr_wdata }
            is("h341".U) { mepc  := io.csr_wdata }
            is("h342".U) { mcause := io.csr_wdata }
            is("h300".U) { mstatus := io.csr_wdata }
        }
    }




    // val csr = Mem(6, UInt(WORD_LEN.W))

    // val csr_addr_process = Wire(UInt(CSR_ADDR.W))
    // csr_addr_process := MuxLookup(io.csr_addr, 0.U)(Seq(
    //     0x300.U(CSR_ADDR_LEN.W)  ->  CSR_MSTATUS_ADDR,
    //     0x305.U(CSR_ADDR_LEN.W)  ->  CSR_MTVEC_ADDR,
    //     0x341.U(CSR_ADDR_LEN.W)  ->  CSR_MEPC_ADDR,
    //     0x342.U(CSR_ADDR_LEN.W)  ->  CSR_MCAUSE_ADDR,
    //     0xf11.U(CSR_ADDR_LEN.W)  ->  CSR_MVENDORID_ADDR,
    //     0xf12.U(CSR_ADDR_LEN.W)  ->  CSR_MARCHID_ADDR
    // ))



    // io.csr_mtvec := csr(CSR_MTVEC_ADDR) //mtvec
    // io.csr_mepc := csr(CSR_MEPC_ADDR) //mepc

    // val csr_raddr_process = Wire(UInt(CSR_ADDR.W))
    // csr_raddr_process := MuxLookup(io.csr_raddr, 0.U)(Seq(
    //     0x300.U(CSR_ADDR_LEN.W)  ->  CSR_MSTATUS_ADDR,
    //     0x305.U(CSR_ADDR_LEN.W)  ->  CSR_MTVEC_ADDR,
    //     0x341.U(CSR_ADDR_LEN.W)  ->  CSR_MEPC_ADDR,
    //     0x342.U(CSR_ADDR_LEN.W)  ->  CSR_MCAUSE_ADDR,
    //     0xf11.U(CSR_ADDR_LEN.W)  ->  CSR_MVENDORID_ADDR,
    //     0xf12.U(CSR_ADDR_LEN.W)  ->  CSR_MARCHID_ADDR
    // ))
    // io.csr_rdata := csr(csr_raddr_process)

    // when(io.csr_cmd === CSR_E){//modified by ypc
    //     csr(CSR_MEPC_ADDR) := io.csr_reg_pc
    //     csr(CSR_MCAUSE_ADDR) := io.csr_irq_num
    // }
    // when(io.csr_cmd > 0.U && io.csr_cmd =/= CSR_E  && io.csr_cmd =/= CSR_M && ~io.csr_is_irq){
    //     csr(csr_addr_process) := io.csr_wdata
    // }


    // csr(CSR_MSTATUS_ADDR) := 0x1800.U //mstatus
    // csr(CSR_MVENDORID_ADDR) := "h79737978".U //mvendorid
    // csr(CSR_MARCHID_ADDR) := 24080032.U //marchid
}
