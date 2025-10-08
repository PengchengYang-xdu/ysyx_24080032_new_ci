/***************************************************************************************
deigned by ypc
***************************************************************************************/

#include <common.h>
#include <circuit.h>
#include <mem.h>
#include <utils.h>

#include <lightsss.h> // 确保路径正确

extern LightSSS lightsss;

void init_monitor(int, char *[]);
void sdb_mainloop();

int main(int argc, char *argv[]) {
    get_time();
    init_monitor(argc, argv);
    #ifdef NPCCONFIG_DUMPWAVE
    init_wave("../build/wave_father.fst");
    #endif
    Verilated::commandArgs(argc, argv);
    reset(10);
    sdb_mainloop();
    close_wave(0);
    #ifdef NPCCONFIG_LIGHTSSS
        lightsss.do_clear(); // 在正常退出时清理子进程
    #endif
}
