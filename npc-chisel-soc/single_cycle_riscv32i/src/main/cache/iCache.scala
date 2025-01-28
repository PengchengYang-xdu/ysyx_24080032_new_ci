package npc.core.icache

import chisel3._
import chisel3.util._
import scala.math._
import npc.common.Config._
import npc.common.Instructions._
import npc.bus.axi._

class iCacheIO extends Bundle {
    val in = new AXI4WithoutClk
    val out = Flipped(new AXI4WithoutClk)
}

//b = 4字节, k = 4个
class iCachePacIO(val m: Int, val n: Int) extends Bundle{
    val valid = Bool()
    val tag = UInt((32 - m - n).W)
    val data = Vec((2 << (m - 1)) / 4, UInt(WORD_LEN.W))
}

class iCache(val b: Int, val k: Int) extends Module{
    val io = IO(new iCacheIO)

    val in_arready = RegInit(true.B)
    val in_rdata = RegInit(0.U)
    val in_rresp = RegInit(0.U)
    val in_rvalid = RegInit(false.B)
    val in_rlast = RegInit(true.B)
    val in_rid = RegInit(0.U)
    val in_awready = RegInit(false.B)
    val in_wready = RegInit(false.B)
    val in_bresp = RegInit(0.U)
    val in_bvalid = RegInit(false.B)
    val in_bid = RegInit(0.U)
    io.in.arready := in_arready
    io.in.rdata := in_rdata
    io.in.rresp := in_rresp
    io.in.rvalid := in_rvalid
    io.in.rlast := in_rlast
    io.in.rid := in_rid
    io.in.awready := in_awready
    io.in.wready := in_wready
    io.in.bresp := in_bresp
    io.in.bvalid := in_bvalid
    io.in.bid := in_bid

    val out_araddr = RegInit(0.U)
    val out_arvalid = RegInit(false.B)
    val out_arid = RegInit(0.U)
    val out_arlen = RegInit(0.U)
    val out_arsize = RegInit(0.U)
    val out_arburst = RegInit(0.U)
    val out_rready = RegInit(false.B)
    val out_awaddr = RegInit(0.U)
    val out_awvalid = RegInit(false.B)
    val out_awid = RegInit(0.U)
    val out_awlen = RegInit(0.U)
    val out_awsize = RegInit(0.U)
    val out_awburst = RegInit(0.U)
    val out_wdata = RegInit(0.U)
    val out_wstrb = RegInit(0.U)
    val out_wvalid = RegInit(false.B)
    val out_wlast = RegInit(true.B)
    val out_bready = RegInit(false.B)
    io.out.araddr := out_araddr
    io.out.arvalid := out_arvalid
    io.out.arid := out_arid
    io.out.arlen := out_arlen
    io.out.arsize := out_arsize
    io.out.arburst := out_arburst
    io.out.rready := out_rready
    io.out.awaddr := out_awaddr
    io.out.awvalid := out_awvalid
    io.out.awid := out_awid
    io.out.awlen := out_awlen
    io.out.awsize := out_awsize
    io.out.awburst := out_awburst
    io.out.wdata := out_wdata
    io.out.wstrb := out_wstrb
    io.out.wvalid := out_wvalid
    io.out.wlast := out_wlast
    io.out.bready := out_bready

    val m = log2(b).toInt
    val n = log2(k).toInt
    val index_width = n
    val offset_width = m
    val tag_width = 32 - m - n

    val req_index = Wire(UInt(index_width.W))
    req_index := io.in.araddr(m + n - 1, m)
    val req_offset = Wire(UInt(offset_width.W))
    req_offset := io.in.araddr(m - 1, 0)
    val req_tag = Wire(UInt(tag_width.W))
    req_tag := io.in.araddr(31, m + n)
    dontTouch(req_index)
    dontTouch(req_offset)
    dontTouch(req_tag)

    val icache = RegInit(VecInit(Seq.fill(k)(0.U.asTypeOf(new iCachePacIO(m, n)))))
    dontTouch(icache)

    /*-----------------------FSM-----------------------*/
    val s_IDLE :: s_icache_lookup :: s_i_0 :: s_i_1 :: s_i_2 :: Nil = Enum(5)
    val c_state = RegInit(s_IDLE)
    val n_state = WireDefault(c_state)
    dontTouch(n_state)

