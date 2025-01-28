`define CLINT_DELAY 0

module Clint(
    input clk,
    input rst,
    //AR
    input [31:0] araddr,
    input arvalid,
    output reg arready,
    //R
    output reg [31:0] rdata,
    output reg [1:0] rresp,
    output reg rvalid,
    input rready,
    //AW
    input [31:0] awaddr,
    input awvalid,
    output reg awready,
    //W
    input [31:0] wdata,
    input [3:0] wstrb,
    input wvalid,
    output reg wready,
    //B
    output reg [1:0] bresp,
    output reg bvalid,
    input bready
);

parameter ADDR = 32'ha0000048;

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
assign AXI_AR_fire = arvalid & arready;
assign AXI_R_fire = rvalid & rready;

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
        arready <= 1'b0;
        rdata <= 32'b0;
        rresp <= 2'b0;
        rvalid <= 1'b0;
        r_delay_unit <= lfsr;
    end
    else begin
        case(nr_state)
            sr_BeforeAXI_AR_Fire: begin
                arready <= 1'b1;
                // rdata <= 32'b0;//rdata has to hold after sr_BeforeAXI_R_Fire state
                rresp <= 2'b0;
                rvalid <= 1'b0;
                r_delay_unit <= lfsr;
            end
            sr_BeforeAXI_R_Fire: begin
                arready <= 1'b0;
                r_delay_unit <= r_delay_unit - 1;
                if(r_delay_unit == 0) begin
                    if(araddr == ADDR) begin
                        rdata <= mtime[31:0];
                        rvalid <= 1'b1;
                        rresp <= 2'b0;
                    end
                    else if(araddr == ADDR + 4) begin
                        rdata <= mtime[63:32];
                        rvalid <= 1'b1;
                        rresp <= 2'b0;
                    end
                    else begin
                        rdata <= 32'b0;
                        rvalid <= 1'b0;
                        rresp <= 2'b1;
                    end
                end
                else begin
                    rdata <= 32'b0;
                    rvalid <= 1'b0;
                end
            end
            default: begin
                arready <= 1'b1;
                rdata <= 32'b0;
                rresp <= 2'b0;
                rvalid <= 1'b0;
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
assign AXI_AWW_fire = (awvalid & awready) & (wvalid & wready);
assign AXI_B_fire = bvalid & bready;

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
        awready <= 1'b0;
        wready <= 1'b0;
        bresp <= 2'b0;
        bvalid <= 1'b0;
        w_delay_unit <= lfsr;
    end
    else begin
        case(nw_state)
            s_BeforeAXI_AWW_Fire: begin
                awready <= 1'b1;
                wready <= 1'b1;
                bresp <= 2'b0;
                bvalid <= 1'b0;
                w_delay_unit <= lfsr;
            end
            s_BeforeAXI_B_Fire: begin
                awready <= 1'b0;
                wready <= 1'b0;
                bresp <= 2'b0;
                w_delay_unit <= w_delay_unit - 1;
                if(w_delay_unit == 0) begin
                    $error("Ilegal write in CLINT\n");
                    bvalid <= 1'b1;
                end
                else begin
                    bvalid <= 1'b0;
                end
            end
            default: begin
                awready <= 1'b1;
                wready <= 1'b1;
                bresp <= 2'b0;
                bvalid <= 1'b0;
                w_delay_unit <= lfsr;
            end 
        endcase
    end
end


















endmodule
