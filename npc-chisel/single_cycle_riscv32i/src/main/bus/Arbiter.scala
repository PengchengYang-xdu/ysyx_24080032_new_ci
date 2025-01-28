package npc.bus.arbiter

import chisel3._
import chisel3.util._
import npc.bus.axi._

class ArbIO extends Bundle{
    //imem ---> arb
    //dmem ---> arb
    val imem = new AXI_liteWithoutClk
    val dmem = new AXI_liteWithoutClk
    //arb  ---> mem
    val mem = Flipped(new AXI_liteWithoutClk)
}

class Arbiter extends Module {
    val io = IO(new ArbIO)

    //imem reg
    val imem_arready = RegInit(true.B)
    val imem_rdata = RegInit(0.U)
    val imem_rresp = RegInit(0.U)
    val imem_rvalid = RegInit(false.B)
    val imem_awready = RegInit(false.B)
    val imem_wready = RegInit(false.B)
    val imem_bresp = RegInit(0.U)
    val imem_bvalid = RegInit(false.B)
    io.imem.arready := imem_arready
    io.imem.rdata := imem_rdata
    io.imem.rresp := imem_rresp
    io.imem.rvalid := imem_rvalid
    io.imem.awready := imem_awready
    io.imem.wready := imem_wready
    io.imem.bresp := imem_bresp
    io.imem.bvalid := imem_bvalid
    //dmem reg
    val dmem_arready = RegInit(true.B)
    val dmem_rdata = RegInit(0.U)
    val dmem_rresp = RegInit(0.U)
    val dmem_rvalid = RegInit(false.B)
    val dmem_awready = RegInit(true.B)
    val dmem_wready = RegInit(false.B)
    val dmem_bresp = RegInit(0.U)
    val dmem_bvalid = RegInit(false.B)
    io.dmem.arready := dmem_arready
    io.dmem.rdata := dmem_rdata
    io.dmem.rresp := dmem_rresp
    io.dmem.rvalid := dmem_rvalid
    io.dmem.awready := dmem_awready
    io.dmem.wready := dmem_wready
    io.dmem.bresp := dmem_bresp
    io.dmem.bvalid := dmem_bvalid
    //mem reg
    val mem_araddr = RegInit(0.U)
    val mem_arvalid = RegInit(false.B)
    val mem_rready = RegInit(false.B)
    val mem_awaddr = RegInit(0.U)
    val mem_awvalid = RegInit(false.B)
    val mem_wdata = RegInit(0.U)
    val mem_wstrb = RegInit(0.U)
    val mem_wvalid = RegInit(false.B)
    val mem_bready = RegInit(false.B)
    io.mem.araddr := mem_araddr
    io.mem.arvalid := mem_arvalid
    io.mem.rready := mem_rready
    io.mem.awaddr := mem_awaddr
    io.mem.awvalid := mem_awvalid
    io.mem.wdata := mem_wdata
    io.mem.wstrb := mem_wstrb
    io.mem.wvalid := mem_wvalid
    io.mem.bready := mem_bready

/*-----------------------FSM-----------------------*/
    val s_IDLE :: s_imem_0 :: s_imem_1 :: s_dmem_0 :: s_dmem_1 :: Nil = Enum(5)
    val c_state = RegInit(s_IDLE)
    val n_state = WireDefault(c_state)
    dontTouch(n_state)

    val imem_req = io.imem.arvalid & imem_arready
    val imem_mem_done = io.mem.rvalid & mem_rready
    val imem_done = imem_rvalid & io.imem.rready

    val dmem_req = (io.dmem.arvalid & dmem_arready) | ((io.dmem.awvalid & dmem_awready) & (io.dmem.wvalid & dmem_wready))
    val dmem_mem_done = (io.mem.rvalid & mem_rready) | (io.mem.bvalid & mem_bready)
    val dmem_done = (dmem_rvalid & io.dmem.rready) | (dmem_bvalid & io.dmem.bready)


    c_state := n_state//first phase

