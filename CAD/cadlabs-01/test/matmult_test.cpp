//
// Created by Hervé Paulino on 24/01/17.
//

#include <array>
#include <memory>
#include <algorithm>
#include <chrono>

#include "cad_test.h"
#include <functions.h>


template <std::size_t Size, unsigned NRuns>
double matmult_test() {

    auto result = std::make_unique<cad::matrix<int, Size, Size>>();

    auto a = std::make_unique<cad::matrix<int, Size, Size>>();
    auto b = std::make_unique<cad::matrix<int, Size, Size>>();

    std::fill (a->begin(), a->end(), 1);
    std::fill (b->begin(), b->end(), 2);

    unsigned long elapsed = 0;
    for (int i = 0 ; i < NRuns; i++) {
        auto start = std::chrono::steady_clock::now();
        cad::matmult(*result, *a, *b);
        elapsed += std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::steady_clock::now() - start).count();
    }

    for (unsigned int i = 0 ;i < Size ; i++)
        cad::expect_container_value((*result)[i], Size*2, Size);

    return elapsed/ ((double) NRuns) ;
};


static constexpr unsigned NRuns = 4;

TEST(MatMult, Size_1000_NRuns_4) {

    double elapsed  = matmult_test<1000, NRuns>();
    std::cout << "Elapsed time: " << elapsed << " milliseconds \n";
}

