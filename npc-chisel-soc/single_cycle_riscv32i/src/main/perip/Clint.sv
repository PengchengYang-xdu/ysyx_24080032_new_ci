// `define CLINT_DELAY_ON
`define CLINT_DELAY 0

module Clint(
    input clk,
    input rst,
    //AR
    input [31:0] axi4_araddr,
    input axi4_arvalid,
    output axi4_arready,
    input [3:0] axi4_arid,
    input [7:0] axi4_arlen,
    input [2:0] axi4_arsize,
    input [1:0] axi4_arburst,
    //R
    output reg [31:0] axi4_rdata,
    output [1:0] axi4_rresp,
    output axi4_rvalid,
    input axi4_rready,
    output axi4_rlast,
    output [3:0] axi4_rid,
    //AW
    input [31:0] axi4_awaddr,
    input axi4_awvalid,
    output axi4_awready,
    input [3:0] axi4_awid,
    input [7:0] axi4_awlen,
    input [2:0] axi4_awsize,
    input [1:0] axi4_awburst,
    //W
    input [31:0] axi4_wdata,
    input [3:0] axi4_wstrb,
    input axi4_wvalid,
    output axi4_wready,
    input axi4_wlast,
    //B
    output [1:0] axi4_bresp,
    output axi4_bvalid,
    input axi4_bready,
    output [3:0] axi4_bid
);

/*-----------------------------initial-----------------------------*/
assign axi4_rlast = 'd1;
assign axi4_rid = 'd0;
assign axi4_bid = 'd0;

// parameter ADDR = 32'ha0000048;
parameter ADDR = 32'h02000000;

/*-----------------------------mtime-----------------------------*/
reg [63:0] mtime;
always @(posedge clk or posedge rst) begin
    if(rst)
        mtime <= 64'b0;
    else
        mtime <= mtime + 1;
end

`ifdef CLINT_DELAY_ON
/*-----------------------------delay process-----------------------------*/
reg [3:0] lfsr;
always @(posedge clk or posedge rst) begin
    if (rst) begin
        lfsr <= `CLINT_DELAY;
    end
    else begin
        lfsr <= {lfsr[2:0], lfsr[3] ^ lfsr[2]};
    end
end

reg [3:0] r_delay_unit;
reg [3:0] w_delay_unit;
`endif

/*-----------------------------read channel-----------------------------*/
//state machine
parameter sr_BeforeAXI_AR_Fire = 1'b0;
parameter sr_BeforeAXI_R_Fire = 1'b1;
reg cr_state, nr_state;
wire AXI_AR_fire, AXI_R_fire;
assign AXI_AR_fire = axi4_arvalid & axi4_arready;
assign AXI_R_fire = axi4_rvalid & axi4_rready;

//first phase
always @(posedge clk or posedge rst)begin
    if(rst)
        cr_state <= sr_BeforeAXI_AR_Fire;
    else
        cr_state <= nr_state;
end

//second phase
always@(*) begin
    case(cr_state)
        sr_BeforeAXI_AR_Fire: begin
            nr_state = AXI_AR_fire ? sr_BeforeAXI_R_Fire : sr_BeforeAXI_AR_Fire;
        end
        sr_BeforeAXI_R_Fire: begin
            nr_state = AXI_R_fire ? sr_BeforeAXI_AR_Fire : sr_BeforeAXI_R_Fire;
        end
        default: begin
            nr_state = sr_BeforeAXI_AR_Fire;
        end
    endcase
end


assign axi4_arready = cr_state == sr_BeforeAXI_AR_Fire ? 1'b1 : 1'b0;
assign axi4_rvalid = cr_state == sr_BeforeAXI_R_Fire ? 1'b1 : 1'b0;
assign axi4_rresp = 0;

always @(posedge clk or posedge rst) begin
    if(rst) begin
        axi4_rdata <= 'd0;
    end
    else if(axi4_araddr === ADDR) begin
        axi4_rdata <= mtime[31:0];
    end
    else begin
        axi4_rdata <= mtime[63:32];
    end
end


endmodule
