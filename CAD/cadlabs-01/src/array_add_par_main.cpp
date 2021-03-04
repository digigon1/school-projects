/**
 * Tests for array_add
 */

#include <iostream>
#include <array>
#include <memory>
#include <algorithm>
#include <chrono>

#include <functions.h>


template <std::size_t Size, unsigned NRuns, unsigned NThreads>
double array_add_test() {

    std::unique_ptr<std::array<int, Size>> result = std::make_unique<std::array<int, Size>>();

    std::unique_ptr<std::array<int, Size>> a = std::make_unique<std::array<int, Size>>();
    std::unique_ptr<std::array<int, Size>> b = std::make_unique<std::array<int, Size>>();

    std::fill (a->begin(), a->end(), 1);
    std::fill (b->begin(), b->end(), 2);

    unsigned long elapsed = 0;
    for (int i = 0 ; i < NRuns; i++) {
        auto start = std::chrono::steady_clock::now();
        cad::array_add_par<NThreads>(*result, *a, *b);
        elapsed += std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::steady_clock::now() - start).count();
    }

    return elapsed/ ((double) NRuns) ;
};


static constexpr unsigned NRuns = 10;

int main() {

    double elapsed  = array_add_test<1000000, NRuns, 2>();
    std::cout << "Size: 1000000 - elapsed time: " << elapsed << " milliseconds \n";

    elapsed  = array_add_test<10000000, NRuns, 2>();
    std::cout << "Size: 10000000 - elapsed time: " << elapsed << " milliseconds \n";

    elapsed  = array_add_test<100000000, NRuns, 2>();
    std::cout << "Size: 100000000 - elapsed time: " << elapsed << " milliseconds \n";
}

