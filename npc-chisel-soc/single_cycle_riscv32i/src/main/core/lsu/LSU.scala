package npc.core.lsu

import chisel3._
import chisel3.util._
import npc.common.Config._
import npc.common.Instructions._
import npc.core.exu._
import npc.bus.axi._

class LSUIO extends Bundle {
    val dmem = Flipped(new AXI4WithoutClk)

    val csr_rdata = Input(UInt(WORD_LEN.W))
}

class LSUIO_pipe_out extends Bundle{
    val ls2wb_wb_addr = Output(UInt(ADDR_LEN.W))
    val ls2wb_rf_wen = Output(UInt(REN_LEN.W))
    val ls2wb_wb_data = Output(UInt(WORD_LEN.W))

    val ls2wb_csr_wdata = Output(UInt(WORD_LEN.W))
    val ls2wb_csr_addr = Output(UInt(CSR_ADDR_LEN.W))
    val ls2wb_csr_cmd = Output(UInt(CSR_LEN.W))
}

class LSUIO_pipe extends Bundle {
    val in = Flipped(Decoupled(new EXUIO_pipe_out))
    val out = Decoupled(new LSUIO_pipe_out)
}

class LSU extends Module {
    val io = IO(new LSUIO)
    val io_pipe = IO(new LSUIO_pipe)

    //disable something in AR R AW W B
    io.dmem.arid := 0.U
    io.dmem.arlen := 0.U
    io.dmem.arburst := 0.U
    io.dmem.awid := 0.U
    io.dmem.awlen := 0.U
    io.dmem.awburst := 0.U
    io.dmem.wlast := true.B


    val notLS = io_pipe.in.bits.exe2ls_mem_op === MEM_OP_X
    val isS = ~notLS & (io_pipe.in.bits.exe2ls_mem_wen === MEN_S)
    val isL = ~notLS & (io_pipe.in.bits.exe2ls_mem_wen === MEN_X)

    //delay
    val lfsr = RegInit(LSU_DELAY)
    lfsr := Cat(lfsr(2,0), lfsr(0)^lfsr(1)^lfsr(2))
    val delay = RegInit(lfsr)
    

















    //handshake between modules
    val in_ready = RegInit(false.B)
    val out_valid = RegInit(false.B)
    io_pipe.in.ready := in_ready
    io_pipe.out.valid := out_valid

    val araddr = RegInit(0.U)
    val arvalid = RegInit(false.B)
    val arsize = RegInit(0.U)
    val rready = RegInit(false.B)
    val awaddr = RegInit(0.U)
    val awvalid = RegInit(false.B)
    val awsize = RegInit(0.U)
    val wdata = RegInit(0.U)
    val wstrb = RegInit(0.U)
    val wvalid = RegInit(false.B)
    val bready = RegInit(false.B)
    io.dmem.araddr  := araddr
    io.dmem.arvalid := arvalid
    io.dmem.arsize := arsize
    io.dmem.rready  := rready
    io.dmem.awaddr  := awaddr
    io.dmem.awvalid := awvalid
    io.dmem.awsize := awsize
    io.dmem.wdata   := wdata
    io.dmem.wstrb   := wstrb
    io.dmem.wvalid  := wvalid
    io.dmem.bready  := bready

    val s_BeforePreFire :: s_BeforeAXI_ARorAWW_Fire :: s_BeforeAXI_RorB_Fire :: s_AfterPreFire :: Nil = Enum(4)
    val c_state = RegInit(s_BeforePreFire)
    val n_state = WireDefault(c_state)
    dontTouch(n_state)

    val AXI_ARorAWW_fire = (arvalid & io.dmem.arready) | ((awvalid & io.dmem.awready) & (wvalid & io.dmem.wready))
    val AXI_RorB_fire = (io.dmem.rvalid & rready) | (io.dmem.bvalid & bready)

    c_state := n_state//first phase

    n_state := MuxLookup(c_state, s_BeforePreFire)(Seq(//second phase
        s_BeforePreFire             ->  Mux(io_pipe.in.fire, Mux(notLS, s_AfterPreFire, s_BeforeAXI_ARorAWW_Fire), s_BeforePreFire),
        s_BeforeAXI_ARorAWW_Fire    ->  Mux(AXI_ARorAWW_fire, s_BeforeAXI_RorB_Fire, s_BeforeAXI_ARorAWW_Fire),
        s_BeforeAXI_RorB_Fire       ->  Mux(AXI_RorB_fire, s_AfterPreFire, s_BeforeAXI_RorB_Fire),
        s_AfterPreFire              ->  Mux(io_pipe.out.fire, s_BeforePreFire, s_AfterPreFire)
    ))

    val dmem_rdata = RegInit(0.U)//保存一下读出的数据
    dmem_rdata := Mux(n_state === s_AfterPreFire, io.dmem.rdata, dmem_rdata)

