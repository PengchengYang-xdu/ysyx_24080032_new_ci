`timescale 1ns/1ps

module ALU(
    input [3:0] a,
    input [3:0] b,
    input [2:0] op,
    output reg ZF,
    output reg OF,
    output reg CF,
    output reg [3:0] result,
    output [7:0] result_seg,
    output [3:0] result_sign
);

wire [3:0] b_comp = ~b + 1;

always @(*) begin
    case (op) 
        3'b000         : begin
            {CF, result} = b + a;
            OF = (a[3] == b[3]) && (result[3] != a[3]);
        end
        3'b001         : begin
            {CF, result} = b_comp + a;
            OF = (a[3] == b[3]) && (result[3] != a[3]);
        end
        3'b010         : begin
            result = ~a;
            CF = 1'b0;
            OF = 1'b0;
        end
        3'b011         : begin
            result = a & b;
            CF = 1'b0;
            OF = 1'b0;
        end
        3'b100         : begin
            result = a | b;
            CF = 1'b0;
            OF = 1'b0;
        end
        3'b101         : begin
            result = a ^ b;
            CF = 1'b0;
            OF = 1'b0;
        end
        3'b110         : begin
            result = {3'b000, ($signed(a) < $signed(b)) ? 1'b1 : 1'b0};
            CF = 1'b0;
            OF = 1'b0;
        end
        3'b111         : begin
            result = {3'b000, (a == b) ? 1'b1 : 1'b0};
            CF = 1'b0;
            OF = 1'b0;
        end
        default        : begin
            result = 'd0;
            CF = 1'b0;
            OF = 1'b0;
        end
    endcase
    ZF = result == 'd0;
end

wire [3:0] result_show;
assign result_show = OF ? (result[3] ? result : ~result + 'd1) : (result[3] ? ~result + 'd1 : result);

assign result_seg[7:4] = result_show / 10;
assign result_seg[3:0] = result_show % 10;
assign result_sign = OF ? ('d10 + {3'd0, !result[3]}) : ('d10 + {3'd0, result[3]});

endmodule
