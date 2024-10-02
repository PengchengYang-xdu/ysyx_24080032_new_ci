TOPNAME = ysyx_24080032_riscv32i
VSRC = $(shell find $(abspath ./vsrc) -name "*.v")
TBVSRC = $(shell find $(abspath ./tb) -name "*.v")
CSRC = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
# SRC_AUTO_BIND = $(abspath $(BUILD_DIR)/auto_bind.cpp)
# CSRC += $(SRC_AUTO_BIND)
# CONSTR = ./constr/top.nxdc
IMG ?= program.bin #image

BUILD_DIR = ./build
OBJ_DIR = $(BUILD_DIR)/obj_dir
BIN = $(BUILD_DIR)/$(TOPNAME)

VERIARG = --cc -MMD --build 

LDFLAGS += -lSDL2 -lSDL2_image

INCFLAGS = $(addprefix -I, $(INC_PATH))

CFLAGS += $(INCFLAGS)  -DTOP_NAME="\"V$(TOPNAME)\""

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
	@$(BIN) $(IMG)

clr: 
	rm -rf $(BUILD_DIR)
	rm -rf *.vcd

sim: 
	iverilog -o run.out $(VSRC) $(TBVSRC)
	vvp -n run.out
	rm -rf *.out

wave: 
	gtkwave *.vcd


