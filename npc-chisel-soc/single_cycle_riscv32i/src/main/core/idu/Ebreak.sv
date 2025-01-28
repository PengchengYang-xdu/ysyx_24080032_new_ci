
module Ebreak(
  input [31:0] inst
);

import "DPI-C" function void npc_trap();

always @(*)begin
    if(inst == 32'h00100073)
        npc_trap();
end

endmodule
