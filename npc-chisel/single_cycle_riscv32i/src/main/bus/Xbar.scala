package npc.bus.xbar

import chisel3._
import chisel3.util._
import npc.bus.axi._
import npc.common.Config._

class XbarIO extends Bundle{
    //arb ---> xbar
    val arb = new AXI_liteWithoutClk
    //xbar ---> sram
    //xbar ---> uart
    //xbar ---> clint
    val sram = Flipped(new AXI_liteWithoutClk)
    val uart = Flipped(new AXI_liteWithoutClk)
    val clint = Flipped(new AXI_liteWithoutClk)
}

class Xbar extends Module {
    val io = IO(new XbarIO)

    //arb reg
    val arb_arready = RegInit(true.B)
    val arb_rdata = RegInit(0.U)
    val arb_rresp = RegInit(0.U)
    val arb_rvalid = RegInit(false.B)
    val arb_awready = RegInit(false.B)
    val arb_wready = RegInit(false.B)
    val arb_bresp = RegInit(0.U)
    val arb_bvalid = RegInit(false.B)
    io.arb.arready := arb_arready
    io.arb.rdata := arb_rdata
    io.arb.rresp := arb_rresp
    io.arb.rvalid := arb_rvalid
    io.arb.awready := arb_awready
    io.arb.wready := arb_wready
    io.arb.bresp := arb_bresp
    io.arb.bvalid := arb_bvalid
    //uart reg
    val uart_araddr = RegInit(0.U)
    val uart_arvalid = RegInit(false.B)
    val uart_rready = RegInit(false.B)
    val uart_awaddr = RegInit(0.U)
    val uart_awvalid = RegInit(false.B)
    val uart_wdata = RegInit(0.U)
    val uart_wstrb = RegInit(0.U)
    val uart_wvalid = RegInit(false.B)
    val uart_bready = RegInit(false.B)
    io.uart.araddr := uart_araddr
    io.uart.arvalid := uart_arvalid
    io.uart.rready := uart_rready
    io.uart.awaddr := uart_awaddr
    io.uart.awvalid := uart_awvalid
    io.uart.wdata := uart_wdata
    io.uart.wstrb := uart_wstrb
    io.uart.wvalid := uart_wvalid
    io.uart.bready := uart_bready
    //sram reg
    val sram_araddr = RegInit(0.U)
    val sram_arvalid = RegInit(false.B)
    val sram_rready = RegInit(false.B)
    val sram_awaddr = RegInit(0.U)
    val sram_awvalid = RegInit(false.B)
    val sram_wdata = RegInit(0.U)
    val sram_wstrb = RegInit(0.U)
    val sram_wvalid = RegInit(false.B)
    val sram_bready = RegInit(false.B)
    io.sram.araddr := sram_araddr
    io.sram.arvalid := sram_arvalid
    io.sram.rready := sram_rready
    io.sram.awaddr := sram_awaddr
    io.sram.awvalid := sram_awvalid
    io.sram.wdata := sram_wdata
    io.sram.wstrb := sram_wstrb
    io.sram.wvalid := sram_wvalid
    io.sram.bready := sram_bready
    //clint reg
    val clint_araddr = RegInit(0.U)
    val clint_arvalid = RegInit(false.B)
    val clint_rready = RegInit(false.B)
    val clint_awaddr = RegInit(0.U)
    val clint_awvalid = RegInit(false.B)
    val clint_wdata = RegInit(0.U)
    val clint_wstrb = RegInit(0.U)
    val clint_wvalid = RegInit(false.B)
    val clint_bready = RegInit(false.B)
    io.clint.araddr := clint_araddr
    io.clint.arvalid := clint_arvalid
    io.clint.rready := clint_rready
    io.clint.awaddr := clint_awaddr
    io.clint.awvalid := clint_awvalid
    io.clint.wdata := clint_wdata
    io.clint.wstrb := sram_wstrb
    io.clint.wvalid := clint_wvalid
    io.clint.bready := clint_bready

/*-----------------------FSM-----------------------*/
    val s_IDLE :: s_outofbound :: s_sram_0 :: s_sram_1 :: s_uart_0 :: s_uart_1 :: s_clint_0 :: s_clint_1 :: Nil = Enum(8)
    val c_state = RegInit(s_IDLE)
    val n_state = WireDefault(c_state)
    dontTouch(n_state)