    n_state := MuxLookup(c_state, s_IDLE)(Seq(//second phase
        s_IDLE      ->  MuxCase(s_IDLE, Seq(
            imem_req   ->   s_imem_0,
            dmem_req   ->   s_dmem_0
        )),
        s_imem_0    ->  Mux(imem_mem_done, s_imem_1, s_imem_0),
        s_imem_1    ->  Mux(imem_done, s_IDLE, s_imem_1),
        s_dmem_0    ->  Mux(dmem_mem_done, s_dmem_1, s_dmem_0),
        s_dmem_1    ->  Mux(dmem_done, s_IDLE, s_dmem_1)
    ))

    switch(n_state){//third phase
        is(s_IDLE){
            DefaultDmem()
            DefaultImem()
            DefaultMem()
        }
        is(s_imem_0){
            ConnectImem()
            DefaultDmem()
            BlockDmem()
        }
        is(s_imem_1){
            ConnectImem()
            DefaultDmem()
            BlockDmem()
            mem_arvalid := false.B
            mem_rready := false.B
        }
        is(s_dmem_0){
            ConnectDmem()
            DefaultImem()
            BlockImem()
        }
        is(s_dmem_1){
            ConnectDmem()
            DefaultImem()
            BlockImem()
            mem_arvalid := false.B
            mem_rready := false.B
            mem_awvalid := false.B
            mem_wvalid := false.B
            mem_bready := false.B
        }
    }



/*-----------------------function-----------------------*/
    def ConnectImem(): Unit = {
        imem_arready := io.mem.arready
        imem_rdata := io.mem.rdata
        imem_rresp := io.mem.rresp
        imem_rvalid := io.mem.rvalid
        imem_awready := io.mem.awready
        imem_wready := io.mem.wready
        imem_bresp := io.mem.bresp
        imem_bvalid := io.mem.bvalid

        mem_araddr := io.imem.araddr
        mem_arvalid := io.imem.arvalid
        mem_rready := io.imem.rready
        mem_awaddr := io.imem.awaddr
        mem_awvalid := io.imem.awvalid
        mem_wdata := io.imem.wdata
        mem_wstrb := io.imem.wstrb
        mem_wvalid := io.imem.wvalid
        mem_bready := io.imem.bready
    }

    def BlockImem(): Unit = {
        io.imem.arready := false.B
    }

    def ConnectDmem(): Unit = {
        dmem_arready := io.mem.arready
        dmem_rdata := io.mem.rdata
        dmem_rresp := io.mem.rresp
        dmem_rvalid := io.mem.rvalid
        dmem_awready := io.mem.awready
        dmem_wready := io.mem.wready
        dmem_bresp := io.mem.bresp
        dmem_bvalid := io.mem.bvalid

        mem_araddr := io.dmem.araddr
        mem_arvalid := io.dmem.arvalid
        mem_rready := io.dmem.rready
        mem_awaddr := io.dmem.awaddr
        mem_awvalid := io.dmem.awvalid
        mem_wdata := io.dmem.wdata
        mem_wstrb := io.dmem.wstrb
        mem_wvalid := io.dmem.wvalid
        mem_bready := io.dmem.bready
    }

    def BlockDmem(): Unit = {
        io.dmem.arready := false.B
        io.dmem.awready := false.B
        io.dmem.wready := false.B
    }

    def DefaultMem(): Unit = {
        mem_araddr := 0.U
        mem_arvalid := false.B
        mem_rready := false.B
        mem_awaddr := 0.U
        mem_awvalid := false.B
        mem_wdata := 0.U
        mem_wstrb := 0.U
        mem_wvalid := false.B
        mem_bready := false.B
    }

    def DefaultImem(): Unit = {
        imem_arready := true.B
        // imem_rdata := 0.U //in IDLE, last time rdata has to hold, not to flush to 0 
        imem_rresp := 0.U
        imem_rvalid := false.B
        imem_awready := false.B
        imem_wready := false.B
        imem_bresp := 0.U
        imem_bvalid := false.B
    }

    def DefaultDmem(): Unit = {
        dmem_arready := true.B
        // dmem_rdata := 0.U //in IDLE, last time rdata has to hold, not to flush to 0 
        dmem_rresp := 0.U
        dmem_rvalid := false.B
        dmem_awready := true.B
        dmem_wready := true.B
        dmem_bresp := 0.U
        dmem_bvalid := false.B
    }




}
