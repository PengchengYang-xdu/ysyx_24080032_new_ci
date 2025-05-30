// package npc.bus.xbar
 
// import chisel3._
// import chisel3.util._
// import npc.bus.axi._
// import npc.common.Config._

// /*              This is the SOC version                        */
// /*              combine Arbiter and Xbar together              */
// class XbarIO extends Bundle{
//     //imem ---> xbar
//     //dmem ---> xbar
//     val imem = new AXI4WithoutClk
//     val dmem = new AXI4WithoutClk
//     //xbar ---> soc
//     //xbar ---> clint
//     val soc = Flipped(new AXI4WithoutClk)
//     val clint = Flipped(new AXI4WithoutClk)
// }

// class Xbar extends Module {
//     val io = IO(new XbarIO)

//     //imem reg
//     val imem_arready = RegInit(false.B)
//     val imem_rdata = RegInit(0.U)
//     val imem_rresp = RegInit(0.U)
//     val imem_rvalid = RegInit(false.B)
//     val imem_rlast = RegInit(true.B)
//     val imem_rid = RegInit(0.U)
//     val imem_awready = RegInit(false.B)
//     val imem_wready = RegInit(false.B)
//     val imem_bresp = RegInit(0.U)
//     val imem_bvalid = RegInit(false.B)
//     val imem_bid = RegInit(0.U)
//     io.imem.arready := imem_arready
//     io.imem.rdata := imem_rdata
//     io.imem.rresp := imem_rresp
//     io.imem.rvalid := imem_rvalid
//     io.imem.rlast := imem_rlast
//     io.imem.rid := imem_rid
//     io.imem.awready := imem_awready
//     io.imem.wready := imem_wready
//     io.imem.bresp := imem_bresp
//     io.imem.bvalid := imem_bvalid
//     io.imem.bid := imem_bid
//     //dmem reg
//     val dmem_arready = RegInit(false.B)
//     val dmem_rdata = RegInit(0.U)
//     val dmem_rresp = RegInit(0.U)
//     val dmem_rvalid = RegInit(false.B)
//     val dmem_rlast = RegInit(true.B)
//     val dmem_rid = RegInit(0.U)
//     val dmem_awready = RegInit(false.B)
//     val dmem_wready = RegInit(false.B)
//     val dmem_bresp = RegInit(0.U)
//     val dmem_bvalid = RegInit(false.B)
//     val dmem_bid = RegInit(0.U)
//     io.dmem.arready := dmem_arready
//     io.dmem.rdata := dmem_rdata
//     io.dmem.rresp := dmem_rresp
//     io.dmem.rvalid := dmem_rvalid
//     io.dmem.rlast := dmem_rlast
//     io.dmem.rid := dmem_rid
//     io.dmem.awready := dmem_awready
//     io.dmem.wready := dmem_wready
//     io.dmem.bresp := dmem_bresp
//     io.dmem.bvalid := dmem_bvalid
//     io.dmem.bid := dmem_bid




