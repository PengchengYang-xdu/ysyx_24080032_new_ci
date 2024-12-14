TOPNAME = ysyx_24080032_riscv32i

VSRC = $(shell find $(abspath ./vsrc) -name "*.v")
TBVSRC = $(shell find $(abspath ./tb) -name "*.v")
CSRC = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
IMG ?= program.bin #image

BUILD_DIR = ./build
OBJ_DIR = $(BUILD_DIR)/obj_dir
BIN = $(BUILD_DIR)/$(TOPNAME)

VERIARG = --cc -MMD --build 

LDFLAGS += -lSDL2 -lSDL2_image $(shell llvm-config --libs) -lreadline -ldl -pie

INC_PATH += /home/ypc/Desktop/ysyx/ysyx-workbench/npc/single_cycle_riscv32i_refactor/include
INCFLAGS = $(addprefix -I, $(INC_PATH))

# include $(NVBOARD_HOME)/scripts/nvboard.mk
$(shell mkdir -p $(BUILD_DIR))

# $(BIN):  $(CSRC) $(VSRC) $(NVBOARD_ARCHIVE)
$(BIN):  $(CSRC) $(VSRC)
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	verilator $(VERIARG) \
		-top $(TOPNAME) \
		--trace \
		$^ \
		$(addprefix -LDFLAGS , $(LDFLAGS)) \
		$(addprefix -CFLAGS , $(CFLAGS)) \
		--Mdir $(OBJ_DIR)  --exe -o $(abspath $(BIN))

# $(SRC_AUTO_BIND): $(CONSTR) 
# 	python3 $(NVBOARD_HOME)/scripts/auto_pin_bind.py  $^ $@

run: $(BIN)
	@LD_LIBRARY_PATH=$(dir $(NEMU_SO)) $(BIN) $(ARGS) $(IMG) 

clr: 
	rm -rf $(BUILD_DIR)
	rm -rf *.vcd

sim: 
	iverilog -o run.out $(VSRC) $(TBVSRC)
	vvp -n run.out
	rm -rf *.out

wave: 
	gtkwave *.vcd





CFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""
CFLAGS += -DNPCCONFIG_ITRACE 
# CFLAGS += -DNPCCONFIG_FTRACE 
# CFLAGS += -DNPCCONFIG_MTRACE 
CFLAGS += -DNPCCONFIG_DUMPWAVE 
# CFLAGS += -DNPCCONFIG_WATCHPOINT 
# CFLAGS += -DNPCCONFIG_DIFFTEST 
























PROJ_PATH = /home/ypc/Desktop/ysyx/ysyx-workbench/npc/single_cycle_riscv32i_refactor

DESIGN ?= ysyx_24080032_riscv32i
SDC_FILE ?= $(PROJ_PATH)/syn/sdc/ysyx_24080032_riscv32i.sdc
RTL_FILES ?= $(shell find $(PROJ_PATH)/vsrc -name "*.v")
export CLK_FREQ_MHZ ?= 100

RESULT_DIR = $(PROJ_PATH)/syn/result/$(DESIGN)-$(CLK_FREQ_MHZ)MHz
SCRIPT_DIR = $(PROJ_PATH)/syn/scripts
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
	$(PROJ_PATH)/syn/bin/iEDA -script $^ $(DESIGN) $@ 2>&1 | tee $(RESULT_DIR)/fix-fanout.log

sta: $(TIMING_RPT)
$(TIMING_RPT): $(SCRIPT_DIR)/sta.tcl $(SDC_FILE) $(NETLIST_FIXED_V)
	$(PROJ_PATH)/syn/bin/iEDA -script $^ $(DESIGN) 2>&1 | tee $(RESULT_DIR)/sta.log

clean:
	-rm -rf $(PROJ_PATH)/syn/result/

.PHONY: init syn fix-fanout sta clean
