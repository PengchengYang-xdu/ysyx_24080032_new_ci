`timescale 1ns / 1ps

`define MSTATUS 12'h300
`define MTVEC   12'h305
`define MEPC    12'h341
`define MCAUSE  12'h342

module ysyx_24080032_csrfile #(
    parameter ADDR_WIDTH = 12,
    parameter DATA_WIDTH = 32)
(
    input                   clk,

    input  [1:0]            irq,
    input  [31:0]           PC,

    output [DATA_WIDTH-1:0] busA,
    input  [ADDR_WIDTH-1:0] Ra,

    input                   CsrWr,
    input  [DATA_WIDTH-1:0] busW,
    input  [ADDR_WIDTH-1:0] Rw,

    output [DATA_WIDTH-1:0] mtvec,
    output [DATA_WIDTH-1:0] mepc
);

reg [DATA_WIDTH-1:0] rf [3:0];

reg [1:0] Rw_addr;
reg [1:0] Ra_addr;

always @(*) begin
    case(Ra)
        `MSTATUS: Ra_addr = 2'b00;
        `MTVEC:   Ra_addr = 2'b01;
        `MEPC:    Ra_addr = 2'b10;
        `MCAUSE:  Ra_addr = 2'b11;
        default:  Ra_addr = 2'b00;
    endcase
end

always @(*) begin
    case(Rw)
        `MSTATUS: Rw_addr = 2'b00;
        `MTVEC:   Rw_addr = 2'b01;
        `MEPC:    Rw_addr = 2'b10;
        `MCAUSE:  Rw_addr = 2'b11;
        default:  Rw_addr = 2'b00;
    endcase
end

always @(posedge clk) begin
    if(CsrWr == 1'b1)
        rf[Rw_addr] <= busW;
    if(irq[0]) begin
        rf[2] <= PC;
        rf[3] <= 32'h0000000b;
    end
end

assign busA = rf[Ra_addr];

initial begin
    rf[0] = 32'h1800;
end

assign mtvec = rf[1];
assign mepc  = rf[2];

endmodule
