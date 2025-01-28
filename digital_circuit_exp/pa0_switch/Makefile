TOPNAME = top
VSRC = $(shell find $(abspath ./vsrc) -name "*.v")
CSRC = $(shell find $(abspath ./csrc) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
# SRC_AUTO_BIND = $(abspath $(BUILD_DIR)/auto_bind.cpp)
# CSRC += $(SRC_AUTO_BIND)
CONSTR = constr/top.nxdc

BUILD_DIR = ./build
OBJ_DIR = $(BUILD_DIR)/obj_dir
BIN = $(BUILD_DIR)/$(TOPNAME)

VERIARG = -Wall --cc -MMD --build 

LDFLAGS += -lSDL2 -lSDL2_image

INCFLAGS = $(addprefix -I, $(INC_PATH))

CFLAGS += $(INCFLAGS)  -DTOP_NAME="\"V$(TOPNAME)\""

include $(NVBOARD_HOME)/scripts/nvboard.mk
$(shell mkdir -p $(BUILD_DIR))

$(BIN):  $(CSRC) $(VSRC)
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	verilator $(VERIARG) \
		-top $(TOPNAME) \
		$^ \
		--trace \
		$(addprefix -LDFLAGS , $(LDFLAGS)) \
		$(addprefix -CFLAGS , $(CFLAGS)) \
		--trace \
		--Mdir $(OBJ_DIR)  --exe -o $(abspath $(BIN))


run: $(BIN)
	@$^

clr: 
	rm -rf $(BUILD_DIR)