//     //soc reg
//     val soc_araddr = RegInit(0.U)
//     val soc_arvalid = RegInit(false.B)
//     val soc_arid = RegInit(0.U)
//     val soc_arlen = RegInit(0.U)
//     val soc_arsize = RegInit(0.U)
//     val soc_arburst = RegInit(0.U)
//     val soc_rready = RegInit(false.B)
//     val soc_awaddr = RegInit(0.U)
//     val soc_awvalid = RegInit(false.B)
//     val soc_awid = RegInit(0.U)
//     val soc_awlen = RegInit(0.U)
//     val soc_awsize = RegInit(0.U)
//     val soc_awburst = RegInit(0.U)
//     val soc_wdata = RegInit(0.U)
//     val soc_wstrb = RegInit(0.U)
//     val soc_wvalid = RegInit(false.B)
//     val soc_wlast = RegInit(true.B)
//     val soc_bready = RegInit(false.B)
//     io.soc.araddr := soc_araddr
//     io.soc.arvalid := soc_arvalid
//     io.soc.arid := soc_arid
//     io.soc.arlen := soc_arlen
//     io.soc.arsize := soc_arsize
//     io.soc.arburst := soc_arburst
//     io.soc.rready := soc_rready
//     io.soc.awaddr := soc_awaddr
//     io.soc.awvalid := soc_awvalid
//     io.soc.awid := soc_awid
//     io.soc.awlen := soc_awlen
//     io.soc.awsize := soc_awsize
//     io.soc.awburst := soc_awburst
//     io.soc.wdata := soc_wdata
//     io.soc.wstrb := soc_wstrb
//     io.soc.wvalid := soc_wvalid
//     io.soc.wlast := soc_wlast
//     io.soc.bready := soc_bready
//     //clint reg
//     val clint_araddr = RegInit(0.U)
//     val clint_arvalid = RegInit(false.B)
//     val clint_arid = RegInit(0.U)
//     val clint_arlen = RegInit(0.U)
//     val clint_arsize = RegInit(0.U)
//     val clint_arburst = RegInit(0.U)
//     val clint_rready = RegInit(false.B)
//     val clint_awaddr = RegInit(0.U)
//     val clint_awvalid = RegInit(false.B)
//     val clint_awid = RegInit(0.U)
//     val clint_awlen = RegInit(0.U)
//     val clint_awsize = RegInit(0.U)
//     val clint_awburst = RegInit(0.U)
//     val clint_wdata = RegInit(0.U)
//     val clint_wstrb = RegInit(0.U)
//     val clint_wvalid = RegInit(false.B)
//     val clint_wlast = RegInit(true.B)
//     val clint_bready = RegInit(false.B)
//     io.clint.araddr := clint_araddr
//     io.clint.arvalid := clint_arvalid
//     io.clint.arid := clint_arid
//     io.clint.arlen := clint_arlen
//     io.clint.arsize := clint_arsize
//     io.clint.arburst := clint_arburst
//     io.clint.rready := clint_rready
//     io.clint.awaddr := clint_awaddr
//     io.clint.awvalid := clint_awvalid
//     io.clint.awid := clint_awid
//     io.clint.awlen := clint_awlen
//     io.clint.awsize := clint_awsize
//     io.clint.awburst := clint_awburst
//     io.clint.wdata := clint_wdata
//     io.clint.wstrb := clint_wstrb
//     io.clint.wvalid := clint_wvalid
//     io.clint.wlast := clint_wlast
//     io.clint.bready := clint_bready

// /*-----------------------FSM-----------------------*/
//     val s_IDLE :: s_soc_i_0 :: s_soc_i_1 :: s_soc_d_0 :: s_soc_d_1 :: s_clint_0 :: s_clint_1 :: Nil = Enum(7)
//     val c_state = RegInit(s_IDLE)
//     val n_state = WireDefault(c_state)
//     dontTouch(n_state)

//     val isclint_raddr = (io.dmem.araddr >= "h0200_0000".U(32.W) && io.dmem.araddr <= "h0200_ffff".U(32.W))
//     val isclint_waddr = (io.dmem.awaddr >= "h0200_0000".U(32.W) && io.dmem.awaddr <= "h0200_ffff".U(32.W))

//     val isimem_req = io.imem.arvalid & imem_arready
//     val isdmem_req_r = io.dmem.arvalid & dmem_arready
//     val isdmem_req_w = io.dmem.awvalid & dmem_awready & io.dmem.wvalid & dmem_wready
//     val isdmem_req = (isdmem_req_r & !isclint_raddr) | (isdmem_req_w & !isclint_waddr)
//     val isclint_req = (isdmem_req_r & isclint_raddr) | (isdmem_req_w & isclint_waddr)

//     val soc_i_done0 = io.soc.rvalid & soc_rready
//     val soc_d_done0 = (io.soc.rvalid & soc_rready) | (io.soc.bvalid & soc_bready)
//     val clint_done0 = (io.clint.rvalid & clint_rready) | (io.clint.bvalid & clint_bready)
//     val soc_i_done1 = imem_rvalid & io.imem.rready
//     val soc_d_done1 = (dmem_rvalid & io.dmem.rready) | (dmem_bvalid & io.dmem.bready)
//     val clint_done1 = (dmem_rvalid & io.dmem.rready) | (dmem_bvalid & io.dmem.bready)

//     c_state := n_state//first phase

//     n_state := MuxLookup(c_state, s_IDLE)(Seq(//second phase
//         s_IDLE       ->  MuxCase(s_IDLE, Seq(
//             isimem_req       ->    s_soc_i_0,
//             isdmem_req       ->    s_soc_d_0,
//             isclint_req      ->    s_clint_0
//         )),
//         s_soc_i_0    ->  Mux(soc_i_done0, s_soc_i_1, s_soc_i_0),
//         s_soc_i_1    ->  Mux(soc_i_done1, s_IDLE, s_soc_i_1),
//         s_soc_d_0    ->  Mux(soc_d_done0, s_soc_d_1, s_soc_d_0),
//         s_soc_d_1    ->  Mux(soc_d_done1, s_IDLE, s_soc_d_1),
//         s_clint_0    ->  Mux(clint_done0, s_clint_1, s_clint_0),
//         s_clint_1    ->  Mux(clint_done1, s_IDLE, s_clint_1)
//     ))

