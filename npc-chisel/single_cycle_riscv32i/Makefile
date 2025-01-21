include /home/ypc/Desktop/ysyx/ysyx-workbench/Makefile

################################################################        VERILATOR        ################################################################
################################        SRC        ################################
TOPNAME = NPC
VSRC = $(shell find $(abspath ./vsrc) -name "*.sv" -or -name "*.v")
CSRC = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
IMG ?= program.bin #image

################################        DIR        ################################
BUILD_DIR = ./build
OBJ_DIR = $(BUILD_DIR)/obj_dir
BIN = $(BUILD_DIR)/$(TOPNAME)
PRJ_PATH = /home/ypc/Desktop/ysyx/ysyx-workbench/npc-chisel/single_cycle_riscv32i

################################        FLAG        ################################
VERIARG = --cc -MMD --build --public-flat-rw -top $(TOPNAME) --trace --no-timing --timescale "1ns/1ns"
LDFLAGS += -lSDL2 -lSDL2_image $(shell llvm-config --libs) -lreadline -ldl -pie
INC_PATH += $(PRJ_PATH)/include
INCFLAGS = $(addprefix -I, $(INC_PATH))
CFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""

################################        SHELL        ################################
$(shell mkdir -p $(BUILD_DIR))

################################       COMMAND       ################################
$(BIN):  $(CSRC) $(VSRC)
	$(call git_commit, "sim RTL_NPC") # DO NOT REMOVE THIS LINE!!!
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

# run: $(BIN)
# 	@LD_LIBRARY_PATH=$(dir $(NEMU_SO)) $(BIN) $(ARGS) $(IMG) 

wave: 
	gtkwave *.vcd

clr: 
	rm -rf $(BUILD_DIR)
	rm -rf *.vcd
	rm -rf $(GEN_DIR)





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



# CFLAGS += -DNPCCONFIG_ITRACE 
# CFLAGS += -DNPCCONFIG_FTRACE 
# CFLAGS += -DNPCCONFIG_MTRACE 
# CFLAGS += -DNPCCONFIG_DUMPWAVE 
# CFLAGS += -DNPCCONFIG_WATCHPOINT 
# CFLAGS += -DNPCCONFIG_DIFFTEST 



# if you want to just run the npc
run: $(BIN)
	@LD_LIBRARY_PATH=$(dir $(NEMU_SO)) $(BIN) $(ARGS) $(IMG) 

#if you want to run the nvboard
# CFLAGS += -DNV_BOARD 
# run: $(NV_BIN)
# 	@LD_LIBRARY_PATH=$(dir $(NEMU_SO)) $(NV_BIN) $(ARGS) $(IMG)
