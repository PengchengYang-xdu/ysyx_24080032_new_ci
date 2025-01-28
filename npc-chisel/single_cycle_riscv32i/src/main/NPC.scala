package npc

import chisel3._
import chisel3.util._
import npc.core._
import npc.perip._
import npc.bus._

class NPCIO extends Bundle{
}

class NPC extends Module {
    val io = IO(new NPCIO)

    val core = Module(new Core)
    val arb = Module(new npc.bus.arbiter.Arbiter)
    val xbar = Module(new npc.bus.xbar.Xbar)
    val mem = Module(new npc.perip.Mem)
    val uart = Module(new npc.perip.Uart)
    val clint = Module(new npc.perip.Clint)
    
    //from chisel
    core.clock := clock
    core.reset := reset.asBool
    arb.clock := clock
    arb.reset := reset.asBool
    xbar.clock := clock
    xbar.reset := reset.asBool

    //from sv
    mem.io.clk := clock
    mem.io.rst := reset.asBool
    uart.io.clk := clock
    uart.io.rst := reset.asBool
    clint.io.clk := clock
    clint.io.rst := reset.asBool

    core.io.imem              <>    arb.io.imem
    core.io.dmem              <>    arb.io.dmem

    arb.io.mem                <>    xbar.io.arb

    xbar.io.sram.araddr       <>    mem.io.araddr
    xbar.io.sram.arvalid      <>    mem.io.arvalid
    xbar.io.sram.arready      <>    mem.io.arready
    xbar.io.sram.rdata        <>    mem.io.rdata
    xbar.io.sram.rresp        <>    mem.io.rresp
    xbar.io.sram.rvalid       <>    mem.io.rvalid
    xbar.io.sram.rready       <>    mem.io.rready
    xbar.io.sram.awaddr       <>    mem.io.awaddr
    xbar.io.sram.awvalid      <>    mem.io.awvalid
    xbar.io.sram.awready      <>    mem.io.awready
    xbar.io.sram.wdata        <>    mem.io.wdata
    xbar.io.sram.wstrb        <>    mem.io.wstrb
    xbar.io.sram.wvalid       <>    mem.io.wvalid
    xbar.io.sram.wready       <>    mem.io.wready
    xbar.io.sram.bresp        <>    mem.io.bresp
    xbar.io.sram.bvalid       <>    mem.io.bvalid
    xbar.io.sram.bready       <>    mem.io.bready

    xbar.io.uart.araddr       <>    uart.io.araddr
    xbar.io.uart.arvalid      <>    uart.io.arvalid
    xbar.io.uart.arready      <>    uart.io.arready
    xbar.io.uart.rdata        <>    uart.io.rdata
    xbar.io.uart.rresp        <>    uart.io.rresp
    xbar.io.uart.rvalid       <>    uart.io.rvalid
    xbar.io.uart.rready       <>    uart.io.rready
    xbar.io.uart.awaddr       <>    uart.io.awaddr
    xbar.io.uart.awvalid      <>    uart.io.awvalid
    xbar.io.uart.awready      <>    uart.io.awready
    xbar.io.uart.wdata        <>    uart.io.wdata
    xbar.io.uart.wstrb        <>    uart.io.wstrb
    xbar.io.uart.wvalid       <>    uart.io.wvalid
    xbar.io.uart.wready       <>    uart.io.wready
    xbar.io.uart.bresp        <>    uart.io.bresp
    xbar.io.uart.bvalid       <>    uart.io.bvalid
    xbar.io.uart.bready       <>    uart.io.bready

    xbar.io.clint.araddr      <>    clint.io.araddr
    xbar.io.clint.arvalid     <>    clint.io.arvalid
    xbar.io.clint.arready     <>    clint.io.arready
    xbar.io.clint.rdata       <>    clint.io.rdata
    xbar.io.clint.rresp       <>    clint.io.rresp
    xbar.io.clint.rvalid      <>    clint.io.rvalid
    xbar.io.clint.rready      <>    clint.io.rready
    xbar.io.clint.awaddr      <>    clint.io.awaddr
    xbar.io.clint.awvalid     <>    clint.io.awvalid
    xbar.io.clint.awready     <>    clint.io.awready
    xbar.io.clint.wdata       <>    clint.io.wdata
    xbar.io.clint.wstrb       <>    clint.io.wstrb
    xbar.io.clint.wvalid      <>    clint.io.wvalid
    xbar.io.clint.wready      <>    clint.io.wready
    xbar.io.clint.bresp       <>    clint.io.bresp
    xbar.io.clint.bvalid      <>    clint.io.bvalid
    xbar.io.clint.bready      <>    clint.io.bready

}
