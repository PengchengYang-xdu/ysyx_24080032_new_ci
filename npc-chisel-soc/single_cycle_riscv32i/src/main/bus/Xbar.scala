package npc.bus.xbar
 
import chisel3._
import chisel3.util._
import npc.bus.axi._
import npc.common.Config._

/*              This is the SOC version                        */
/*              combine Arbiter and Xbar together              */
class XbarIO extends Bundle{
    //imem ---> xbar
    //dmem ---> xbar
    val imem = new AXI4WithoutClk
    val dmem = new AXI4WithoutClk
    //xbar ---> soc
    //xbar ---> clint
    val soc = Flipped(new AXI4WithoutClk)
    val clint = Flipped(new AXI4WithoutClk)
}

class Xbar extends Module {
    val io = IO(new XbarIO)

    //imem reg
    val imem_arready = RegInit(true.B)
    val imem_rdata = RegInit(0.U)
    val imem_rresp = RegInit(0.U)
    val imem_rvalid = RegInit(false.B)
    val imem_rlast = RegInit(true.B)
    val imem_rid = RegInit(0.U)
    val imem_awready = RegInit(false.B)
    val imem_wready = RegInit(false.B)
    val imem_bresp = RegInit(0.U)
    val imem_bvalid = RegInit(false.B)
    val imem_bid = RegInit(0.U)
    io.imem.arready := imem_arready
    io.imem.rdata := imem_rdata
    io.imem.rresp := imem_rresp
    io.imem.rvalid := imem_rvalid
    io.imem.rlast := imem_rlast
    io.imem.rid := imem_rid
    io.imem.awready := imem_awready
    io.imem.wready := imem_wready
    io.imem.bresp := imem_bresp
    io.imem.bvalid := imem_bvalid
    io.imem.bid := imem_bid
    //dmem reg
    val dmem_arready = RegInit(true.B)
    val dmem_rdata = RegInit(0.U)
    val dmem_rresp = RegInit(0.U)
    val dmem_rvalid = RegInit(false.B)
    val dmem_rlast = RegInit(true.B)
    val dmem_rid = RegInit(0.U)
    val dmem_awready = RegInit(false.B)
    val dmem_wready = RegInit(false.B)
    val dmem_bresp = RegInit(0.U)
    val dmem_bvalid = RegInit(false.B)
    val dmem_bid = RegInit(0.U)
    io.dmem.arready := dmem_arready
    io.dmem.rdata := dmem_rdata
    io.dmem.rresp := dmem_rresp
    io.dmem.rvalid := dmem_rvalid
    io.dmem.rlast := dmem_rlast
    io.dmem.rid := dmem_rid
    io.dmem.awready := dmem_awready
    io.dmem.wready := dmem_wready
    io.dmem.bresp := dmem_bresp
    io.dmem.bvalid := dmem_bvalid
    io.dmem.bid := dmem_bid




