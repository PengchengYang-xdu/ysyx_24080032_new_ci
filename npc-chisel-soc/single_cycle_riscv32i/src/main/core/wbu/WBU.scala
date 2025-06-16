package npc.core.wbu

import chisel3._
import chisel3.util._
import npc.common.Config._
import npc.common.Instructions._
import npc.core.lsu._


class WBUIO_HAZARD extends Bundle {
    val flush_flg = Input(Bool())
}

class WBUIO extends Bundle {
    val gpr_wen = Output(UInt(REN_LEN.W))
    val gpr_addr = Output(UInt(ADDR_LEN.W))
    val gpr_wdata = Output(UInt(WORD_LEN.W))

    val csr_addr = Output(UInt(CSR_ADDR_LEN.W))
    val csr_cmd = Output(UInt(CSR_LEN.W))
    val csr_wdata = Output(UInt(WORD_LEN.W))

    val is_irq = Output(Bool())
    val irq_num = Output(UInt(IRQ_NUM_WIDTH.W))

    val irq_valid = Output(Bool())
}

class WBUIO_pipe_out extends Bundle{
}

class WBUIO_pipe extends Bundle {
    val in = Flipped(Decoupled(new LSUIO_pipe_out))
    val out = Decoupled(new WBUIO_pipe_out)
}

class WBU extends Module {
    val io = IO(new WBUIO)
    val io_pipe = IO(new WBUIO_pipe)

    dontTouch(io_pipe)



    val io_hazard = IO(new WBUIO_HAZARD)



    //main process
    //connect
    io.gpr_wen := Mux(io_pipe.in.valid && ~io_hazard.flush_flg, io_pipe.in.bits.ls2wb_rf_wen, 0.U)
    // io.gpr_wen := io_pipe.in.bits.ls2wb_rf_wen
    io.gpr_addr := io_pipe.in.bits.ls2wb_wb_addr
    io.gpr_wdata := io_pipe.in.bits.ls2wb_wb_data

    io.csr_cmd := Mux(io_pipe.in.valid && ~io_hazard.flush_flg, io_pipe.in.bits.ls2wb_csr_cmd, 0.U)
    // io.csr_cmd := io_pipe.in.bits.ls2wb_csr_cmd
    io.csr_addr := io_pipe.in.bits.ls2wb_csr_addr
    io.csr_wdata := io_pipe.in.bits.ls2wb_csr_wdata















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
            //disable csr and gpr write en
            io.gpr_wen := 0.U
            io.csr_cmd := 0.U
        }
        is(s_AfterPreFire){
            in_ready := false.B
            out_valid := true.B
        }
    }





    //irq
    io.is_irq := io_pipe.in.bits.ls2wb_is_irq
    io.irq_num := io_pipe.in.bits.ls2wb_irq_num
    io.irq_valid := io_pipe.in.valid && ~io_hazard.flush_flg
}

