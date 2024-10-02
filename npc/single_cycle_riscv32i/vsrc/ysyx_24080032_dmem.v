`timescale 1ns / 1ps

module ysyx_24080032_dmem(
    input             wr_clk,
    input             rd_clk,
    input             WrEn,
    input             RdEn,
    input      [31:0] Addr,
    input      [31:0] DataIn,
    input      [2:0 ] MemOp,
    output reg [31:0] DataOut
);

import "DPI-C" function int paddr_read(int addr, int is_pc_read, int WriteRd);
import "DPI-C" function void paddr_write(int addr, int data);

reg  [31:0] tempout;
reg  [31:0] tempin;

reg  [31:0] dataout_temp;

always@(posedge rd_clk)begin
    if(WrEn)
        tempout <= paddr_read({Addr[31:2], 2'b00}, 0, 1);
    else if(RdEn)
        dataout_temp <= paddr_read({Addr[31:2], 2'b00}, 0, 0);
end

always@(negedge wr_clk)begin
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


// reg  [31:0] RAM [32767:0];

// // initial begin
// //     $readmemh("./riscvtest.txt", ROM); 
// // end

// reg  [31:0] tempout;
// wire [31:0] tempin;

// reg  [31:0] dataout_temp;

// always@(posedge rd_clk)begin
//     if(WrEn)
//         tempout <= RAM[Addr];
//     else
//         dataout_temp <= RAM[Addr];
// end

// always@(negedge wr_clk)begin
//     if(WrEn)
//         RAM[Addr] <= tempin;
// end

// reg [3:0] byteen;

// assign tempin[7:0]   = (byteen[0]) ? DataIn[7:0]  : tempout[7:0];
// assign tempin[15:8]  = (byteen[1]) ? DataIn[15:8] : tempout[15:8];
// assign tempin[23:16] = (byteen[2]) ? DataIn[23:16]: tempout[23:16];
// assign tempin[31:24] = (byteen[3]) ? DataIn[31:24]: tempout[31:24];

// always @(*)begin
//     case(MemOp)
//         3'b000 : 
//             case(Addr[1:0])
//                 2'b00 : DataOut = {{24{dataout_temp[7]}}, dataout_temp[7:0]};
//                 2'b01 : DataOut = {{24{dataout_temp[15]}}, dataout_temp[15:8]};
//                 2'b10 : DataOut = {{24{dataout_temp[23]}}, dataout_temp[23:16]};
//                 2'b11 : DataOut = {{24{dataout_temp[31]}}, dataout_temp[31:24]};
//             endcase
//         3'b001 :
//             case(Addr[1:0])
//                 2'b00, 2'b01 : DataOut = {{16{dataout_temp[15]}}, dataout_temp[15:0]};
//                 2'b10, 2'b11 : DataOut = {{16{dataout_temp[31]}}, dataout_temp[31:16]};
//             endcase
//         3'b010 :
//             DataOut = dataout_temp;
//         3'b100 :
//             case(Addr[1:0])
//                 2'b00 : DataOut = {24'b0, dataout_temp[7:0]};
//                 2'b01 : DataOut = {24'b0, dataout_temp[15:8]};
// 				2'b10 : DataOut = {24'b0, dataout_temp[23:16]};
// 				2'b11 : DataOut = {24'b0, dataout_temp[31:24]};
//             endcase
//         3'b101 :
//             case(Addr[1:0])
//                 2'b00, 2'b01: DataOut = {16'b0, dataout_temp[15:0]};
// 				2'b10, 2'b11: DataOut = {16'b0, dataout_temp[31:16]};
// 			endcase
//         default :
//             DataOut = 32'bx;
//     endcase
// end

// always @(*)begin
//     case(MemOp)
//         3'b000 :
//             case(Addr[1:0])
//                 2'b00 : byteen = 4'b0001;
//                 2'b01 : byteen = 4'b0010;
//                 2'b10 : byteen = 4'b0100;
//                 2'b11 : byteen = 4'b1000;
//             endcase
//         3'b001 :
//             case(Addr[1:0])
//                 2'b00, 2'b01 : byteen = 4'b0011;
//                 2'b10, 2'b11 : byteen = 4'b1100;
//             endcase
//         3'b010 :
//             byteen = 4'b1111;
//         default:
//             byteen = 4'bx;
//     endcase
// end

// endmodule
