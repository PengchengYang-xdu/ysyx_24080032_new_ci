include /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/Makefile
#自2024-12-05添加这一行makefile, 此时做到GPIO结束, 也就是完成SEG显示学号, 前面由于没有添加导致没有commit追踪, 哭死

################################################################        VERILATOR        ################################################################
################################        SRC        ################################
TOPNAME = ysyxSoCFull
# VSRC = $(shell find $(abspath ./vsrc) -maxdepth 1 -type f \( -name "*.sv" -o -name "*.v" \))
VSRC = /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/npc-chisel-soc/single_cycle_riscv32i/final_rtl/build/ysyx_24080032.sv
CSRC = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
IMG ?= program.bin #image

################################        DIR        ################################
SOC_DIR = /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/ysyxSoC
BUILD_DIR = ./build
OBJ_DIR = $(BUILD_DIR)/obj_dir
BIN = $(BUILD_DIR)/$(TOPNAME)
PRJ_PATH = /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/npc-chisel-soc/single_cycle_riscv32i

################################        FLAG        ################################
VSRC += $(shell find $(abspath $(SOC_DIR)/build) -name "*.sv" -or -name "*.v")
VSRC += $(shell find $(abspath $(SOC_DIR)/perip) -name "*.sv" -or -name "*.v")
VERIARG = --cc -MMD --build --trace-threads 2 --public-flat-rw -top $(TOPNAME) --trace-fst --no-timing --timescale "1ns/1ns" -I$(SOC_DIR)/perip/uart16550/rtl -I$(SOC_DIR)/perip/spi/rtl -O3 -j 64 --autoflush
LDFLAGS += -lSDL2 -lSDL2_image $(shell llvm-config --libs) -lreadline -ldl -pie
INC_PATH += $(PRJ_PATH)/include
INCFLAGS = $(addprefix -I, $(INC_PATH))
CFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""

################################        SHELL        ################################
$(shell mkdir -p $(BUILD_DIR))

################################       COMMAND       ################################
$(BIN):  $(CSRC) $(VSRC)
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	verilator $(VERIARG) \
		$^ \
		$(addprefix -LDFLAGS , $(LDFLAGS)) \
		$(addprefix -CFLAGS , $(CFLAGS)) \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))


################################################################        MILL        ################################################################
################################        SRC        ################################
GEN_DIR = ./vsrc
PRJ = playground
################################        FLAG        ################################
MILLFLAG = --split-verilog --target-dir $(GEN_DIR)
################################       COMMAND       ################################
verilog:
	$(call git_commit, "generate verilog")
	mkdir -p $(GEN_DIR)
	CHISEL_FIRTOOL_PATH=/home/yangpengcheng/workspace/tools/firtool-1.131.0/bin mill -i $(PRJ).runMain Elaborate \
	$(MILLFLAG)

# IMG_CHAR = /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/ysyxSoC/char-test/build/char-test.bin

wave:
	gtkwave *.vcd

clr:
	rm -rf $(BUILD_DIR)
	rm -rf *.vcd
	rm -rf $(GEN_DIR)
	rm -rf *.fst





################################################################        YOSYS        ################################################################
DESIGN ?= NPC
SDC_FILE ?= $(PRJ_PATH)/syn/sdc/NPC.sdc
RTL_FILES ?= $(shell find $(PRJ_PATH)/vsrc -name "*.sv" -or -name "*.v")
export CLK_FREQ_MHZ ?= 100

RESULT_DIR = $(PRJ_PATH)/syn/result/$(DESIGN)-$(CLK_FREQ_MHZ)MHz
SCRIPT_DIR = $(PRJ_PATH)/syn/scripts
NETLIST_SYN_V   = $(RESULT_DIR)/$(DESIGN).netlist.syn.v
NETLIST_FIXED_V = $(RESULT_DIR)/$(DESIGN).netlist.fixed.v
TIMING_RPT = $(RESULT_DIR)/$(DESIGN).rpt

init:
	bash -c "$$(wget -O - https://ysyx.oscc.cc/slides/resources/scripts/init-yosys-sta.sh)"

syn: $(NETLIST_SYN_V)
$(NETLIST_SYN_V): $(RTL_FILES) $(SCRIPT_DIR)/yosys.tcl
	mkdir -p $(@D)
	echo tcl $(SCRIPT_DIR)/yosys.tcl $(DESIGN) \"$(RTL_FILES)\" $@ | yosys -l $(@D)/yosys.log -s -

