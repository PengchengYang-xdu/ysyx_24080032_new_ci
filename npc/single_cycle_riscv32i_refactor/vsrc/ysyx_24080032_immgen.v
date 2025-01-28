`timescale 1ns/1ps

module ysyx_24080032_immgen(
    input      [2:0]  ExtOP,
    input      [31:7] Instr,
    output reg [31:0] imm
);

wire [31:0] immI, immU, immS, immJ, immB;
assign immI = {{20{Instr[31]}}, Instr[31:20]};
assign immU = {Instr[31:12], 12'b0};
assign immS = {{20{Instr[31]}}, Instr[31:25], Instr[11:7]};
assign immB = {{20{Instr[31]}}, Instr[7], Instr[30:25], Instr[11:8], 1'b0};
assign immJ = {{12{Instr[31]}}, Instr[19:12], Instr[20], Instr[30:21], 1'b0};

always@(*) begin
     case(ExtOP)
          3'b000:   imm = immI;
          3'b001:   imm = immU;
          3'b010:   imm = immS;
          3'b011:   imm = immB;
          3'b100:   imm = immJ;
          default:  imm = 32'd0;
     endcase
end

endmodule