    val w_req = io.arb.awvalid & arb_awready
    val r_req = io.arb.arvalid & arb_arready
    val req = w_req | r_req
    //fire signal 
    val sram_done0 = (io.sram.rvalid & sram_rready) | (io.sram.bvalid & sram_bready)
    val sram_done1 = (arb_rvalid & io.arb.rready) | (arb_bvalid & io.arb.bready)
    val uart_done0 = (io.uart.rvalid & uart_rready) | (io.uart.bvalid & uart_bready)
    val uart_done1 = (arb_rvalid & io.arb.rready) | (arb_bvalid & io.arb.bready)
    val clint_done0 = (io.clint.rvalid & clint_rready) | (io.clint.bvalid & clint_bready)
    val clint_done1 = (arb_rvalid & io.arb.rready) | (arb_bvalid & io.arb.bready)
    //read channel
    val r_sram_bound = io.arb.araddr >= "h8000_0000".U(WORD_LEN.W) && io.arb.araddr <= "h87ff_ffff".U(WORD_LEN.W)
    val r_uart_bound = io.arb.araddr === "ha000_03f8".U(WORD_LEN.W)
    val r_clint_bound = io.arb.araddr === "ha000_0048".U(32.W) || io.arb.araddr === "ha000_004c".U(32.W)
    //write channel
    val w_sram_bound = io.arb.awaddr >= "h8000_0000".U(WORD_LEN.W) && io.arb.awaddr <= "h87ff_ffff".U(WORD_LEN.W)
    val w_uart_bound = io.arb.awaddr === "ha000_03f8".U(WORD_LEN.W)
    val w_clint_bound = io.arb.awaddr === "ha000_0048".U(32.W) || io.arb.awaddr === "ha000_004c".U(32.W)
    //trigger
    val sw = MuxCase(s_outofbound, Seq(
        w_sram_bound  -> s_sram_0,
        w_uart_bound  -> s_uart_0,
        w_clint_bound -> s_clint_0
    ))
    val sr = MuxCase(s_outofbound, Seq(
        r_sram_bound  -> s_sram_0,
        r_uart_bound  -> s_uart_0,
        r_clint_bound -> s_clint_0
    ))

    c_state := n_state//first phase

    n_state := MuxLookup(c_state, s_IDLE)(Seq(//second phase
        s_IDLE       ->  Mux(req, Mux(w_req, sw, sr), s_IDLE),
        s_sram_0     ->  Mux(sram_done0, s_sram_1, s_sram_0),
        s_sram_1     ->  Mux(sram_done1, s_IDLE, s_sram_1),
        s_uart_0     ->  Mux(uart_done0, s_uart_1, s_uart_0),
        s_uart_1     ->  Mux(uart_done1, s_IDLE, s_uart_1),
        s_clint_0    ->  Mux(clint_done0, s_clint_1, s_clint_0),
        s_clint_1    ->  Mux(clint_done1, s_IDLE, s_clint_1)
    ))