fix-fanout: $(NETLIST_FIXED_V)
$(NETLIST_FIXED_V): $(SCRIPT_DIR)/fix-fanout.tcl $(SDC_FILE) $(NETLIST_SYN_V)
	$(PRJ_PATH)/syn/bin/iEDA -script $^ $(DESIGN) $@ 2>&1 | tee $(RESULT_DIR)/fix-fanout.log

sta: $(TIMING_RPT)
$(TIMING_RPT): $(SCRIPT_DIR)/sta.tcl $(SDC_FILE) $(NETLIST_FIXED_V)
	$(PRJ_PATH)/syn/bin/iEDA -script $^ $(DESIGN) 2>&1 | tee $(RESULT_DIR)/sta.log

clean:
	-rm -rf $(PRJ_PATH)/syn/result/

.PHONY: init syn fix-fanout sta clean verilog iverilog




################################################################        NVBOARD        ################################################################
################################        SRC        ################################
NVBOARD_HOME = /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/nvboard
WORKSPACE = ./nvboard
NXDC_FILES = $(WORKSPACE)/constr/$(TOPNAME).nxdc
NV_INC_PATH ?=
NV_BUILD_DIR = $(WORKSPACE)/build
NV_OBJ_DIR = $(NV_BUILD_DIR)/obj_dir
NV_BIN = $(NV_BUILD_DIR)/$(TOPNAME)

$(shell mkdir -p $(NV_BUILD_DIR))

# constraint file
SRC_AUTO_BIND = $(abspath $(NV_BUILD_DIR)/auto_bind.cpp)
$(SRC_AUTO_BIND): $(NXDC_FILES)
	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py $^ $@

CSRC += $(SRC_AUTO_BIND)

# rules for NVBoard
include $(NVBOARD_HOME)/scripts/nvboard.mk

################################       COMMAND       ################################
$(NV_BIN): $(CSRC) $(VSRC) $(NVBOARD_ARCHIVE)
	$(call git_commit, "sim NV") # DO NOT REMOVE THIS LINE!!!
	verilator $(VERIARG) \
		$^ \
		$(addprefix -LDFLAGS , $(LDFLAGS)) \
		$(addprefix -CFLAGS , $(CFLAGS)) \
		--Mdir $(NV_OBJ_DIR) --exe -o $(abspath $(NV_BIN))

clean_nv:
	rm -rf $(NV_BUILD_DIR)







# CFLAGS += -DNPCCONFIG_ITRACE
# CFLAGS += -DNPCCONFIG_FTRACE
# CFLAGS += -DNPCCONFIG_MTRACE
# CFLAGS += -DNPCCONFIG_DUMPWAVE
# CFLAGS += -DNPCCONFIG_WATCHPOINT
CFLAGS += -DNPCCONFIG_DIFFTEST
CFLAGS += -DNPCCONFIG_LIGHTSSS



# if you want to just run the ysyxsoc
run: $(BIN)
	@LD_LIBRARY_PATH=$(dir $(NEMU_SO)) $(BIN) $(ARGS) $(IMG)

#if you want to run the nvboard
# CFLAGS += -DNV_BOARD
# run: $(NV_BIN)
# 	@LD_LIBRARY_PATH=$(dir $(NEMU_SO)) $(NV_BIN) $(ARGS) $(IMG)









#如果想要评估性能,在microbench文件夹下(别忘了把trm.c的mainargs换成train),使用指令 make ARCH=riscv32e-ysyxsoc perf
#所有评估性能的commit都提交到perf(pipe)分支上面,这个分支也是用来完成性能优化和缓存的分支(后来做到pipe用pipe了)

PERF_MD = /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/npc-chisel-soc/single_cycle_riscv32i/perf/perf.md
SAVE_MD = /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/npc-chisel-soc/single_cycle_riscv32i/records/perf_$(shell date +%Y%m%d_%H%M%S).md
EXPLAIN = 任务进度:流水线所有东西基本完成 还剩fencei 转发做掉了 跑通了cputest rtt microbench test (之前那次的9月1号perf没用 difftest trigger了) 现在debug完成了 再跑一次

perf: checkout run save_perf

checkout:
	@git checkout pipe
	@echo "现在切换到了pipe分支,用于评估性能并commit"

save_perf:
	@git commit --allow-empty -am "$(EXPLAIN)"
	@echo "已提交并保存这次的perf数据"
	@CURRENT_HASH=$$(git rev-parse HEAD); \
	cat $(PERF_MD) > $(SAVE_MD); \
	echo -e "本次提交哈希值: $$CURRENT_HASH $(EXPLAIN)" >> $(SAVE_MD); \
	echo "性能数据保存到: $(SAVE_MD)"
