    val issdram_raddr = (io.in.araddr >= "ha000_0000".U(32.W) && io.in.araddr <= "hbfff_ffff".U(32.W))
    val isifu_rreq = io.in.arvalid & in_arready

    val hit0 = RegEnable(icache(req_index).tag === req_tag, n_state === s_icache_lookup)

    val icache_wdata = RegInit(0.U(32.W))
    icache_wdata := Mux(n_state === s_i_2, io.out.rdata, icache_wdata)

    c_state := n_state//first phase

    n_state := MuxLookup(c_state, s_IDLE)(Seq(//second phase
        s_IDLE           ->  Mux(isifu_rreq, s_icache_lookup, s_IDLE),
        s_icache_lookup  ->  Mux(hit0, s_IDLE, s_i_0),
        s_i_0            ->  Mux(io.out.arready & out_arvalid, s_i_1, s_i_0),
        s_i_1            ->  Mux(io.out.rvalid & out_rready, s_i_2, s_i_1),
        s_i_2            ->  Mux(in_rvalid & io.in.rready, s_IDLE, s_i_2)
    ))

    switch(n_state){//third phase
        is(s_IDLE){
            DefaultIn()
            DefaultOut()
        }
        is(s_icache_lookup){
            in_arready := false.B
            in_rvalid := icache(req_index).tag === req_tag
            in_rdata := icache(req_index).data(req_offset >> 2)
        }
        is(s_i_0){
            ConnectIn2Out()
            out_arvalid := ~hit0
        }
        is(s_i_1){
            ConnectIn2Out()
            out_arvalid := false.B
            out_rready := true.B
        }
        is(s_i_2){
            ConnectIn2Out()
            out_rready := false.B
        }
    }

    when(c_state === s_i_2){
        icache(req_index).valid := true.B
        icache(req_index).tag := req_tag
        icache(req_index).data(req_offset >> 2) := icache_wdata
    }

/*-----------------------function-----------------------*/
    def log2(x: Int): Double = {
        math.log(x) / math.log(2)
    }

    def DefaultIn(): Unit = {
        in_arready := true.B
        // in_rdata := 0.U
        in_rresp := 0.U
        in_rvalid := false.B
        in_rlast := true.B
        in_rid := 0.U
        in_awready := false.B
        in_wready := false.B
        in_bresp := 0.U
        in_bvalid := false.B
        in_bid := 0.U
    }

    def DefaultOut(): Unit = {
        out_araddr := 0.U
        out_arvalid := false.B
        out_arid := 0.U
        out_arlen := 0.U
        out_arsize := 0.U
        out_arburst := 0.U
        out_rready := false.B
        out_awaddr := 0.U
        out_awvalid := false.B
        out_awid := 0.U
        out_awlen := 0.U
        out_awsize := 0.U
        out_awburst := 0.U
        out_wdata := 0.U
        out_wstrb := 0.U
        out_wvalid := false.B
        out_wlast := true.B
        out_bready := false.B
    }

    def ConnectIn2Out(): Unit = {
        in_arready := io.out.arready
        in_rdata := io.out.rdata
        in_rresp := io.out.rresp
        in_rvalid := io.out.rvalid
        in_rlast := io.out.rlast
        in_rid := io.out.rid
        in_awready := io.out.awready
        in_wready := io.out.wready
        in_bresp := io.out.bresp
        in_bvalid := io.out.bvalid
        in_bid := io.out.bid

        out_araddr := io.in.araddr
        out_arvalid := io.in.arvalid
        out_arid := io.in.arid
        out_arlen := io.in.arlen
        out_arsize := io.in.arsize
        out_arburst := io.in.arburst
        out_rready := io.in.rready
        out_awaddr := io.in.awaddr
        out_awvalid := io.in.awvalid
        out_awid := io.in.awid
        out_awlen := io.in.awlen
        out_awsize := io.in.awsize
        out_awburst := io.in.awburst
        out_wdata := io.in.wdata
        out_wstrb := io.in.wstrb
        out_wvalid := io.in.wvalid
        out_wlast := io.in.wlast
        out_bready := io.in.bready
    }
}