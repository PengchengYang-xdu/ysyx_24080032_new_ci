`timescale 1ns/1ps

module ysyx_24080032_ctrgen(
    input      [6:0]  op,
    input      [2:0]  func3,
    input      [6:0]  func7,

    output     [2:0]  ExtOP,
    output            RegWr,
    output            ALUAsrc,
    output     [1:0]  ALUBsrc,
    output     [3:0]  ALUctr,
    output     [2:0]  Branch,
    output            MemtoReg,
    output            MemWr,
    output            MemRd,
    output     [2:0]  MemOp
);

reg [19:0] controls;
assign {ExtOP, RegWr, ALUAsrc, ALUBsrc, ALUctr, Branch, MemtoReg, MemWr, MemRd, MemOp} = controls;

always @(*)begin
     casez({op[6:2], func3, func7[5]})
          9'b01101_???_?:  controls = 20'b001_1_x_01_0011_000_0_0_0_xxx;
          9'b00101_???_?:  controls = 20'b001_1_1_01_0000_000_0_0_0_xxx;

          9'b00100_000_?:  controls = 20'b000_1_0_01_0000_000_0_0_0_xxx;
          9'b00100_010_?:  controls = 20'b000_1_0_01_0010_000_0_0_0_xxx;
          9'b00100_011_?:  controls = 20'b000_1_0_01_1010_000_0_0_0_xxx;
          9'b00100_100_?:  controls = 20'b000_1_0_01_0100_000_0_0_0_xxx;
          9'b00100_110_?:  controls = 20'b000_1_0_01_0110_000_0_0_0_xxx;
          9'b00100_111_?:  controls = 20'b000_1_0_01_0111_000_0_0_0_xxx;
          9'b00100_001_0:  controls = 20'b000_1_0_01_0001_000_0_0_0_xxx;
          9'b00100_101_0:  controls = 20'b000_1_0_01_0101_000_0_0_0_xxx;
          9'b00100_101_1:  controls = 20'b000_1_0_01_1101_000_0_0_0_xxx;

          9'b01100_000_0:  controls = 20'bxxx_1_0_00_0000_000_0_0_0_xxx;
          9'b01100_000_1:  controls = 20'bxxx_1_0_00_1000_000_0_0_0_xxx;
          9'b01100_001_0:  controls = 20'bxxx_1_0_00_0001_000_0_0_0_xxx;
          9'b01100_010_0:  controls = 20'bxxx_1_0_00_0010_000_0_0_0_xxx;
          9'b01100_011_0:  controls = 20'bxxx_1_0_00_1010_000_0_0_0_xxx;
          9'b01100_100_0:  controls = 20'bxxx_1_0_00_0100_000_0_0_0_xxx;
          9'b01100_101_0:  controls = 20'bxxx_1_0_00_0101_000_0_0_0_xxx;
          9'b01100_101_1:  controls = 20'bxxx_1_0_00_1101_000_0_0_0_xxx;
          9'b01100_110_0:  controls = 20'bxxx_1_0_00_0110_000_0_0_0_xxx;
          9'b01100_111_0:  controls = 20'bxxx_1_0_00_0111_000_0_0_0_xxx;

          9'b11011_???_?:  controls = 20'b100_1_1_10_0000_001_0_0_0_xxx;
          9'b11001_000_?:  controls = 20'b000_1_1_10_0000_010_0_0_0_xxx;

          9'b11000_000_?:  controls = 20'b011_0_0_00_0010_100_x_0_0_xxx;
          9'b11000_001_?:  controls = 20'b011_0_0_00_0010_101_x_0_0_xxx;
          9'b11000_100_?:  controls = 20'b011_0_0_00_0010_110_x_0_0_xxx;
          9'b11000_101_?:  controls = 20'b011_0_0_00_0010_111_x_0_0_xxx;
          9'b11000_110_?:  controls = 20'b011_0_0_00_1010_110_x_0_0_xxx;
          9'b11000_111_?:  controls = 20'b011_0_0_00_1010_111_x_0_0_xxx;

          9'b00000_000_?:  controls = 20'b000_1_0_01_0000_000_1_0_1_000;
          9'b00000_001_?:  controls = 20'b000_1_0_01_0000_000_1_0_1_001;
          9'b00000_010_?:  controls = 20'b000_1_0_01_0000_000_1_0_1_010;
          9'b00000_100_?:  controls = 20'b000_1_0_01_0000_000_1_0_1_100;
          9'b00000_101_?:  controls = 20'b000_1_0_01_0000_000_1_0_1_101;

          9'b01000_000_?:  controls = 20'b010_0_0_01_0000_000_x_1_0_000;
          9'b01000_001_?:  controls = 20'b010_0_0_01_0000_000_x_1_0_001;
          9'b01000_010_?:  controls = 20'b010_0_0_01_0000_000_x_1_0_010;
          default       :  controls = 20'bxxx_x_x_xx_xxxx_xxx_x_x_x_xxx;
     endcase
end


endmodule
