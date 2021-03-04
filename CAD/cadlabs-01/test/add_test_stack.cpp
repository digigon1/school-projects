//
// Created by Hervé Paulino on 24/01/17.
//

#include <array>
#include <memory>
#include <algorithm>

#include "cad_test.h"
#include <functions.h>

template <std::size_t SIZE, unsigned NITER>
double add_test_stack() {

    std::array<int, SIZE> result;

    std::array<int, SIZE> a;
    std::array<int, SIZE> b;

    std::fill (a.begin(), a.end(), 1);
    std::fill (b.begin(), b.end(), 2);

    unsigned long elapsed = 0;
    for (int i = 0 ; i < NITER; i++) {
        auto start = std::chrono::steady_clock::now();
        cad::add(result, a, b);
        elapsed += std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::steady_clock::now() - start).count();
    }

    cad::expect_container_value(result, 3);

    return elapsed/ ((double) NITER) ;
};


static constexpr unsigned NITER = 10;


TEST(ADD_STACK, SIZE_10000) {

    double elapsed  = add_test_stack<100000, NITER>();
    std::cout << "Elapsed time: " << elapsed << " milliseconds \n";
}


TEST(ADD_STACK, SIZE_100000) {

    double elapsed  = add_test_stack<1000000, NITER>();
    std::cout << "Elapsed time: " << elapsed << " milliseconds \n";
}
