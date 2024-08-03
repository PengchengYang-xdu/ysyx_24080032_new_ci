`timescale 1ns/1ps

module tb_encoder83();

reg [7:0] din;
reg en;

wire [2:0] dout;
wire sig;

encoder83 u_encoder83(
    .din    (din ),
    .en     (en  ),
    .dout   (dout),
    .sig    (sig )
);

initial begin
    din = 0;
    en = 0;
    #10;
    din = 8'b00001110;
    #10;
    en = 1;
    #10;
    din = 8'd0;
    #10;
    en = 0;
    #10;
    en = 1;
    #10;
    din = $random;
    #10;
    din = $random;
    #10;
    $finish;
end

initial begin
    $dumpfile("encoder83.vcd");
    $dumpvars;
end

endmodule
