/***************************************************************************************
deigned by ypc
***************************************************************************************/
#include <nvboard.h>
#include <circuit.h>
#include <getopt.h>
#include <mem.h>
#include <utils.h>
#include <debug.h>

void init_log(const char *log_file);
void init_mem();
void init_difftest(char *ref_so_file, long img_size);
void init_device();
void init_sdb();
extern "C" void init_disasm(const char *triple);

static void welcome() {
  Log("Trace: %s", ANSI_FMT("ON", ANSI_FG_GREEN));
  Log("If trace is enabled, a log file will be generated "
        "to record the trace. This may lead to a large log file. "
        "If it is not necessary, you can disable it in menuconfig");
  Log("Build time: %s, %s", __TIME__, __DATE__);
  printf("Welcome to %s-ysyxsoc!\n", ANSI_FMT("riscv32e", ANSI_FG_YELLOW ANSI_BG_RED));
  printf("For help, type \"help\"\n");
}

void sdb_set_batch_mode();

static char *elf_file = NULL;
static char *log_file = NULL;
static char *diff_so_file = NULL;
static char *img_file = NULL;

static long load_img() {
  if (img_file == NULL) {
    Log("No image is given. Use the default build-in image.");
    return 1024; // built-in image size
  }

  FILE *fp = fopen(img_file, "rb");
  Assert(fp, "Can not open '%s'", img_file);

  fseek(fp, 0, SEEK_END);
  long size = ftell(fp);

  Log("The image is %s, size = %ld", img_file, size);
  fflush(stdout);

  fseek(fp, 0, SEEK_SET);
  int ret = fread(guest_to_host(FLASH_BASE), size, 1, fp);
  assert(ret == 1);

  fclose(fp);

//   for (long i = 0; i < size; i+=4) {
//       uint32_t instruction = *(uint32_t *)(guest_to_host(CONFIG_MBASE + i));
//       printf("Instruction at Memory[%#lx] = %#x\n", CONFIG_MBASE + i, instruction);
//   }





/* just for char-flash-test and xip-flash-test use */
    // FILE *char_test = fopen("/home/ypc/Desktop/ysyx/ysyx-workbench/am-kernels/tests/char-test/build/chartest-riscv32e-ysyxsoc.bin", "rb");
    // fseek(char_test, 0, SEEK_END);
    // long size_t = ftell(char_test);
    // fflush(stdout);
    // fseek(char_test, 0, SEEK_SET);
    // int ret_t = fread(guest_to_host(FLASH_BASE), size_t, 1, char_test);
    // assert(ret_t == 1);
    // fclose(char_test);
    // for (long i = 0; i < size; i+=4) {
    // uint32_t instruction = *(uint32_t *)(guest_to_host(FLASH_BASE + i));
    // printf("Instruction at Memory[%#lx] = %#x\n", FLASH_BASE + i, instruction);
    // }
/* just for char-flash-test and xip-flash-test use */






  return size;
}

static int parse_args(int argc, char *argv[]) {
  const struct option table[] = {
    {"elf"      , required_argument, NULL, 'e'},
    {"batch"    , no_argument      , NULL, 'b'},
    {"log"      , required_argument, NULL, 'l'},
    {"diff"     , required_argument, NULL, 'd'},
    {"help"     , no_argument      , NULL, 'h'},
    {0          , 0                , NULL,  0 },
  };
  int o;
  while ( (o = getopt_long(argc, argv, "-bhl:d:e:", table, NULL)) != -1) {
    switch (o) {
      case 'e': elf_file = optarg; break;
      case 'b': sdb_set_batch_mode(); break;
      case 'l': log_file = optarg; break;
      case 'd': diff_so_file = optarg; break;
      case 1: img_file = optarg; return 0;
      default:
        printf("\t-e,--elf=FILE           elf file pointed\n");
        printf("Usage: %s [OPTION...] IMAGE [args]\n\n", argv[0]);
        printf("\t-b,--batch              run with batch mode\n");
        printf("\t-l,--log=FILE           output log to FILE\n");
        printf("\t-d,--diff=REF_SO        run DiffTest with reference REF_SO\n");
        printf("\n");
        exit(0);
    }
  }
  return 0;
}

void init_monitor(int argc, char *argv[]) {
  /* Perform some global initialization. */

  /* Parse arguments. */
  parse_args(argc, argv);

  parse_elf(elf_file);
  // printf_symbol();

  /* Open the log file. */
  init_log(log_file);

  /* Initialize memory. */
  init_mem();
  init_flash();
  init_psram();
  init_sdram();

  /* Load the image to memory. This will overwrite the built-in image. */
  long img_size = load_img();

  /* Initialize differential testing. */
  #ifdef NPCCONFIG_DIFFTEST
  init_difftest(diff_so_file, img_size);
  printf("difftest init success\n");
  #endif

  init_disasm("riscv32-pc-linux-gnu");

  /* Initialize the simple debugger. */
  init_sdb();

  #ifdef NV_BOARD
  nvboard_bind_all_pins(top);
  nvboard_init();
  #endif

  /* Display welcome message. */
  welcome();
}
