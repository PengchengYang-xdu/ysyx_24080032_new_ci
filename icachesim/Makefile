ICAHCESIM_HOME = ../icachesim

BUILD_DIR = $(ICAHCESIM_HOME)/build
ICACHESIM_LOG_DIR = $(ICAHCESIM_HOME)/icachesim_log
SRCS = $(shell find $(ICAHCESIM_HOME)/src/ -name "*.cpp")
SRCS += $(shell find $(ICAHCESIM_HOME)/src/ -name "*.c")
CPPFLAGS = -std=c++17 -O2 -pthread
INCLUDE_FLAGS = -I$(ICAHCESIM_HOME)/include

bin = $(BUILD_DIR)/icachesim

$(bin): $(SRCS)
    @echo "building icachesim..."
    @mkdir -p $(BUILD_DIR)
    @g++ $(CPPFLAGS) -o $@ $(INCLUDE_FLAGS) $^

run: $(bin)
    @echo "running icachesim..."
    $(bin) $(ICACHESIM_LOG_DIR)/icachesim.log.bz2

clean:
	rm -rf $(BUILD_DIR)
	rm -rf ./printf.log
