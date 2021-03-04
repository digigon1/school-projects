//
// Created by Hervé Paulino on 24/01/17.
//

#include <chrono>

#include "cad_test.h"
#include <nbodies.h>


using namespace cad;

TEST(NBodies, Bodies_4k_iterations_100) {

    nbodies nb(data_path + "nbodies4096.txt", 100);
    auto start = std::chrono::steady_clock::now();
    nb.run();
    unsigned long elapsed = std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::steady_clock::now() - start).count();

    std::cout << "Elapsed time: " << elapsed << " milliseconds \n";
}



TEST(NBodies, Bodies_64k_iterations_1) {

    nbodies nb(data_path + "nbodies65536.txt", 1);
    auto start = std::chrono::steady_clock::now();
    nb.run();
    unsigned long elapsed = std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::steady_clock::now() - start).count();

    std::cout << "Elapsed time: " << elapsed << " milliseconds \n";
}