    //soc reg
    val soc_araddr = RegInit(0.U)
    val soc_arvalid = RegInit(false.B)
    val soc_arid = RegInit(0.U)
    val soc_arlen = RegInit(0.U)
    val soc_arsize = RegInit(0.U)
    val soc_arburst = RegInit(0.U)
    val soc_rready = RegInit(false.B)
    val soc_awaddr = RegInit(0.U)
    val soc_awvalid = RegInit(false.B)
    val soc_awid = RegInit(0.U)
    val soc_awlen = RegInit(0.U)
    val soc_awsize = RegInit(0.U)
    val soc_awburst = RegInit(0.U)
    val soc_wdata = RegInit(0.U)
    val soc_wstrb = RegInit(0.U)
    val soc_wvalid = RegInit(false.B)
    val soc_wlast = RegInit(true.B)
    val soc_bready = RegInit(false.B)
    io.soc.araddr := soc_araddr
    io.soc.arvalid := soc_arvalid
    io.soc.arid := soc_arid
    io.soc.arlen := soc_arlen
    io.soc.arsize := soc_arsize
    io.soc.arburst := soc_arburst
    io.soc.rready := soc_rready
    io.soc.awaddr := soc_awaddr
    io.soc.awvalid := soc_awvalid
    io.soc.awid := soc_awid
    io.soc.awlen := soc_awlen
    io.soc.awsize := soc_awsize
    io.soc.awburst := soc_awburst
    io.soc.wdata := soc_wdata
    io.soc.wstrb := soc_wstrb
    io.soc.wvalid := soc_wvalid
    io.soc.wlast := soc_wlast
    io.soc.bready := soc_bready
    //clint reg
    val clint_araddr = RegInit(0.U)
    val clint_arvalid = RegInit(false.B)
    val clint_arid = RegInit(0.U)
    val clint_arlen = RegInit(0.U)
    val clint_arsize = RegInit(0.U)
    val clint_arburst = RegInit(0.U)
    val clint_rready = RegInit(false.B)
    val clint_awaddr = RegInit(0.U)
    val clint_awvalid = RegInit(false.B)
    val clint_awid = RegInit(0.U)
    val clint_awlen = RegInit(0.U)
    val clint_awsize = RegInit(0.U)
    val clint_awburst = RegInit(0.U)
    val clint_wdata = RegInit(0.U)
    val clint_wstrb = RegInit(0.U)
    val clint_wvalid = RegInit(false.B)
    val clint_wlast = RegInit(true.B)
    val clint_bready = RegInit(false.B)
    io.clint.araddr := clint_araddr
    io.clint.arvalid := clint_arvalid
    io.clint.arid := clint_arid
    io.clint.arlen := clint_arlen
    io.clint.arsize := clint_arsize
    io.clint.arburst := clint_arburst
    io.clint.rready := clint_rready
    io.clint.awaddr := clint_awaddr
    io.clint.awvalid := clint_awvalid
    io.clint.awid := clint_awid
    io.clint.awlen := clint_awlen
    io.clint.awsize := clint_awsize
    io.clint.awburst := clint_awburst
    io.clint.wdata := clint_wdata
    io.clint.wstrb := clint_wstrb
    io.clint.wvalid := clint_wvalid
    io.clint.wlast := clint_wlast
    io.clint.bready := clint_bready

/*-----------------------FSM-----------------------*/
    val s_IDLE :: s_soc_i_0 :: s_soc_i_1 :: s_soc_d_0 :: s_soc_d_1 :: s_clint_0 :: s_clint_1 :: Nil = Enum(7)
    val c_state = RegInit(s_IDLE)
    val n_state = WireDefault(c_state)
    dontTouch(n_state)

    val isclint_raddr = (io.dmem.araddr >= "h0200_0000".U(32.W) && io.dmem.araddr <= "h0200_ffff".U(32.W))
    val isclint_waddr = (io.dmem.awaddr >= "h0200_0000".U(32.W) && io.dmem.awaddr <= "h0200_ffff".U(32.W))

    val isimem_req = io.imem.arvalid & imem_arready
    val isdmem_req_r = io.dmem.arvalid & dmem_arready
    val isdmem_req_w = io.dmem.awvalid & dmem_awready & io.dmem.wvalid & dmem_wready
    val isdmem_req = (isdmem_req_r & !isclint_raddr) | (isdmem_req_w & !isclint_waddr)
    val isclint_req = (isdmem_req_r & isclint_raddr) | (isdmem_req_w & isclint_waddr)

    val soc_i_done0 = io.soc.rvalid & soc_rready
    val soc_d_done0 = (io.soc.rvalid & soc_rready) | (io.soc.bvalid & soc_bready)
    val clint_done0 = (io.clint.rvalid & clint_rready) | (io.clint.bvalid & clint_bready)
    val soc_i_done1 = imem_rvalid & io.imem.rready
    val soc_d_done1 = (dmem_rvalid & io.dmem.rready) | (dmem_bvalid & io.dmem.bready)
    val clint_done1 = (dmem_rvalid & io.dmem.rready) | (dmem_bvalid & io.dmem.bready)

    c_state := n_state//first phase

    n_state := MuxLookup(c_state, s_IDLE)(Seq(//second phase
        s_IDLE       ->  MuxCase(s_IDLE, Seq(
            isimem_req       ->    s_soc_i_0,
            isdmem_req       ->    s_soc_d_0,
            isclint_req      ->    s_clint_0
        )),
        s_soc_i_0    ->  Mux(soc_i_done0, s_soc_i_1, s_soc_i_0),
        s_soc_i_1    ->  Mux(soc_i_done1, s_IDLE, s_soc_i_1),
        s_soc_d_0    ->  Mux(soc_d_done0, s_soc_d_1, s_soc_d_0),
        s_soc_d_1    ->  Mux(soc_d_done1, s_IDLE, s_soc_d_1),
        s_clint_0    ->  Mux(clint_done0, s_clint_1, s_clint_0),
        s_clint_1    ->  Mux(clint_done1, s_IDLE, s_clint_1)
    ))

