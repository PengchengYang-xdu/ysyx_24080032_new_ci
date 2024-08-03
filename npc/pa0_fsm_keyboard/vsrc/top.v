`timescale 1ns/1ps

module top(
  input clk,
  input rst,
  input ps2_clk,
  input ps2_data,
  output reg [7:0] seg0,
  output reg [7:0] seg1,
  output reg [7:0] seg2,
  output reg [7:0] seg3
);

wire [7:0] data;
wire ready;
reg nextdata_n;
wire overflow;

ps2_keyboard u_ps2_keyboard(
    .clk(clk),
    .clrn(rst),
    .ps2_clk(ps2_clk),
    .ps2_data(ps2_data),
    .data(data),
    .ready(ready),
    .nextdata_n(nextdata_n),
    .overflow(overflow)
);

reg ready_r1;
always @(posedge clk or negedge rst) begin
    if(!rst)
        ready_r1 <= 'd0;
    else
        ready_r1 <= ready;
end

wire ready_rise;
assign ready_rise = ready & !ready_r1;

reg [7:0] data_last;

reg [3:0] num0;
reg [3:0] num1;
reg [3:0] num2;
reg [3:0] num3;

always @(posedge clk or negedge rst) begin
    if(!rst) begin
        num0 <= 'd0;
        num1 <= 'd0;
        data_last <= 'd0;
        nextdata_n <= 'd1;
    end
    else if(ready_rise == 1'b1) begin
        if(data == 8'hf0)
            {num1, num0} <= 8'b11111111;
        else
            {num1, num0} <= data_last == 8'hf0 ? 8'b11111111 : data;
        data_last <= data;
        nextdata_n <= 'd0;
    end
    else
        nextdata_n <= 'd1;
end

seg u_seg0(
    .b   (num0),
    .h   (seg0)
);

seg u_seg1(
    .b   (num1),
    .h   (seg1)
);

always @(posedge clk or negedge rst) begin
    if(!rst) begin
        num2 <= 'd0;
        num3 <= 'd0;
    end
    else if(ready_rise == 'd1 && data_last == 8'hf0) begin
        num2 <= num2 == 'd9 ? 'd0 : num2 + 'd1;
        num3 <= num2 == 'd9 ? (num3 == 'd9 ? 'd0 : num3 + 'd1) : num3;
    end
end

seg u_seg2(
    .b   (num2),
    .h   (seg2)
);

seg u_seg3(
    .b   (num3),
    .h   (seg3)
);

endmodule
