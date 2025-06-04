include /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/Makefile
#自2024-12-05添加这一行makefile, 此时做到GPIO结束, 也就是完成SEG显示学号, 前面由于没有添加导致没有commit追踪, 哭死

################################################################        VERILATOR        ################################################################
################################        SRC        ################################
TOPNAME = ysyxSoCFull
VSRC = $(shell find $(abspath ./vsrc) -name "*.sv" -or -name "*.v")
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
	mill -i $(PRJ).runMain Elaborate \
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

.PHONY: init syn fix-fanout sta clean




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



# if you want to just run the ysyxsoc
run: $(BIN)
	@LD_LIBRARY_PATH=$(dir $(NEMU_SO)) $(BIN) $(ARGS) $(IMG) 

#if you want to run the nvboard
# CFLAGS += -DNV_BOARD
# run: $(NV_BIN)
# 	@LD_LIBRARY_PATH=$(dir $(NEMU_SO)) $(NV_BIN) $(ARGS) $(IMG)









#如果想要评估性能,在microbench文件夹下(别忘了把trm.c的mainargs换成train),使用指令 make ARCH=riscv32e-ysyxsoc perf
#所有评估性能的commit都提交到perf分支上面,这个分支也是用来完成性能优化和缓存的分支

PERF_MD = /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/npc-chisel-soc/single_cycle_riscv32i/perf/perf.md
SAVE_MD = /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/npc-chisel-soc/single_cycle_riscv32i/records/perf_$(shell date +%Y%m%d_%H%M%S).md
EXPLAIN = 任务进度:完成所有的简易缓存实验，接下来做流水线 属性:icache开启,block=8,sets=4,ways=1,policy=lru

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