    switch(n_state){//third phase
        is(s_IDLE){
            DefaultImem()
            DefaultDmem()
            DefaultSoc()
            DefaultClint()
        }
        is(s_soc_i_0){
            ConnectImem2Soc()
            DefaultDmem()
            DefaultClint()
        }
        is(s_soc_i_1){
            ConnectImem2Soc()
            DefaultDmem()
            DefaultClint()
            soc_arvalid := false.B
            soc_rready := false.B
            soc_awvalid := false.B
            soc_wvalid := false.B
            soc_bready := false.B
        }
        is(s_soc_d_0){
            ConnectDmem2Soc()
            DefaultImem()
            DefaultClint()
        }
        is(s_soc_d_1){
            ConnectDmem2Soc()
            DefaultImem()
            DefaultClint()
            soc_arvalid := false.B
            soc_rready := false.B
            soc_awvalid := false.B
            soc_wvalid := false.B
            soc_bready := false.B
        }
        is(s_clint_0){
            ConnectDmem2Clint()
            DefaultImem()
            DefaultSoc()
        }
        is(s_clint_1){
            ConnectDmem2Clint()
            DefaultImem()
            DefaultSoc()
            clint_arvalid := false.B
            clint_rready := false.B
            clint_awvalid := false.B
            clint_wvalid := false.B
            clint_bready := false.B
        }
    }

/*-----------------------function-----------------------*/
    def ConnectImem2Soc(): Unit = {
        imem_arready := io.soc.arready
        imem_rdata := io.soc.rdata
        imem_rresp := io.soc.rresp
        imem_rvalid := io.soc.rvalid
        imem_rlast := io.soc.rlast
        imem_rid := io.soc.rid
        imem_awready := io.soc.awready
        imem_wready := io.soc.wready
        imem_bresp := io.soc.bresp
        imem_bvalid := io.soc.bvalid
        imem_bid := io.soc.bid

        soc_araddr := io.imem.araddr
        soc_arvalid := io.imem.arvalid
        soc_arid := io.imem.arid
        soc_arlen := io.imem.arlen
        soc_arsize := io.imem.arsize
        soc_arburst := io.imem.arburst
        soc_rready := io.imem.rready
        soc_awaddr := io.imem.awaddr
        soc_awvalid := io.imem.awvalid
        soc_awid := io.imem.awid
        soc_awlen := io.imem.awlen
        soc_awsize := io.imem.awsize
        soc_awburst := io.imem.awburst
        soc_wdata := io.imem.wdata
        soc_wstrb := io.imem.wstrb
        soc_wvalid := io.imem.wvalid
        soc_wlast := io.imem.wlast
        soc_bready := io.imem.bready
    }

    def ConnectDmem2Soc(): Unit = {
        dmem_arready := io.soc.arready
        dmem_rdata := io.soc.rdata
        dmem_rresp := io.soc.rresp
        dmem_rvalid := io.soc.rvalid
        dmem_rlast := io.soc.rlast
        dmem_rid := io.soc.rid
        dmem_awready := io.soc.awready
        dmem_wready := io.soc.wready
        dmem_bresp := io.soc.bresp
        dmem_bvalid := io.soc.bvalid
        dmem_bid := io.soc.bid

        soc_araddr := io.dmem.araddr
        soc_arvalid := io.dmem.arvalid
        soc_arid := io.dmem.arid
        soc_arlen := io.dmem.arlen
        soc_arsize := io.dmem.arsize
        soc_arburst := io.dmem.arburst
        soc_rready := io.dmem.rready
        soc_awaddr := io.dmem.awaddr
        soc_awvalid := io.dmem.awvalid
        soc_awid := io.dmem.awid
        soc_awlen := io.dmem.awlen
        soc_awsize := io.dmem.awsize
        soc_awburst := io.dmem.awburst
        soc_wdata := io.dmem.wdata
        soc_wstrb := io.dmem.wstrb
        soc_wvalid := io.dmem.wvalid
        soc_wlast := io.dmem.wlast
        soc_bready := io.dmem.bready
    }