    switch(n_state){//third phase
        is(s_IDLE){
            DefaultArb()
            DefaultSram()
            DefaultUart()
            DefaultClint()
        }
        is(s_outofbound){
            DefaultArb()
            DefaultSram()
            DefaultUart()
            DefaultClint()
            arb_bresp := Mux(sw === s_outofbound, 1.U, 0.U)
            arb_rresp := Mux(sr === s_outofbound, 1.U, 0.U)
        }
        is(s_sram_0){
            ConnectSram()
            DefaultUart()
            DefaultClint()
        }
        is(s_sram_1){
            ConnectSram()
            DefaultUart()
            DefaultClint()
            sram_arvalid := false.B
            sram_rready := false.B
            sram_awvalid := false.B
            sram_wvalid := false.B
            sram_bready := false.B
        }
        is(s_uart_0){
            ConnectUart()
            DefaultSram()
            DefaultClint()
        }
        is(s_uart_1){
            ConnectUart()
            DefaultSram()
            DefaultClint()
            uart_arvalid := false.B
            uart_rready := false.B
            uart_awvalid := false.B
            uart_wvalid := false.B
            uart_bready := false.B
        }
        is(s_clint_0){
            ConnectClint()
            DefaultSram()
            DefaultUart()
        }
        is(s_clint_1){
            ConnectClint()
            DefaultSram()
            DefaultUart()
            clint_arvalid := false.B
            clint_rready := false.B
            clint_awvalid := false.B
            clint_wvalid := false.B
            clint_bready := false.B
        }
    }



/*-----------------------function-----------------------*/
    def ConnectSram(): Unit = {
        arb_arready := io.sram.arready
        arb_rdata := io.sram.rdata
        arb_rresp := io.sram.rresp
        arb_rvalid := io.sram.rvalid
        arb_awready := io.sram.awready
        arb_wready := io.sram.wready
        arb_bresp := io.sram.bresp
        arb_bvalid := io.sram.bvalid

        sram_araddr := io.arb.araddr
        sram_arvalid := io.arb.arvalid
        sram_rready := io.arb.rready
        sram_awaddr := io.arb.awaddr
        sram_awvalid := io.arb.awvalid
        sram_wdata := io.arb.wdata
        sram_wstrb := io.arb.wstrb
        sram_wvalid := io.arb.wvalid
        sram_bready := io.arb.bready
    }

    def ConnectUart(): Unit = {
        arb_arready := io.uart.arready
        arb_rdata := io.uart.rdata
        arb_rresp := io.uart.rresp
        arb_rvalid := io.uart.rvalid
        arb_awready := io.uart.awready
        arb_wready := io.uart.wready
        arb_bresp := io.uart.bresp
        arb_bvalid := io.uart.bvalid

        uart_araddr := io.arb.araddr
        uart_arvalid := io.arb.arvalid
        uart_rready := io.arb.rready
        uart_awaddr := io.arb.awaddr
        uart_awvalid := io.arb.awvalid
        uart_wdata := io.arb.wdata
        uart_wstrb := io.arb.wstrb
        uart_wvalid := io.arb.wvalid
        uart_bready := io.arb.bready
    }

    def ConnectClint(): Unit = {
        arb_arready := io.clint.arready
        arb_rdata := io.clint.rdata
        arb_rresp := io.clint.rresp
        arb_rvalid := io.clint.rvalid
        arb_awready := io.clint.awready
        arb_wready := io.clint.wready
        arb_bresp := io.clint.bresp
        arb_bvalid := io.clint.bvalid

        clint_araddr := io.arb.araddr
        clint_arvalid := io.arb.arvalid
        clint_rready := io.arb.rready
        clint_awaddr := io.arb.awaddr
        clint_awvalid := io.arb.awvalid
        clint_wdata := io.arb.wdata
        clint_wstrb := io.arb.wstrb
        clint_wvalid := io.arb.wvalid
        clint_bready := io.arb.bready
    }

    def DefaultSram(): Unit = {
        sram_araddr := 0.U
        sram_arvalid := false.B
        sram_rready := false.B
        sram_awaddr := 0.U
        sram_awvalid := false.B
        sram_wdata := 0.U
        sram_wstrb := 0.U
        sram_wvalid := false.B
        sram_bready := false.B
    }

    def DefaultUart(): Unit = {
        uart_araddr := 0.U
        uart_arvalid := false.B
        uart_rready := false.B
        uart_awaddr := 0.U
        uart_awvalid := false.B
        uart_wdata := 0.U
        uart_wstrb := 0.U
        uart_wvalid := false.B
        uart_bready := false.B
    }

    def DefaultClint(): Unit = {
        clint_araddr := 0.U
        clint_arvalid := false.B
        clint_rready := false.B
        clint_awaddr := 0.U
        clint_awvalid := false.B
        clint_wdata := 0.U
        clint_wstrb := 0.U
        clint_wvalid := false.B
        clint_bready := false.B
    }

    def DefaultArb(): Unit = {
        arb_arready := true.B
        // arb_rdata := 0.U
        arb_rresp := 0.U
        arb_rvalid := false.B
        arb_awready := true.B
        arb_wready := true.B
        arb_bresp := 0.U
        arb_bvalid := false.B
    }


}
