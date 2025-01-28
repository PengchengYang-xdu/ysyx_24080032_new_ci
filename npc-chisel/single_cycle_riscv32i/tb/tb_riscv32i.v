`timescale 1ns / 1ps

module tb_riscv32i();

reg              clock;
reg              reset;

NPC u_NPC(
    .clock   (clock ),
    .reset   (reset )
);


initial begin
    reset <= 0;
    #10;
    reset <= 1;
    #2000;
    $finish;
end

always begin
    clock <= 1;
    #5;
    clock <= 0;
    #5;
end

initial begin
    $dumpfile("iverilog.vcd");
    $dumpvars;
end

endmodule
