VSRCS = $(shell find $(./vsrc ) -name "*.v")
CSRCS = $(shell find $(./csrc ) -name "*.c" -or -name "*.cc" -or -name "*.cpp")
 
INCLUDE = ./vsrc/include

sim:
	$(call git_commit, "sim RTL") # DO NOT REMOVE THIS LINE!!!
	@echo $(VSRCS)
	verilator --trace --cc --exe --build \
	--top-module top \
	-I$(INCLUDE) ./csrc/sim_main.cpp $(VSRCS) \
	-LDFLAGS -"lreadline"
 
vd:	
	./obj_dir/Vtop	
	gtkwave *.vcd
 
clr:
	rm -rf obj_dir
	rm -f $(shell find -name "*.vcd")
 
include ../Makefile