    switch(n_state){//third phase
        is(s_BeforePreFire){
            //between modules
            in_ready := true.B
            out_valid := false.B
            //AXI
            arvalid := false.B
            rready := false.B
            awvalid := false.B
            wvalid := false.B
            bready := false.B
            arsize := 2.U
            awsize := 2.U
            //delay
            delay := lfsr
        }
        is(s_BeforeAXI_ARorAWW_Fire){
            //between modules
            in_ready := false.B
            out_valid := false.B
            //AXI
            when(delay === 0.U){
                arvalid := Mux(isL, true.B, false.B)
                arsize := MuxLookup(io_pipe.in.bits.exe2ls_mem_op, 2.U)(Seq(
                    MEM_OP_1S  ->  0.U,
                    MEM_OP_1U  ->  0.U,
                    MEM_OP_2S  ->  1.U,
                    MEM_OP_2U  ->  1.U,
                    MEM_OP_4   ->  2.U
                ))
                rready := false.B
                awvalid := Mux(isS, true.B, false.B)
                awsize := MuxLookup(io_pipe.in.bits.exe2ls_mem_op, 2.U)(Seq(
                    MEM_OP_1S  ->  0.U,
                    MEM_OP_2S  ->  1.U,
                    MEM_OP_4   ->  2.U
                ))
                wvalid := Mux(isS, true.B, false.B)
                bready := false.B
            }.otherwise{
                arvalid := false.B
                rready := false.B
                awvalid := false.B
                wvalid := false.B
                bready := false.B
                arsize := 2.U
                awsize := 2.U
                //delay
                delay := delay - 1.U
            }
        }
        is(s_BeforeAXI_RorB_Fire){
            //between modules
            in_ready := false.B
            out_valid := false.B
            //AXI
            arvalid := false.B
            arsize := MuxLookup(io_pipe.in.bits.exe2ls_mem_op, 2.U)(Seq(
                MEM_OP_1S  ->  0.U,
                MEM_OP_1U  ->  0.U,
                MEM_OP_2S  ->  1.U,
                MEM_OP_2U  ->  1.U,
                MEM_OP_4   ->  2.U
            ))
            rready := Mux(isL, true.B, false.B)
            awvalid := false.B
            awsize := MuxLookup(io_pipe.in.bits.exe2ls_mem_op, 2.U)(Seq(
                MEM_OP_1S  ->  0.U,
                MEM_OP_2S  ->  1.U,
                MEM_OP_4   ->  2.U
            ))
            wvalid := false.B
            bready := Mux(isS, true.B, false.B)
        }
        is(s_AfterPreFire){
            //between modules
            in_ready := false.B
            out_valid := true.B
            //AXI
            arvalid := false.B
            rready := false.B
            awvalid := false.B
            wvalid := false.B
            bready := false.B
            arsize := 2.U
            awsize := 2.U
        }
    }















    //main process
    val dmem_rdata_processed = WireDefault(0.U(WORD_LEN.W))
    val csr_wdata =  MuxCase(0.U(WORD_LEN.W), Seq(
        (io_pipe.in.bits.exe2ls_csr_cmd === CSR_W) -> io_pipe.in.bits.exe2ls_op1_data,
        (io_pipe.in.bits.exe2ls_csr_cmd === CSR_S) -> (io.csr_rdata | io_pipe.in.bits.exe2ls_op1_data),
        (io_pipe.in.bits.exe2ls_csr_cmd === CSR_C) -> (io.csr_rdata & ~io_pipe.in.bits.exe2ls_op1_data),
        (io_pipe.in.bits.exe2ls_csr_cmd === CSR_E) -> 11.U(WORD_LEN.W)
    ))

    val wb_data = MuxCase(io_pipe.in.bits.exe2ls_alu_out, Seq(
        (io_pipe.in.bits.exe2ls_wb_sel === WB_MEM) -> dmem_rdata_processed,
        (io_pipe.in.bits.exe2ls_wb_sel === WB_PC)  -> (io_pipe.in.bits.exe2ls_reg_pc + 4.U(WORD_LEN.W)),
        (io_pipe.in.bits.exe2ls_wb_sel === WB_CSR) -> io.csr_rdata
    ))

    switch(io_pipe.in.bits.exe2ls_mem_op) {
        is(0.U) {//byte
            wstrb := "b00000001".U << io_pipe.in.bits.exe2ls_alu_out(1,0)
        }
        is(1.U) {//half word
            wstrb := "b00000011".U << io_pipe.in.bits.exe2ls_alu_out(1,0)
        }
        is(2.U) {//word
            wstrb := "b00001111".U << io_pipe.in.bits.exe2ls_alu_out(1,0)
        }
    }

    //process wmask and read mode
    val shift_rdata = dmem_rdata >> (io_pipe.in.bits.exe2ls_alu_out(1,0) << 3.U)
    switch(io_pipe.in.bits.exe2ls_mem_op) {
        is(0.U) {//1s
            dmem_rdata_processed := Cat(Fill(24, shift_rdata(7)), shift_rdata(7,0))
        }
        is(1.U) {//2s
            dmem_rdata_processed := Cat(Fill(16, shift_rdata(15)), shift_rdata(15,0))
        }
        is(2.U) {//4
            dmem_rdata_processed := shift_rdata
        }
        is(4.U) {//1u
            dmem_rdata_processed := Cat(Fill(24, 0.U), shift_rdata(7,0))
        }
        is(5.U) {//2u
            dmem_rdata_processed := Cat(Fill(16, 0.U), shift_rdata(15,0))
        }
    }

    //connect
    araddr := io_pipe.in.bits.exe2ls_alu_out
    awaddr := io_pipe.in.bits.exe2ls_alu_out
    wdata := io_pipe.in.bits.exe2ls_rs2_data << (io_pipe.in.bits.exe2ls_alu_out(1,0) << 3.U)

    io_pipe.out.bits.ls2wb_csr_addr := io_pipe.in.bits.exe2ls_csr_addr//csr write channel
    io_pipe.out.bits.ls2wb_csr_wdata := csr_wdata
    io_pipe.out.bits.ls2wb_csr_cmd := io_pipe.in.bits.exe2ls_csr_cmd

    io_pipe.out.bits.ls2wb_wb_addr := io_pipe.in.bits.exe2ls_wb_addr//gpr write channel
    io_pipe.out.bits.ls2wb_rf_wen := io_pipe.in.bits.exe2ls_rf_wen
    io_pipe.out.bits.ls2wb_wb_data := wb_data




}

