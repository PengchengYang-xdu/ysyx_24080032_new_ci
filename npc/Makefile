STUID := ysyx_24080032
STUNAME = 杨鹏城

YSYXSOC ?= n

ifeq ($(ARCH), riscv32e-ysyxsoc)
YSYXSOC = y
else ifeq ($(ARCH), riscv32e-npc)
YSYXSOC = n
else
$(WARNING: ====================)
$(WARNING: ====UNKNOWN ARCH====)
$(WARNING: ====================)
endif
# __     _______ ____  ___ _     ___   ____
# \ \   / / ____|  _ \|_ _| |   / _ \ / ___|
#  \ \ / /|  _| | |_) || || |  | | | | |  _
#   \ V / | |___|  _ < | || |__| |_| | |_| |
#    \_/  |_____|_| \_\___|_____\___/ \____|

VERILOG_BUILD_DIR = ./build
TEMP_VERILOG_DIR = $(VERILOG_BUILD_DIR)/verilog_obj
FIRTOOL_DIR = ./
PRJ = playground
MILLFLAG = --target-dir $(TEMP_VERILOG_DIR)

OUTPUT_VERILOG_FILE = $(VERILOG_BUILD_DIR)/$(STUID).sv
INPUT_VERILOG_FILE = \
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

verilog:
	rm -rf $(VERILOG_BUILD_DIR)/$(STUID).sv
	mkdir -p $(VERILOG_BUILD_DIR) $(TEMP_VERILOG_DIR)
	CHISEL_FIRTOOL_PATH=$(FIRTOOL_DIR) mill -i $(PRJ).runMain Elaborate $(MILLFLAG)
	cat $(addprefix $(TEMP_VERILOG_DIR)/, $(INPUT_VERILOG_FILE)) > $(OUTPUT_VERILOG_FILE)
	rm -rf $(TEMP_VERILOG_DIR)


# __     _______ ____  ___ _        _  _____ ___  ____
# \ \   / / ____|  _ \|_ _| |      / \|_   _/ _ \|  _ \
#  \ \ / /|  _| | |_) || || |     / _ \ | || | | | |_) |
#   \ V / | |___|  _ < | || |___ / ___ \| || |_| |  _ <
#    \_/  |_____|_| \_\___|_____/_/   \_\_| \___/|_| \_\

TOPNAME = ysyxSoCFull
VERILATOR_BUILD_DIR = ./build
TEMP_VERILATOR_DIR = $(VERILATOR_BUILD_DIR)/verilator_obj
BIN = $(VERILATOR_BUILD_DIR)/$(TOPNAME)

SOC_DIR = ../ysyxSoC

VSRC = $(OUTPUT_VERILOG_FILE)
ifeq ($(YSYXSOC), y)
VSRC += $(shell find $(abspath $(SOC_DIR)/build) -name "*.sv" -or -name "*.v")
VSRC += $(shell find $(abspath $(SOC_DIR)/perip) -name "*.sv" -or -name "*.v")
endif
CSRC = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")

VERIARG = --cc -MMD --build --trace-threads 2 --public-flat-rw -top $(TOPNAME) --trace-fst --no-timing --timescale "1ns/1ns" -I$(SOC_DIR)/perip/uart16550/rtl -I$(SOC_DIR)/perip/spi/rtl -O3 -j 64 --autoflush
LDFLAGS += -lSDL2 -lSDL2_image $(shell llvm-config --libs) -lreadline -ldl -pie
INC_PATH = $(abspath ./include)
INCFLAGS = $(addprefix -I, $(INC_PATH))
CFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""
CFLAGS += -DNPCCONFIG_CI

# CFLAGS += -DNPCCONFIG_ITRACE
# CFLAGS += -DNPCCONFIG_FTRACE
# CFLAGS += -DNPCCONFIG_MTRACE
# CFLAGS += -DNPCCONFIG_DUMPWAVE
# CFLAGS += -DNPCCONFIG_WATCHPOINT
# CFLAGS += -DNPCCONFIG_DIFFTEST
# CFLAGS += -DNPCCONFIG_LIGHTSSS

$(BIN): $(CSRC) $(VSRC)
	rm -rf $(TEMP_VERILATOR_DIR)
	verilator $(VERIARG) \
		$^ \
		$(addprefix -LDFLAGS , $(LDFLAGS)) \
		$(addprefix -CFLAGS , $(CFLAGS)) \
		--Mdir $(TEMP_VERILATOR_DIR) --exe -o $(abspath $(BIN))

run: $(BIN)
	@$(BIN) $(ARGS) $(IMG)



# _____     _______ ____  ___ _     ___   ____
#|_ _\ \   / / ____|  _ \|_ _| |   / _ \ / ___|
# | | \ \ / /|  _| | |_) || || |  | | | | |  _
# | |  \ V / | |___|  _ < | || |__| |_| | |_| |
#|___|  \_/  |_____|_| \_\___|_____\___/ \____|

IMG ?= dummy-riscv32e-npc.bin

IVERILOG_DIR = ./iverilog
IVERILOG_TB_SRC = $(IVERILOG_DIR)/tb/tb_top.sv
IVERILOG_MEM_SRC =  $(IVERILOG_DIR)/mem/axi4_memory.sv

sim-iverilog:
	@echo "INFO: ========================================"
	@echo "INFO: ====修改cpu代码复位从0x80000000开始====="
	@echo "INFO: ========================================"