//     switch(n_state){//third phase
//         is(s_IDLE){
//             DefaultImem()
//             DefaultDmem()
//             DefaultSoc()
//             DefaultClint()
//         }
//         is(s_soc_i_0){
//             ConnectImem2Soc()
//             DefaultDmem()
//             DefaultClint()
//         }
//         is(s_soc_i_1){
//             ConnectImem2Soc()
//             DefaultDmem()
//             DefaultClint()
//             soc_arvalid := false.B
//             soc_rready := false.B
//             soc_awvalid := false.B
//             soc_wvalid := false.B
//             soc_bready := false.B
//         }
//         is(s_soc_d_0){
//             ConnectDmem2Soc()
//             DefaultImem()
//             DefaultClint()
//         }
//         is(s_soc_d_1){
//             ConnectDmem2Soc()
//             DefaultImem()
//             DefaultClint()
//             soc_arvalid := false.B
//             soc_rready := false.B
//             soc_awvalid := false.B
//             soc_wvalid := false.B
//             soc_bready := false.B
//         }
//         is(s_clint_0){
//             ConnectDmem2Clint()
//             DefaultImem()
//             DefaultSoc()
//         }
//         is(s_clint_1){
//             ConnectDmem2Clint()
//             DefaultImem()
//             DefaultSoc()
//             clint_arvalid := false.B
//             clint_rready := false.B
//             clint_awvalid := false.B
//             clint_wvalid := false.B
//             clint_bready := false.B
//         }
//     }

// /*-----------------------function-----------------------*/
//     def ConnectImem2Soc(): Unit = {
//         imem_arready := io.soc.arready
//         imem_rdata := io.soc.rdata
//         imem_rresp := io.soc.rresp
//         imem_rvalid := io.soc.rvalid
//         imem_rlast := io.soc.rlast
//         imem_rid := io.soc.rid
//         imem_awready := io.soc.awready
//         imem_wready := io.soc.wready
//         imem_bresp := io.soc.bresp
//         imem_bvalid := io.soc.bvalid
//         imem_bid := io.soc.bid

//         soc_araddr := io.imem.araddr
//         soc_arvalid := io.imem.arvalid
//         soc_arid := io.imem.arid
//         soc_arlen := io.imem.arlen
//         soc_arsize := io.imem.arsize
//         soc_arburst := io.imem.arburst
//         soc_rready := io.imem.rready
//         soc_awaddr := io.imem.awaddr
//         soc_awvalid := io.imem.awvalid
//         soc_awid := io.imem.awid
//         soc_awlen := io.imem.awlen
//         soc_awsize := io.imem.awsize
//         soc_awburst := io.imem.awburst
//         soc_wdata := io.imem.wdata
//         soc_wstrb := io.imem.wstrb
//         soc_wvalid := io.imem.wvalid
//         soc_wlast := io.imem.wlast
//         soc_bready := io.imem.bready
//     }

//     def ConnectDmem2Soc(): Unit = {
//         dmem_arready := io.soc.arready
//         dmem_rdata := io.soc.rdata
//         dmem_rresp := io.soc.rresp
//         dmem_rvalid := io.soc.rvalid
//         dmem_rlast := io.soc.rlast
//         dmem_rid := io.soc.rid
//         dmem_awready := io.soc.awready
//         dmem_wready := io.soc.wready
//         dmem_bresp := io.soc.bresp
//         dmem_bvalid := io.soc.bvalid
//         dmem_bid := io.soc.bid

//         soc_araddr := io.dmem.araddr
//         soc_arvalid := io.dmem.arvalid
//         soc_arid := io.dmem.arid
//         soc_arlen := io.dmem.arlen
//         soc_arsize := io.dmem.arsize
//         soc_arburst := io.dmem.arburst
//         soc_rready := io.dmem.rready
//         soc_awaddr := io.dmem.awaddr
//         soc_awvalid := io.dmem.awvalid
//         soc_awid := io.dmem.awid
//         soc_awlen := io.dmem.awlen
//         soc_awsize := io.dmem.awsize
//         soc_awburst := io.dmem.awburst
//         soc_wdata := io.dmem.wdata
//         soc_wstrb := io.dmem.wstrb
//         soc_wvalid := io.dmem.wvalid
//         soc_wlast := io.dmem.wlast
//         soc_bready := io.dmem.bready
//     }

