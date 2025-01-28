package npc.bus.axi
 
import chisel3._
import chisel3.util._
import npc.common.Config._
import npc.common.Instructions._

// class AXI_lite extends Bundle{
//   //slave
//   //clk and rst
//   val clk = Input(Clock())
//   val rst = Input(Reset())
//   //AR
//   val araddr = Input(UInt(WORD_LEN.W))
//   val arvalid = Input(Bool())
//   val arready = Output(Bool())
//   //R
//   val rdata = Output(UInt(WORD_LEN.W))
//   val rresp = Output(UInt(2.W))
//   val rvalid = Output(Bool())
//   val rready = Input(Bool())
//   //AW
//   val awaddr = Input(UInt(WORD_LEN.W))
//   val awvalid = Input(Bool())
//   val awready = Output(Bool())
//   //W
//   val wdata = Input(UInt(WORD_LEN.W))
//   val wstrb = Input(UInt(4.W))
//   val wvalid = Input(Bool())
//   val wready = Output(Bool())
//   //B
//   val bresp = Output(UInt(2.W))
//   val bvalid = Output(Bool())
//   val bready = Input(Bool())
// }


// class AXI_liteWithoutClk extends Bundle{
//   //slave
//   //AR
//   val araddr = Input(UInt(WORD_LEN.W))
//   val arvalid = Input(Bool())
//   val arready = Output(Bool())
//   //R
//   val rdata = Output(UInt(WORD_LEN.W))
//   val rresp = Output(UInt(2.W))
//   val rvalid = Output(Bool())
//   val rready = Input(Bool())
//   //AW
//   val awaddr = Input(UInt(WORD_LEN.W))
//   val awvalid = Input(Bool())
//   val awready = Output(Bool())
//   //W
//   val wdata = Input(UInt(WORD_LEN.W))
//   val wstrb = Input(UInt(4.W))
//   val wvalid = Input(Bool())
//   val wready = Output(Bool())
//   //B
//   val bresp = Output(UInt(2.W))
//   val bvalid = Output(Bool())
//   val bready = Input(Bool())
// }

class AXI4 extends Bundle{
  //slave
  //clk and rst
  val clk = Input(Clock())
  val rst = Input(Reset())
  //AR
  val araddr = Input(UInt(WORD_LEN.W))
  val arvalid = Input(Bool())
  val arready = Output(Bool())
    val arid = Input(UInt(4.W))
    val arlen = Input(UInt(8.W))
    val arsize = Input(UInt(3.W))
    val arburst = Input(UInt(2.W))
  //R
  val rdata = Output(UInt(WORD_LEN.W))
  val rresp = Output(UInt(2.W))
  val rvalid = Output(Bool())
  val rready = Input(Bool())
    val rlast = Output(Bool())
    val rid = Output(UInt(4.W))
  //AW
  val awaddr = Input(UInt(WORD_LEN.W))
  val awvalid = Input(Bool())
  val awready = Output(Bool())
    val awid = Input(UInt(4.W))
    val awlen = Input(UInt(8.W))
    val awsize = Input(UInt(3.W))
    val awburst = Input(UInt(2.W))
  //W
  val wdata = Input(UInt(WORD_LEN.W))
  val wstrb = Input(UInt(4.W))
  val wvalid = Input(Bool())
  val wready = Output(Bool())
    val wlast = Input(Bool())
  //B
  val bresp = Output(UInt(2.W))
  val bvalid = Output(Bool())
  val bready = Input(Bool())
    val bid = Output(UInt(4.W))
}


class AXI4WithoutClk extends Bundle{
  //slave
  //AR
  val araddr = Input(UInt(WORD_LEN.W))
  val arvalid = Input(Bool())
  val arready = Output(Bool())
    val arid = Input(UInt(4.W))
    val arlen = Input(UInt(8.W))
    val arsize = Input(UInt(3.W))
    val arburst = Input(UInt(2.W))
  //R
  val rdata = Output(UInt(WORD_LEN.W))
  val rresp = Output(UInt(2.W))
  val rvalid = Output(Bool())
  val rready = Input(Bool())
    val rlast = Output(Bool())
    val rid = Output(UInt(4.W))
  //AW
  val awaddr = Input(UInt(WORD_LEN.W))
  val awvalid = Input(Bool())
  val awready = Output(Bool())
    val awid = Input(UInt(4.W))
    val awlen = Input(UInt(8.W))
    val awsize = Input(UInt(3.W))
    val awburst = Input(UInt(2.W))
  //W
  val wdata = Input(UInt(WORD_LEN.W))
  val wstrb = Input(UInt(4.W))
  val wvalid = Input(Bool())
  val wready = Output(Bool())
    val wlast = Input(Bool())
  //B
  val bresp = Output(UInt(2.W))
  val bvalid = Output(Bool())
  val bready = Input(Bool())
    val bid = Output(UInt(4.W))
}

