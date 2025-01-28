`timescale 1ns/1ps

module ysyx_24080032_IDU(
    input clk,

    //input from ifu
    input [31:0] i_ifu_Instr,
    input [31:0] i_ifu_PC,
    input [31:0] i_ifu_RegbusW,
    input [31:0] i_ifu_CsrbusW,

    //output to exu
    output o_exu_ALUAsrc,
    output [1:0] o_exu_ALUBsrc,
    output [3:0] o_exu_ALUctr,
    output [2:0] o_exu_Branch,
    output [31:0] o_exu_rs2,
    output [31:0] o_exu_rs1,
    output [1:0] o_exu_MemtoReg,
    output o_exu_WcsrSrc,
    output [31:0] o_exu_Rcsr,
    output o_exu_MemWr,
    output o_exu_MemRd,
    output [2:0] o_exu_MemOp,
    output [31:0] o_exu_mtvec,
    output [31:0] o_exu_mepc,
    output [1:0] o_exu_irq,
    output [31:0] o_exu_imm,
    output [31:0] o_exu_PC
);

assign o_exu_PC = i_ifu_PC;

/*DPI-C*/
import "DPI-C" function void npc_trap();
always @(*)begin
    if(i_ifu_Instr == 32'h00100073)
        npc_trap();
end

wire [6:0] op;
wire [2:0] func3;
wire [6:0] func7;
wire [11:0] func12;

assign {op, func3, func7, func12} = {i_ifu_Instr[6:0], i_ifu_Instr[14:12], i_ifu_Instr[31:25], i_ifu_Instr[31:20]};

wire [2:0] ExtOP;
wire RegWr;
wire CsrWr;

ysyx_24080032_ctrgen u_ysyx_24080032_ctrgen(
    .op       (op                    ),
    .func3    (func3                 ),
    .func7    (func7                 ),
    .func12   (func12                ),
    .ExtOP    (ExtOP                 ),
    .RegWr    (RegWr                 ),
    .CsrWr    (CsrWr                 ),
    .ALUAsrc  (o_exu_ALUAsrc         ),
    .ALUBsrc  (o_exu_ALUBsrc         ),
    .ALUctr   (o_exu_ALUctr          ),
    .Branch   (o_exu_Branch          ),
    .MemtoReg (o_exu_MemtoReg        ),
    .WcsrSrc  (o_exu_WcsrSrc         ),
    .MemWr    (o_exu_MemWr           ),
    .MemRd    (o_exu_MemRd           ),
    .MemOp    (o_exu_MemOp           ),
    .irq      (o_exu_irq             )
);

ysyx_24080032_immgen u_ysyx_24080032_immgen(
    .ExtOP   (ExtOP                  ),
    .Instr   (i_ifu_Instr[31:7]      ),
    .imm     (o_exu_imm              )
);

wire [4:0] RegRa, RegRb, RegRw;
assign {RegRa, RegRb, RegRw} = {i_ifu_Instr[19:15], i_ifu_Instr[24:20], i_ifu_Instr[11:7]};

ysyx_24080032_regfile #(.ADDR_WIDTH(5), .DATA_WIDTH(32)) u_ysyx_24080032_regfile(
    .clk      (clk                   ),
    .busA     (o_exu_rs1             ),
    .busB     (o_exu_rs2             ),
    .Ra       (RegRa                 ),
    .Rb       (RegRb                 ),
    .RegWr    (RegWr                 ),
    .busW     (i_ifu_RegbusW         ),
    .Rw       (RegRw                 )
);

wire [11:0] CsrRa, CsrRw;
assign {CsrRa, CsrRw} = {2{i_ifu_Instr[31:20]}};

ysyx_24080032_csrfile #(.ADDR_WIDTH(12), .DATA_WIDTH(32)) u_ysyx_24080032_csrfile(
    .clk      (clk             ),
    .irq      (o_exu_irq       ),
    .PC       (i_ifu_PC        ),
    .busA     (o_exu_Rcsr      ),
    .Ra       (CsrRa           ),
    .CsrWr    (CsrWr           ),
    .busW     (i_ifu_CsrbusW   ),
    .Rw       (CsrRw           ),
    .mtvec    (o_exu_mtvec     ),
    .mepc     (o_exu_mepc      )
);

endmodule