//     def ConnectDmem2Clint(): Unit = {
//         dmem_arready := io.clint.arready
//         dmem_rdata := io.clint.rdata
//         dmem_rresp := io.clint.rresp
//         dmem_rvalid := io.clint.rvalid
//         dmem_rlast := io.clint.rlast
//         dmem_rid := io.clint.rid
//         dmem_awready := io.clint.awready
//         dmem_wready := io.clint.wready
//         dmem_bresp := io.clint.bresp
//         dmem_bvalid := io.clint.bvalid
//         dmem_bid := io.clint.bid

//         clint_araddr := io.dmem.araddr
//         clint_arvalid := io.dmem.arvalid
//         clint_arid := io.dmem.arid
//         clint_arlen := io.dmem.arlen
//         clint_arsize := io.dmem.arsize
//         clint_arburst := io.dmem.arburst
//         clint_rready := io.dmem.rready
//         clint_awaddr := io.dmem.awaddr
//         clint_awvalid := io.dmem.awvalid
//         clint_awid := io.dmem.awid
//         clint_awlen := io.dmem.awlen
//         clint_awsize := io.dmem.awsize
//         clint_awburst := io.dmem.awburst
//         clint_wdata := io.dmem.wdata
//         clint_wstrb := io.dmem.wstrb
//         clint_wvalid := io.dmem.wvalid
//         clint_wlast := io.dmem.wlast
//         clint_bready := io.dmem.bready
//     }

//     def DefaultImem(): Unit = {
//         imem_arready := true.B
//         // imem_rdata := 0.U
//         imem_rresp := 0.U
//         imem_rvalid := false.B
//         imem_rlast := true.B
//         imem_rid := 0.U
//         imem_awready := false.B
//         imem_wready := false.B
//         imem_bresp := 0.U
//         imem_bvalid := false.B
//         imem_bid := 0.U
//     }

//     def DefaultDmem(): Unit = {
//         dmem_arready := true.B
//         // dmem_rdata := 0.U
//         dmem_rresp := 0.U
//         dmem_rvalid := false.B
//         dmem_rlast := true.B
//         dmem_rid := 0.U
//         dmem_awready := true.B
//         dmem_wready := true.B
//         dmem_bresp := 0.U
//         dmem_bvalid := false.B
//         dmem_bid := 0.U
//     }

//     def DefaultSoc(): Unit = {
//         soc_araddr := 0.U
//         soc_arvalid := false.B
//         soc_arid := 0.U
//         soc_arlen := 0.U
//         soc_arsize := 0.U
//         soc_arburst := 0.U
//         soc_rready := false.B
//         soc_awaddr := 0.U
//         soc_awvalid := false.B
//         soc_awid := 0.U
//         soc_awlen := 0.U
//         soc_awsize := 0.U
//         soc_awburst := 0.U
//         soc_wdata := 0.U
//         soc_wstrb := 0.U
//         soc_wvalid := false.B
//         soc_wlast := true.B
//         soc_bready := false.B
//     }

//     def DefaultClint(): Unit = {
//         clint_araddr := 0.U
//         clint_arvalid := false.B
//         clint_arid := 0.U
//         clint_arlen := 0.U
//         clint_arsize := 0.U
//         clint_arburst := 0.U
//         clint_rready := false.B
//         clint_awaddr := 0.U
//         clint_awvalid := false.B
//         clint_awid := 0.U
//         clint_awlen := 0.U
//         clint_awsize := 0.U
//         clint_awburst := 0.U
//         clint_wdata := 0.U
//         clint_wstrb := 0.U
//         clint_wvalid := false.B
//         clint_wlast := true.B
//         clint_bready := false.B
//     }

// }


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
    val soc_rvalid_r = RegNext(io.soc.rvalid)
    val soc_rready_r = RegNext(io.soc.rready)
    val soc_bvalid_r = RegNext(io.soc.bvalid)
    val soc_bready_r = RegNext(io.soc.bready)
    val clint_rvalid_r = RegNext(io.clint.rvalid)
    val clint_rready_r = RegNext(io.clint.rready)
    val clint_bvalid_r = RegNext(io.clint.bvalid)
    val clint_bready_r = RegNext(io.clint.bready)

    val burstCnt = dontTouch(RegInit(0.U(8.W)))
