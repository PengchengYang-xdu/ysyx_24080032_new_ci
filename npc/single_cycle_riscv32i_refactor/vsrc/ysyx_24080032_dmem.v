`timescale 1ns / 1ps

module ysyx_24080032_dmem(
    input             clk,
    input             WrEn,
    input             RdEn,
    input      [31:0] Addr,
    input      [31:0] DataIn,
    input      [2:0 ] MemOp,
    output reg [31:0] DataOut
);

import "DPI-C" function int paddr_read(int addr, int is_pc_read, int WriteRd);
import "DPI-C" function void paddr_write(int addr, int data);

wire [31:0] tempout;
reg [31:0] tempin;

wire [31:0] dataout_temp;

assign dataout_temp = RdEn ? paddr_read({Addr[31:2], 2'b00}, 0, 0) : 0;
assign tempout = WrEn ? paddr_read({Addr[31:2], 2'b00}, 0, 1) : 0;

always@(posedge clk)begin
    if(WrEn)
        paddr_write({Addr[31:2], 2'b00}, tempin);
end


reg [3:0] byteen;

always @(*)begin
    case(MemOp)
        3'b000 : 
            case(Addr[1:0])
                2'b00 : DataOut = {{24{dataout_temp[7]}}, dataout_temp[7:0]};
                2'b01 : DataOut = {{24{dataout_temp[15]}}, dataout_temp[15:8]};
                2'b10 : DataOut = {{24{dataout_temp[23]}}, dataout_temp[23:16]};
                2'b11 : DataOut = {{24{dataout_temp[31]}}, dataout_temp[31:24]};
            endcase
        3'b001 :
            case(Addr[1:0])
                2'b00, 2'b01 : DataOut = {{16{dataout_temp[15]}}, dataout_temp[15:0]};
                2'b10, 2'b11 : DataOut = {{16{dataout_temp[31]}}, dataout_temp[31:16]};
            endcase
        3'b010 :
            DataOut = dataout_temp;
        3'b100 :
            case(Addr[1:0])
                2'b00 : DataOut = {24'b0, dataout_temp[7:0]};
                2'b01 : DataOut = {24'b0, dataout_temp[15:8]};
				2'b10 : DataOut = {24'b0, dataout_temp[23:16]};
				2'b11 : DataOut = {24'b0, dataout_temp[31:24]};
            endcase
        3'b101 :
            case(Addr[1:0])
                2'b00, 2'b01: DataOut = {16'b0, dataout_temp[15:0]};
				2'b10, 2'b11: DataOut = {16'b0, dataout_temp[31:16]};
			endcase
        default :
            DataOut = 32'bx;
    endcase
end

always @(*)begin
    case(MemOp)
        3'b000 :
            case(Addr[1:0])
                2'b00 : begin byteen = 4'b0001; tempin = {tempout[31:8], DataIn[7:0]}; end
                2'b01 : begin byteen = 4'b0010; tempin = {tempout[31:16], DataIn[7:0], tempout[7:0]}; end
                2'b10 : begin byteen = 4'b0100; tempin = {tempout[31:24], DataIn[7:0], tempout[15:0]}; end
                2'b11 : begin byteen = 4'b1000; tempin = {DataIn[7:0], tempout[23:0]}; end
            endcase
        3'b001 :
            case(Addr[1:0])
                2'b00, 2'b01 : begin byteen = 4'b0011; tempin = {tempout[31:16], DataIn[15:0]}; end
                2'b10, 2'b11 : begin byteen = 4'b1100; tempin = {DataIn[15:0], tempout[15:0]}; end
            endcase
        3'b010 :
            begin byteen = 4'b1111; tempin = DataIn; end
        default:
            begin byteen = 4'bx; tempin = 32'bx; end
    endcase
end















endmodule

