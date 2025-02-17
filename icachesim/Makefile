ICAHCESIM_HOME = ../icachesim

BUILD_DIR = $(ICAHCESIM_HOME)/build
ICACHESIM_LOG_DIR = $(ICAHCESIM_HOME)/icachesim_log
SRCS = $(shell find $(ICAHCESIM_HOME)/src/ -name "*.cpp")
SRCS += $(shell find $(ICAHCESIM_HOME)/src/ -name "*.c")
CPPFLAGS = -std=c++17 -O2 -pthread
INCLUDE_FLAGS = -I$(ICAHCESIM_HOME)/include

bin = $(BUILD_DIR)/icachesim

# 默认的cache参数
SETS ?= 16
WAYS ?= 3
BLOCK_SIZE ?= 4
POLICY ?= LRU

MD_FILE = icachesim_results.md

$(bin): $(SRCS)
	@echo "building icachesim..."
	@mkdir -p $(BUILD_DIR)
	@g++ $(CPPFLAGS) -o $@ $(INCLUDE_FLAGS) $^

run: $(bin)
	@echo "running icachesim..."
	@echo "SETS=$(SETS), WAYS=$(WAYS), BLOCK_SIZE=$(BLOCK_SIZE), POLICY=$(POLICY)"
	@./$(bin) $(SETS) $(WAYS) $(BLOCK_SIZE) $(POLICY) $(MD_FILE)

clean:
	rm -rf $(BUILD_DIR)
	rm -rf ./printf.log ./$(MD_FILE)

batch_run:
	@echo "Running multiple cache configurations..."
	$(MAKE) run SETS=16 WAYS=3 BLOCK_SIZE=4 POLICY=LRU
	$(MAKE) run SETS=16 WAYS=3 BLOCK_SIZE=8 POLICY=LRU
	@echo "Results written to $(MD_FILE)"
