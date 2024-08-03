`timescale 1ns/1ps

module encoder83(
    input [7:0] din,
    input en,
    output reg [2:0] dout,
    output reg sig
);

always @(*) begin
    if(en)
        casez(din)
            8'b1??????? : begin dout = 3'b111; sig = 1'b1; end
            8'b01?????? : begin dout = 3'b110; sig = 1'b1; end
            8'b001????? : begin dout = 3'b101; sig = 1'b1; end
            8'b0001???? : begin dout = 3'b100; sig = 1'b1; end
            8'b00001??? : begin dout = 3'b011; sig = 1'b1; end
            8'b000001?? : begin dout = 3'b010; sig = 1'b1; end
            8'b0000001? : begin dout = 3'b001; sig = 1'b1; end
            8'b00000001 : begin dout = 3'b000; sig = 1'b1; end
            default     : begin dout = 3'b000; sig = 1'b0; end
        endcase
    else begin
        dout = 3'b000;
        sig = 1'b0;
    end
end

endmodule
