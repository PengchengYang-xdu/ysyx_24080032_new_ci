STUDENT_ID := 24080032
VERILOG_SRC_DIR := ../vsrc
OUTPUT_DIR := ./build
OUTPUT_FILE := $(OUTPUT_DIR)/ysyx_$(STUDENT_ID).sv


INPUT_FILES := \
	ysyx_24080032.sv \
	ysyx_24080032_Core.sv \
	ysyx_24080032_Xbar.sv \
	ysyx_24080032_Clint.sv \
	ysyx_24080032_IFU.sv \
	ysyx_24080032_IDU.sv \
	ysyx_24080032_ISU.sv \
	ysyx_24080032_EXU.sv \
	ysyx_24080032_WBU.sv \
	ysyx_24080032_gpr_16x32.sv \
	ysyx_24080032_CSR.sv \
	ysyx_24080032_ALU.sv \
	ysyx_24080032_LSU.sv \
	ysyx_24080032_iCache.sv \
	ysyx_24080032_Ebreak.sv

# ==============================================================================
# 目标 (Targets)
# ==============================================================================

.PHONY: all verilog clean

# 默认目标：执行文件合并
all: verilog

# verilog_merge 目标：使用 cat 命令合并文件
verilog:
	@make clean
	@echo "-> 正在创建输出目录 $(OUTPUT_DIR)..."
	mkdir -p $(OUTPUT_DIR)
	@echo "-> 正在合并 Verilog 文件到 $(OUTPUT_FILE)..."
	cat $(addprefix $(VERILOG_SRC_DIR)/, $(INPUT_FILES)) > $(OUTPUT_FILE)
	@echo "-> 合并完成！输出文件：$(OUTPUT_FILE)"

# 清理目标：删除生成的文件
clean:
	@echo "-> 正在清理生成的 Verilog 文件..."
	rm -rf $(OUTPUT_DIR)