################################################################        IVERILOG        ################################################################
CONFIG_FILE = /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/npc-chisel-soc/single_cycle_riscv32i/src/main/common/Config.scala
IVERILOG_DIR = ./iverilog

TARGET ?= amtest

AM_DIR = /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/am-kernels/tests/am-tests
RTT_DIR = /home/yangpengcheng/ysyx/rt-thread-am/bsp/abstract-machine
MB_DIR = /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/am-kernels/benchmarks/microbench

ifeq ($(TARGET), amtest)
	TARGET_DIR = $(AM_DIR)
else ifeq ($(TARGET), rtthread)
    TARGET_DIR = $(RTT_DIR)
else ifeq ($(TARGET), microbench)
    TARGET_DIR = $(MB_DIR)
else
    $(error Unknown TARGET "$(TARGET)")
endif

IVERILOG_TB_SRC = $(IVERILOG_DIR)/tb/tb_top.sv
IVERILOG_MEM_SRC =  $(IVERILOG_DIR)/mem/axi4_memory.sv #存放额外实现的mem的axi读写接口
IVERILOG_V_SRC = $(IVERILOG_DIR)/src/ysyx_24080032.sv

# IVERILOG_FINAL_V_SRC =  ./final_rtl/build/ysyx_24080032.sv #前仿
IVERILOG_FINAL_V_SRC =  ../../ppa_opt/syn/result/ysyx_24080032-500MHz/ysyx_24080032.v #网表仿真

IVERILOG_ELF_SRC = $(TARGET_DIR)/build/$(TARGET)-riscv32e-npc.elf
IVERILOG_BIN_SRC = $(TARGET_DIR)/build/$(TARGET)-riscv32e-npc.bin
IVERILOG_HEX_SRC = ./iverilog/hex/code-riscv32e-npc.hex

IVERILOG_OUT = $(IVERILOG_DIR)/build/top.vvp
IVERILOG_WAVE = $(IVERILOG_DIR)/wave/top.vcd

iverilog:
	@echo ">> 编译出elf文件..."
	@make -C $(TARGET_DIR) ARCH=riscv32e-npc

	@echo ">> 切换pc从0x80000000开始..."
	@sed -i 's/val START_ADDR = "h30000000"/val START_ADDR = "h80000000"/' $(CONFIG_FILE)

	@echo ">> mill生成v文件..."
	@make clr
	@make verilog

	@echo ">> 合并大文件..."
	@make -C ./final_rtl verilog
	@mkdir -p $(dir $(IVERILOG_V_SRC))
	@cp $(IVERILOG_FINAL_V_SRC) $(IVERILOG_V_SRC)

	@echo ">> 删除DPI-C ECALL"
	@sed -i '/import "DPI-C" function void npc_trap();/d' $(IVERILOG_V_SRC)
	@sed -i "/npc_trap()/c\;" $(IVERILOG_V_SRC)

	@echo ">> 切换pc从0x30000000开始..."
	@sed -i 's/val START_ADDR = "h80000000"/val START_ADDR = "h30000000"/' $(CONFIG_FILE)

	@echo ">> 从elf生成hex文件..."
	@mkdir -p $(dir $(IVERILOG_HEX_SRC))
	# @riscv64-unknown-linux-gnu-objcopy -O verilog --adjust-vma -0x80000000 $(IVERILOG_ELF_SRC) $(IVERILOG_HEX_SRC)
	@python $(IVERILOG_DIR)/hex/makehex.py $(IVERILOG_BIN_SRC) 33554432 > $(IVERILOG_HEX_SRC)

	@echo ">> 开始iverilog编译..."
	@mkdir -p $(dir $(IVERILOG_OUT))
	@mkdir -p $(dir $(IVERILOG_WAVE))
	@iverilog -g2012 -Wall -D__ICARUS__ -o $(IVERILOG_OUT) \
		$(IVERILOG_TB_SRC) $(IVERILOG_MEM_SRC) $(IVERILOG_V_SRC) ../../ppa_opt/syn/nangate45/sim/cells.v

	@echo ">> 编译完成，开始执行仿真..."
	@vvp $(IVERILOG_OUT)
	@echo ">> 仿真完成，波形文件生成在: $(IVERILOG_WAVE)"



iverilog_clr:
	@rm -rf ./iverilog/hex/code-riscv32e-npc.hex
	@rm -rf ./iverilog/src
	@rm -rf ./iverilog/build
	@rm -rf ./iverilog/wave