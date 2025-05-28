################################################################        YOSYS        ################################################################
SOC_PATH ?= ../npc-chisel-soc/single_cycle_riscv32i/vsrc

################################################################        YOSYS        ################################################################
PRJ_PATH ?= .

DESIGN ?= NPC
SDC_FILE ?= $(PRJ_PATH)/syn/sdc/NPC.sdc
RTL_FILES ?= $(shell find $(PRJ_PATH)/vsrc -name "*.sv" -or -name "*.v")
export CLK_FREQ_MHZ ?= 450

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


COPY_FILE:
	@rm -rf $(PRJ_PATH)/syn/result/*
	@rm -rf $(PRJ_PATH)/vsrc/*
	@cp $(SOC_PATH)/* $(PRJ_PATH)/vsrc
	@sed -i '/import "DPI-C" function void npc_trap();/d' $(PRJ_PATH)/vsrc/Ebreak.sv
	@sed -i "/npc_trap()/c\;" $(PRJ_PATH)/vsrc/Ebreak.sv
	@sed -i "s/\([^a-zA-Z0-9_]\)\(io_pipe_in_bits_id2exe_exe_fun == 5'hB\)/\1(\2)/g" $(PRJ_PATH)/vsrc/EXU.sv
	@sed -i "s/^\(io_pipe_in_bits_id2exe_exe_fun == 5'hB\)/(\1)/g" $(PRJ_PATH)/vsrc/EXU.sv
	@sed -i "s/\([^a-zA-Z0-9_]\)\(io_pipe_in_bits_id2exe_exe_fun == 5'hD\)/\1(\2)/g" $(PRJ_PATH)/vsrc/EXU.sv
	@sed -i "s/^\(io_pipe_in_bits_id2exe_exe_fun == 5'hD\)/(\1)/g" $(PRJ_PATH)/vsrc/EXU.sv

GEN_NPC:
	@make -C /home/yangpengcheng/ysyx/ysyx/ysyx-workbench/npc-chisel-soc/single_cycle_riscv32i verilog

TRUNC:
	@bash ./trunc.sh $(RESULT_DIR) $(DESIGN) $(PRJ_PATH)


sta: GEN_NPC COPY_FILE $(TIMING_RPT)
$(TIMING_RPT): $(SCRIPT_DIR)/sta.tcl $(SDC_FILE) $(NETLIST_FIXED_V)
	$(PRJ_PATH)/syn/bin/iEDA -script $^ $(DESIGN) 2>&1 | tee $(RESULT_DIR)/sta.log
	@make TRUNC


.PHONY: init syn fix-fanout sta clean