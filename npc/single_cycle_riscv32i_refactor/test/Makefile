TOPNAME = ysyx_24080032_riscv32i
BUILD_DIR = ./build
OBJ_DIR = $(BUILD_DIR)/obj_dir
BIN = $(BUILD_DIR)/$(TOPNAME)
LDFLAGS += -lSDL2 -lSDL2_image $(shell llvm-config --libs) -lreadline -ldl -pie
VSRC = $(shell find $(abspath ../vsrc) -name "*.v")
CSRC = ./main.cpp
VERIARG = --cc -MMD --build 
CFLAGS += $(INCFLAGS) -DTOP_NAME="\"V$(TOPNAME)\""
CFLAGS += -DNPCCONFIG_ITRACE 
# CFLAGS += -DNPCCONFIG_FTRACE 
# CFLAGS += -DNPCCONFIG_MTRACE 
CFLAGS += -DNPCCONFIG_DUMPWAVE 
# CFLAGS += -DNPCCONFIG_WATCHPOINT 
CFLAGS += -DNPCCONFIG_DIFFTEST 

run : $(CSRC) $(VSRC)
		verilator $(VERIARG) \
		--top $(TOPNAME) \
		--trace \
		$^ \
		$(addprefix -LDFLAGS , $(LDFLAGS)) \
		$(addprefix -CFLAGS , $(CFLAGS)) \
		--Mdir $(OBJ_DIR) --exe -o $(abspath $(BIN))
