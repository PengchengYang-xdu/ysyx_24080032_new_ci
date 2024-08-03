`timescale 1ns/1ps

module seg(
  input  [3:0] b,
  output reg [7:0] h
);
always @(*) begin
  	case(b)
      'd0 : h = 8'b00000010;
      'd1 : h = 8'b10011111;
      'd2 : h = 8'b00100101;
      'd3 : h = 8'b00001101;
      'd4 : h = 8'b10011001;
      'd5 : h = 8'b01001001;
      'd6 : h = 8'b01000001;
      'd7 : h = 8'b00011111;
      'd8 : h = 8'b00000000;
      'd9 : h = 8'b00001001;
      'd10: h = 8'b00010001;
      'd11: h = 8'b11000001;
      'd12: h = 8'b01100011;
      'd13: h = 8'b10000101;
      'd14: h = 8'b01100001;
      // 'd15: h = 8'b01110001;
      'd15: h = 8'b11111111;
      default : h = 8'b11111111;
    endcase
end
endmodule
