`timescale 1ns/1ps

module top(
    input [7:0] din,
    input en,
    output [2:0] dout,
    output sig,
    output [6:0] h
);

encoder83 u_encoder83(
    .din  (din ),
    .en   (en  ),
    .dout (dout),
    .sig  (sig )
);

seg u_seg(
    .b  ({sig, dout}),
    .h  (h          )
);

endmodule