    def ConnectDmem2Clint(): Unit = {
        dmem_arready := io.clint.arready
        dmem_rdata := io.clint.rdata
        dmem_rresp := io.clint.rresp
        dmem_rvalid := io.clint.rvalid
        dmem_rlast := io.clint.rlast
        dmem_rid := io.clint.rid
        dmem_awready := io.clint.awready
        dmem_wready := io.clint.wready
        dmem_bresp := io.clint.bresp
        dmem_bvalid := io.clint.bvalid
        dmem_bid := io.clint.bid

        clint_araddr := io.dmem.araddr
        clint_arvalid := io.dmem.arvalid
        clint_arid := io.dmem.arid
        clint_arlen := io.dmem.arlen
        clint_arsize := io.dmem.arsize
        clint_arburst := io.dmem.arburst
        clint_rready := io.dmem.rready
        clint_awaddr := io.dmem.awaddr
        clint_awvalid := io.dmem.awvalid
        clint_awid := io.dmem.awid
        clint_awlen := io.dmem.awlen
        clint_awsize := io.dmem.awsize
        clint_awburst := io.dmem.awburst
        clint_wdata := io.dmem.wdata
        clint_wstrb := io.dmem.wstrb
        clint_wvalid := io.dmem.wvalid
        clint_wlast := io.dmem.wlast
        clint_bready := io.dmem.bready
    }

    def DefaultImem(): Unit = {
        imem_arready := true.B
        // imem_rdata := 0.U
        imem_rresp := 0.U
        imem_rvalid := false.B
        imem_rlast := true.B
        imem_rid := 0.U
        imem_awready := false.B
        imem_wready := false.B
        imem_bresp := 0.U
        imem_bvalid := false.B
        imem_bid := 0.U
    }

    def DefaultDmem(): Unit = {
        dmem_arready := true.B
        // dmem_rdata := 0.U
        dmem_rresp := 0.U
        dmem_rvalid := false.B
        dmem_rlast := true.B
        dmem_rid := 0.U
        dmem_awready := true.B
        dmem_wready := true.B
        dmem_bresp := 0.U
        dmem_bvalid := false.B
        dmem_bid := 0.U
    }

    def DefaultSoc(): Unit = {
        soc_araddr := 0.U
        soc_arvalid := false.B
        soc_arid := 0.U
        soc_arlen := 0.U
        soc_arsize := 0.U
        soc_arburst := 0.U
        soc_rready := false.B
        soc_awaddr := 0.U
        soc_awvalid := false.B
        soc_awid := 0.U
        soc_awlen := 0.U
        soc_awsize := 0.U
        soc_awburst := 0.U
        soc_wdata := 0.U
        soc_wstrb := 0.U
        soc_wvalid := false.B
        soc_wlast := true.B
        soc_bready := false.B
    }