/*-----------------------FSM-----------------------*/
    val s_IDLE :: s_i_soc :: s_d_soc :: s_d_clint :: Nil = Enum(4)
    val c_state = RegInit(s_IDLE)
    val n_state = WireDefault(c_state)
    dontTouch(n_state)

    val isclint_raddr = (io.dmem.araddr >= "h0200_0000".U(32.W) && io.dmem.araddr <= "h0200_ffff".U(32.W))
    val isclint_waddr = (io.dmem.awaddr >= "h0200_0000".U(32.W) && io.dmem.awaddr <= "h0200_ffff".U(32.W))

    val isdmem_req_r = io.dmem.arvalid === true.B
    val isdmem_req_w = io.dmem.awvalid === true.B
    val isdmem_req_soc = (isdmem_req_r & !isclint_raddr) | (isdmem_req_w & !isclint_waddr)
    val isdmem_req_clint = (isdmem_req_r & isclint_raddr) | (isdmem_req_w & isclint_waddr)
    val isimem_req_soc = Mux(io.imem.arvalid === true.B, Mux(isdmem_req_r || isdmem_req_w, false.B, true.B), false.B)



    val soc_i_done = ~io.soc.rvalid & soc_rvalid_r & soc_rready & burstCnt === 0.U
    val soc_d_done = (~io.soc.rvalid & soc_rvalid_r & soc_rready) | (soc_bvalid_r & soc_bready)
    val clint_d_done = (~io.clint.rvalid & clint_rvalid_r) | (~io.clint.bvalid & clint_bvalid_r)

    c_state := n_state//first phase

    n_state := MuxLookup(c_state, s_IDLE)(Seq(//second phase
        s_IDLE       ->  MuxCase(s_IDLE, Seq(
            isimem_req_soc        ->    s_i_soc,
            isdmem_req_soc        ->    s_d_soc,
            isdmem_req_clint      ->    s_d_clint
        )),
        s_i_soc    ->  Mux(soc_i_done, Mux(io.imem.arvalid, s_i_soc, s_IDLE), s_i_soc),
        s_d_soc    ->  Mux(soc_d_done, s_IDLE, s_d_soc),
        s_d_clint  ->  Mux(clint_d_done, s_IDLE, s_d_clint)
    ))

    DefaultImem()
    DefaultDmem()
    DefaultSoc()
    DefaultClint()

    val imem_araddr = RegEnable(io.imem.araddr, io.imem.arvalid)//由valid控制, 如果没有产生这个信号, 那么从设备接收到的addr是无效的, 是上次的垃圾信号, 不能作为握手
    val imem_arburst = RegEnable(io.imem.arburst, io.imem.arvalid)
    val imem_arlen = RegEnable(io.imem.arlen, io.imem.arvalid)
    val imem_arsize = RegEnable(io.imem.arsize, io.imem.arvalid)
    val dmem_araddr = RegEnable(io.dmem.araddr, io.dmem.arvalid)
    val dmem_awaddr = RegEnable(io.dmem.awaddr, io.dmem.awvalid)

    switch(n_state){//third phase
        is(s_IDLE){
        }
        is(s_i_soc){
            ConnectImem2Soc()
            when(io.soc.arvalid & io.soc.arready){
                soc_arvalid := false.B
            }.elsewhen(io.soc.arvalid & ~io.soc.arready){
                soc_arvalid := true.B
                soc_araddr := imem_araddr
                soc_arburst := imem_arburst
                soc_arlen := imem_arlen
                soc_arsize := imem_arsize
            }
        }
        is(s_d_soc){
            ConnectDmem2Soc()
            when(io.soc.arvalid & io.soc.arready){
                soc_arvalid := false.B
            }.elsewhen(io.soc.arvalid & ~io.soc.arready){
                soc_arvalid := true.B
                soc_araddr := dmem_araddr
            }
            when(io.soc.awvalid & io.soc.awready){
                soc_awvalid := false.B
            }.elsewhen(io.soc.awvalid & ~io.soc.awready){
                soc_awvalid := true.B
                soc_awaddr := dmem_awaddr
            }
        }
        is(s_d_clint){
            ConnectDmem2Clint()
        }
    }

    when(io.soc.arvalid & io.soc.arready){
        burstCnt := io.soc.arlen + 1.U
    }.elsewhen(burstCnt =/= 0.U & io.soc.rready & io.soc.rvalid){
        burstCnt := burstCnt - 1.U
    }

