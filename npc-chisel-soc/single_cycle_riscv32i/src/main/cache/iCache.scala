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

class iCacheBlock(val m: Int, val n: Int) extends Bundle{
    val valid = Bool()
    val tag = UInt((32 - m - n).W)
    val data = Vec((2 << (m - 1)) / 4, UInt(WORD_LEN.W))
}

class iCacheSet(val m: Int, val n: Int, val ways: Int, val ways_width: Int) extends Bundle{
    val set = Vec(ways, new iCacheBlock(m, n))
    val lruMatrix = Vec(ways, Vec(ways, UInt(1.W)))
    val fifoPtr = UInt(ways_width.W)
}

class iCache(val block_size: Int, val sets: Int, val ways: Int, val replacementPolicy: String) extends Module{
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

    val m = log2(block_size).toInt
    val n = log2(sets).toInt
    val w = math.ceil(log2(ways)).toInt
    val index_width = n
    val offset_width = m
    val tag_width = 32 - m - n
    val ways_width = w

    val req_index = Wire(UInt(index_width.W))
    req_index := io.in.araddr(m + n - 1, m)
    val req_offset = Wire(UInt(offset_width.W))
    req_offset := io.in.araddr(m - 1, 0)
    val req_tag = Wire(UInt(tag_width.W))
    req_tag := io.in.araddr(31, m + n)
    val addr_align = Wire(UInt(WORD_LEN.W))
    addr_align := io.in.araddr & "hfffffff0".U(WORD_LEN.W)
    dontTouch(req_index)
    dontTouch(req_offset)
    dontTouch(req_tag)
    dontTouch(addr_align)

    val icache = RegInit(VecInit(Seq.fill(sets)(0.U.asTypeOf(new iCacheSet(m, n, ways, ways_width)))))
    dontTouch(icache)

    /*-----------------------FSM-----------------------*/
    val s_IDLE :: s_icache_lookup :: s_i_0 :: s_i_1 :: s_i_2 :: Nil = Enum(5)
    val c_state = RegInit(s_IDLE)
    val n_state = WireDefault(c_state)
    dontTouch(n_state)

    val issdram_raddr = (io.in.araddr >= "ha000_0000".U(32.W) && io.in.araddr <= "hbfff_ffff".U(32.W))
    val isifu_rreq = io.in.arvalid & in_arready

    val ways_hit = Wire(Bool())
    ways_hit := false.B
    val ways_hit_num = Wire(UInt(ways_width.W))
    ways_hit_num := 0.U
    for (i <- 0 until ways) {
        when (icache(req_index).set(i).tag === req_tag && icache(req_index).set(i).valid === true.B) {
            ways_hit := true.B
            ways_hit_num := i.U
        }
    }

    val hit0 = RegEnable(ways_hit, n_state === s_icache_lookup)

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
            in_rvalid := ways_hit
            in_rdata := icache(req_index).set(ways_hit_num).data(req_offset >> 2)
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

    val policy = replacementPolicy.toUpperCase match {
        case "LRU" => "LRU"
        case "FIFO" => "FIFO"
        case "RANDOM" => "RANDOM"
        case _ => throw new Exception("Unknown replacement policy!")
    }

    //检查空闲的cache块
    val hasEmpty = Wire(Bool())
    hasEmpty := false.B
    val emptyIndex = RegInit(0.U(ways_width.W))
    for (i <- (ways - 1) to 0 by -1) {
        when(icache(req_index).set(i).valid === false.B) {
            hasEmpty := true.B
            emptyIndex := i.U
        }
    }

    //命中的时候更新LRU矩阵
    if(replacementPolicy == "LRU"){
        when(hit0){
            updateLRU(icache(req_index), ways_hit_num)
        }
    }

    when(c_state === s_i_2 && issdram_raddr){//替换或填充逻辑, 这里需要补充根据配置选择LRU或者FIFO或者RANDOM
        val set = icache(req_index).set

        when(hasEmpty === true.B) {
            // 如果有空闲块，填充
            set(emptyIndex).valid := true.B
            set(emptyIndex).tag := req_tag
            set(emptyIndex).data(req_offset >> 2) := icache_wdata
            //填充的时候更新LRU矩阵
            if(replacementPolicy == "LRU"){
                updateLRU(icache(req_index), emptyIndex)
            } else if(replacementPolicy == "FIFO"){
                icache(req_index).fifoPtr := (emptyIndex + 1.U) % ways.U
            }
        } .otherwise{
            // 如果没有空闲块，替换逻辑
            policy match {
                case "LRU" =>
                    val lruIndex = getLRUIndex(icache(req_index), ways_width)
                    set(lruIndex).valid := true.B
                    set(lruIndex).tag := req_tag
                    set(lruIndex).data(req_offset >> 2) := icache_wdata
                    //替换的时候更新LRU矩阵
                    updateLRU(icache(req_index), lruIndex)
                case "FIFO" =>
                    val fifoIndex = icache(req_index).fifoPtr
                    set(fifoIndex).valid := true.B
                    set(fifoIndex).tag := req_tag
                    set(fifoIndex).data(req_offset >> 2) := icache_wdata
                    //替换的时候更新FIFO指针
                    icache(req_index).fifoPtr := (fifoIndex + 1.U) % ways.U
                case "RANDOM" =>
                    val randomIndex = scala.util.Random.nextInt(ways)
                    set(randomIndex).valid := true.B
                    set(randomIndex).tag := req_tag
                    set(randomIndex).data(req_offset >> 2) := icache_wdata
            }
        }
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

   def updateLRU(set: iCacheSet, ways_hit_num: UInt): Unit = {
       val lruMatrix = set.lruMatrix
       for(j <- 0 until ways) {
           when(j.U =/= ways_hit_num){
               lruMatrix(ways_hit_num)(j) := 1.U
           }
       }
       for(i <- 0 until ways){
           lruMatrix(i)(ways_hit_num) := 0.U
       }
   }

   def getLRUIndex(set: iCacheSet, ways_width: Int): UInt = {
       val LRUIndex = Wire(UInt(ways_width.W))
       LRUIndex := 0.U
       val lruMatrix = set.lruMatrix
       for(i <- 0 until ways){
            val isZeroRow = (0 until ways).map(j => lruMatrix(i)(j) === 0.U).reduce(_ && _)
            when(isZeroRow){
                LRUIndex := i.U
            }
       }
       LRUIndex
   }

    
}