    def DefaultClint(): Unit = {
        clint_araddr := 0.U
        clint_arvalid := false.B
        clint_arid := 0.U
        clint_arlen := 0.U
        clint_arsize := 0.U
        clint_arburst := 0.U
        clint_rready := false.B
        clint_awaddr := 0.U
        clint_awvalid := false.B
        clint_awid := 0.U
        clint_awlen := 0.U
        clint_awsize := 0.U
        clint_awburst := 0.U
        clint_wdata := 0.U
        clint_wstrb := 0.U
        clint_wvalid := false.B
        clint_wlast := true.B
        clint_bready := false.B
    }



// /*-----------------------FSM-----------------------*/
//     val s_IDLE :: s_soc_i :: s_soc_d :: s_clint :: Nil = Enum(4)
//     val c_state = RegInit(s_IDLE)
//     val n_state = WireDefault(c_state)
//     dontTouch(n_state)

//     val isclint_r = (io.dmem.araddr >= "h0200_0000".U(32.W) && io.dmem.araddr <= "h0200_ffff".U(32.W))
//     val isclint_w = (io.dmem.awaddr >= "h0200_0000".U(32.W) && io.dmem.awaddr <= "h0200_ffff".U(32.W))
//     val isclint = isclint_r | isclint_w

//     // val w_req = io.arb.awvalid & arb_awready
//     // val r_req = io.arb.arvalid & arb_arready
//     // val req = w_req | r_req
//     // //fire signal 
//     // val sram_done0 = (io.sram.rvalid & sram_rready) | (io.sram.bvalid & sram_bready)
//     // val sram_done1 = (arb_rvalid & io.arb.rready) | (arb_bvalid & io.arb.bready)
//     // val uart_done0 = (io.uart.rvalid & uart_rready) | (io.uart.bvalid & uart_bready)
//     // val uart_done1 = (arb_rvalid & io.arb.rready) | (arb_bvalid & io.arb.bready)
//     // val clint_done0 = (io.clint.rvalid & clint_rready) | (io.clint.bvalid & clint_bready)
//     // val clint_done1 = (arb_rvalid & io.arb.rready) | (arb_bvalid & io.arb.bready)
//     // //read channel
//     // val r_sram_bound = io.arb.araddr >= "h8000_0000".U(WORD_LEN.W) && io.arb.araddr <= "h87ff_ffff".U(WORD_LEN.W)
//     // val r_uart_bound = io.arb.araddr === "ha000_03f8".U(WORD_LEN.W)
//     // val r_clint_bound = io.arb.araddr === "ha000_0048".U(32.W) || io.arb.araddr === "ha000_004c".U(32.W)
//     // //write channel
//     // val w_sram_bound = io.arb.awaddr >= "h8000_0000".U(WORD_LEN.W) && io.arb.awaddr <= "h87ff_ffff".U(WORD_LEN.W)
//     // val w_uart_bound = io.arb.awaddr === "ha000_03f8".U(WORD_LEN.W)
//     // val w_clint_bound = io.arb.awaddr === "ha000_0048".U(32.W) || io.arb.awaddr === "ha000_004c".U(32.W)
//     // //trigger
//     // val sw = MuxCase(s_outofbound, Seq(
//     //     w_sram_bound  -> s_sram_0,
//     //     w_uart_bound  -> s_uart_0,
//     //     w_clint_bound -> s_clint_0
//     // ))
//     // val sr = MuxCase(s_outofbound, Seq(
//     //     r_sram_bound  -> s_sram_0,
//     //     r_uart_bound  -> s_uart_0,
//     //     r_clint_bound -> s_clint_0
//     // ))

//     c_state := n_state//first phase

//     n_state := MuxLookup(c_state, s_IDLE)(Seq(//second phase
//         s_IDLE       ->  MuxCase(s_IDLE, Seq(
//             io.imem.arvalid                                                      ->    s_soc_i,
//             ((io.dmem.arvalid & !isclint_r) | (io.dmem.awvalid & !isclint_w))    ->    s_soc_d,
//             ((io.dmem.arvalid & isclint_r) | (io.dmem.awvalid & isclint_w))      ->    s_clint
//         )),
//         s_soc_i      ->  Mux(sram_done0, s_sram_1, s_sram_0),
//     ))

//     switch(n_state){//third phase
//         is(s_IDLE){
//             DefaultArb()
//             DefaultSram()
//             DefaultUart()
//             DefaultClint()
//         }
//         is(s_outofbound){
//             DefaultArb()
//             DefaultSram()
//             DefaultUart()
//             DefaultClint()
//             arb_bresp := Mux(sw === s_outofbound, 1.U, 0.U)
//             arb_rresp := Mux(sr === s_outofbound, 1.U, 0.U)
//         }
//         is(s_sram_0){
//             ConnectSram()
//             DefaultUart()
//             DefaultClint()
//         }
//         is(s_sram_1){
//             ConnectSram()
//             DefaultUart()
//             DefaultClint()
//             sram_arvalid := false.B
//             sram_rready := false.B
//             sram_awvalid := false.B
//             sram_wvalid := false.B
//             sram_bready := false.B
//         }
//         is(s_uart_0){
//             ConnectUart()
//             DefaultSram()
//             DefaultClint()
//         }
//         is(s_uart_1){
//             ConnectUart()
//             DefaultSram()
//             DefaultClint()
//             uart_arvalid := false.B
//             uart_rready := false.B
//             uart_awvalid := false.B
//             uart_wvalid := false.B
//             uart_bready := false.B
//         }
//         is(s_clint_0){
//             ConnectClint()
//             DefaultSram()
//             DefaultUart()
//         }
//         is(s_clint_1){
//             ConnectClint()
//             DefaultSram()
//             DefaultUart()
//             clint_arvalid := false.B
//             clint_rready := false.B
//             clint_awvalid := false.B
//             clint_wvalid := false.B
//             clint_bready := false.B
//         }
//     }



// /*-----------------------function-----------------------*/
//     def ConnectSram(): Unit = {
//         arb_arready := io.sram.arready
//         arb_rdata := io.sram.rdata
//         arb_rresp := io.sram.rresp
//         arb_rvalid := io.sram.rvalid
//         arb_awready := io.sram.awready
//         arb_wready := io.sram.wready
//         arb_bresp := io.sram.bresp
//         arb_bvalid := io.sram.bvalid

//         sram_araddr := io.arb.araddr
//         sram_arvalid := io.arb.arvalid
//         sram_rready := io.arb.rready
//         sram_awaddr := io.arb.awaddr
//         sram_awvalid := io.arb.awvalid
//         sram_wdata := io.arb.wdata
//         sram_wstrb := io.arb.wstrb
//         sram_wvalid := io.arb.wvalid
//         sram_bready := io.arb.bready
//     }

//     def ConnectUart(): Unit = {
//         arb_arready := io.uart.arready
//         arb_rdata := io.uart.rdata
//         arb_rresp := io.uart.rresp
//         arb_rvalid := io.uart.rvalid
//         arb_awready := io.uart.awready
//         arb_wready := io.uart.wready
//         arb_bresp := io.uart.bresp
//         arb_bvalid := io.uart.bvalid

//         uart_araddr := io.arb.araddr
//         uart_arvalid := io.arb.arvalid
//         uart_rready := io.arb.rready
//         uart_awaddr := io.arb.awaddr
//         uart_awvalid := io.arb.awvalid
//         uart_wdata := io.arb.wdata
//         uart_wstrb := io.arb.wstrb
//         uart_wvalid := io.arb.wvalid
//         uart_bready := io.arb.bready
//     }

//     def ConnectClint(): Unit = {
//         arb_arready := io.clint.arready
//         arb_rdata := io.clint.rdata
//         arb_rresp := io.clint.rresp
//         arb_rvalid := io.clint.rvalid
//         arb_awready := io.clint.awready
//         arb_wready := io.clint.wready
//         arb_bresp := io.clint.bresp
//         arb_bvalid := io.clint.bvalid

//         clint_araddr := io.arb.araddr
//         clint_arvalid := io.arb.arvalid
//         clint_rready := io.arb.rready
//         clint_awaddr := io.arb.awaddr
//         clint_awvalid := io.arb.awvalid
//         clint_wdata := io.arb.wdata
//         clint_wstrb := io.arb.wstrb
//         clint_wvalid := io.arb.wvalid
//         clint_bready := io.arb.bready
//     }

//     def DefaultSram(): Unit = {
//         sram_araddr := 0.U
//         sram_arvalid := false.B
//         sram_rready := false.B
//         sram_awaddr := 0.U
//         sram_awvalid := false.B
//         sram_wdata := 0.U
//         sram_wstrb := 0.U
//         sram_wvalid := false.B
//         sram_bready := false.B
//     }

//     def DefaultUart(): Unit = {
//         uart_araddr := 0.U
//         uart_arvalid := false.B
//         uart_rready := false.B
//         uart_awaddr := 0.U
//         uart_awvalid := false.B
//         uart_wdata := 0.U
//         uart_wstrb := 0.U
//         uart_wvalid := false.B
//         uart_bready := false.B
//     }

//     def DefaultClint(): Unit = {
//         clint_araddr := 0.U
//         clint_arvalid := false.B
//         clint_rready := false.B
//         clint_awaddr := 0.U
//         clint_awvalid := false.B
//         clint_wdata := 0.U
//         clint_wstrb := 0.U
//         clint_wvalid := false.B
//         clint_bready := false.B
//     }

//     def DefaultArb(): Unit = {
//         arb_arready := true.B
//         // arb_rdata := 0.U
//         arb_rresp := 0.U
//         arb_rvalid := false.B
//         arb_awready := true.B
//         arb_wready := true.B
//         arb_bresp := 0.U
//         arb_bvalid := false.B
//     }
}