/*-----------------------function-----------------------*/
    def ConnectImem2Soc(): Unit = {
        io.imem.arready := io.soc.arready
        io.imem.rdata := io.soc.rdata
        io.imem.rresp := io.soc.rresp
        io.imem.rvalid := io.soc.rvalid
        io.imem.rlast := io.soc.rlast
        io.imem.rid := io.soc.rid
        io.imem.awready := io.soc.awready
        io.imem.wready := io.soc.wready
        io.imem.bresp := io.soc.bresp
        io.imem.bvalid := io.soc.bvalid
        io.imem.bid := io.soc.bid

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
        io.dmem.arready := io.soc.arready
        io.dmem.rdata := io.soc.rdata
        io.dmem.rresp := io.soc.rresp
        io.dmem.rvalid := io.soc.rvalid
        io.dmem.rlast := io.soc.rlast
        io.dmem.rid := io.soc.rid
        io.dmem.awready := io.soc.awready
        io.dmem.wready := io.soc.wready
        io.dmem.bresp := io.soc.bresp
        io.dmem.bvalid := io.soc.bvalid
        io.dmem.bid := io.soc.bid

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
        io.dmem.arready := io.clint.arready
        io.dmem.rdata := io.clint.rdata
        io.dmem.rresp := io.clint.rresp
        io.dmem.rvalid := io.clint.rvalid
        io.dmem.rlast := io.clint.rlast
        io.dmem.rid := io.clint.rid
        io.dmem.awready := io.clint.awready
        io.dmem.wready := io.clint.wready
        io.dmem.bresp := io.clint.bresp
        io.dmem.bvalid := io.clint.bvalid
        io.dmem.bid := io.clint.bid

        io.clint.araddr := io.dmem.araddr
        io.clint.arvalid := io.dmem.arvalid
        io.clint.arid := io.dmem.arid
        io.clint.arlen := io.dmem.arlen
        io.clint.arsize := io.dmem.arsize
        io.clint.arburst := io.dmem.arburst
        io.clint.rready := io.dmem.rready
        io.clint.awaddr := io.dmem.awaddr
        io.clint.awvalid := io.dmem.awvalid
        io.clint.awid := io.dmem.awid
        io.clint.awlen := io.dmem.awlen
        io.clint.awsize := io.dmem.awsize
        io.clint.awburst := io.dmem.awburst
        io.clint.wdata := io.dmem.wdata
        io.clint.wstrb := io.dmem.wstrb
        io.clint.wvalid := io.dmem.wvalid
        io.clint.wlast := io.dmem.wlast
        io.clint.bready := io.dmem.bready
    }

    def DefaultImem(): Unit = {
        io.imem.arready := true.B
        io.imem.rdata := DontCare
        io.imem.rresp := 0.U
        io.imem.rvalid := false.B
        io.imem.rlast := true.B
        io.imem.rid := 0.U
        io.imem.awready := false.B
        io.imem.wready := false.B
        io.imem.bresp := 0.U
        io.imem.bvalid := false.B
        io.imem.bid := 0.U
    }

    def DefaultDmem(): Unit = {
        io.dmem.arready := true.B
        io.dmem.rdata := DontCare
        io.dmem.rresp := 0.U
        io.dmem.rvalid := false.B
        io.dmem.rlast := true.B
        io.dmem.rid := 0.U
        io.dmem.awready := true.B
        io.dmem.wready := true.B
        io.dmem.bresp := 0.U
        io.dmem.bvalid := false.B
        io.dmem.bid := 0.U
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
        io.clint.araddr := 0.U
        io.clint.arvalid := false.B
        io.clint.arid := 0.U
        io.clint.arlen := 0.U
        io.clint.arsize := 0.U
        io.clint.arburst := 0.U
        io.clint.rready := false.B
        io.clint.awaddr := 0.U
        io.clint.awvalid := false.B
        io.clint.awid := 0.U
        io.clint.awlen := 0.U
        io.clint.awsize := 0.U
        io.clint.awburst := 0.U
        io.clint.wdata := 0.U
        io.clint.wstrb := 0.U
        io.clint.wvalid := false.B
        io.clint.wlast := true.B
        io.clint.bready := false.B
    }

}
