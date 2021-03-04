/**
 * Tests for array_add
 */

#include <array>
#include <memory>
#include <algorithm>
#include <chrono>

#include "cad_test.h"
#include <functions.h>


template <std::size_t SIZE, unsigned NRuns>
double array_add_test() {

    std::unique_ptr<std::array<int, SIZE>> result = std::make_unique<std::array<int, SIZE>>();

    std::unique_ptr<std::array<int, SIZE>> a = std::make_unique<std::array<int, SIZE>>();
    std::unique_ptr<std::array<int, SIZE>> b = std::make_unique<std::array<int, SIZE>>();

    std::fill (a->begin(), a->end(), 1);
    std::fill (b->begin(), b->end(), 2);

    unsigned long elapsed = 0;
    for (int i = 0 ; i < NRuns; i++) {
        auto start = std::chrono::steady_clock::now();
        cad::array_add(*result, *a, *b);
        elapsed += std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::steady_clock::now() - start).count();
    }

    cad::expect_container_value(*result, 3);

    return elapsed/ ((double) NRuns) ;
};


static constexpr unsigned NRuns = 10;

TEST(ArrayAdd, SIZE_1000000) {
    double elapsed  = array_add_test<1000000, NRuns>();
    std::cout << "Elapsed time: " << elapsed << " milliseconds \n";
}


TEST(ArrayAdd, SIZE_10000000) {
    double elapsed  = array_add_test<10000000, NRuns>();
    std::cout << "Elapsed time: " << elapsed << " milliseconds \n";
}

TEST(ArrayAdd, Size_100000000) {
    double elapsed  = array_add_test<100000000, NRuns>();
    std::cout << "Elapsed time: " << elapsed << " milliseconds \n";
}

