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

    val ing_w_or_r = RegInit(false.B)
    dontTouch(ing_w_or_r)
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
    // val isimem_req_soc = io.imem.arvalid === true.B



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
            ing_w_or_r := false.B
        }
        is(s_i_soc){
            ConnectImem2Soc()

            when(io.soc.arvalid & io.soc.arready){
                soc_arvalid := false.B
                ing_w_or_r := true.B
            }.elsewhen(io.soc.arvalid & ~io.soc.arready){
                soc_arvalid := true.B
                soc_araddr := imem_araddr
                soc_arburst := imem_arburst
                soc_arlen := imem_arlen
                soc_arsize := imem_arsize
                ing_w_or_r := false.B
            }
        }
        is(s_d_soc){
            ConnectDmem2Soc()

            when(io.soc.arvalid & io.soc.arready){
                soc_arvalid := false.B
                ing_w_or_r := true.B
            }.elsewhen(io.soc.arvalid & ~io.soc.arready){
                soc_arvalid := true.B
                soc_araddr := dmem_araddr
                ing_w_or_r := false.B
            }
            when(io.soc.awvalid & io.soc.awready){
                soc_awvalid := false.B
                ing_w_or_r := true.B
            }.elsewhen(io.soc.awvalid & ~io.soc.awready){
                soc_awvalid := true.B
                soc_awaddr := dmem_awaddr
                ing_w_or_r := false.B
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

    io.dmem.arready := Mux(c_state === s_IDLE, true.B, Mux(c_state === s_d_soc, io.soc.arready, false.B))
    io.dmem.awready := Mux(c_state === s_IDLE, true.B, Mux(c_state === s_d_soc, io.soc.awready, false.B))
    io.dmem.wready  := Mux(c_state === s_IDLE, false.B, Mux(c_state === s_d_soc, io.soc.wready, false.B))

    io.imem.arready := Mux(c_state === s_IDLE, ~(io.dmem.arvalid | io.dmem.awvalid), Mux(c_state === s_i_soc, io.soc.arready, false.B))
    io.imem.awready := Mux(c_state === s_IDLE, false.B, Mux(c_state === s_i_soc, io.soc.awready, false.B))
    io.imem.wready  := Mux(c_state === s_IDLE, false.B, Mux(c_state === s_i_soc, io.soc.wready, false.B))


/*-----------------------function-----------------------*/
    def ConnectImem2Soc(): Unit = {
        // io.imem.arready := io.soc.arready & ~ing_w_or_r
        io.imem.rdata := io.soc.rdata
        io.imem.rresp := io.soc.rresp
        io.imem.rvalid := io.soc.rvalid
        io.imem.rlast := io.soc.rlast
        io.imem.rid := io.soc.rid
        // io.imem.awready := io.soc.awready & ~ing_w_or_r
        // io.imem.wready := io.soc.wready
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
        // io.dmem.arready := io.soc.arready & ~ing_w_or_r
        io.dmem.rdata := io.soc.rdata
        io.dmem.rresp := io.soc.rresp
        io.dmem.rvalid := io.soc.rvalid
        io.dmem.rlast := io.soc.rlast
        io.dmem.rid := io.soc.rid
        // io.dmem.awready := io.soc.awready & ~ing_w_or_r
        // io.dmem.wready := io.soc.wready
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
        // io.imem.arready := true.B
        io.imem.rdata := DontCare
        io.imem.rresp := 0.U
        io.imem.rvalid := false.B
        io.imem.rlast := true.B
        io.imem.rid := 0.U
        // io.imem.awready := false.B
        io.imem.wready := false.B
        io.imem.bresp := 0.U
        io.imem.bvalid := false.B
        io.imem.bid := 0.U
    }

    def DefaultDmem(): Unit = {
        // io.dmem.arready := true.B
        io.dmem.rdata := DontCare
        io.dmem.rresp := 0.U
        io.dmem.rvalid := false.B
        io.dmem.rlast := true.B
        io.dmem.rid := 0.U
        // io.dmem.awready := true.B
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
