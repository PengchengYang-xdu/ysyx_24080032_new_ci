`timescale 1ns / 1ps

module ysyx_24080032_alu(
    input      [31:0] dataa,
	input      [31:0] datab,
	input      [3:0]  ALUctr,
	output reg        Less,
	output            Zero,
	output reg [31:0] Result
);

wire [31:0] sum;
wire cf;
wire [31:0] datab_inv;
assign datab_inv = ~datab;
assign {cf, sum} = ALUctr[3] ? (dataa + datab_inv + 32'b1) : (dataa + datab);

always @ (*) begin
	Less = 1'bx;
	casez(ALUctr)
		4'b?000: Result = sum;
		4'b?001: Result = dataa << datab[4:0];
		4'b0010: begin Less = $signed(dataa) < $signed(datab); Result = {31'd0, Less}; end
		4'b1010: begin Less = dataa < datab; Result = {31'd0, Less}; end
		4'b?011: Result = datab;
		4'b?100: Result = dataa ^ datab;
		4'b0101: Result = dataa >> datab[4:0];
		4'b1101: Result = $signed(dataa) >>> datab[4:0];
		4'b?110: Result = dataa | datab;
		4'b?111: Result = dataa & datab;
		default: Result = 32'bx;
	endcase
end

assign Zero = ALUctr[2:0] == 3'b010 ? dataa == datab : ~(|Result);

endmodule