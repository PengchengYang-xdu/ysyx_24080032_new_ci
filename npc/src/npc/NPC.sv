`timescale 1ns / 1ps

module NPC (
    input clock,
    input reset
);

    // --- Core IMEM/DMEM Wires ---
    // Instruction Memory (IMEM) Wires
    wire [31:0] imem_araddr;
    wire        imem_arvalid;
    wire        imem_arready;
    wire [2:0]  imem_arsize;
    wire [1:0]  imem_arburst;
    wire [31:0] imem_rdata;
    wire        imem_rvalid;
    wire        imem_rready;
    wire        imem_rlast;

    // Data Memory (DMEM) Wires - AXI-Lite Read (AR/R)
    wire [31:0] dmem_araddr;
    wire        dmem_arvalid;
    wire        dmem_arready;
    wire [2:0]  dmem_arsize;
    wire [31:0] dmem_rdata;
    wire        dmem_rvalid;
    wire        dmem_rready;

    // Data Memory (DMEM) Wires - AXI-Lite Write (AW/W/B)
    wire [31:0] dmem_awaddr;
    wire        dmem_awvalid;
    wire        dmem_awready;
    wire [2:0]  dmem_awsize;
    wire [31:0] dmem_wdata;
    wire [3:0]  dmem_wstrb;
    wire        dmem_wvalid;
    wire        dmem_wready;
    wire        dmem_bvalid;
    wire        dmem_bready;


    // --- Mem 模块端口信号 Wires ---
    // 由于 CPU 的 IMEM 和 DMEM 都要连接到 Mem，我们需要进行简单的 OR 逻辑合并。

    // Mem AR/R Channel (合并 IMEM 和 DMEM 的读请求)
    wire [31:0] mem_araddr;
    wire        mem_arvalid;
    wire        mem_arready;
    wire [31:0] mem_rdata;
    wire [1:0]  mem_rresp; // Mem 模块输出
    wire        mem_rvalid;
    wire        mem_rready;

    // Mem AW/W/B Channel (只连接 DMEM 的写请求)
    wire [31:0] mem_awaddr;
    wire        mem_awvalid;
    wire        mem_awready;
    wire [31:0] mem_wdata;
    wire [3:0]  mem_wstrb;
    wire        mem_wvalid;
    wire        mem_wready;
    wire [1:0]  mem_bresp; // Mem 模块输出
    wire        mem_bvalid;
    wire        mem_bready;


    // --- 逻辑连接 (IMEM & DMEM -> Mem) ---

    // 1. Mem Read Address Channel (AR)
    // 假设 IMEM 和 DMEM 不会同时发起访问，或者使用简单的 OR 逻辑（实际系统需仲裁）
    // 为了满足“只连接 Mem”的要求，这里假设 Mem 作为一个简单的统一总线。
    assign mem_arvalid = imem_arvalid | dmem_arvalid;

    // Mux 地址：优先选择 IMEM 地址（通常指令取指比数据访问更重要）
    assign mem_araddr  = imem_arvalid ? imem_araddr + 32'h50000000 : dmem_araddr;

    // 反馈 Ready 信号给 CPU
    assign imem_arready = mem_arready & imem_arvalid; // 只有 mem_arvalid 是 IMEM 发出时，才连接
    assign dmem_arready = mem_arready & dmem_arvalid;

    // 2. Mem Read Data Channel (R)
    // 反馈 Data/Valid/Ready 信号给 CPU (Demux)
    assign imem_rdata   = mem_rdata;
    assign imem_rvalid  = mem_rvalid & imem_arvalid; // 假设返回的数据是 IMEM 请求的
    assign imem_rlast   = 1'b1; // 假设为单拍传输

    assign dmem_rdata   = mem_rdata;
    assign dmem_rvalid  = mem_rvalid & dmem_arvalid; // 假设返回的数据是 DMEM 请求的

    // Mem 的 Rready 接收来自 IMEM 或 DMEM 的 ready 信号
    assign mem_rready   = (imem_rready & imem_arvalid) | (dmem_rready & dmem_arvalid);


    // 3. Mem Write Address/Data Channel (AW/W)
    // DMEM 的写访问直接连接到 Mem (IMEM 只有读操作)
    assign mem_awaddr   = dmem_awaddr;
    assign mem_awvalid  = dmem_awvalid;
    assign mem_wdata    = dmem_wdata;
    assign mem_wstrb    = dmem_wstrb;
    assign mem_wvalid   = dmem_wvalid;

    // 反馈 Ready 信号给 CPU
    assign dmem_awready = mem_awready;
    assign dmem_wready  = mem_wready;

    // 4. Mem Write Response Channel (B)
    assign dmem_bvalid  = mem_bvalid;
    assign mem_bready  = dmem_bready;


    // --- 例化 CPU Core ---
    ysyx_24080032_Core u_core (
        .clock             (clock),
        .reset             (reset),
        // IMEM 接口
        .io_imem_araddr    (imem_araddr),
        .io_imem_arvalid   (imem_arvalid),
        .io_imem_arready   (imem_arready),
        .io_imem_arsize    (imem_arsize),
        .io_imem_arburst   (imem_arburst),
        .io_imem_rdata     (imem_rdata),
        .io_imem_rvalid    (imem_rvalid),
        .io_imem_rready    (imem_rready),
        .io_imem_rlast     (imem_rlast),
        // DMEM 接口
        .io_dmem_araddr    (dmem_araddr),
        .io_dmem_arvalid   (dmem_arvalid),
        .io_dmem_arready   (dmem_arready),
        .io_dmem_arsize    (dmem_arsize),
        .io_dmem_rdata     (dmem_rdata),
        .io_dmem_rvalid    (dmem_rvalid),
        .io_dmem_rready    (dmem_rready),
        .io_dmem_awaddr    (dmem_awaddr),
        .io_dmem_awvalid   (dmem_awvalid),
        .io_dmem_awready   (dmem_awready),
        .io_dmem_awsize    (dmem_awsize),
        .io_dmem_wdata     (dmem_wdata),
        .io_dmem_wstrb     (dmem_wstrb),
        .io_dmem_wvalid    (dmem_wvalid),
        .io_dmem_wready    (dmem_wready),
        .io_dmem_bvalid    (dmem_bvalid),
        .io_dmem_bready    (dmem_bready)
    );


    // --- 例化 Mem 模块 ---
    Mem u_mem (
        .clk        (clock),
        .rst        (reset),
        // AR
        .araddr     (mem_araddr),
        .arvalid    (mem_arvalid),
        .arready    (mem_arready),
        // R
        .rdata      (mem_rdata),
        .rresp      (mem_rresp),
        .rvalid     (mem_rvalid),
        .rready     (mem_rready),
        // AW
        .awaddr     (mem_awaddr),
        .awvalid    (mem_awvalid),
        .awready    (mem_awready),
        // W
        .wdata      (mem_wdata),
        .wstrb      (mem_wstrb),
        .wvalid     (mem_wvalid),
        .wready     (mem_wready),
        // B
        .bresp      (mem_bresp),
        .bvalid     (mem_bvalid),
        .bready     (mem_bready)
    );

endmodule