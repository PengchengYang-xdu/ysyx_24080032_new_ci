package npc.core.ifu

import chisel3._
import chisel3.util._
import npc.common.Config._
import npc.common.Instructions._
import npc.core.wbu._
import npc.bus.axi._

class IFUIO_HAZARD extends Bundle {
    val flush_flg = Input(Bool())
    val pc_plus4 = Output(UInt(WORD_LEN.W))
    val pc_real_next = Input(UInt(WORD_LEN.W))
    val is_mret_rise = Input(Bool())
<<<<<<< HEAD
=======
    val reg_pc = Output(UInt(WORD_LEN.W))
>>>>>>> tracer-ysyx
}


class IFUIO extends Bundle {
    val imem = Flipped(new AXI4WithoutClk)
}

class IFUIO_pipe_out extends Bundle{
    val if2id_reg_pc = Output(UInt(WORD_LEN.W)) //pipe
    val if2id_inst = Output(UInt(WORD_LEN.W)) //pipe
}

class IFUIO_pipe extends Bundle {
    val in = Flipped(Decoupled(new WBUIO_pipe_out))
    val out = Decoupled(new IFUIO_pipe_out)
}

class IFU extends Module {
    val io = IO(new IFUIO)
    val io_pipe = IO(new IFUIO_pipe)

    dontTouch(io_pipe)


    val io_hazard = IO(new IFUIO_HAZARD)


    //disable AW W B and something in AR R
    io.imem.arid := 0.U
    io.imem.arlen := 0.U
    io.imem.arburst := 0.U
    io.imem.awaddr := 0.U
    io.imem.awvalid := false.B
    io.imem.awid := 0.U
    io.imem.awlen := 0.U
    io.imem.awsize := 0.U
    io.imem.awburst := 0.U
    io.imem.wdata := 0.U
    io.imem.wstrb := 0.U
    io.imem.wvalid := false.B
    io.imem.wlast := false.B
    io.imem.bready := false.B


    val is_mret_rise = io_hazard.is_mret_rise

    val is_flush = io_hazard.flush_flg | is_mret_rise
    //handshake between modules && handshake between Imem
    val in_ready = RegInit(false.B)
    val out_valid = RegInit(false.B)
    io_pipe.in.ready := in_ready
    io_pipe.out.valid := out_valid & ~is_flush

    val araddr = Wire(UInt(WORD_LEN.W))
    val arvalid = RegInit(false.B)
    val rready = RegInit(false.B)
    io.imem.araddr := araddr
    io.imem.arvalid := arvalid
    io.imem.rready := rready
    io.imem.arsize := 2.U


    val s_BeforePreFire :: s_BeforeAXI_AR_Fire :: s_BeforeAXI_R_Fire :: s_AfterPreFire :: s_Flush :: Nil = Enum(5)
    val c_state = RegInit(s_BeforeAXI_AR_Fire)
    val n_state = WireDefault(c_state)
    dontTouch(n_state)

    val AXI_AR_fire = arvalid & io.imem.arready
    val AXI_R_fire = io.imem.rvalid & rready

    //flush states
    val R_while_flush = AXI_R_fire & is_flush
    val flush_before_R = ~AXI_R_fire & is_flush
    val fetch_normal = AXI_R_fire & ~is_flush

    // val start = io_pipe.in.fire//this is the multi cycle version, change it auto fetch to fit 5 pipelines
    val start =  io_pipe.in.valid && io.imem.arready && ~is_flush

    c_state := n_state//first phase

    n_state := MuxLookup(c_state, s_BeforePreFire)(Seq(//second phase
        s_BeforePreFire       ->  Mux(start, s_BeforeAXI_AR_Fire, s_BeforePreFire),
        s_BeforeAXI_AR_Fire   ->  Mux(AXI_AR_fire, Mux(is_mret_rise, s_Flush, s_BeforeAXI_R_Fire), s_BeforeAXI_AR_Fire),
        s_BeforeAXI_R_Fire    ->  Mux(fetch_normal, s_AfterPreFire, Mux(flush_before_R, s_Flush, Mux(R_while_flush, s_BeforePreFire, s_BeforeAXI_R_Fire))),
        s_AfterPreFire        ->  Mux(is_flush | io_pipe.out.fire, s_BeforePreFire, s_AfterPreFire),
        s_Flush               ->  Mux(AXI_R_fire, s_BeforePreFire, s_Flush)
    ))//发起的请求必须等取到这次取指之后，再冲刷

    switch(n_state){//third phase
        is(s_BeforePreFire){
            //between modules
            in_ready := true.B
            out_valid := false.B
            //AXI
            arvalid := false.B
            rready := false.B
        }
        is(s_BeforeAXI_AR_Fire){
            //between modules
            in_ready := false.B
            out_valid := false.B
            //AXI
            arvalid := true.B
            rready := false.B
        }
        is(s_BeforeAXI_R_Fire){
            //between modules
            in_ready := false.B
            out_valid := false.B
            //AXI
            arvalid := false.B
            rready := true.B
        }
        is(s_AfterPreFire){
            //between modules
            in_ready := false.B
            out_valid := true.B
            //AXI
            arvalid := false.B
            rready := false.B
        }
        is(s_Flush){
            //between modules
            in_ready := false.B
            out_valid := false.B
            //AXI
            arvalid := false.B
            rready := true.B
        }
    }











    //main process
    val pc_next = Wire(UInt(WORD_LEN.W))
    dontTouch(pc_next)
    
    val reg_pc = withReset(reset.asAsyncReset){
        RegEnable(pc_next, START_ADDR, io_pipe.in.valid & io_pipe.in.ready)
    }

    val pc_plus4 = reg_pc + 4.U(WORD_LEN.W)
    io_hazard.pc_plus4 := pc_plus4

    pc_next := io_hazard.pc_real_next
    
    //connect
    araddr := reg_pc
<<<<<<< HEAD
=======
    io_hazard.reg_pc := reg_pc
>>>>>>> tracer-ysyx

    io_pipe.out.bits.if2id_reg_pc := reg_pc
    io_pipe.out.bits.if2id_inst := io.imem.rdata
}

