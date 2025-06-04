`define CLINT_DELAY 0

module Clint(
    input clk,
    input rst,
    //AR
    input [31:0] axi4_araddr,
    input axi4_arvalid,
    output reg axi4_arready,
    input [3:0] axi4_arid,
    input [7:0] axi4_arlen,
    input [2:0] axi4_arsize,
    input [1:0] axi4_arburst,
    //R
    output reg [31:0] axi4_rdata,
    output reg [1:0] axi4_rresp,
    output reg axi4_rvalid,
    input axi4_rready,
    output axi4_rlast,
    output [3:0] axi4_rid,
    //AW
    input [31:0] axi4_awaddr,
    input axi4_awvalid,
    output reg axi4_awready,
    input [3:0] axi4_awid,
    input [7:0] axi4_awlen,
    input [2:0] axi4_awsize,
    input [1:0] axi4_awburst,
    //W
    input [31:0] axi4_wdata,
    input [3:0] axi4_wstrb,
    input axi4_wvalid,
    output reg axi4_wready,
    input axi4_wlast,
    //B
    output reg [1:0] axi4_bresp,
    output reg axi4_bvalid,
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

//third phase
always @(posedge clk or posedge rst) begin
    if(rst) begin
        axi4_arready <= 1'b0;
        axi4_rdata <= 32'b0;
        axi4_rresp <= 2'b0;
        axi4_rvalid <= 1'b0;
        r_delay_unit <= lfsr;
    end
    else begin
        case(nr_state)
            sr_BeforeAXI_AR_Fire: begin
                axi4_arready <= 1'b1;
                // axi4_rdata <= 32'b0;//axi4_rdata has to hold after sr_BeforeAXI_R_Fire state
                axi4_rresp <= 2'b0;
                axi4_rvalid <= 1'b0;
                r_delay_unit <= lfsr;
            end
            sr_BeforeAXI_R_Fire: begin
                axi4_arready <= 1'b0;
                r_delay_unit <= r_delay_unit - 1;
                if(r_delay_unit == 0) begin
                    if(axi4_araddr == ADDR) begin
                        axi4_rdata <= mtime[31:0];
                        axi4_rvalid <= 1'b1;
                        axi4_rresp <= 2'b0;
                    end
                    else if(axi4_araddr == ADDR + 4) begin
                        axi4_rdata <= mtime[63:32];
                        axi4_rvalid <= 1'b1;
                        axi4_rresp <= 2'b0;
                    end
                    else begin
                        axi4_rdata <= 32'b0;
                        axi4_rvalid <= 1'b0;
                        axi4_rresp <= 2'b1;
                    end
                end
                else begin
                    axi4_rdata <= 32'b0;
                    axi4_rvalid <= 1'b0;
                end
            end
            default: begin
                axi4_arready <= 1'b1;
                axi4_rdata <= 32'b0;
                axi4_rresp <= 2'b0;
                axi4_rvalid <= 1'b0;
                r_delay_unit <= lfsr;
            end 
        endcase
    end
end







































/*-----------------------------read channel-----------------------------*/
//state machine
parameter s_BeforeAXI_AWW_Fire = 1'b0;
parameter s_BeforeAXI_B_Fire = 1'b1;
reg cw_state, nw_state;
wire AXI_AWW_fire, AXI_B_fire;
assign AXI_AWW_fire = (axi4_awvalid & axi4_awready) & (axi4_wvalid & axi4_wready);
assign AXI_B_fire = axi4_bvalid & axi4_bready;

//first phase
always @(posedge clk or posedge rst)begin
    if(rst)
        cw_state <= s_BeforeAXI_AWW_Fire;
    else
        cw_state <= nw_state;
end

//second phase
always@(*) begin
    case(cw_state)
        s_BeforeAXI_AWW_Fire: begin
            nw_state = AXI_AWW_fire ? s_BeforeAXI_B_Fire : s_BeforeAXI_AWW_Fire;
        end
        s_BeforeAXI_B_Fire: begin
            nw_state = AXI_B_fire ? s_BeforeAXI_AWW_Fire : s_BeforeAXI_B_Fire;
        end
        default: begin
            nw_state = s_BeforeAXI_AWW_Fire;
        end
    endcase
end

//third phase
always @(posedge clk or posedge rst) begin
    if(rst) begin
        axi4_awready <= 1'b0;
        axi4_wready <= 1'b0;
        axi4_bresp <= 2'b0;
        axi4_bvalid <= 1'b0;
        w_delay_unit <= lfsr;
    end
    else begin
        case(nw_state)
            s_BeforeAXI_AWW_Fire: begin
                axi4_awready <= 1'b1;
                axi4_wready <= 1'b1;
                axi4_bresp <= 2'b0;
                axi4_bvalid <= 1'b0;
                w_delay_unit <= lfsr;
            end
            s_BeforeAXI_B_Fire: begin
                axi4_awready <= 1'b0;
                axi4_wready <= 1'b0;
                axi4_bresp <= 2'b0;
                w_delay_unit <= w_delay_unit - 1;
                if(w_delay_unit == 0) begin
                    // $error("Ilegal write in CLINT\n");
                    axi4_bvalid <= 1'b1;
                end
                else begin
                    axi4_bvalid <= 1'b0;
                end
            end
            default: begin
                axi4_awready <= 1'b1;
                axi4_wready <= 1'b1;
                axi4_bresp <= 2'b0;
                axi4_bvalid <= 1'b0;
                w_delay_unit <= lfsr;
            end 
        endcase
    end
end


















endmodule
