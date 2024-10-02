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

INC_PATH += /home/ypc/Desktop/ysyx/ysyx-workbench/npc/single_cycle_riscv32i/include
INCFLAGS = $(addprefix -I, $(INC_PATH))

# include $(NVBOARD_HOME)/scripts/nvboard.mk
$(shell mkdir -p $(BUILD_DIR))

# $(BIN):  $(CSRC) $(VSRC) $(NVBOARD_ARCHIVE)
$(BIN):  $(CSRC) $(VSRC)
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	verilator $(VERIARG) \
		-top $(TOPNAME) \
		--trace \
		--timing \
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
CFLAGS += -DNPCCONFIG_DIFFTEST 
