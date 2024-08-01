`timescale 1ns/1ps

module seg(
  input  [3:0] b,
  output reg [6:0] h
);
always @(*) begin
  if(b[3] == 0)
    h = 7'b0000000;
  else begin
  	case(b[2:0])
      3'd0 : h = ~7'b0111111;
      3'd1 : h = ~7'b0000110;
      3'd2 : h = ~7'b1011011;
      3'd3 : h = ~7'b1001111;
      3'd4 : h = ~7'b1100110;
      3'd5 : h = ~7'b1101101;
      3'd6 : h = ~7'b1111101;
      3'd7 : h = ~7'b0000111;
      //'d8 : h = ~7'b1111111;
      //'d9 : h = ~7'b1101111;
      default : h = ~7'b1111111;
    endcase
  end
end
endmodule
