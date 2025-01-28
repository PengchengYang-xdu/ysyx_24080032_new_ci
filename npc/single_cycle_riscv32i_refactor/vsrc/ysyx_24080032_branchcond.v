`timescale 1ns/1ps

module ysyx_24080032_branchcond(
    input      [2:0]  Branch,
    input             Less,
    input             Zero,
    output reg        PCASrc,
    output reg        PCBSrc
);

always @(*)begin
    case(Branch)
        3'b000        :  {PCASrc, PCBSrc} = 2'b00;
        3'b001        :  {PCASrc, PCBSrc} = 2'b10;
        3'b010        :  {PCASrc, PCBSrc} = 2'b11;
        3'b100        :  begin
            case(Zero)
                1'b0  :  {PCASrc, PCBSrc} = 2'b00;
                1'b1  :  {PCASrc, PCBSrc} = 2'b10;
            endcase
        end
        3'b101        :  begin
            case(Zero)
                1'b0  :  {PCASrc, PCBSrc} = 2'b10;
                1'b1  :  {PCASrc, PCBSrc} = 2'b00;
            endcase
        end
        3'b110        :  begin
            case(Less)
                1'b0  :  {PCASrc, PCBSrc} = 2'b00;
                1'b1  :  {PCASrc, PCBSrc} = 2'b10;
            endcase
        end
        3'b111        :  begin
            case(Less)
                1'b0  :  {PCASrc, PCBSrc} = 2'b10;
                1'b1  :  {PCASrc, PCBSrc} = 2'b00;
            endcase
        end
        default       :  {PCASrc, PCBSrc} = 2'bxx;
    endcase
end

endmodule
